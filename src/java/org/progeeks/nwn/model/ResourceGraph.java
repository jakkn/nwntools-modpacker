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

package org.progeeks.nwn.model;

import java.io.File;
import java.util.*;

import org.apache.commons.collections.Predicate;

import com.phoenixst.plexus.*;

import org.progeeks.graph.*;
import org.progeeks.util.*;

import org.progeeks.nwn.resource.*;

/**
 *  The main graph containing all of the project's resources
 *  and their relationships.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceGraph extends DefaultGraph
{
    /**
     *  Keep around a reference to the nodes by key so
     *  so that they are easier to lookup.  (Note: this
     *  could technically be moved outside of this as
     *  a sub-graph of composite graph and then the keys
     *  would could up as nodes... something to consider.)
     */
    private HashMap nodeCache = new HashMap();

    /**
     *  This is the root node to which all FileIndex paths
     *  will originate.
     */
    private ProjectRoot root;

    public ResourceGraph( ProjectRoot root )
    {
        setRoot( root );
    }

    public ResourceGraph()
    {
    }

    /**
     *  Sets the root if this graph.  This will throw an exception if
     *  the graph already has a root.
     */
    public void setRoot( ProjectRoot root )
    {
        if( this.root != null )
            throw new RuntimeException( "Root is already set." );
        this.root = root;
        addNode( root );
    }

    /**
     *  Returns the current root of this graph.
     */
    public ProjectRoot getRoot()
    {
        return( root );
    }

    protected void nodeAdded( Object o )
    {
        super.nodeAdded( o );
        if( o instanceof ResourceIndex )
            {
            nodeCache.put( ((ResourceIndex)o).getKey(), o );
            }
    }

    protected void nodeRemoved( Object o )
    {
        super.nodeRemoved( o );
        if( o instanceof ResourceIndex )
            {
            nodeCache.remove( ((ResourceIndex)o).getKey() );
            }
    }

    /**
     *  Returns the resource index for the specified key.
     */
    public ResourceIndex getResourceIndex( ResourceKey key )
    {
        return( (ResourceIndex)nodeCache.get( key ) );
    }

    /**
     *  Custom implementation of nodes() that can take
     *  advantage of the ResouceLocator filter to do a
     *  direct node lookup.
     */
    public Collection nodes( Predicate filter )
    {
        return( new NodeCollection( filter ) );
    }

    /**
     *  Custom implementation of getNode that can take
     *  advantage of the ResouceLocator filter to do a
     *  direct node lookup.
     */
    public Object getNode( Predicate filter )
    {
        if( filter instanceof ResourceLocator )
            {
            ResourceKey key = ((ResourceLocator)filter).getResourceKey();
            return( getResourceIndex( key ) );
            }
        return( super.getNode( filter ) );
    }

    /**
     *  Utility method for adding a directory to the graph.
     */
    public void addDirectory( FileIndex directory )
    {
        if( containsNode( directory ) )
            return;

        // We go ahead and use recursion because it's just so easy
        Object p = directory.getParent();
        if( p == null )
            p = root;
        else
            addDirectory( (FileIndex)p );

        addNode( directory );
        addEdge( ProjectGraph.EDGE_FILE, p, directory, true );
    }

    /**
     *  Adds a resource attached to the specified directory index.
     */
    public void addResource( FileIndex directory, ResourceIndex resource )
    {
        // Make sure the directory exists
        addDirectory( directory );

        // Make sure the resource has been added too
        addNode( resource );

        // Now attach the resource to the directory
        addEdge( ProjectGraph.EDGE_FILE, directory, resource, true );
    }

    private class NodeCollection extends AbstractFilteredNodeCollection
    {
        public NodeCollection( Predicate filter )
        {
            super( filter, ResourceGraph.this );
        }

        public Iterator iterator()
        {
            Predicate filter = getFilter();

            if( filter instanceof ResourceLocator )
                {
                Object o = getNode( filter );
                if( o == null )
                    return( Collections.EMPTY_LIST.iterator() );
                return( new SingletonIterator( o ) );
                }

            return( ResourceGraph.super.nodes(filter).iterator() );
        }
    }
}
