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
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.progeeks.util.log.*;
import org.progeeks.util.swing.*;

import org.progeeks.nwn.model.*;

/**
 *  The main window for the IDE.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class MainWindow extends JFrame
{
    static Log log = Log.getLog( MainWindow.class );

    private WindowContext context;
    private JSplitPane main;

    public MainWindow( WindowContext context )
    {
        this.context = context;
        context.setRequestHandler( new SwingRequestHandler( this ) );
        context.addPropertyChangeListener( new ContextListener() );

        setTitle( context.getFullTitle() );

        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );

        // Get the root menu action list and create the menu bar.
        ActionList root = context.getRootActionList();
        JMenuBar bar = ActionUtils.createActionMenuBar( root );
        setJMenuBar( bar );

        main = new JSplitPane();
        getContentPane().add( main, "Center" );

        JTree tree = new JTree( context.getFileTreeModel() );

        // This could probably be done with a Format-based
        // thing added to Meta-JB.  We'll hard-code for now.
        tree.setCellRenderer( new DefaultTreeCellRenderer()
                {
                    public java.awt.Component getTreeCellRendererComponent( JTree tree,
                                                                            Object value,
                                                                            boolean sel,
                                                                            boolean expanded,
                                                                            boolean leaf,
                                                                            int row,
                                                                            boolean hasFocus )
                    {
                        if( value instanceof FileIndex )
                            value = ((FileIndex)value).getName();

                        return( super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf,
                                                                    row, hasFocus ) );
                    }
                } );

        main.setLeftComponent( new JScrollPane( tree ) );

        setSize( 1024, 768 );
    }

    public WindowContext getWindowContext()
    {
        return( context );
    }

    public static void main( String[] args )
    {
        // Initialize the logging framework
        Log.initialize();

        // Set to use the system look and feel
        try
            {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            }
        catch( Exception e )
            {
            // Use the default
            log.warn( "Error setting system look and feel.", e );
            }

        try
            {
            // Create a window manager and a global context
            GlobalContext appContext = new GlobalContext();
            WindowManager winMgr = new WindowManager( appContext );

            // Create a context for the first main window
            WindowContext winContext = new WindowContext( appContext );

            // And it should just be a matter of adding it to the global
            // context's window context list.
            appContext.getWindowContexts().add( winContext );
            }
        catch( Exception e )
            {
            log.error( "Error starting appliction", e );
            }
    }

    private class ContextListener implements PropertyChangeListener
    {
        public void propertyChange( PropertyChangeEvent event )
        {
            String name = event.getPropertyName();
            if( WindowContext.PROP_TITLE.equals( name ) )
                {
                setTitle( context.getFullTitle() );
                }
        }
    }
}
