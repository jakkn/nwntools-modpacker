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

package org.progeeks.nwn.ui.action;

import java.awt.event.ActionEvent;
import java.beans.*;
import java.io.File;
import javax.swing.AbstractAction;

import org.progeeks.util.*;

import org.progeeks.nwn.model.*;
import org.progeeks.nwn.ui.*;

/**
 *  Action used to remove a sub-directory from a tree.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class RemoveDirectoryAction extends AbstractAction
                                   implements PropertyChangeListener
{
    private WindowContext context;
    private FileIndex     dir;

    public RemoveDirectoryAction( WindowContext context )
    {
        super( "Remove directory" );
        this.context = context;
        context.addPropertyChangeListener( WindowContext.PROP_SELECTED_OBJECTS, this );
        setParent( context.getSelectedObject() );
    }

    protected void setParent( Object obj )
    {
        dir = null;
        if( obj instanceof FileIndex )
            dir = (FileIndex)obj;

        setEnabled( dir != null );
    }

    public void propertyChange( PropertyChangeEvent event )
    {
        setParent( context.getSelectedObject() );
    }

    public void actionPerformed( ActionEvent event )
    {
        if( dir == null )
            return;

        UserRequestHandler reqHandler = context.getRequestHandler();

        FileTreeModel tree = context.getFileTreeModel();
        Project project = context.getProject();
        ProjectGraph graph = project.getProjectGraph();

        // Check to see if the directory has any children
        if( tree.getChildCount( dir ) > 0 )
            {
            reqHandler.requestShowMessage( "Cannot remove a directory that contains other objects." );
            // If we ever do this remember to do depth-first removal to catch everything
            // properly
            return;
            }

        // Double check that the user really wants to remove it
        Boolean b = reqHandler.requestConfirmation( "Remove Directory", "Really remove the directory?", false );
        if( b == null || !b.booleanValue() )
            return;

        // Remove the directory
        File f = dir.getFile( project );
        if( f.exists() && !f.delete() )
            {
            reqHandler.requestShowError( "Remove Directory", "Directory was not removed." );
            return;
            }

        // Remove the node
        graph.removeNode( dir );
    }
}


