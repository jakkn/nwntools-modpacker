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

package org.progeeks.nwn.status;

import java.io.*;
import java.util.*;

import org.progeeks.parser.regex.NameValuePair;

/**
 *  Keeps track of NWN server state and writes it to HTML when
 *  committed.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class HtmlGeneratingProcessor implements EventProcessor
{
    private File outputFile;

    private ServerState server;

    public HtmlGeneratingProcessor()
    {
    }

    /**
     *  Sets the file to use for web page generation.
     */
    public void setOutputFile( File file )
    {
        this.outputFile = file;
    }

    /**
     *  Sets the server state object that will keep track of
     *  the events that have been processed.
     */
    public void setServerStateModel( ServerState state )
    {
        this.server = state;
    }

    /**
     *  Processes the parsed object to update the server state.
     */
    public boolean processObject( Object obj )
    {
        if( obj == null )
            return( false );
        if( obj instanceof NameValuePair )
            {
            // We'll go ahead and peel out the value if it's
            // just an event since it will already have the name
            // embedded
            Object val = ((NameValuePair)obj).getValue();
            if( val instanceof ServerEvent )
                return( server.processObject( (ServerEvent)val ) );
            }
        //return( server.processObject( obj ) );
        System.out.println( "Unknown event type:" + obj );
        return( false );
    }

    /**
     *  Writes the HTML status file from accumulated state changes.
     */
    public void commit()
    {
        try
            {
            writeServerState();
            }
        catch( IOException e )
            {
            throw new RuntimeException( "Error writing server state to:" + outputFile, e );
            }
    }

    protected void writeServerState() throws IOException
    {
        // Just for testing, just dump some stuff
        FileWriter fOut = new FileWriter( outputFile );
        PrintWriter out = new PrintWriter( fOut );
        try
            {
            out.println( "Server options:" + server.getServerOptions() );
            out.println( "Module:" + server.getModuleName() );
            out.println( "Online:" + server.isOnline() );
            out.println( "Players:" );
            for( Iterator i = server.getPlayers().iterator(); i.hasNext(); )
                {
                Player p = (Player)i.next();
                out.println( "    " + p.getName() );
                }
            }
        finally
            {
            out.close();
            }
    }
}
