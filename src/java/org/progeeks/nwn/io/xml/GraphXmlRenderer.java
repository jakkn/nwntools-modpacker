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
public class GraphXmlRenderer implements XmlPropertyRenderer
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

    protected void renderNode( Object node, MetaClass graphClass, MetaKit metaKit, XmlRenderContext context )
    {
System.out.println( "writing node:" + node );
        // Need to do a little guessing about the node type.
        MetaClass mClass = metaKit.getMetaClassForObject( node, graphClass.getClassRegistry() );
        PropertyType type;
        if( mClass != null )
            {
            node = metaKit.wrapObject( node, mClass );
            type = new MetaClassPropertyType( mClass );
            }
        else
            {
            type = new ClassPropertyType( node.getClass() );
            }
System.out.println( "      type:" + type );

        XmlPropertyRenderer renderer = context.getRenderer( type, node );
        if( renderer.isAttribute() )
            return;

        /*out.pushTag( name );

        if( graph.degree( node ) > 0 )
            {
            // Then something else refers to it
            out.printAttribute( MetaXmlReader.OID_DIRECTIVE, context.registerObject( node ) );
            }*/

        renderer.render( node, type, context );

        //out.popTag();
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
        MetaKit metaKit = mValue.getMetaKit();
        value = metaKit.getInternalObject( mValue );

        if( !(value instanceof Graph) )
            throw new RuntimeException( "Value is not a graph:" + value );

        Graph graph = (Graph)value;
        out.pushTag( graph.getClass().getName() );

        out.pushTag( "nodes" );
        // Render the nodes
        for( Iterator i = graph.nodeIterator(); i.hasNext(); )
            {
            Object node = i.next();
            renderNode( node, mValue.getMetaClass(), metaKit, context );
            }
        out.popTag();

        out.pushTag( "edges" );
        // Render the edges
        for( Iterator i = graph.edgeIterator(); i.hasNext(); )
            {
            Object edge = i.next();
            }
        out.popTag();

        out.popTag();
    }
}

