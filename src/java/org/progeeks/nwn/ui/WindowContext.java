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
import org.progeeks.meta.*;
import org.progeeks.meta.beans.*;
import org.progeeks.meta.swing.*;
import org.progeeks.util.*;
import org.progeeks.util.beans.*;
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
     *  The window's current list of consoles.
     */
    public static final String PROP_CONSOLES = "consoles";


    /**
     *  Constant associated with the file menu action list.
     */
    public static final String ACTIONS_FILE = "File";

    /**
     *  Constant associated with the build menu action list.
     */
    public static final String ACTIONS_BUILD = "Build";

    /**
     *  Constant associated with the action menu action list.
     */
    public static final String ACTIONS_ACTION = "Action";

    /**
     *  Constant associated with the application exit action.
     */
    public static final String ACTION_OPEN_PROJECT = "Open Project";
    public static final String ACTION_SAVE_PROJECT = "Save Project";
    public static final String ACTION_IMPORT_MODULE = "Import Module";
    public static final String ACTION_UPDATE_PROJECT = "Update Project";
    public static final String ACTION_IMPORT_RESOURCE = "Import Single Resource";
    public static final String ACTION_EXIT = "Exit";
    public static final String ACTION_BUILD_MODULE = "Build Module";
    public static final String ACTION_ADD_DIRECTORY = "Add Directory";
    public static final String ACTION_REMOVE_DIRECTORY = "Remove Directory";
    public static final String ACTION_REMOVE_RESOURCE = "Remove Resource";

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
    private ObservableList selectedObjects = new DefaultObservableList( PROP_SELECTED_OBJECTS, new ArrayList() );

    /**
     *  The current list of console contexts that this window should be
     *  displaying in its console tab.
     */
    private ObservableList consoles = new DefaultObservableList( PROP_CONSOLES, new ArrayList() );

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

        // Register a listener to dispatch window list changes to
        // reguler GlobalContext listeners.
        selectedObjects.addPropertyChangeListener( new BeanChangeDelegate(this) );
        consoles.addPropertyChangeListener( new BeanChangeDelegate(this) );
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
     *  Returns true if it is safe to terminate this window.  If
     *  fullCheck is false, then the user is only prompted if the
     *  window context's data has not been saved.  Otherwise the window
     *  context is closeable without question.
     */
    public boolean canTerminate( boolean fullCheck )
    {
        // Check for unsaved data

        // otherwise
        if( fullCheck )
            {
            Boolean b = getRequestHandler().requestConfirmation( "Close Window", "Close this window?",
                                                             false );
            return( b == Boolean.TRUE );
            }

        return( true );
    }

    /**
     *  Returns the observable list of selected objects.
     */
    public ObservableList getSelectedObjects()
    {
        return( selectedObjects );
    }

    /**
     *  Sets the currently selected object.
     */
    public void setSelectedObject( Object obj )
    {
        getSelectedObjects().clear();
        getSelectedObjects().add( obj );
    }

    /**
     *  Returns the current single-selected object or null if there
     *  are more than one objects selected or no objects selected.
     */
    public Object getSelectedObject()
    {
        int size = getSelectedObjects().size();
        if( size == 0 || size > 1 )
            return( null );
        return( getSelectedObjects().get( 0 ) );
    }

    /**
     *  Returns the current set of console context objects as an observable
     *  list.
     */
    public ObservableList getConsoles()
    {
        return( consoles );
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
     *  Wraps the specified object in a meta-object as necessary
     *  for its class.  This method is preferable to manual creation
     *  because it may cache instances, etc..
     */
    public MetaObject wrapObject( Object obj )
    {
        // The kit may actually be different depending on the object
        MetaKit kit = BeanUtils.getMetaKit();
        MetaClass metaClass = kit.getMetaClassForObject( obj );
        if( metaClass == null )
            return( null );

        return( kit.wrapObject( obj, metaClass ) );
    }

    public MetaObjectUI createMetaObjectEditor( MetaClass type )
    {
        return( getGlobalContext().getFactoryRegistry().createMetaObjectEditor( type ) );
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
     *  Saves the current project.
     */
    public void saveProject() throws java.io.IOException
    {
        getGlobalContext().saveProject( getProject() );
    }

    /**
     *  Stores the current project in the working directory.
     */
    public void cacheProject() throws java.io.IOException
    {
        getGlobalContext().cacheProject( getProject() );
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
        file.add( addAction( ACTION_SAVE_PROJECT, new SaveProjectAction( this ) ) );
        file.add( null );
        file.add( addAction( ACTION_IMPORT_MODULE, new ImportModuleAction( this ) ) );
        file.add( addAction( ACTION_IMPORT_RESOURCE, new AddFileAction( this ) ) );
        file.add( addAction( ACTION_UPDATE_PROJECT, new UpdateFromModuleAction( this ) ) );
        file.add( null );
        file.add( addAction( ACTION_EXIT, new ExitAction( this ) ) );


        ActionList action = addActionList( ACTIONS_ACTION, new ActionList( "Action" ) );
        rootActionList.add( action );

        action.add( addAction( ACTION_ADD_DIRECTORY, new AddDirectoryAction( this ) ) );
        action.add( addAction( ACTION_REMOVE_DIRECTORY, new RemoveDirectoryAction( this ) ) );
        action.add( null );
        action.add( addAction( ACTION_REMOVE_RESOURCE, new RemoveResourceAction( this ) ) );


        ActionList build = addActionList( ACTIONS_BUILD, new ActionList( "Build" ) );
        rootActionList.add( build );

        build.add( addAction( ACTION_BUILD_MODULE, new BuildAction( this ) ) );
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

}
