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
import javax.swing.Action;

import org.progeeks.util.*;
import org.progeeks.util.swing.*;

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
     *  Constant associated with the file menu action list.
     */
    public static final String ACTIONS_FILE = "File";

    /**
     *  Constant associated with the application exit action.
     */
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
     *  Creates a new WindowContext as a child of the specified global
     *  context.
     */
    public WindowContext( GlobalContext appContext )
    {
        super( appContext );

        setupMetaClasses();
        setupActionLists();
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
     *  Returns the root action list.
     */
    public ActionList getRootActionList()
    {
        return( rootActionList );
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

}
