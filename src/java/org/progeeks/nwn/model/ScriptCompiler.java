/*
 * $Id$
 *
 * Copyright (c) 2004, Paul Speed
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3) Neither the names "Progeeks", "Meta-JB", nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.progeeks.nwn.model;

import java.io.*;
import java.util.*;

import org.progeeks.util.log.*;
import org.progeeks.util.thread.*;

/**
 *  Runs the script compiler.  I did it this way so that it would
 *  be easy to update the implementation later.  Perhaps even make
 *  this an interface and have actually implementations that can
 *  be swapped in when configuring Pandora.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ScriptCompiler
{
    public static final int MAX_COMPILERS = 10;

    static Log log = Log.getLog( ScriptCompiler.class );

    private CompilerWatcher watcher = new CompilerWatcher();

    public ScriptCompiler()
    {
        watcher.start();
    }

    /**
     *  This call determines if a script compiler is available.  This
     *  may be an expensive call since for commandline tools it must
     *  execute the compiler with not arguments to be sure.
     */
    public boolean hasCompiler()
    {
        // Do a test run to see if the compiler exists
        try
            {
            Process p = Runtime.getRuntime().exec( "nwnnsscomp" );
            return( true );
            }
        catch( IOException e )
            {
            log.warn( "Compiler not found", e );
            return( false );
            }
    }

    /**
     *  Builds the specified script that resides in the specified directory.
     */
    public void compileScript( String script, File directory )
    {
        try
            {
            System.out.println( "Exec: nwnnsscomp " + script );
            Process p = Runtime.getRuntime().exec( new String[] { "nwnnsscomp", script }, null,
                                                   directory );
            //runningCompiles.add( p );
            watcher.addCompiler( "nwnnsscomp " + script, p );
            }
        catch( IOException e )
            {
            log.error( "Error compiling script", e );
            }
    }

    /**
     *  Waits for all of the scripts to finish compiling.
     */
    public void waitForAll() throws InterruptedException
    {
        watcher.waitForAll();
    }


    /**
     *  Watches all of the running compiles to see when they
     *  terminate.
     */
    private class CompilerWatcher extends Thread
    {
        private List compilers = new ArrayList();
        private ConditionalLock pause = new ConditionalLock( true );
        private boolean go = true;
        private int waiters = 0;

        public synchronized void addCompiler( String command, Process p )
        {
            while( compilers.size() > MAX_COMPILERS )
                {
                waiters++;
                try
                    {
                    // Then wait
                    wait();
                    }
                catch( InterruptedException e )
                    {
                    log.error( "Error waiting", e );
                    }
                waiters--;
                }

            compilers.add( new CompilerInfo( command, p ) );
            pause.setFalse();
        }

        public void waitForAll() throws InterruptedException
        {
            pause.waitForTrue();
        }

        public void run()
        {
            while( go )
                {
                try
                    {
                    pause.waitForFalse();
                    synchronized( this )
                        {
                        for( Iterator i = compilers.iterator(); i.hasNext(); )
                            {
                            try
                                {
                                CompilerInfo p = (CompilerInfo)i.next();

                                //System.out.println( "Checking:" + p.command + "  done?: " + p.done );

                                // Read a character
                                p.update();

                                if( p.done )
                                    {
                                    System.out.println( p.command + " finished:" + p.p.exitValue() );
                                    p.cleanup();
                                    if( p.results.indexOf( "Error:" ) >= 0
                                        || p.results.indexOf( "Warning:" ) >= 0 )
                                        {
                                        System.out.println( "Output:\n" + p.results );
                                        }
                                    if( p.errors.length() > 0 )
                                        System.out.println( "Errors:\n" + p.errors );
                                    i.remove();
                                    }
                                }
                            catch( IOException e )
                                {
                                log.error( "Error getting process info", e );
                                }
                            }

                        if( compilers.size() < MAX_COMPILERS && waiters > 0 )
                            {
                            notifyAll();
                            }

                        if( compilers.size() == 0 )
                            {
                            pause.setTrue();
                            }
                        }

                    }
                catch( InterruptedException e )
                    {
                    log.error( "Error waiting", e );
                    }
                }
        }
    }

    private class CompilerInfo
    {
        String command;
        Process p;
        long started;
        InputStream in;
        OutputStream out;
        InputStream err;
        boolean done = false;
        boolean inDone = false;
        boolean errDone = false;

        StringBuffer results = new StringBuffer();
        StringBuffer errors = new StringBuffer();

        public CompilerInfo( String command, Process p )
        {
            this.started = System.currentTimeMillis();
            this.command = command;
            this.p = p;
            in = p.getInputStream();
            out = p.getOutputStream();
            err = p.getErrorStream();
        }

        public void update() throws IOException
        {
            if( done )
                return;

            if( !errDone )
                {
                int b = err.read();
                if( b >= 0 )
                    errors.append( (char)b );
                else
                    errDone = true;
                }

            if( !inDone )
                {
                int b = in.read();
                if( b >= 0 )
                    results.append( (char)b );
                else
                    inDone = true;
                }

            done = inDone && errDone;
        }

        public void cleanup()
        {
            // Process the results buffer
            int chop = results.indexOf( "Compiling:" );
            if( chop > 0 )
                results.delete( 0, chop );

            chop = results.indexOf( "Total Execution time" );
            if( chop > 0 )
                results.delete( chop, results.length() );
        }
    }
}
