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

import java.beans.*;
import javax.swing.Action;

import org.progeeks.util.*;

import org.progeeks.nwn.ui.*;

/**
 *  Enables or disables an action based on the existence
 *  of a property value in the window context.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class PropertyActionConnector implements PropertyChangeListener
{
    private Inspector ins;
    private String property;
    private Action action;

    protected PropertyActionConnector( String property, Action a, WindowContext context )
    {
        this.property = property;
        this.action = a;
        this.ins = new Inspector( context );
        context.addPropertyChangeListener( property, this );
        updateState();
    }

    public static void connect( String property, Action a, WindowContext context )
    {
        new PropertyActionConnector( property, a, context );
    }

    protected void updateState()
    {
        Object value = ins.get( property );
        action.setEnabled( value != null );
    }

    public void propertyChange( PropertyChangeEvent event )
    {
        if( property.equals( event.getPropertyName() ) )
            {
            updateState();
            }
    }
}
