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
import java.util.*;
import javax.swing.AbstractAction;

import org.progeeks.util.*;

import org.progeeks.nwn.model.*;
import org.progeeks.nwn.ui.*;

/**
 *  Action used to remove a resource from the project.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class RemoveResourceAction extends AbstractAction
                                  implements PropertyChangeListener
{
    private WindowContext context;
    private List resources = new ArrayList();

    public RemoveResourceAction( WindowContext context )
    {
        super( "Remove Resource" );
        this.context = context;
        context.addPropertyChangeListener( WindowContext.PROP_SELECTED_OBJECTS, this );
        setResources( context.getSelectedObjects() );
    }

    protected void setResources( List resources )
    {
        this.resources.clear();

        for( Iterator i = resources.iterator(); i.hasNext(); )
            {
            Object obj = i.next();
            if( obj instanceof ResourceIndex )
                this.resources.add( obj );
            }

        setEnabled( resources.size() > 0 );
    }

    public void propertyChange( PropertyChangeEvent event )
    {
        setResources( context.getSelectedObjects() );
    }

    protected void removeResource( ResourceIndex ri, UserRequestHandler reqHandler )
    {
        Project project = context.getProject();
        ProjectGraph graph = project.getProjectGraph();

        // Remove the File
        File f = ri.getSource().getFile( project );
        if( f.exists() && !f.delete() )
            {
            reqHandler.requestShowError( "Remove Resource", "Resource source was not removed." );
            return;
            }
        f = ri.getDestination().getFile( project );
        if( f.exists() && !f.delete() )
            {
            reqHandler.requestShowError( "Remove Resource", "Resource target was not removed." );
            return;
            }

        // Remove the node
        graph.removeNode( ri );
    }

    public void actionPerformed( ActionEvent event )
    {
        if( resources.size() == 0 )
            return;

        StringBuffer sb = new StringBuffer( "Really remove resource" );
        if( resources.size() == 1 )
            {
            ResourceIndex ri = (ResourceIndex)resources.get(0);
            sb.append( ":" + ri.getName() + "?" );
            }
        else
            {
            sb.append( "s:" );
            int count = 0;
            for( Iterator i = resources.iterator(); i.hasNext(); )
                {
                if( count > 0 )
                    sb.append( ", " );
                if( (count % 4) == 0 )
                    sb.append( "\n" );
                ResourceIndex ri = (ResourceIndex)i.next();
                sb.append( "\"" + ri.getName() + "\"" );
                count++;
                }
            }
        UserRequestHandler reqHandler = context.getRequestHandler();
        Boolean result = reqHandler.requestConfirmation( "Remove Resource", sb.toString(),
                                                         false );
        if( result == null || !result.booleanValue() )
            return;

        ArrayList list = new ArrayList( resources );
        for( Iterator i = list.iterator(); i.hasNext(); )
            {
            ResourceIndex ri = (ResourceIndex)i.next();
            removeResource( ri, reqHandler );
            }
    }
}


