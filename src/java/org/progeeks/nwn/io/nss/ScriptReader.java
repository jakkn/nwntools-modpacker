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
    private ScriptBlock lastBlock = null;

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
        st.wordChars( '_', '_' );
        st.wordChars( '\'', '\'' );
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
        ScriptBlock block = new ScriptBlock( new StringBuffer() );
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
                    block.append( st.sval );
                    if( block.getType() == ScriptBlock.WHITESPACE )
                        {
                        if( st.sval.startsWith( "/*" ) ) // */ Working around a bug in my editor
                            {
                            block = block.getBlockForType( ScriptBlock.MULTICOMMENT );
                            closeAtEol = false;
                            }
                        else if( st.sval.startsWith( "//" ) )
                            {
                            block = block.getBlockForType( ScriptBlock.COMMENT );
                            closeAtEol = true;
                            }
                        else if( st.sval.equals( "#include" ) )
                            {
                            block = block.getBlockForType( ScriptBlock.INCLUDE );
                            closeAtEol = true;
                            }
                        else if( st.sval.equals( "const" ) )
                            {
                            block = block.getBlockForType( ScriptBlock.CONSTANT );
                            closeAtEol = false;
                            }
                        else
                            {
                            block = block.getBlockForType( ScriptBlock.DECLARATION );
                            closeAtEol = false;
                            }
                        }
                    else if( block.getType() == ScriptBlock.MULTICOMMENT )
                        {
                        if( st.sval.endsWith( "*/" ) )
                            {
                            closeAtEol = true;
                            }
                        }
                    break;
                case '"':
                    block.append( "\"" + st.sval + "\"" );
                    break;
                case StreamTokenizer.TT_NUMBER:
                    block.append( st.nval );
                    break;
                case StreamTokenizer.TT_EOL:
                    block.append( "\n" );
                    if( closeAtEol )
                        {
                        block.setEndLine( st.lineno() - 1 );
                        return( block );
                        }
                    break;
                default:
                    block.append( (char)token );
                    switch( block.getType() )
                        {
                        case ScriptBlock.WHITESPACE:
                            if( token != ' ' && token != '\t' )
                                {
                                block = block.getBlockForType( ScriptBlock.DECLARATION );
                                closeAtEol = false;
                                }
                            break;
                        case ScriptBlock.CONSTANT:
                        case ScriptBlock.DECLARATION:
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
                            break;
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
        ScriptBlock result = lastBlock;

        if( endOfFile )
            {
            lastBlock = null;
            return( result );
            }

        if( result == null )
            result = lastBlock = readSingleBlock();

        ScriptBlock block = readSingleBlock();

        // See if the last block and the current block can be merged
        if( result.getType() == ScriptBlock.WHITESPACE
            || result.getType() == ScriptBlock.COMMENT )
            {
            while( block != null && result.getType() == block.getType() )
                {
                result.appendBlock( block );
                block = readSingleBlock();
                }
            }

        lastBlock = block;
        return( result );
    }

    public List readAllBlocks() throws IOException
    {
        List results = new ArrayList();
        ScriptBlock block = null;
        while( (block = readNextBlock()) != null )
            {
            results.add( block );
            }
        return( results );
    }

    public void close() throws IOException
    {
        in.close();
    }

    /**
     *  Convenience method for reading a script and returning it as a
     *  Script object.
     */
    public static Script readScript( File f ) throws IOException
    {
        FileReader in = new FileReader( f );
        ScriptReader r = new ScriptReader( in );
        try
            {
            List blocks = r.readAllBlocks();
            return( new Script( f.getName(), f, blocks ) );
            }
        finally
            {
            r.close();
            }
    }

    public static void main( String[] args ) throws Exception
    {
        for( int i = 0; i < args.length; i++ )
            {
            ScriptReader r = new ScriptReader( new FileReader( new File(args[i]) ) );
            try
                {
                ScriptBlock block = null;
                while( (block = r.readNextBlock()) != null )
                    {
//                    System.out.print( "Line:" + block.getBlockText() );
                    System.out.print( block );
//                    System.out.print( block.getBlockText() );
                    }
                }
            finally
                {
                r.close();
                }
            }
    }
}


