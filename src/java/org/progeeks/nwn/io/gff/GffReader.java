/*
 * $Id$
 *
 * Copyright (c) 2001-2002, Paul Speed
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
 *  Reader for reading the data elements from a GFF file.
 *  NOT a standard implementation of java.io.Reader.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GffReader
{
    private BinaryDataInputStream in;
    private Header header;
    private List structs = new ArrayList();
    private List fields = new ArrayList();
    private List labels = new ArrayList();

    public GffReader( InputStream in ) throws IOException
    {
        this.in = new BinaryDataInputStream(in);

        header = new Header( this.in );

        List structStubs = readStubBlock( header.getStructs() );
        List fieldStubs = readStubBlock( header.getFields() );

        // Now sort based on the data elements
        Collections.sort( fieldStubs );
        Collections.sort( structStubs, new IndexComparator() );

        readLabels( header.getLabels() );

        resolveFields( fieldStubs );
        resolveFieldIndices( structStubs );
        resolveStructReferences( fieldStubs );
        resolveLists( fieldStubs );
    }

    public Struct getRootStruct()
    {
        return( (Struct)structs.get(0) );
    }

    public void printElement( Object obj, String indent )
    {
        List l = null;
        /*if( obj instanceof List )
            {
            System.out.println( indent + "  Entry:" );
            indent += "  ";
            l = (List)obj;
            }
        else */
        if( obj instanceof Struct )
            {
            Struct s = (Struct)obj;
            System.out.println( indent + "  Struct {" );
            indent += "  ";
            l = s.getValues();
            }
        else if( obj instanceof StructElement )
            {
            StructElement sl = (StructElement)obj;
            System.out.println( indent + sl.getName() + " {" );
            l = sl.getStruct().getValues();
            }
        else if( obj instanceof ListElement )
            {
            ListElement el = (ListElement)obj;

            System.out.println( indent + el.getName() + ":" );
            l = el.getValue();
            }
        else
            {
            System.out.println( indent + obj );
            return;
            }

        for( Iterator i = l.iterator(); i.hasNext(); )
            {
            printElement( i.next(), indent + "  " );
            }

        if( obj instanceof Struct || obj instanceof StructElement )
            {
            System.out.println( indent + "}" );
            }
    }

    private List readStubBlock( BlockIndex block ) throws IOException
    {
        ArrayList results = new ArrayList();
        in.gotoPosition( block.getOffset() );
        int count = block.getSize();

        for( int i = 0; i < count; i++ )
            {
            Stub s = new Stub(i, in);
            results.add( s );
            }

        return( results );
    }

    private void readLabels( BlockIndex block ) throws IOException
    {
        in.gotoPosition( block.getOffset() );
        int count = block.getSize();

        byte[] buff = new byte[16];
        for( int i = 0; i < count; i++ )
            {
            in.readFully( buff );
            String s = new String(buff).trim();
            labels.add( s );
            }
    }

    /**
     *  Resolves all but the list-based fields.
     */
    private void resolveFields( List fieldStubs ) throws IOException
    {
        // We make the assumption that the fields are sorted
        // such that we don't have to jump back and forth through
        // the data section.  We'll make a second pass for the list
        // based elements.

        // Make sure the fields list has the right number of entries
        for( int i = 0; i < fieldStubs.size(); i++ )
            {
            fields.add( null );
            }

        int dataOffset = header.getValues().getOffset();
//System.out.println( "Data offset:" + dataOffset );
        for( Iterator i = fieldStubs.iterator(); i.hasNext(); )
            {
            Stub stub = (Stub)i.next();
            String name = (String)labels.get(stub.index);
//System.out.println( "Name:" + name + "  type:" + stub.type + "  index:" + stub.index + "  data:" + stub.data + "      offset:" + in.getFilePosition() );
            Element el = null;
            int len;
            byte[] buff;

            switch( stub.type )
                {
                case Element.TYPE_UINT8:
                case Element.TYPE_INT8:
                case Element.TYPE_UINT16:
                case Element.TYPE_INT16:
                case Element.TYPE_UINT32:
                case Element.TYPE_INT32:
                    el = new IntElement( name, stub.type, stub.data );
                    break;
                case Element.TYPE_FLOAT:
                    el = new FloatElement( name, stub.type, Float.intBitsToFloat(stub.data) );
                    break;
                case Element.TYPE_UINT64:
                    in.gotoPosition( dataOffset + stub.data );
                    el = new ObjectElement( name, stub.type, new Long( in.readLong() ) );
                    break;
                case Element.TYPE_INT64:
                    in.gotoPosition( dataOffset + stub.data );
                    el = new ObjectElement( name, stub.type, new Long( in.readLong() ) );
                    break;
                case Element.TYPE_DOUBLE:
                    in.gotoPosition( dataOffset + stub.data );
                    el = new ObjectElement( name, stub.type, new Double( in.readDouble() ) );
                    break;
                case Element.TYPE_STRING:
                    in.gotoPosition( dataOffset + stub.data );
                    len = in.readInt();
                    buff = new byte[len];
                    in.readFully( buff );
                    el = new StringElement( name, stub.type, new String(buff) );
                    break;
                case Element.TYPE_RESREF:
                    in.gotoPosition( dataOffset + stub.data );
                    len = in.readUnsignedByte();
                    buff = new byte[len];
                    in.readFully( buff );
                    el = new StringElement( name, stub.type, new String(buff) );
                    break;
                case Element.TYPE_STRREF:
                    in.gotoPosition( dataOffset + stub.data );

                    len = in.readInt();
                    int id = in.readInt();
                    int langCount = in.readInt();
                    String val = null;

                    for( int t = 0; t < langCount; t++ )
                        {
                        int l = in.readInt();
                        len = in.readInt();
                        buff = new byte[len];
                        in.readFully( buff );
                        if( val == null )
                            val = new String( buff );
                        }
                    el = new StringElement( name, stub.type, val );
                    break;
                case Element.TYPE_DATREF:
                    in.gotoPosition( dataOffset + stub.data );
                    len = in.readInt();
                    buff = new byte[len];
                    in.readFully( buff );
                    el = new ObjectElement( name, stub.type, buff );
                    break;
                case Element.TYPE_STRUCTREF:
                    //in.gotoPosition( dataOffset + stub.data );
                    // Data is an index into the struct array
                    el = new StructElement( name, stub.type, null );
                    break;
                case Element.TYPE_LIST:
                    // Do these on another pass
                    el = new ListElement( name, stub.type );
                    break;
                }

            fields.set( stub.entryNumber, el );
            }
    }

    private void resolveFieldIndices( List structStubs ) throws IOException
    {
        int multimapOffset = header.getFieldIndices().getOffset();

        // Make sure the structs list has the right number of entries
        for( int i = 0; i < structStubs.size(); i++ )
            {
            structs.add( null );
            }

        for( Iterator i = structStubs.iterator(); i.hasNext(); )
            {
            Stub stub = (Stub)i.next();
            ArrayList list = new ArrayList();

            if( stub.index < 0 )
                {
                System.out.println( "Negative struct index encountered.  Skipping." );
                }
            else if( stub.data == 1 )
                {
                // Just one element, so pull it directly
                Element el = (Element)fields.get( stub.index );

                list.add( el );
                }
            else
                {
                in.gotoPosition( stub.index + multimapOffset );

                // Add indirect element references
                for( int t = 0; t < stub.data; t++ )
                    {
                    int elementNum = in.readInt();
                    Element el = (Element)fields.get(elementNum);
                    list.add( el );
                    }
                }

            structs.set( stub.entryNumber, new Struct( list ) );
            }

    }

    private void resolveStructReferences( List fieldStubs )
    {
        for( Iterator i = fieldStubs.iterator(); i.hasNext(); )
            {
            Stub stub = (Stub)i.next();
            if( stub.type != Element.TYPE_STRUCTREF )
                continue;
            StructElement el = (StructElement)fields.get( stub.entryNumber );

            el.setStruct( (Struct)structs.get( stub.data ) );
            }
    }

    private void resolveLists( List fieldStubs ) throws IOException
    {
        long listOffset = header.getLists().getOffset();

        // First make a list with just the list elements
        ArrayList listStubs = new ArrayList();
        for( Iterator i = fieldStubs.iterator(); i.hasNext(); )
            {
            Stub stub = (Stub)i.next();
            if( stub.type != Element.TYPE_LIST )
                continue;
            listStubs.add( stub );
            }

        // Now sort based on the data element
 //       Collections.sort( listStubs );

        // And resolve
        for( Iterator i = listStubs.iterator(); i.hasNext(); )
            {
            Stub stub = (Stub)i.next();
            String name = (String)labels.get(stub.index);

            in.gotoPosition( listOffset + stub.data );

            // Get the original index
            int index = stub.entryNumber; //fieldStubs.indexOf( stub );

            // And thus the original position of the real element
            ListElement el = (ListElement)fields.get(index);

            int count = in.readInt();

            ArrayList list = new ArrayList();
            for( int t = 0; t < count; t++ )
                {
                int num = in.readInt();

                Object ent = structs.get( num );

                list.add( ent );
                }

            el.setValue( list );
            }
    }

    public static void main( String[] args ) throws IOException
    {
        for( int i = 0; i < args.length; i++ )
            {
            FileInputStream fIn = new FileInputStream( args[i] );
            BufferedInputStream bIn = new BufferedInputStream( fIn, 16384 );
            try
                {
                System.out.println( args[i] );
                GffReader reader = new GffReader( bIn );

                Struct root = reader.getRootStruct();
                for( Iterator it = root.getValues().iterator(); it.hasNext(); )
                    {
                    Element e = (Element)it.next();
                    reader.printElement( e, "    " );
                    }

                }
            finally
                {
                bIn.close();
                }
            }
    }

    private class IndexComparator implements Comparator
    {
        public int compare( Object o1, Object o2 )
        {
            int d1 = ((Stub)o1).index;
            int d2 = ((Stub)o2).index;
            if( d1 < d2 )
                return( -1 );
            if( d1 > d2 )
                return( 1 );
            return( 0 );
        }
    }

    private class Stub implements Comparable
    {
        int entryNumber;
        int type;
        int index;
        int data;

        public Stub( int entryNumber, DataInput in ) throws IOException
        {
            this.entryNumber = entryNumber;
            type = in.readInt();
            index = in.readInt();
            data = in.readInt();
        }

        /**
         *  Compares based on the data element.
         */
        public int compareTo( Object obj )
        {
            int d2 = ((Stub)obj).data;
            if( data < d2 )
                return( -1 );
            if( data > d2 )
                return( 1 );
            return( 0 );
            //return( data - ((Stub)obj).data );
        }

        public String toString()
        {
            return( "Stub[" + type + ", " + index + ", " + data + "]" );
        }
    }
}
