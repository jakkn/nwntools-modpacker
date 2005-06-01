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

package org.progeeks.nwn.io.bif;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.io.*;


/**
 *  Reader for NWN BIF files.
 *  NOT a standard implementation of java.io.Reader.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class BifReader
{
    private BinaryDataInputStream in;
    private Header header;
    private List   variableEntries;
    private int    currentResource;

    public BifReader( InputStream in ) throws IOException
    {
        this.in = new BinaryDataInputStream( in );
        this.header = new Header( this.in );

        // Since the fixed resource block isn't used,
        // we don't even bother to try and read it.
        readVariableResourceTable();
    }

    public void close() throws IOException
    {
        in.close();
    }

    /**
     *  Returns an input stream for the specified resource ID.  Resources
     *  must be accessed in order using this method or an exception will be
     *  thrown.
     */
    public InputStream getResource( int id ) throws IOException
    {
        int index = id & 0x3fff;
        VariableEntry ve = (VariableEntry)variableEntries.get(index);
        return( new ResourceInputStream(ve) );
    }

    public InputStream nextResource() throws IOException
    {
        if( currentResource >= variableEntries.size() )
            return( null );

        VariableEntry ve = (VariableEntry)variableEntries.get(currentResource++);

        return( new ResourceInputStream(ve) );
    }

    protected void readVariableResourceTable() throws IOException
    {
        in.gotoPosition( header.getVariableResourceOffset() );

        int count = header.getVariableResourceCount();
        variableEntries = new ArrayList( count );
        for( int i = 0; i < count; i++ )
            {
            VariableEntry entry = new VariableEntry( in );
            variableEntries.add( entry );
//System.out.println( "Entry[" + i + "] id:" +Integer.toHexString(entry.id) + "  offset:" + entry.offset
//                    + "  index:" + (entry.id & 0x3fff));
            }
    }

    public static void main( String[] args ) throws IOException
    {
        for( int i = 0; i < args.length; i++ )
            {
            File f = new File( args[i] );
            FileInputStream fIn = new FileInputStream( f );
            try
                {
                BifReader reader = new BifReader( fIn );
                }
            finally
                {
                fIn.close();
                }
            }
    }

    private class VariableEntry
    {
        int id;
        int offset;
        int size;
        int type;

        public VariableEntry( BinaryDataInputStream in ) throws IOException
        {
            id = in.readInt();
            offset = in.readInt();
            size = in.readInt();
            type = in.readInt();
        }
    }

    private class ResourceInputStream extends InputStream
    {
        private VariableEntry ve;
        private int start;
        private int bytesLeft;

        public ResourceInputStream( VariableEntry entry ) throws IOException
        {
            this.ve = entry;
            this.start = entry.offset;
            this.bytesLeft = entry.size;
            if( start > 0 )
                in.gotoPosition( start );
        }

        public int read() throws IOException
        {
            if( bytesLeft <= 0 )
                return( -1 );
            int result = (in.readByte() & 0xff);
            bytesLeft--;
            return( result );
        }

        public int read( byte[] b ) throws IOException
        {
            if( bytesLeft == 0 )
                return( -1 );
            int len = Math.min( bytesLeft, b.length );
            return( read( b, 0, len ) );
        }

        public int read( byte[] b, int off, int len ) throws IOException
        {
            if( bytesLeft == 0 )
                return( -1 );
            len = Math.min( bytesLeft, len );
            int result = in.read( b, off, len );
            bytesLeft -= result;
            return( result );
        }
    }
}



