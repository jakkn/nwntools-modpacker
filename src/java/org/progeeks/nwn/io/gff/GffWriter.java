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

    protected Object getLastItem( List items )
    {
        if( items == null )
            return( null );

        int size = items.size();
        if( size == 0 )
            return( null );
        return( items.get( size - 1 ) );
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
        ListStub lastStub = (ListStub)getLastItem( listStubs );
        ListStub stub = new ListStub( values.size(), lastStub );
        listStubs.add( stub );

        for( Iterator i = values.iterator(); i.hasNext(); )
            {
            Struct s = (Struct)i.next();
            stub.indices.add( new Integer( addStruct( s ) ) );
            }

        return( stub.getOffset() );
    }

    protected int addElement( Element el )
    {
        int i = elements.indexOf( el );
        if( i >= 0 )
            return( i );

        i = elements.size();
        elements.add( el );

        ElementStub lastStub = (ElementStub)getLastItem( elementStubs );

        String label = el.getName();
        ElementStub stub = new ElementStub( el.getType(), addLabel( label ), lastStub );
        elementStubs.add( stub );

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
        else if( el instanceof FloatElement )
            {
            stub.data = Float.floatToIntBits( ((FloatElement)el).getValue() );
            }
        else if( el instanceof IntElement )
            {
            stub.data = ((IntElement)el).getValue();
            }
        else
            {
            // The offset should be accurate here because we are the only
            // type of stub with an actual size... and we can't nest ourselves.
            // This means that any previous stubs are either 0 in size or
            // the same type as we are and therefore can accurately calculate
            // their size.  This is not true for lists and structs which is why
            // they take extra care.
            stub.data = stub.getOffset();
            stub.size = el.getSize();
            }

        return( i );
    }

    protected int addStruct( Struct struct )
    {
        int i = structs.indexOf( struct );
        if( i >= 0 )
            return( i );

        StructStub lastStub = (StructStub)getLastItem( structStubs );

        i = structs.size();
        structs.add( struct );
        StructStub stub = new StructStub( struct.getId(), lastStub );
        structStubs.add( stub );

        for( Iterator it = struct.values(); it.hasNext(); )
            {
            Element el = (Element)it.next();
            stub.elementIndex.add( new Integer( addElement( el ) ) );
            }

        return( i );
    }

    protected void writeHeader() throws IOException
    {
        int headerSize = Header.HEADER_SIZE;
        int structSize = 12 * structStubs.size();
        int fieldSize = 12 * elementStubs.size();
        int labelSize = 16 * labels.size();

        int dataBlockSize = 0;
        ElementStub lastElement = (ElementStub)getLastItem( elementStubs );
        if( lastElement != null )
            dataBlockSize = lastElement.getEndOffset();

        int fieldIndexSize = 0;
        StructStub lastStub = (StructStub)getLastItem( structStubs );
        if( lastStub != null )
            fieldIndexSize = lastStub.getEndOffset();

        int listBlockSize = 0;
        ListStub lastList = (ListStub)getLastItem( listStubs );
        if( lastList != null )
            listBlockSize = lastList.getEndOffset();

        out.write( type );
        out.write( version );

        // Write the offset/size pairs for the different blocks

        // Structs
        int offset = headerSize;
//System.out.println( "Offset:" + offset + "   size:" + structStubs.size() + "  bytes:" + structSize );
        out.writeInt( offset );
        out.writeInt( structStubs.size() );
        offset += structSize;

        // Fields
//System.out.println( "Offset:" + offset + "   size:" + elementStubs.size() + "  bytes:" + fieldSize );
        out.writeInt( offset );
        out.writeInt( elementStubs.size() );
        offset += fieldSize;

        // Labels
//System.out.println( "Offset:" + offset + "   size:" + labels.size() + "  bytes:" + labelSize );
        out.writeInt( offset );
        out.writeInt( labels.size() );
        offset += labelSize;

        // Field data
//System.out.println( "Offset:" + offset + "   size:" + dataBlockSize );
        out.writeInt( offset );
        out.writeInt( dataBlockSize );
        offset += dataBlockSize;

        // Field index
//System.out.println( "Offset:" + offset + "   size:" + fieldIndexSize );
        out.writeInt( offset );
        out.writeInt( fieldIndexSize );
        offset += fieldIndexSize;

        // Lists
//System.out.println( "Offset:" + offset + "   size:" + listBlockSize );
        out.writeInt( offset );
        out.writeInt( listBlockSize );
    }

    protected void writeData() throws IOException
    {
        // Structs
        for( Iterator i = structStubs.iterator(); i.hasNext(); )
            {
            StructStub stub = (StructStub)i.next();
            out.writeInt( stub.type );
            int size = stub.elementIndex.size();

            if( size == 0 )
                {
                out.writeInt( -1 );
                }
            else if( size == 1 )
                {
                Integer index = (Integer)stub.elementIndex.get(0);
                out.writeInt( index.intValue() );
                }
            else
                {
                out.writeInt( stub.getOffset() );
                }

            out.writeInt( size );
            }

        // Fields
        for( Iterator i = elementStubs.iterator(); i.hasNext(); )
            {
            ElementStub stub = (ElementStub)i.next();
            out.writeInt( stub.type );
            out.writeInt( stub.labelIndex );
            out.writeInt( stub.data );
            }

        // Labels
        for( Iterator i = labels.iterator(); i.hasNext(); )
            {
            String s = (String)i.next();
            byte[] sb = s.getBytes();
            if( sb.length >= 16 )
                {
                out.write( sb, 0, 16 );
                }
            else
                {
                out.write( sb );
                for( int j = sb.length; j < 16; j++ )
                    out.write( 0 );
                }
            }

        // Field data
        Double d;
        Long l;
        String s;
        byte[] sb;
        for( Iterator i = elements.iterator(); i.hasNext(); )
            {
            Element el = (Element)i.next();
            switch( el.getType() )
                {
                case Element.TYPE_UINT8:
                case Element.TYPE_INT8:
                case Element.TYPE_UINT16:
                case Element.TYPE_INT16:
                case Element.TYPE_UINT32:
                case Element.TYPE_INT32:
                case Element.TYPE_FLOAT:
                case Element.TYPE_STRUCTREF:
                case Element.TYPE_LIST:
                case Element.TYPE_STRUCT:
                    // These are all written in the stub or in another
                    // section.
                    break;

                case Element.TYPE_UINT64:
                    l = (Long)((ObjectElement)el).getValue();
                    out.writeLong( l.longValue() );
                    break;
                case Element.TYPE_INT64:
                    l = (Long)((ObjectElement)el).getValue();
                    out.writeLong( l.longValue() );
                    break;
                case Element.TYPE_DOUBLE:
                    d = (Double)((ObjectElement)el).getValue();
                    out.writeDouble( d.doubleValue() );
                    break;
                case Element.TYPE_STRING:
                    s = (String)((StringElement)el).getValue();
                    sb = s.getBytes();
                    out.writeInt( sb.length );
                    out.write( sb );
                    break;
                case Element.TYPE_RESREF:
                    s = (String)((StringElement)el).getValue();
                    sb = s.getBytes();
                    out.writeByte( sb.length );
                    out.write( sb, 0, Math.min( 256, sb.length ) );
                    break;
                case Element.TYPE_STRREF:
                    LocalizedStringElement lse = (LocalizedStringElement)el;

                    out.writeInt( lse.getSize() );
                    out.writeInt( lse.getReferenceId() );

                    Map map = lse.getLocalStrings();
                    out.writeInt( map.size() );
                    for( Iterator it = map.entrySet().iterator(); it.hasNext(); )
                        {
                        Map.Entry e = (Map.Entry)it.next();
                        out.writeInt( ((Integer)e.getKey()).intValue() );

                        String val = (String)e.getValue();
                        sb = val.getBytes();
                        out.writeInt( sb.length );
                        out.write( sb );
                        }

                    break;
                case Element.TYPE_DATREF:
                    sb = (byte[])((ObjectElement)el).getValue();
                    out.writeInt( sb.length );
                    out.write( sb );
                    break;
                default:
                    throw new RuntimeException( "Cannot write type." );
                }
            }

        // Field index
        for( Iterator i = structStubs.iterator(); i.hasNext(); )
            {
            StructStub stub = (StructStub)i.next();
            if( stub.elementIndex.size() <= 1 )
                continue;

            for( Iterator j = stub.elementIndex.iterator(); j.hasNext(); )
                {
                Integer index = (Integer)j.next();
                out.writeInt( index.intValue() );
                }
            }

        // Lists
        for( Iterator i = listStubs.iterator(); i.hasNext(); )
            {
            ListStub stub = (ListStub)i.next();
            out.writeInt( stub.indices.size() );
            for( Iterator j = stub.indices.iterator(); j.hasNext(); )
                {
                Integer index = (Integer)j.next();
                out.writeInt( index.intValue() );
                }
            }
    }

    public void writeStruct( Struct struct ) throws IOException
    {
        // Accumulate the data into nice orderly piles.
        addStruct( struct );

        try
            {
            writeHeader();
            writeData();
            }
        finally
            {
            out.close();
            }
    }

    protected void close() throws IOException
    {
        out.close();
    }

    private class Stub
    {
        Stub previous;

        public Stub( Stub previous )
        {
            this.previous = previous;
        }

        public int getOffset()
        {
            if( previous == null )
                return( 0 );
            return( previous.getEndOffset() );
        }

        public int getSize()
        {
            return( 0 );
        }

        public int getEndOffset()
        {
            return( getOffset() + getSize() );
        }
    }

    private class StructStub extends Stub
    {
        // Offset in the field index data block
        int type;
        List elementIndex = new ArrayList();

        public StructStub( int type, Stub previous )
        {
            super( previous );
            this.type = type;
        }

        public int getSize()
        {
            // In the case of only one element it's just stored
            // directly in the struct
            if( elementIndex.size() <= 1 )
                return( 0 );

            return( elementIndex.size() * 4 );
        }
    }

    private class ElementStub extends Stub
    {
        // Size of the element in the field data block.
        // Size isn't used by all written stubs, but it is
        // used to keep the current offset in the top list item.
        int size = 0;

        int type;
        int labelIndex;
        int data;

        public ElementStub( int type, int labelIndex, Stub previous )
        {
            super( previous );
            this.type = type;
            this.labelIndex = labelIndex;
        }

        public int getSize()
        {
            return( size );
        }
    }

    private class ListStub extends Stub
    {
        int indexSize = 0;
        List indices = new ArrayList();

        public ListStub( int indexSize, Stub previous )
        {
            super( previous );

            // Set the index size up front since it is easy
            // and means this stub can properly participate in
            // offset calculations even though it doesn't have all
            // of its list items yet.
            this.indexSize = indexSize;
        }

        public int getSize()
        {
            return( 4 + (indexSize * 4) );
        }
    }
}
