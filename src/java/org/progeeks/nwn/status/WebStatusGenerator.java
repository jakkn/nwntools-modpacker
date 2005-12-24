/*
 * $Id$
 *
 * Copyright (c) 2005, Paul Speed
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
 * 3) Neither the names "Progeeks", "NWN Tools", nor the names of its contributors
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

package org.progeeks.nwn.status;

import java.io.*;
import java.util.*;

import org.progeeks.util.xml.*;

/**
 *  Entry point for a utility that generates a server status
 *  web page from an NWN log file and some regex parsing rules.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class WebStatusGenerator extends Thread
{
    private static ObjectXmlReader xmlReader = new ObjectXmlReader();
    static
    {
        BeanObjectHandler beanHandler = new BeanObjectHandler();
        beanHandler.importPackage( "org.progeeks.nwn.status" );
        beanHandler.importPackage( "org.progeeks.parser.regex" );
        xmlReader.addObjectHandler( beanHandler );
    }

    private boolean go = true;
    private File logFile;
    private FileInputStream in;
    private File outputFile;
    private long writeInterval = 15000;  // Every 15 seconds at most.

    /**
     *  Read the log every second by default.
     */
    private long readInterval = 1000;

    /**
     *  Tracks the number of changes to the status since the
     *  last time the file was generated.
     */
    private long changeCount = 0;


    public WebStatusGenerator()
    {
    }

    /**
     *  Sets the file that will be read as the log file.
     */
    public void setLogFile( File file ) throws IOException
    {
        this.logFile = file;
        this.in = new FileInputStream( file );
    }

    /**
     *  Sets the file to use for web page generation.
     */
    public void setOutputFile( File file )
    {
        this.outputFile = file;
    }

    /**
     *  Sets the minimum number of milliseconds to wait between
     *  web page generations.
     */
    public void setGenerationInterval( long millis )
    {
        this.writeInterval = millis;
    }

    /**
     *  Sets the number of milliseconds to wait between log file
     *  checks.
     */
    public void setReadInterval( long millis )
    {
        this.readInterval = millis;
    }

    public static WebStatusGenerator loadGenerator( File xmlConfig ) throws IOException
    {
        Reader in = new BufferedReader( new FileReader( xmlConfig ) );
        try
            {
            WebStatusGenerator generator = (WebStatusGenerator)xmlReader.readObject( in );
            return( generator );
            }
        finally
            {
            in.close();
            }
    }

    protected void processLine( String line )
    {
        System.out.println( "Line:" + line );
    }

    protected void generateFile()
    {
        System.out.println( "Generate file." );
    }

    public void run()
    {
        byte[] buffer = new byte[1024];
        StringBuffer sBuff = new StringBuffer();

        long lastWriteTime = 0;
        long bytesRead = 0;
        long changeCount = 0;
        boolean newLine = true;

        while( go )
            {
            try
                {
                long length = logFile.length();
                if( length > bytesRead )
                    {
                    int bytes = in.read( buffer );
                    bytesRead += bytes;

                    for( int i = 0; i < bytes; i++ )
                        {
                        if( newLine && buffer[i] == '.' )
                            continue;

                        if( buffer[i] == '\r' )
                            continue;

                        if( buffer[i] == '\n' )
                            {
                            processLine( sBuff.toString() );
                            sBuff = new StringBuffer();
                            newLine = true;
                            continue;
                            }

                        newLine = false;
                        sBuff.append( (char)buffer[i] );
                        //System.out.print( (char)buffer[i] );
                        }

                    // just for testing.
                    changeCount = 1;
                    }
                else
                    {
                    long time = System.currentTimeMillis();

                    if( changeCount > 0 && time > lastWriteTime + writeInterval )
                        {
                        generateFile();
                        changeCount = 0;
                        lastWriteTime = time;
                        }
                    sleep( readInterval );
                    }
                }
            catch( Exception e )
                {
                e.printStackTrace();
                }
            }
    }

    /**
     *  Stops the generator in a nice way.
     */
    public void close() throws InterruptedException
    {
        this.go = false;
        join();
    }

    public static void main( String[] args ) throws Exception
    {
        if( args.length == 0 )
            {
            System.out.println( "Usage: WebStatus <config>" );
            System.out.println( "Where: <config> is an XML configuration file." );
            return;
            }

        // Load the config from XML
        WebStatusGenerator generator = WebStatusGenerator.loadGenerator( new File(args[0]) );
        generator.start();

        // Wait for console commands and such
        BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
        String line = null;
        while( (line = in.readLine()) != null )
            {
            if( "?".equals( line ) )
                {
                System.out.println( "Commands:" );
                System.out.println( "   exit" );
                }
            else if( "exit".equals( line ) )
                {
                System.out.println( "Exiting..." );
                break;
                }
            }

        generator.close();
        System.out.println( "Done." );
    }
}
