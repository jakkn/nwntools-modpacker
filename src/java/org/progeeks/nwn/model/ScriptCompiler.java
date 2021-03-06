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
import java.text.*;
import java.util.*;

import org.progeeks.util.*;
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

    private int maxCompilerCount = MAX_COMPILERS;
    private File compiler = new File( "nwnnsscomp" );
    private CompilerWatcher watcher = new CompilerWatcher();
    private volatile int compiledCount = 0;
    private volatile int includeCount = 0;

    public ScriptCompiler()
    {
        watcher.start();
    }

    /**
     *  Sets the maximum number of compile threads that will be spawned
     *  by this script compiler.
     */
    public void setMaxCompilerCount( int count )
    {
        this.maxCompilerCount = count;
    }

    /**
     *  Returns the maximum number of compile threads that will be spawned
     *  by this script compiler.
     */
    public int getMaxCompilerCount()
    {
        return( maxCompilerCount );
    }

    public void resetCompiledCount()
    {
        compiledCount = 0;
        includeCount = 0;
    }

    /**
     *  Returns the number of compiles that actually produced output.
     */
    public int getCompiledCount()
    {
        return( compiledCount );
    }

    /**
     *  Returns the number of scripts that were skipped because they were
     *  just include files with no main() or conditional check.
     */
    public int getIncludeCount()
    {
        return( includeCount );
    }

    /**
     *  Sets the compiler path include the name of the exe file.
     */
    public void setCompilerPath( File compiler )
    {
        this.compiler = compiler;
    }

    /**
     *  Returns the current compiler path including the name of the exe file.
     */
    public File getCompilerPath()
    {
        return( compiler );
    }

    /**
     *  Returns true if a compiler exists at the specified path... makes
     *  a lot of assumptions.  The path should include the executable name...
     *  but does not need to include the path.
     */
    public boolean hasCompiler( File path )
    {
        // Do a test run to see if the compiler exists
        try
            {
            Process p = Runtime.getRuntime().exec( path.toString() );
            return( true );
            }
        catch( IOException e )
            {
            log.warn( "Compiler not found", e );
            return( false );
            }
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
            Process p = Runtime.getRuntime().exec( compiler.toString() );
            return( true );
            }
        catch( IOException e )
            {
            log.warn( "Compiler not found:" + compiler, e );
            return( false );
            }
    }

    /**
     *  Builds the specified script that resides in the specified directory.
     */
    public void compileScript( String script, File directory, ErrorListener listener )
    {
        try
            {
            System.out.println( "Exec: nwnnsscomp " + script );
            Process p = Runtime.getRuntime().exec( new String[] { compiler.toString(), script }, null,
                                                   directory );
            //runningCompiles.add( p );
            watcher.addCompiler( script, "nwnnsscomp " + script, p, listener );
            }
        catch( IOException e )
            {
            log.error( "Error compiling script", e );
            }
    }

    /**
     *  Builds the specified script that resides in the specified directory.
     */
    public void compileScript( String script, String[] args, File directory, ErrorListener listener )
    {
        try
            {
            StringBuffer debug = new StringBuffer( compiler.toString() + " " );

            String[] cmdLine = new String[ 2 + args.length ];
            cmdLine[0] = compiler.toString();
            for( int i = 0; i < args.length; i++ )
                {
                cmdLine[i + 1] = args[i];

                if( log.isDebugEnabled() )
                    debug.append( args[i] + " " );
                }
            cmdLine[args.length + 1] = "\"" + script + "\"";

            if( log.isDebugEnabled() )
                {
                debug.append( script );
                log.debug( "Command line:" + debug );
                }

            //System.out.println( "nwnnsscomp " + script );
            Process p = Runtime.getRuntime().exec( cmdLine, null,
                                                   directory );
            //runningCompiles.add( p );
            watcher.addCompiler( script, "nwnnsscomp " + script, p, listener );
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

    protected void processOutput( CompilerInfo info )
    {
        StringBuffer results = info.results;

        log.debug( results );

        if( info.errors.length() > 0 )
            {
            // I've never actually seen this happen.
            System.out.println( "Errors:\n" + info.errors );
            }

        // Don't bother checking the include files
        if( results.indexOf( "File is an include file, ignored" ) >= 0 )
            {
            // And hope the string doesn't change
            includeCount++;
            return;
            }

        if( results.indexOf( "Error:" ) < 0
            && results.indexOf( "Warning:" ) > 0 )
            {
            compiledCount++;
            return;
            }

        MessageFormat errorFormat = new MessageFormat( "{0}({1,number}): Error: {2}" );
        if( info.listener == null )
            {
            System.out.println( "Output:\n" + results );
            return;
            }

        int errors = 0;
        StringTokenizer st = new StringTokenizer( results.toString(), "\r\n" );
        while( st.hasMoreTokens() )
            {
            String token = st.nextToken();
            if( token.startsWith( "Error:" ) )
                {
                String error = token.substring( "Error:".length() );
                error = error.trim();

                ErrorInfo err = new ErrorInfo( "Compiling", error );
                info.listener.error( info.script, err );
                errors++;
                }
            else if( token.indexOf( "Error:" ) > 0 )
                {
                try
                    {
                    Object[] objs = errorFormat.parse( token );
                    Number num = (Number)objs[1];

                    CompileError err = new CompileError( String.valueOf(objs[2]), String.valueOf(objs[0]),
                                                         num.intValue() );
                    info.listener.error( info.script, err );
                    }
                catch( ParseException e )
                    {
                    System.out.println( "Bad string:" + token );
                    }
                errors++;
                }
            else
                {
                //System.out.println( "Token:" + token );
                }
            }

        if( errors == 0 )
            compiledCount++;
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

        public synchronized void addCompiler( String script, String command, Process p, ErrorListener listener )
        {
            while( compilers.size() > maxCompilerCount )
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

            compilers.add( new CompilerInfo( script, command, p, listener ) );
            pause.setFalse();
        }

        public void waitForAll() throws InterruptedException
        {
            pause.waitForTrue();
        }

        public void run()
        {
            try
                {
                runLoop();
                }
            catch( Throwable t )
                {
                t.printStackTrace();
                }
        }

        public void runLoop()
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
                                    //System.out.println( p.command + " finished:" + p.p.exitValue() );
                                    p.cleanup();
                                    processOutput( p );
                                    i.remove();
                                    }
                                }
                            catch( IOException e )
                                {
                                log.error( "Error getting process info", e );
                                }
                            }

                        if( compilers.size() < maxCompilerCount && waiters > 0 )
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
        String script;
        String command;
        Process p;
        ErrorListener listener;
        long started;
        InputStream in;
        OutputStream out;
        InputStream err;
        boolean done = false;
        boolean inDone = false;
        boolean errDone = false;

        StringBuffer results = new StringBuffer();
        StringBuffer errors = new StringBuffer();

        public CompilerInfo( String script, String command, Process p, ErrorListener listener )
        {
            this.script = script;
            this.started = System.currentTimeMillis();
            this.command = command;
            this.p = p;
            this.listener = listener;
            in = p.getInputStream();
            out = p.getOutputStream();
            err = p.getErrorStream();
        }

        public void update() throws IOException
        {
            if( done )
                return;
//System.out.println( "Results:" + results );
            if( !inDone )
                {
                int b = in.read();
                if( b >= 0 )
                    results.append( (char)b );
                else
                    inDone = true;
                }

            /*
            We never seem to actually see errors and the blocking
            causes problems sometimes.
            if( !errDone && err.available() > 0 )
                {
                int b = err.read();
                if( b >= 0 )
                    errors.append( (char)b );
                else
                    errDone = true;
                }*/

            done = inDone; // && errDone;
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
