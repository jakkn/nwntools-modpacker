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

import java.util.*;

import org.xml.sax.AttributeList;

import com.phoenixst.plexus.*;

import org.progeeks.meta.xml.*;
import org.progeeks.util.xml.*;

/**
 *  Object handler implementation that can deal with
 *  converting XML to plexus Graph objects.  For the most
 *  part this treats the object like a bean, but certain
 *  fields have significant meaning to this handler and do
 *  not directly relate to bean setters.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GraphObjectHandler extends DefaultObjectHandler
{
    public GraphObjectHandler()
    {
        importPackage( "org.progeeks.nwn.model" );
    }

    /**
     *  Returns true if this handler applies to the specified
     *  object tag.
     */
    public boolean canHandle( String tag )
    {
        // Check for the edge tag first.
        if( "edge".equals( tag ) )
            return( true );

        return( super.canHandle( tag ) );
    }

    /**
     *  Sets a property on the specified object.  Does special
     *  processing for the "nodes" and "edges" fields.
     */
    public void setProperty( Object obj, String field, Object value, ObjectXmlReader reader )
    {
        if( "nodes".equals( field ) )
            {
            Graph graph = (Graph)obj;
            List nodes = (List)value;
            for( Iterator i = nodes.iterator(); i.hasNext(); )
                {
                graph.addNode( i.next() );
                }
            }
        else if( "edges".equals( field ) )
            {
            Graph graph = (Graph)obj;
            List edges = (List)value;
            for( Iterator i = edges.iterator(); i.hasNext(); )
                {
                EdgeHolder edge = (EdgeHolder)i.next();
                graph.addEdge( edge.getObject(), edge.getTail(), edge.getHead(), edge.isDirected() );
                }
            }
        else
            {
            super.setProperty( obj, field, value, reader );
            }
    }

    /**
     *  Returns the base type for the specified field.  Does special
     *  processing for the "nodes" and "edges" fields.
     */
    public Class getPropertyClass( Object obj, String field, ObjectXmlReader reader )
    {
        if( "nodes".equals( field ) )
            {
            return( List.class );
            }
        else if( "edges".equals( field ) )
            {
            return( List.class );
            }
        else
            {
            return( super.getPropertyClass( obj, field, reader ) );
            }
    }

    /**
     *  Returns the a collection for the specified field that can be used to
     *  accumulate multiple object values.  This is used when the XML field
     *  declaration specifies the progressive attribute.
     */
    public Collection getPropertyCollection( Object obj, String field, ObjectXmlReader reader )
    {
        if( "nodes".equals( field ) )
            {
            return( new ArrayList() );
            }
        else if( "edges".equals( field ) )
            {
            return( new ArrayList() );
            }
        else
            {
            return( super.getPropertyCollection( obj, field, reader ) );
            }
    }

    /**
     *  Creates and returns an object based on the specified parameters.
     */
    public Object createObject( String tag, AttributeList atts, ObjectXmlReader reader )
    {
        if( !"edge".equals( tag ) )
            return( super.createObject( tag, atts, reader ) );

        EdgeHolder edge = new EdgeHolder();

        // We'll handle all edge properties ourselves since we
        // know exactly what they are.
        String tailId = atts.getValue( "tail" );
        edge.tail = reader.getReferenceObject( tailId );

        String headId = atts.getValue( "head" );
        edge.head = reader.getReferenceObject( headId );

        String directed = atts.getValue( "directed" );
        if( directed != null )
            setTextProperty( edge, "directed", directed, reader );

        String userObject = atts.getValue( "object" );
        if( userObject != null )
            setTextProperty( edge, "object", userObject, reader );

        return( edge );
    }

    public class EdgeHolder
    {
        private Object tail;
        private Object head;
        private Object userObject;
        private boolean directed;

        public EdgeHolder()
        {
        }

        public void setDirected( boolean directed )
        {
            this.directed = directed;
        }

        public boolean isDirected()
        {
            return( directed );
        }

        public void setTail( Object tail )
        {
            this.tail = tail;
        }

        public Object getTail()
        {
            return( tail );
        }

        public void setHead( Object head )
        {
            this.head = head;
        }

        public Object getHead()
        {
            return( head );
        }

        public void setObject( Object userObject )
        {
            this.userObject = userObject;
        }

        public Object getObject()
        {
            return( userObject );
        }
    }
}


