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
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class AddDirectoryAction extends AbstractAction
                                implements PropertyChangeListener
{
    private WindowContext context;
    private FileIndex     parent;

    public AddDirectoryAction( WindowContext context )
    {
        super( "Add directory..." );
        this.context = context;
        context.addPropertyChangeListener( WindowContext.PROP_SELECTED_OBJECTS, this );
        setParent( context.getSelectedObject() );
    }

    protected void setParent( Object obj )
    {
        parent = null;
        if( obj instanceof FileIndex )
            parent = (FileIndex)obj;

        setEnabled( parent != null );
    }

    public void propertyChange( PropertyChangeEvent event )
    {
        setParent( context.getSelectedObject() );
    }

    public void actionPerformed( ActionEvent event )
    {
        if( parent == null )
            return;

        UserRequestHandler reqHandler = context.getRequestHandler();
        String name = reqHandler.requestString( "Directory Name",
                                                "Enter sub-directory name for [" + parent.getName() + "]:",
                                                "" );
        if( name == null )
            return; // user canceled
        name = name.trim();
        if( name.length() == 0 )
            return; // not usable

        Project project = context.getProject();

        FileIndex child = new FileIndex( parent, name );
        File f = child.getFile( project );
        if( !f.exists() && !f.mkdirs() )
            {
            reqHandler.requestShowError( "Directory Error", "Directory was not created." );
            return;
            }

        ProjectGraph graph = project.getProjectGraph();
        graph.addDirectory( child );
    }
}


