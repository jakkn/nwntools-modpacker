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

package org.progeeks.nwn.io.xml;

import java.io.*;
import java.util.*;

import org.xml.sax.*;

import org.progeeks.util.log.*;
import org.progeeks.util.xml.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;

/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GffXmlReader extends XmlReader
{
    public static final String TAG_GFF  = "gff";
    public static final String TAG_STRUCT = "struct";
    public static final String TAG_ELEMENT = "element";
    public static final String TAG_STRING_VALUE = "value";
    public static final String TAG_LOCAL_STRING = "localString";

    static Log log = Log.getLog( GffXmlReader.class );

    private String name;
    private String type;
    private String version;
    private Struct root;

    public GffXmlReader()
    {
        setDefaultHandler( new TestTagReader() );
        registerHandler( TAG_GFF, new GffTagReader() );
        registerHandler( TAG_STRUCT, new StructTagReader() );
        registerHandler( TAG_ELEMENT, new ElementTagReader() );
        registerHandler( TAG_STRING_VALUE, new StringValueTagReader() );
        registerHandler( TAG_LOCAL_STRING, new LocalStringTagReader() );
    }

    /**
     *  Returns the resource name of the last file read.
     */
    public String getName()
    {
        return( name );
    }

    /**
     *  Returns the resource type string of the last file read.
     */
    public String getType()
    {
        return( type );
    }

    /**
     *  Returns the resource version of the last file read.
     */
    public String getVersion()
    {
        return( version );
    }

    public Object readObject( Reader in ) throws IOException
    {
        try
            {
            readXml( in );

            return( root );
            }
        catch( XmlException e )
            {
            log.error( "Error reading XML", e );
            throw new IOException( e.getMessage() );
            }
    }

    public void reset()
    {
        name = null;
        root = null;
    }

    public static void main( String[] args ) throws IOException
    {
        Log.initialize();

        GffXmlReader reader = new GffXmlReader();

        for( int i = 0; i < args.length; i++ )
            {
            FileReader fIn = new FileReader( args[i] );
            BufferedReader bIn = new BufferedReader( fIn, 16384 );
            try
                {
                System.out.println( args[i] );

                Object obj = reader.readObject( bIn );
                Struct root = (Struct)obj;
                for( Iterator it = root.getValues().iterator(); it.hasNext(); )
                    {
                    Element e = (Element)it.next();
                    GffReader.printElement( e, "    " );
                    }
                }
            finally
                {
                bIn.close();
                }
            }
    }


    // <gff name="_reserved.git" type="GIT " version="V3.2" >
    private class GffTagReader extends TagReader
    {
        public Object tagStart( String tag, AttributeList atts,
                                Object parent ) throws XmlException
        {
            // Just in case
            reset();

            GffXmlReader.this.name = atts.getValue( "name" );
            GffXmlReader.this.type = atts.getValue( "type" );
            GffXmlReader.this.version = atts.getValue( "version" );

            return( null );
        }
    }

    // <struct id="-1" >
    private class StructTagReader extends TagReader
    {
        public Object tagStart( String tag, AttributeList atts,
                                Object parent ) throws XmlException
        {
            int id = Integer.parseInt( atts.getValue( "id" ) );

            Struct struct = new Struct( id );

            if( parent == null )
                {
                root = struct;
                }
            else if( parent instanceof StructElement )
                {
                ((StructElement)parent).setStruct( struct );
                }
            else if( parent instanceof ListElement )
                {
                ((ListElement)parent).addValue( struct );
                }
            else
                {
                throw new RuntimeException( "Unknown parent type:" + parent.getClass() );
                }

            return( struct );
        }
    }

    // <element name="AreaProperties" type="14" >
    // <element name="AmbientSndDay" type="5" value="51" />
    private class ElementTagReader extends TagReader
    {
        public Object tagStart( String tag, AttributeList atts,
                                Object parent ) throws XmlException
        {
            String label = atts.getValue( "name" );
            int type = Integer.parseInt( atts.getValue( "type" ) );
            Element el;

            switch( type )
                {
                case Element.TYPE_UINT8:
                case Element.TYPE_INT8:
                case Element.TYPE_UINT16:
                case Element.TYPE_INT16:
                case Element.TYPE_UINT32:
                case Element.TYPE_INT32:
                    el = new IntElement( label, type, Integer.parseInt( atts.getValue( "value" ) ) );
                    break;
                case Element.TYPE_FLOAT:
                    el = new FloatElement( label, type, Float.parseFloat( atts.getValue( "value" ) ) );
                    break;
                case Element.TYPE_UINT64:
                    el = new ObjectElement( label, type, new Long( atts.getValue( "value" ) ) );
                    break;
                case Element.TYPE_INT64:
                    el = new ObjectElement( label, type, new Long( atts.getValue( "value" ) ) );
                    break;
                case Element.TYPE_DOUBLE:
                    el = new ObjectElement( label, type, new Double( atts.getValue( "value" ) ) );
                    break;
                case Element.TYPE_STRING:
                    // We'll fill in the text in the end tag
                    el = new StringElement( label, type, atts.getValue( "value" ) );
                    break;
                case Element.TYPE_RESREF:
                    el = new StringElement( label, type, atts.getValue( "value" ) );
                    break;
                case Element.TYPE_STRREF:
                    int refId = Integer.parseInt( atts.getValue( "value" ) );
                    el = new LocalizedStringElement( label, type, refId );
                    break;
                case Element.TYPE_DATREF:
                    // We don't really handle this type correctly yet.
                    // Need to come up with a decent encoding scheme.
                    el = new ObjectElement( label, type, atts.getValue( "value" ) );
                    break;
                case Element.TYPE_STRUCTREF:
                    // We'll pick up the struct as a nested tag
                    el = new StructElement( label, type, null );
                    break;
                case Element.TYPE_LIST:
                    // We'll pick up the entries as nested tags
                    el = new ListElement( label, type );
                    break;
                default:
                    throw new RuntimeException( "Cannot calculate size for type." );
                }

            if( parent instanceof Struct )
                {
                Struct struct = (Struct)parent;
                struct.addValue( el );
                }
            else
                {
                throw new RuntimeException( "Unknown parent type:" + parent.getClass() );
                }

            return( el );
        }

        public void tagEnd( String tag, String text,
                            Object parent, Object tagObject ) throws XmlException
        {
            if( tagObject instanceof StringElement )
                {
                StringElement se = (StringElement)tagObject;
                if( se.getValue() == null )
                    {
                    //System.out.println( "Setting string element[" + text + "]" );
                    se.setValue( text );
                    }
                }
        }
    }

    private class StringValueTagReader extends TagReader
    {
        public void tagEnd( String tag, String text,
                            Object parent, Object tagObject ) throws XmlException
        {
            if( parent instanceof StringElement )
                {
                //System.out.println( "Value:[" + text + "]" );
                StringElement se = (StringElement)parent;

                //System.out.println( "Setting string element[" + text + "]" );
                se.setValue( text );
                }
            else if( parent instanceof LocalStringHolder )
                {
                // Resolve the holder to the original element
                LocalStringHolder holder = (LocalStringHolder)parent;
                holder.el.addLocalString( holder.languageId, text );
                }
            else
                {
                throw new RuntimeException( "Unknown parent type:" + parent.getClass() );
                }
        }
    }

    // <localString languageId="0" value=" @`€ Ààÿ" />
    private class LocalStringTagReader extends TagReader
    {
        public Object tagStart( String tag, AttributeList atts,
                                Object parent ) throws XmlException
        {
            if( parent instanceof LocalizedStringElement )
                {
                LocalizedStringElement el = (LocalizedStringElement)parent;

                int langId = Integer.parseInt( atts.getValue( "languageId" ) );
                String value = atts.getValue( "value" );
                if( value != null )
                    {
                    el.addLocalString( langId, value );
                    }
                else
                    {
                    // Have to create a place-holder object to catch the text
                    return( new LocalStringHolder( el, langId ) );
                    }
                }
            else
                {
                throw new RuntimeException( "Unknown parent type:" + parent.getClass() );
                }
            return( tag );
        }
    }

    private class TestTagReader extends TagReader
    {
        public Object tagStart( String tag, AttributeList atts,
                                Object parent ) throws XmlException
        {
            System.out.println( "tagStart(" + tag + ", " + parent + ")" );
            return( tag );
        }

        public void tagText( String tag, String text, StringBuffer buffer,
                             Object parent, Object tagObject ) throws XmlException
        {
            System.out.println( "tagText(" + tag + ", " + text + ")" );
            buffer.append( text );
        }

        public void tagEnd( String tag, String text,
                            Object parent, Object tagObject ) throws XmlException
        {
            System.out.println( "tagEnd(" + tag + ", " + parent + ", " + text + ")" );
        }
    }

    private class LocalStringHolder
    {
        LocalizedStringElement el;
        int languageId;

        public LocalStringHolder( LocalizedStringElement el, int languageId )
        {
            this.languageId = languageId;
        }
    }
}

