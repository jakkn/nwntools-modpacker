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

import org.progeeks.meta.*;
import org.progeeks.meta.xml.*;

import com.phoenixst.plexus.*;

/**
 *  A generic XML renderer that can render any plexus-based Graph
 *  object as XML.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GraphXmlRenderer extends XmlMetaObjectRenderer
{
    /**
     *  Returns true if the property value can be rendered
     *  as an attribute.  Always returns false, graphs can never be
     *  rendered as an attribute.
     */
    public boolean isAttribute()
    {
        return( false );
    }

    /**
     *  Throws UnsupportedOperationException.
     */
    public String getAsAttribute( Object value, PropertyType type, XmlRenderContext context )
    {
        throw new UnsupportedOperationException( "Value cannot be retrieved as an attribute." );
    }

    protected void renderObject( Object obj, boolean register, MetaClassRegistry registry,MetaKit metaKit,
                                 XmlRenderContext context )
    {
        Object original = obj;

        // Need to do a little guessing about the node type.
        MetaClass mClass = metaKit.getMetaClassForObject( obj, registry );
        PropertyType type;
        if( mClass != null )
            {
            obj = metaKit.wrapObject( obj, mClass );
            type = new MetaClassPropertyType( mClass );
            }
        else
            {
            type = new ClassPropertyType( obj.getClass() );
            }

        XmlPropertyRenderer renderer = context.getRenderer( type, obj );
        if( renderer.isAttribute() )
            {
            // Need extra wrapping for now.  Attribute renderers should be
            // able to render themselves as objects too.
            context.getWriter().printTag( obj.getClass().getName() );
            context.getWriter().printAttribute( "_ctor", renderer.getAsAttribute( obj, type, context ) );
            return;
            }

        if( register )
            {
            // Then something else refers to it so register the object
            // and hope that it's renderer is smart enough to stick in the
            // OID directive.  We register both objects since the
            // tail and source writing short-circuits the standard renderering.
            // The internal cache handles mutliple objects with the same id.
            String oid = context.registerObject( original );
            context.registerObject( oid, obj );
            }

        renderer.render( obj, type, context );
    }

    protected void renderNode( Object node, Graph graph, MetaClass graphClass, MetaKit metaKit,
                               XmlRenderContext context )
    {
        if( graph.degree( node ) > 0 )
            renderObject( node, true, graphClass.getClassRegistry(), metaKit, context );
        else
            renderObject( node, false, graphClass.getClassRegistry(), metaKit, context );
    }

    protected void renderEdge( Graph.Edge edge, Graph graph, MetaClass graphClass, MetaKit metaKit,
                               XmlRenderContext context )
    {
        XmlPrintWriter out = context.getWriter();

        out.pushTag( "edge" );
        out.printAttribute( "directed", String.valueOf( edge.isDirected() ) );

        Object uObj = edge.getUserObject();
        if( uObj != null )
            {
            if( uObj instanceof String )
                {
                out.printAttribute( "object", (String)uObj );
                }
            else
                {
                out.pushTag( "object" );
                renderObject( uObj, false, graphClass.getClassRegistry(), metaKit, context );
                out.popTag();
                }
            }

        String oid;
        out.pushTag( "head" );
        // We cheat because we know that the node should already be in the cache
        oid = context.getObjectId( edge.getHead() );
        out.printAttribute( MetaXmlReader.REFERENCE_DIRECTIVE, oid );
        out.popTag();

        out.pushTag( "tail" );
        oid = context.getObjectId( edge.getTail() );
        out.printAttribute( MetaXmlReader.REFERENCE_DIRECTIVE, oid );
        out.popTag();

        out.popTag();
    }

    /**
     *  Renders the specified Mutator's value to the specified writer.
     */
    public void render( Object value, PropertyType type, XmlRenderContext context )
    {
        if( !(value instanceof MetaObject) )
            throw new RuntimeException( "Value is not a meta-object:" + value );

        XmlPrintWriter out = context.getWriter();

        // Unwrap it since MetaObjects don't provide all of the access
        // that we need
        MetaObject mValue = (MetaObject)value;
        MetaClass  metaClass = mValue.getMetaClass();
        MetaKit metaKit = mValue.getMetaKit();
        value = metaKit.getInternalObject( mValue );

        if( !(value instanceof Graph) )
            throw new RuntimeException( "Value is not a graph:" + value );

        Collection properties = getFields();
        if( properties == null )
            properties = metaClass.getPropertyNames();

        Graph graph = (Graph)value;
        out.pushTag( graph.getClass().getName() );

        // Write out the attributes.
        writeAttributes( mValue, properties, context );

        out.pushTag( "nodes" );
        // Render the nodes
        for( Iterator i = graph.nodeIterator(); i.hasNext(); )
            {
            Object node = i.next();
            renderNode( node, graph, mValue.getMetaClass(), metaKit, context );
            }
        out.popTag();

        out.pushTag( "edges" );
        // Render the edges
        for( Iterator i = graph.edgeIterator(); i.hasNext(); )
            {
            Graph.Edge edge = (Graph.Edge)i.next();
            renderEdge( edge, graph, mValue.getMetaClass(), metaKit, context );
            }
        out.popTag();

        // Render any of the other properties on the object
        writeElements( mValue, properties, context );

        out.popTag();
    }
}

