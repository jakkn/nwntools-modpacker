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

package org.progeeks.nwn;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.io.nss.*;

/**
 *  Reads and parses scripts, outputting a possibly modified
 *  version doing constant additions, variable substitution, etc..
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ScriptModifier
{
    private File output = null;
    private Map constants = new HashMap();

    public ScriptModifier()
    {
    }

    public void setOutputDirectory( File f )
    {
        this.output = f;
        System.out.println( "Sending processed files to:" + f );
    }

    public void setConstant( String constant )
    {
        String c = constant.trim();
        if( !c.startsWith( "const" ) )
            {
            throw new RuntimeException( "Invalid constant format:" + constant );
            }
        c = c.substring( "const".length() + 1 );
        int split = c.indexOf( ' ' );
        if( split < 0 )
            {
            throw new RuntimeException( "Invalid constant format:" + constant );
            }
        String type = c.substring( 0, split );
        c = c.substring( split + 1 );

        split = c.indexOf( '=' );
        if( split < 0 )
            {
            throw new RuntimeException( "Invalid constant format:" + constant );
            }
        String name = c.substring( 0, split ).trim();
        c = c.substring( split + 1 );

        System.out.println( "Setting constant:" + name + " to:" + c );
        constants.put( name, constant );
    }

    protected List readScript( File script ) throws IOException
    {
        System.out.println( "    parsing:" + script );
        ScriptReader r = new ScriptReader( new FileReader( script ) );
        try
            {
            return( r.readAllBlocks() );
            }
        finally
            {
            r.close();
            }
    }

    protected void writeScript( File script, List blocks ) throws IOException
    {
        if( output != null )
            script = new File( output, script.getName() );
        System.out.println( "    writing:" + script );
        FileWriter fw = new FileWriter(script);
        PrintWriter out = new PrintWriter(fw);
        try
            {
            for( Iterator i = blocks.iterator(); i.hasNext(); )
                {
                ScriptBlock block = (ScriptBlock)i.next();
                out.print( block.getBlockText() );
                }
            }
        finally
            {
            out.close();
            }
    }

    public void processScript( File scriptFile ) throws IOException
    {
        System.out.println( "Processing:" + scriptFile );
        List blocks = readScript( scriptFile );

        // Set any constants

        // Perform any processing

        // Write it back out
        writeScript( scriptFile, blocks );
    }

    public static void main( String[] args ) throws IOException
    {
        ScriptModifier scriptModder = new ScriptModifier();

        for( int i = 0; i < args.length; i++ )
            {
            if( args[i].startsWith( "-" ) )
                {
                if( args[i].equals( "-destination" ) && i + 1 < args.length )
                    {
                    scriptModder.setOutputDirectory( new File(args[i + 1]) );
                    i++;
                    }
                else if( args[i].equals( "-setConstant" ) && i + 1 < args.length )
                    {
                    scriptModder.setConstant( args[i + 1] );
                    i++;
                    }
                else
                    {
                    System.out.println( "Error processing argument:" + args[i] );
                    }
                }
            else if( args[i].toLowerCase().endsWith( ".nss" ) )
                {
                // Treat is as a script file
                scriptModder.processScript( new File(args[i]) );
                }
            else
                {
                System.out.println( "Don't know what to do with argument:" + args[i] );
                }
            }
    }
}
