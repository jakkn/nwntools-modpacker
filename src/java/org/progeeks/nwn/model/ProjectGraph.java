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

import com.phoenixst.collections.TruePredicate;
import com.phoenixst.plexus.*;

import org.progeeks.graph.*;
import org.progeeks.util.*;

import org.progeeks.nwn.resource.*;

/**
 *  The main graph containing all of the project's resources
 *  and their relationships and how they relate to other things
 *  such as resouce dependencies, source/target differences, errors,
 *  etc..  Internally these different things are represented as
 *  separate graphs.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ProjectGraph extends CompositeGraph
{
    /**
     *  Shows that two nodes have a file-system based relationship.
     *  The direction of the edge shows containment.
     */
    public static final String EDGE_FILE = "File";

    /**
     *  Shows that two nodes have a linking relatonship such that
     *  the linked objects represent one larger object.
     */
    public static final String EDGE_AGGREGATE = "Agg";

    /**
     *  Edge indicating an attached error of some kind.
     */
    public static final String EDGE_ERROR = "Error";

    private ResourceGraph resources;
    private DefaultEnhancedGraph errors;

    public ProjectGraph( ProjectRoot root )
    {
        initializeSubgraphs();
        setRoot( root );
    }

    public ProjectGraph()
    {
        initializeSubgraphs();
    }

    protected void initializeSubgraphs()
    {
        // Setup the different subgraphs for which we
        // are a composite
        resources = new ResourceGraph();
        errors = new DefaultEnhancedGraph();

        addGraph( resources, new ResourceCoordinator() );
        addGraph( errors, new ErrorCoordinator() );

    }

    /**
     *  Returns the resource index for the specified key.
     */
    public ResourceIndex getResourceIndex( ResourceKey key )
    {
        return( resources.getResourceIndex( key ) );
    }

    /**
     *  Sets the root if this graph.  This will throw an exception if
     *  the graph already has a root.
     */
    public void setRoot( ProjectRoot root )
    {
        resources.setRoot( root );
    }

    /**
     *  Returns the current root of this graph.
     */
    public ProjectRoot getRoot()
    {
        return( resources.getRoot() );
    }

    /**
     *  Utility method for adding a directory to the graph.
     */
    public void addDirectory( FileIndex directory )
    {
        resources.addDirectory( directory );
    }

    /**
     *  Utility method for clearing all of the errors for a given
     *  node.
     */
    public void clearErrors( ResourceIndex ri )
    {
        TraverserFilter filter = new DefaultTraverserFilter( TruePredicate.INSTANCE, EDGE_ERROR,
                                                             GraphUtils.DIRECTED_OUT_MASK );
        for( Traverser t = traverser( ri, filter ); t.hasNext(); )
            {
            Object node = t.next();
            // See if we're the only attached node or not
            if( inDegree( node ) > 1 )
                {
                System.out.println( "Remove edge:" + t.getEdge() );
                // Just remove the edge
                t.removeEdge();
                }
            else
                {
                System.out.println( "Remove node:" + node );
                // Else remove the node too
                t.remove();
                }
            }

        // Then remove the resource index from the error graph
        errors.removeNode( ri );
    }

    /**
     *  Returns true if the specified resource has errors associated with it.
     */
    public boolean hasErrors( ResourceIndex ri )
    {
        // If it's in the error graph then it has errors...
        // easy enough check
        return( errors.containsNode( ri ) );
    }

    private class ErrorCoordinator extends GraphCoordinator
    {
        public boolean acceptsNode( Object node, Graph graph )
        {
            // Only accepts ErrorInfo as a direct add.
            if( node instanceof ErrorInfo )
                return( true );
            return( false );
        }

        public boolean acceptsConnectedNode( Object node, Graph graph )
        {
            // Accepts any nodes that can normally be added to the graph
            // plus ResourceIndex objects in case they are being copied
            // into the error graph.
            if( acceptsNode( node, graph ) )
                return( true );
            if( node instanceof ResourceIndex )
                return( true );
            return( false );
        }

        public boolean acceptsEdge( Object edge, Object tail, Object head, boolean directed, Graph graph )
        {
            // Only accept error edges
            if( EDGE_ERROR == edge || EDGE_ERROR.equals( edge ) )
                return( true );
            return( false );
        }

        public boolean hasPotentialNodes( NodeFilter filter )
        {
            // The error coordinator rejects the ResourceLocator filter
            // not because it can't handle it but because the other graphs
            // can potentially handle it better... and it will only ever
            // have duplicate resources.
            if( filter instanceof ResourceLocator )
                return( false );
            return( true );
        }

    }

    private class ResourceCoordinator extends GraphCoordinator
    {
        public boolean acceptsNode( Object node, Graph graph )
        {
            // Only accepts FileIndex, ResourceIndex, and ProjectRoot
            if( node instanceof FileIndex )
                return( true );
            if( node instanceof ResourceIndex )
                return( true );
            if( node instanceof ProjectRoot )
                return( true );
            return( false );
        }

        public boolean acceptsEdge( Object edge, Object tail, Object head, boolean directed, Graph graph )
        {
            // Accepts file and aggregate edges
            if( EDGE_FILE == edge || EDGE_FILE.equals( edge ) )
                return( true );
            if( EDGE_AGGREGATE == edge || EDGE_AGGREGATE.equals( edge ) )
                return( true );
            return( false );
        }
    }
}
