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

package org.progeeks.nwn.io.nss;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.resource.*;

/**
 *  Parses a script... just sort of a test for now.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ScriptReader
{
    private static final int NORMAL = 0;
    private static final int COMMENT = 1;
    private static final int BLOCK = 2;
    private static final int END_AT_EOL = 3;

    private Reader in;
    private StreamTokenizer st;
    private boolean endOfFile = false;

    public ScriptReader( InputStream in ) throws IOException
    {
        this( new InputStreamReader(in) );
    }

    public ScriptReader( Reader in )
    {
        this.in = in;
        st = new StreamTokenizer( this.in );
        st.eolIsSignificant( true );
        st.ordinaryChar( ' ' );
        st.ordinaryChar( '\t' );
        st.ordinaryChar( '/' );
        st.ordinaryChars( '0', '9' );
        st.ordinaryChar( '.' );
        st.ordinaryChar( '-' );
        st.ordinaryChar( '#' );
        st.wordChars( '0', '9' );
        st.wordChars( '.', '.' );
        st.wordChars( '#', '#' );
        st.wordChars( '/', '/' );
        st.wordChars( '*', '*' );
    }

    /**
     *  Reads the next full line from the parser.  This is a line in
     *  scripting terms not in file terms.
     */
    public String readScriptLine() throws IOException
    {
        if( endOfFile )
            return( null );

        StringBuffer line = new StringBuffer();

        // Parse until we have a complete line.
        int state = NORMAL;
        int blockDepth = 0;
        int token = 0;
        while( true )
            {
            token = st.nextToken();

            if( token == StreamTokenizer.TT_EOF )
                {
                endOfFile = true;
                break;
                }

            switch( token )
                {
                case StreamTokenizer.TT_WORD:
                    line.append( st.sval );
                    if( st.sval.startsWith( "/*" ) )
                        state = COMMENT;
                    else if( st.sval.endsWith( "*/" ) )
                        state = END_AT_EOL;
                    break;
                case '"':
                    line.append( "\"" + st.sval + "\"" );
                    break;
                case StreamTokenizer.TT_NUMBER:
                    line.append( st.nval );
                    break;
                case StreamTokenizer.TT_EOL:
                    line.append( "\n" );
                    if( state == END_AT_EOL )
                        {
                        state = NORMAL;
                        return( line.toString() );
                        }
                    break;
                default:
                    line.append( (char)token );
                    if( token == ';' && state == NORMAL )
                        {
                        state = END_AT_EOL;
                        }
                    else if( token == '{' )
                        {
                        state = BLOCK;
                        blockDepth++;
                        }
                    else if( token == '}' )
                        {
                        blockDepth--;
                        if( blockDepth == 0 )
                            state = END_AT_EOL;
                        }
                    break;
                }
            }

        return( line.toString() );
    }
/*
    public void readScript() throws IOException
    {
        int token = 0;
        while( (token = st.nextToken()) != StreamTokenizer.TT_EOF )
            {
            switch( token )
                {
                case StreamTokenizer.TT_WORD:
                    System.out.print( "[" + st.sval + "]" );
                    if( directive != null )
                        {
                        directive += st.sval;
                        continue;
                        }
                    break;
                case '#':
                    directive = "#";
                    System.out.print( "#" );
                    continue;
                case '"':
                    if( "#include".equals( directive ) )
                        {
                        ResourceKey key = new ResourceKey( st.sval, ResourceTypes.TYPE_NSS );
                        }
                    System.out.print( "\"" + st.sval + "\"" );
                    break;
                case StreamTokenizer.TT_NUMBER:
                    System.out.print( st.nval );
                    break;
                case StreamTokenizer.TT_EOF:
                    return;
                case StreamTokenizer.TT_EOL:
                    System.out.println();
                    break;
                default:
                    System.out.print( (char)token );
                    break;
                }

            directive = null;
            }

    }*/

    public void close() throws IOException
    {
        in.close();
    }

    public static void main( String[] args ) throws Exception
    {
        for( int i = 0; i < args.length; i++ )
            {
            ScriptReader r = new ScriptReader( new FileReader( new File(args[i]) ) );
            try
                {
                //r.readScript();
                String line = null;
                while( (line = r.readScriptLine()) != null )
                    {
//                    System.out.print( "Line:" + line );
                    System.out.print( line );
                    }
                }
            finally
                {
                r.close();
                }
            }
    }
}


