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

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import org.progeeks.util.*;
import org.progeeks.util.log.*;

/**
 *  Manages the current window list by listening to the window
 *  context list in the global context.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class WindowManager
{
    static Log log = Log.getLog( WindowManager.class );

    private GlobalContext context;
    private ContextListener contextListener = new ContextListener();

    private Map windowMap = new HashMap();
    private WindowStateListener windowListener = new WindowStateListener();

    public WindowManager( GlobalContext context )
    {
        this.context = context;
        context.addPropertyChangeListener( GlobalContext.PROP_WINDOWS, contextListener );
    }

    protected void windowAdded( WindowContext context )
    {
        if( log.isInfoEnabled() )
            log.info( "Adding window for:" + context );

        MainWindow win = new MainWindow( context );
        windowMap.put( context, win );

        win.addWindowListener( windowListener );
        win.setVisible( true );
    }

    protected void windowRemoved( WindowContext context )
    {
        if( log.isInfoEnabled() )
            log.info( "Removing window for:" + context );

        MainWindow win = (MainWindow)windowMap.get( context );

        windowMap.remove( context );

        win.dispose();

        // If it was the last window, then we need to exit
        if( windowMap.size() == 0 )
            {
            log.info( "Exiting application." );
            // Just to be safe.
            System.exit( 0 );
            }
    }

    private class ContextListener implements PropertyChangeListener
    {
        public void propertyChange( PropertyChangeEvent event )
        {
            if( !(event instanceof ListPropertyChangeEvent) )
                return;
            ListPropertyChangeEvent lce = (ListPropertyChangeEvent)event;
            List oldList = (List)lce.getOldValue();
            List newList = (List)lce.getNewValue();

            switch( lce.getType() )
                {
                case ListPropertyChangeEvent.INSERT:
                    for( int i = lce.getFirstIndex(); i <= lce.getLastIndex(); i++ )
                        {
                        windowAdded( (WindowContext)newList.get( i ) );
                        }
                    break;
                case ListPropertyChangeEvent.UPDATE:
                    throw new UnsupportedOperationException( "Window contexts cannot be swapped." );
                    //for( int i = lce.getFirstIndex(); i <= lce.getLastIndex(); i++ )
                    //    {
                    //    }
                    //break;
                case ListPropertyChangeEvent.DELETE:
                    for( int i = lce.getFirstIndex(); i <= lce.getLastIndex(); i++ )
                        {
                        windowRemoved( (WindowContext)oldList.get( i ) );
                        }
                    break;
                }
        }
    }

    private class WindowStateListener extends WindowAdapter
    {
        public void windowClosing( WindowEvent event )
        {
            if( log.isInfoEnabled() )
                log.info( "Window closing:" + event.getSource() );

            MainWindow win = (MainWindow)event.getSource();
            WindowContext ctx = win.getWindowContext();

            // If it's the last window, confirm
            /*Boolean b = ctx.getRequestHandler().requestConfirmation( "Exit Application",
                                                                     "Really exit?", true );
            if( b != Boolean.TRUE )
                return;*/

            // Clear the window's context from the context list
            context.removeWindowContext( ctx );
        }

        public void windowClosed( WindowEvent event )
        {
            if( log.isInfoEnabled() )
                log.info( "Window closed:" + event.getSource() );

            // Remove it from our map
            MainWindow win = (MainWindow)event.getSource();
            windowMap.remove( win.getWindowContext() );
        }
    }
}
