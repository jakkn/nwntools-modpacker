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

import java.awt.BorderLayout;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import org.progeeks.util.*;

import org.progeeks.nwn.model.*;

/**
 *  The main viewer panel that will show the views of
 *  areas, scripts, etc..
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ObjectPanel extends JScrollPane
{
    private WindowContext context;
    private JLabel temporary;

    public ObjectPanel( WindowContext context )
    {
        this.context = context;

        context.addPropertyChangeListener( new ContextListener() );
        context.addPropertyChangeListener( WindowContext.PROP_SELECTED_OBJECTS, new SelectionListener() );

        temporary = new JLabel();
        setViewportView( temporary );
    }

    private class SelectionListener extends ListPropertyChangeListener
    {
        /**
         *  Called when an item has been inserted.
         */
        protected void itemInserted( Object source, int index, List oldList, List newList )
        {
            Object o = newList.get( index );
            System.out.println( "Added selection:" + o );
            if( o instanceof ResourceIndex )
                {
                // This should really be done closer to the component...
                // probably in the cache since the cache would have to be
                // refreshed.
                ResourceIndex ri = (ResourceIndex)o;
                boolean stale = ri.makeSourceCurrent( context.getProject() );
                System.out.println( "Was stale:" + stale );
                }
        }

        /**
         *  Called when an item has been replaced.
         */
        protected void itemUpdated( Object source, int index, List oldList, List newList )
        {
            Object o = newList.get( index );
            System.out.println( "Updated selection:" + o );
        }

        /**
         *  Called when an item has been deleted.
         */
        protected void itemDeleted( Object source, int index, List oldList, List newList )
        {
            Object o = oldList.get( index );
            System.out.println( "Removed selection:" + o );
        }
    }

    private class ContextListener implements PropertyChangeListener
    {
        public void propertyChange( PropertyChangeEvent event )
        {
            String name = event.getPropertyName();
            /*if( WindowContext.PROP_SELECTED_OBJECTS.equals( name ) )
                {
                temporary.setText( String.valueOf( context.getSelectedObjects() ) );
                }*/
        }
    }
}
