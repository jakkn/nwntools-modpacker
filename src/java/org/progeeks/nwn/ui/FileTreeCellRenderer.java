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

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.progeeks.util.swing.*;

import org.progeeks.nwn.model.*;
import org.progeeks.nwn.resource.*;

/**
 *  Renders the values in the FileTree.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer
{
    private static final int ICON_AREA = 0;
    private static final int ICON_SCRIPT = 1;
    private static final int ICON_CREATURE = 2;
    private static final int ICON_OBJECT = 3;
    private static final int ICON_DOOR = 4;
    private static final int ICON_WAYPOINT = 5;
    private static final int ICON_TRIGGER = 6;
    private static final int ICON_CONVERSATION = 7;
    private static final int ICON_PLACEABLE = 8;
    private static final int ICON_ENCOUNTER = 9;
    private static final int ICON_CHANGED = 10;
    private static final int ICON_ERROR = 11;
    private static final int ICON_COUNT = 12;

    private static ImageIcon[] icons;
    static
    {
        Class root = FileTreeCellRenderer.class;
        icons = new ImageIcon[ICON_COUNT];
        icons[ICON_AREA] = new ImageIcon( root.getResource( "icons/earth15.gif" ) );
        icons[ICON_SCRIPT] = new ImageIcon( root.getResource( "icons/Document.gif" ) );
        icons[ICON_CREATURE] = new ImageIcon( root.getResource( "icons/User.gif" ) );
        icons[ICON_OBJECT] = new ImageIcon( root.getResource( "icons/Object.gif" ) );
        icons[ICON_DOOR] = new ImageIcon( root.getResource( "icons/Door.gif" ) );
        icons[ICON_WAYPOINT] = new ImageIcon( root.getResource( "icons/Pin.gif" ) );
        icons[ICON_TRIGGER] = new ImageIcon( root.getResource( "icons/Eyeball.gif" ) );
        icons[ICON_CONVERSATION] = new ImageIcon( root.getResource( "icons/Talk.gif" ) );
        icons[ICON_PLACEABLE] = new ImageIcon( root.getResource( "icons/Box.gif" ) );
        icons[ICON_ENCOUNTER] = new ImageIcon( root.getResource( "icons/Users.gif" ) );
        icons[ICON_CHANGED] = new ImageIcon( root.getResource( "icons/Save.gif" ) );
        icons[ICON_ERROR] = new ImageIcon( root.getResource( "icons/Exclamation.gif" ) );
    }

    private CompositeIcon combined;

    public FileTreeCellRenderer()
    {
        combined = new CompositeIcon();
    }

    protected Icon getIcon( int type )
    {
        switch( type )
            {
            case ResourceTypes.TYPE_NSS:
                return( icons[ICON_SCRIPT] );
            case ResourceTypes.TYPE_ARE:
                return( icons[ICON_AREA] );
            case ResourceTypes.TYPE_UTC:
                return( icons[ICON_CREATURE] );
            case ResourceTypes.TYPE_UTI:
                return( icons[ICON_OBJECT] );
            case ResourceTypes.TYPE_UTD:
                return( icons[ICON_DOOR] );
            case ResourceTypes.TYPE_UTW:
                return( icons[ICON_WAYPOINT] );
            case ResourceTypes.TYPE_UTT:
                return( icons[ICON_TRIGGER] );
            case ResourceTypes.TYPE_DLG:
                return( icons[ICON_CONVERSATION] );
            case ResourceTypes.TYPE_UTP:
                return( icons[ICON_PLACEABLE] );
            case ResourceTypes.TYPE_UTE:
                return( icons[ICON_ENCOUNTER] );
            }
        return( null );
    }

    public java.awt.Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel,
                                                            boolean expanded, boolean leaf, int row,
                                                            boolean hasFocus )
    {
        combined.setIconA( null );
        combined.setIconB( null );

        ResourceIndex ri = null;

        if( value instanceof FileIndex )
            {
            value = ((FileIndex)value).getName();
            }
        else if( value instanceof ResourceIndex )
            {
            ri = (ResourceIndex)value;
            Icon icon = getIcon( ri.getKey().getType() );
            if( icon != null )
                {
                // Then go ahead and use prettier text
                value = ri.getName();
                combined.setIconA( icon );
                }
            else
                {
                // Do the combined name so that the type is still known
                value = ri.getKey().getName() + " (" + ri.getKey().getDescription() + ")";
                }
            }

        java.awt.Component retVal = super.getTreeCellRendererComponent( tree, value, sel, expanded,
                                                                        leaf, row, hasFocus );
//System.out.println( "retVal:" + retVal );
        if( ri != null && ri.isSourceNewer() )
            {
            setIcon( combined );
            if( ri.isSourceNewer() )
                setForeground( java.awt.Color.red );

            // If has errors
                //combined.setIconB( icons[ICON_ERROR] );
                //combined.setOffset( combined.getIconA().getIconWidth(), 0 );
            }

        return( retVal );
    }
}

