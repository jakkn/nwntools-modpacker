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

package org.progeeks.nwn.io.gff;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.io.*;
import org.progeeks.nwn.gff.*;


/**
 *  Writes GFF data structures to a gff formatted file.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GffWriter
{
    private byte[] type = new byte[4];
    private byte[] version = new byte[4];
    private BinaryDataOutputStream out;
    private List structs = new ArrayList();
    private List structStubs = new ArrayList();
    private List labels = new ArrayList();
    private List elements = new ArrayList();
    private List elementStubs = new ArrayList();
    private List listStubs = new ArrayList();

    public GffWriter( String type, String version, OutputStream out )
    {
        this.out = new BinaryDataOutputStream( out );

        byte[] t = type.getBytes();
        byte[] v = version.getBytes();

        for( int i = 0; i < 4; i++ )
            {
            this.type[i] = t[i];
            this.version[i] = v[i];
            }
    }

    /**
     *  Adds the specified label and returns the label index.
     */
    protected int addLabel( String label )
    {
        int i = labels.indexOf( label );
        if( i >= 0 )
            return( i );

        i = labels.size();
        labels.add( label );

        return( i );
    }

    /**
     *  Adds the specified list and returns the byte offset of
     *  list in the list data block.
     */
    protected int addList( List values )
    {
        ListStub lastStub = null;
        if( listStubs.size() > 0 )
            lastStub = (ListStub)listStubs.get( listStubs.size() - 1 );

        ListStub stub = new ListStub();
        listStubs.add( stub );
        if( lastStub != null )
            stub.offset = lastStub.getEndOffset();

        for( Iterator i = values.iterator(); i.hasNext(); )
            {
            Struct s = (Struct)i.next();
            stub.indices.add( new Integer( addStruct( s ) ) );
            }

        return( stub.offset );
    }

    protected int addElement( Element el )
    {
        int i = elements.indexOf( el );
        if( i >= 0 )
            return( i );

        i = elements.size();
        elements.add( el );

        ElementStub lastStub = null;
        if( elementStubs.size() > 0 )
            lastStub = (ElementStub)elementStubs.get( elementStubs.size() - 1 );

        String label = el.getName();
        ElementStub stub = new ElementStub( el.getType(), addLabel( label ) );
        elementStubs.add( stub );

        if( lastStub != null )
            stub.offset = lastStub.getEndOffset();

        // Certain elements contain additional data that needs
        // to be indexed also
        if( el instanceof StructElement )
            {
            StructElement se = (StructElement)el;
            stub.data = addStruct( se.getStruct() );
            }
        else if( el instanceof ListElement )
            {
            ListElement ll = (ListElement)el;
            stub.data = addList( ll.getValue() );
            }
        else
            {
            stub.data = stub.offset;
            stub.size = el.getSize();
            }

        return( i );
    }

    protected int addStruct( Struct struct )
    {
        int i = structs.indexOf( struct );
        if( i >= 0 )
            return( i );

        i = structs.size();
        structs.add( struct );
        StructStub stub = new StructStub( struct.getId() );
        structStubs.add( stub );

        for( Iterator it = struct.values(); it.hasNext(); )
            {
            Element el = (Element)it.next();
            stub.elementIndex.add( new Integer( addElement( el ) ) );
            }

        return( i );
    }

    public void writeStruct( Struct struct ) throws IOException
    {
        // Accumulate the data into nice orderly piles.
        addStruct( struct );
    }

    protected void close() throws IOException
    {
        out.close();
    }

    private class StructStub
    {
        int type;
        List elementIndex = new ArrayList();

        public StructStub( int type )
        {
            this.type = type;
        }
    }

    private class ElementStub
    {
        // Offset and size aren't used by all written stubs, but they
        // are used to keep the current offset in the top list item.
        int offset;
        int size = 0;

        int type;
        int labelIndex;
        int data;

        public ElementStub( int type, int labelIndex )
        {
            this.type = type;
            this.labelIndex = labelIndex;
        }

        public int getEndOffset()
        {
            return( offset + size );
        }
    }

    private class ListStub
    {
        int offset;
        List indices = new ArrayList();

        public int getEndOffset()
        {
            return( offset + (indices.size() * 4) );
        }
    }
}
