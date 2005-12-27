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

import org.progeeks.parser.regex.*;
import org.progeeks.util.xml.*;

/**
 *  Processes a log file through a parser and sends events to
 *  a separate event processor.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class LogFileProcessor extends Thread
{
    private static ObjectXmlReader xmlReader = new ObjectXmlReader();
    static
    {
        BeanObjectHandler beanHandler = new BeanObjectHandler();
        beanHandler.importPackage( "org.progeeks.nwn.status" );
        beanHandler.importPackage( "org.progeeks.parser.regex" );
        xmlReader.addObjectHandler( beanHandler );
    }

    private long startTime;
    private boolean go = true;
    private File logFile;
    private Reader in;
    private long commitInterval = 15000;  // Every 15 seconds at most.

    /**
     *  Read the log every second by default.
     */
    private long readInterval = 1000;

    /**
     *  Tracks the number of changes to the status since the
     *  last time the file was generated.
     */
    private long changeCount = 0;

    /**
     *  The root pattern through which we'll pass all incoming
     *  data using the find() method.  We might want to make this
     *  configurable but it's the easiest way to skip large chunks
     *  of data that we don't care about.
     */
    private CompositePattern rootPattern;

    /**
     *  The object that receives new objects that we've parsed
     *  from the log file.
     */
    private EventProcessor eventProcessor;

    public LogFileProcessor()
    {
    }

    /**
     *  Sets the file that will be read as the log file.
     */
    public void setLogFile( File file ) throws IOException
    {
        this.logFile = file;
        this.in = new FileReader( file );
        //this.in = new BufferedReader( new FileReader( file ), 16384 );
    }

    /**
     *  Sets the minimum number of milliseconds to wait between
     *  calling commit on the EventProcessor after new data has
     *  been sent.
     */
    public void setCommitInterval( long millis )
    {
        this.commitInterval = millis;
    }

    /**
     *  Sets the number of milliseconds to wait between log file
     *  checks.
     */
    public void setReadInterval( long millis )
    {
        this.readInterval = millis;
    }

    /**
     *  Sets the root CompositePattern to use for parsing
     *  the log file data.
     */
    public void setRootPattern( CompositePattern rootPattern )
    {
        this.rootPattern = rootPattern;
    }

    /**
     *  Sets the event processor to which all parsed objects will
     *  be passed.
     */
    public void setEventProcessor( EventProcessor eventProcessor )
    {
        this.eventProcessor = eventProcessor;
    }

    public static LogFileProcessor loadProcessor( File xmlConfig ) throws IOException
    {
        Reader in = new BufferedReader( new FileReader( xmlConfig ) );
        try
            {
            LogFileProcessor generator = (LogFileProcessor)xmlReader.readObject( in );
            return( generator );
            }
        finally
            {
            in.close();
            }
    }

    protected StringBuffer processInput( StringBuffer sb )
    {
        // Process the parts that we're interested in and
        // return the left-overs.  This lets our parser
        // do much fancier things with the input than if we had
        // already broken out by line.

        if( rootPattern == null )
            {
            System.out.println( "No parser configured.  Input[" + sb + "]" );
            // For now, just empty the buffer
            return( new StringBuffer() );
            }

        System.out.println( "Processing[" + sb + "]" );

        CompositeMatcher matcher = rootPattern.matcher( sb );

        // Try to keep finding parts until we can't anymore
        int end = -1;
        while( matcher.find() )
            {
            Object value = matcher.getValue();

            if( eventProcessor == null )
                {
                System.out.println( "Found:" + value );
                }
            else
                {
                eventProcessor.processObject( value );
                }
            end = matcher.end();
            }

        // Figure out what's left
        if( end > -1 )
            {
            // Then we can chop it
            sb = new StringBuffer( sb.substring( end ) );
            }
        else
            {
            // We do nothing because we need to keep appending until
            // we find something.
            }

        return( sb );
    }

    protected void generateFile()
    {
        System.out.println( "Commit.  Running time:" + (System.currentTimeMillis() - startTime) );
        if( eventProcessor != null )
            {
            eventProcessor.commit();
            }
    }

    public void run()
    {
        this.startTime = System.currentTimeMillis();

        char[] buffer = new char[264];
        StringBuffer sBuff = new StringBuffer();

        long lastWriteTime = 0;
        long charsRead = 0;
        long changeCount = 0;
        boolean newLine = true;

        while( go )
            {
            try
                {
                long length = logFile.length();
                if( length > charsRead )
                    {
                    int chars = in.read( buffer );
                    charsRead += chars;

                    // Put the new data into the buffer
                    sBuff.append( buffer, 0, chars );

                    // Process the data read so far
                    sBuff = processInput( sBuff );

                    // just for testing.
                    changeCount = 1;
                    }
                else
                    {
                    long time = System.currentTimeMillis();

                    if( changeCount > 0 && time > lastWriteTime + commitInterval )
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
            System.out.println( "Usage: LogFileProcessor <config>" );
            System.out.println( "Where: <config> is an XML configuration file." );
            return;
            }

        // Load the config from XML
        LogFileProcessor generator = LogFileProcessor.loadProcessor( new File(args[0]) );
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
