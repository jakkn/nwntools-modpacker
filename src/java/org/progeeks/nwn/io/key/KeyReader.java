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

package org.progeeks.nwn.io.key;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.*;
import org.progeeks.nwn.io.*;

/**
 *  Reader for NWN Key files.
 *  NOT a standard implementation of java.io.Reader.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class KeyReader
{
    private BinaryDataInputStream in;
    private Header header;
    private List files = new ArrayList();

    public KeyReader( InputStream in ) throws IOException
    {
        this.in = new BinaryDataInputStream( in );
        this.header = new Header( this.in );

System.out.println( header );
        readFileTable();
        readKeyTable();
    }

    protected void readFileTable() throws IOException
    {
        // Position the file in the right place
        in.gotoPosition( header.getFileTableOffset() );

        List entries = new ArrayList();

        int count = header.getFileCount();
        for( int i = 0; i < count; i++ )
            {
            FileEntryStub entry = new FileEntryStub( i, in );
            entries.add( entry );
            files.add( new FileEntry( entry ) ); // so it has one in a slot
            }

        // Sort the entries.
        Collections.sort( entries );

        // Read the names
        for( Iterator i = entries.iterator(); i.hasNext(); )
            {
            FileEntryStub stub = (FileEntryStub)i.next();
            in.gotoPosition( stub.nameOffset );

            FileEntry file = (FileEntry)files.get( stub.entryNumber );

            byte[] buff = new byte[stub.nameSize];
            in.readFully( buff );
            file.name = new String( buff );
            }
    }

    protected void readKeyTable() throws IOException
    {
        // Position the file in the right place
        in.gotoPosition( header.getKeyTableOffset() );

        int count = header.getKeyCount();
        for( int i = 0; i < count; i++ )
            {
            KeyEntry entry = new KeyEntry( in );
System.out.println( "KeyEntry:" );
System.out.println( "    key:" + entry.key );
System.out.println( "    type:" + entry.type + "   (" + ResourceUtils.getExtensionForType( entry.type ) + ")" );
System.out.println( "    id:" + Integer.toHexString( entry.id ) );
System.out.println( "   file:" + entry.fileIndex + "  index:" + entry.index + "   tableIndex:" + entry.tableIndex );
            FileEntry file = (FileEntry)files.get( entry.fileIndex );
System.out.println( "       :" + file.name );
            }
    }

    public static void main( String[] args ) throws IOException
    {
        for( int i = 0; i < args.length; i++ )
            {
            System.out.println( "Reading:" + args[i] );
            FileInputStream fIn = new FileInputStream( new File(args[i]) );
            try
                {
                KeyReader reader = new KeyReader( fIn );
                }
            finally
                {
                fIn.close();
                }
            }
    }

    private class FileEntry
    {
        String name;
        int fileSize;
        int driveFlags;

        public FileEntry( FileEntryStub stub )
        {
            this.fileSize = stub.fileSize;
            this.driveFlags = stub.driveFlags;
        }
    }

    private class FileEntryStub implements Comparable
    {
        int entryNumber;
        int fileSize;
        int nameOffset;
        int nameSize;
        int driveFlags;

        public FileEntryStub( int entryNumber, BinaryDataInputStream in ) throws IOException
        {
            this.entryNumber = entryNumber;

            fileSize = in.readInt();
            nameOffset = in.readInt();
            nameSize = in.readShort();
            driveFlags = in.readShort();
        }

        /**
         *  Compares based on the name offset.
         */
        public int compareTo( Object obj )
        {
            int no = ((FileEntryStub)obj).nameOffset;
            if( nameOffset < no )
                return( -1 );
            if( nameOffset > no )
                return( 1 );
            return( 0 );
        }
    }

    private class KeyEntry
    {
        String key;
        int type;
        int id;
        int fileIndex;
        int index;
        int tableIndex;

        public KeyEntry( BinaryDataInputStream in ) throws IOException
        {
            byte[] buff = new byte[16];
            in.readFully( buff );
            key = new String(buff);
            type = in.readShort();
            id = in.readInt();

            fileIndex = id >> 20;
            index = id & 0x3fff;
            tableIndex = (id >> 14) & 0x3f;
        }
    }
}