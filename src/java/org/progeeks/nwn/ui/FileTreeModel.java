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

package org.progeeks.nwn.ui;

import java.util.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.phoenixst.plexus.*;

import org.progeeks.util.swing.*;

import org.progeeks.nwn.model.*;

/**
 *  Wraps a project graph's FileTreeView to adapt it to the
 *  Swing TreeModel interface.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class FileTreeModel extends AbstractTreeModel
{
    private FileTreeView tree;

    // Implements a simple child caching scheme based on the
    // fact the requests will usually be contigous.
    private Object lastParent = null;
    private List   lastChildren = null;

    public FileTreeModel()
    {
    }

    public void setFileTreeView( FileTreeView tree )
    {
        // Kill any old listeners

        this.tree = tree;

        // Add any new listeners

        // Let the tree model listeners no things have changed in
        // a big way.
        fireTreeStructureChanged( new TreeModelEvent( this, new TreePath( getRoot() ) ) );
    }

    public Object getRoot()
    {
        if( tree != null )
            return( tree.getRoot() );
        return( null );
    }

    public List getChildList( Object parent )
    {
        if( tree == null )
            return( Collections.EMPTY_LIST );

        if( lastParent == parent )
            return( lastChildren );

        lastChildren = new ArrayList();
        for( Iterator i = tree.childTraverser( parent ); i.hasNext(); )
            lastChildren.add( i.next() );
        lastParent = parent;

        return( lastChildren );
    }

    /**
     *  Returns true if the specified node is a leaf.
     */
    public boolean isLeaf( Object node )
    {
        if( tree == null )
            return( false );

        // Quick and dirty optimization since currently
        // we know what is being stored inside of us.
        if( node instanceof java.io.File )
            return( tree.isLeaf( node ) );
        else if( node instanceof Project )
            return( tree.isLeaf( node ) );

        // Only files contain other things right now
        return( true );
    }

    protected void clearCache()
    {
        lastChildren = null;
        lastParent = null;
    }

    protected void fireTreeNodesChanged( TreeModelEvent event )
    {
        // Clear the cache to be safe
        clearCache();

        super.fireTreeNodesChanged( event );
    }

    protected void fireTreeNodesInserted( TreeModelEvent event )
    {
        // Clear the cache to be safe
        clearCache();

        super.fireTreeNodesInserted( event );
    }

    protected void fireTreeNodesRemoved( TreeModelEvent event )
    {
        // Clear the cache to be safe
        clearCache();

        super.fireTreeNodesRemoved( event );
    }

    protected void fireTreeStructureChanged( TreeModelEvent event )
    {
        // Clear the cache to be safe
        clearCache();

        super.fireTreeStructureChanged( event );
    }
}