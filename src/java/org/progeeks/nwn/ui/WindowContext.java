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

import java.beans.*;
import java.util.*;
import javax.swing.Action;
import javax.swing.tree.TreeModel;

import org.progeeks.cmd.swing.SwingCommandProcessor;
import org.progeeks.util.*;
import org.progeeks.util.swing.*;

import org.progeeks.nwn.model.*;
import org.progeeks.nwn.resource.ResourceManager;
import org.progeeks.nwn.ui.action.*;

/**
 *  The model object for the main window.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class WindowContext extends DefaultViewContext
{
    /**
     *  The window title property.
     */
    public static final String PROP_TITLE = "title";

    /**
     *  The window's current project property.
     */
    public static final String PROP_PROJECT = "project";

    /**
     *  The window's current list of selected objects.
     */
    public static final String PROP_SELECTED_OBJECTS = "selectedObjects";

    /**
     *  Constant associated with the file menu action list.
     */
    public static final String ACTIONS_FILE = "File";

    /**
     *  Constant associated with the application exit action.
     */
    public static final String ACTION_OPEN_PROJECT = "Open Project";
    public static final String ACTION_IMPORT_MODULE = "Import Module";
    public static final String ACTION_EXIT = "Exit";

    /**
     *  The root-level menu action list.
     *  Note: eventually these will be clones of action lists
     *  in a GlobalContext.
     */
    private ActionList rootActionList = new ActionList();

    /**
     *  Maps actions constants to their ActionLists.  Useful for
     *  reconfiguring menus at runtime.
     */
    private Map actionLists = new HashMap();

    /**
     *  The UI-specific request handler for this context.
     */
    private UserRequestHandler requestHandler;

    /**
     *  The file-based tree model.
     */
    private FileTreeModel fileTreeModel;

    /**
     *  The current list of selected objects within this window.
     */
    private ObservableList selectedObjects = new ObservableList( new ArrayList() );

    /**
     *  Listens to the lists to adapt their change events to the window context
     *  listeners.
     */
    private ListListener listListener = new ListListener();

    /**
     *  Creates a new WindowContext as a child of the specified global
     *  context.
     */
    public WindowContext( GlobalContext appContext )
    {
        super( appContext );

        setupMetaClasses();
        setupActionLists();

        fileTreeModel = new FileTreeModel();

        selectedObjects.addPropertyChangeListener( listListener );
    }

    /**
     *  Sets the current user request handler.
     */
    public void setRequestHandler( UserRequestHandler reqHandler )
    {
        this.requestHandler = reqHandler;

        // Worth firing an event over?
    }

    /**
     *  Returns the current user request handler.
     */
    public UserRequestHandler getRequestHandler()
    {
        return( requestHandler );
    }

    /**
     *  Convenience method for accessing the global context.
     */
    public GlobalContext getGlobalContext()
    {
        return( (GlobalContext)getParentContext() );
    }

    /**
     *  Returns the observable list of selected objects.
     */
    public ObservableList getSelectedObjects()
    {
        return( selectedObjects );
    }

    /**
     *  Sets the title of this context.  This should appear in any window
     *  title bars or other appropriate areas.
     */
    public void setTitle( String title )
    {
        setStringProperty( PROP_TITLE, title );
    }

    /**
     *  Returns the title of this context.
     */
    public String getTitle()
    {
        return( getStringProperty( PROP_TITLE, null ) );
    }

    /**
     *  Returns the full title of this context, including application
     *  and version.
     */
    public String getFullTitle()
    {
        GlobalContext parent = getGlobalContext();

        if( getTitle() == null )
            return( parent.getApplication() + " " + parent.getVersion() );
        return( getTitle() + " - " + parent.getApplication() + " " + parent.getVersion() );
    }

    /**
     *  Returns the root action list.
     */
    public ActionList getRootActionList()
    {
        return( rootActionList );
    }

    /**
     *  Returns the project file-based tree model.
     */
    public FileTreeModel getFileTreeModel()
    {
        return( fileTreeModel );
    }

    public ResourceManager getResourceManager()
    {
        return( getGlobalContext().getResourceManager() );
    }

    public SwingCommandProcessor getCommandProcessor()
    {
        return( getGlobalContext().getCommandProcessor() );
    }

    /**
     *  Sets the currently loaded project.
     */
    public void setProject( Project project )
    {
        setObjectProperty( PROP_PROJECT, project );
        setTitle( project.getName() );
    }

    /**
     *  Returns the currently loaded project.
     */
    public Project getProject()
    {
        return( (Project)getObjectProperty( PROP_PROJECT ) );
    }

    /**
     *  Setup any meta-classes.
     */
    protected void setupMetaClasses()
    {
    }

    /**
     *  Setup the action list menu structure.
     */
    protected void setupActionLists()
    {
        // Eventually we'll want to both clone this from the
        // global context as well as read the configuration from
        // XML.

        ActionList file = addActionList( ACTIONS_FILE, new ActionList( "File" ) );
        rootActionList.add( file );

        file.add( addAction( ACTION_OPEN_PROJECT, new OpenProjectAction( this ) ) );
        file.add( addAction( ACTION_IMPORT_MODULE, new ImportModuleAction( this ) ) );
        file.add( addAction( ACTION_EXIT, new ExitAction( this ) ) );

    }

    protected Action addAction( String key, Action action )
    {
        // Add it to the action map  - FIXME

        return( action );
    }

    protected ActionList addActionList( String key, ActionList list )
    {
        actionLists.put( key, list );
        return( list );
    }

    private class ListListener implements PropertyChangeListener
    {
        public void propertyChange( PropertyChangeEvent event )
        {
            Object source = event.getSource();
            if( source == selectedObjects )
                {
                ListPropertyChangeEvent lce = new ListPropertyChangeEvent( WindowContext.this,
                                                                           PROP_SELECTED_OBJECTS,
                                                                           (ListPropertyChangeEvent)event );
                firePropertyChange( lce );
                }
        }
    }
}
