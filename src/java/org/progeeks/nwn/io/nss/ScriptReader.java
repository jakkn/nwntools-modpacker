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
     *  Internal method for eading a new raw unmerged block of
     *  text.
     */
    protected ScriptBlock readSingleBlock() throws IOException
    {
        if( endOfFile )
            return( null );

        // In some cases, we keep appending to the last block until
        // the block type changes.
        StringBuffer line = new StringBuffer();
        ScriptBlock block = new ScriptBlock( line );
        block.setType( ScriptBlock.WHITESPACE );
        block.setStartLine( st.lineno() );

        // Parse until we have a complete line.
        boolean closeAtEol = true;
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
                    if( block.getType() == ScriptBlock.WHITESPACE )
                        {
                        if( st.sval.startsWith( "/*" ) ) // */ Working around a bug in my editor
                            {
                            block.setType( ScriptBlock.COMMENT );
                            closeAtEol = false;
                            }
                        else if( st.sval.startsWith( "//" ) )
                            {
                            block.setType( ScriptBlock.COMMENT );
                            closeAtEol = true;
                            }
                        else if( st.sval.equals( "#include" ) )
                            {
                            block.setType( ScriptBlock.INCLUDE );
                            closeAtEol = true;
                            }
                        else
                            {
                            block.setType( ScriptBlock.CODE );
                            closeAtEol = false;
                            }
                        }
                    else if( block.getType() == ScriptBlock.COMMENT )
                        {
                        if( st.sval.endsWith( "*/" ) )
                            {
                            closeAtEol = true;
                            }
                        }
                    break;
                case '"':
                    line.append( "\"" + st.sval + "\"" );
                    break;
                case StreamTokenizer.TT_NUMBER:
                    line.append( st.nval );
                    break;
                case StreamTokenizer.TT_EOL:
                    line.append( "\n" );
                    if( closeAtEol )
                        {
                        block.setEndLine( st.lineno() - 1 );
                        return( block );
                        }
                    break;
                default:
                    line.append( (char)token );
                    if( block.getType() == ScriptBlock.WHITESPACE )
                        {
                        if( token != ' ' && token != '\t' )
                            {
                            block.setType( ScriptBlock.CODE );
                            closeAtEol = false;
                            }
                        }
                    else if( block.getType() == ScriptBlock.CODE )
                        {
                        if( token == ';' && blockDepth == 0 )
                            {
                            closeAtEol = true;
                            }
                        else if( token == '{' )
                            {
                            blockDepth++;
                            closeAtEol = false;
                            }
                        else if( token == '}' )
                            {
                            blockDepth--;
                            if( blockDepth == 0 )
                                closeAtEol = true;
                            }
                        }

                    break;
                }
            }

        block.setEndLine( st.lineno() );
        return( block );
    }

    /**
     *  Reads the next full block from the parser.  A ScriptBlock is a logical
     *  portion of a script.
     */
    public ScriptBlock readNextBlock() throws IOException
    {
        ScriptBlock block = readSingleBlock();
        if( block == null )
            return( null );

        return( block );
    }

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
                ScriptBlock block = null;
                while( (block = r.readNextBlock()) != null )
                    {
//                    System.out.print( "Line:" + block.getBlockText() );
//                    System.out.print( block );
                    System.out.print( block.getBlockText() );
//                    System.out.print( line );
                    }
                }
            finally
                {
                r.close();
                }
            }
    }
}


