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
import java.io.*;
import javax.swing.AbstractAction;

import org.progeeks.cmd.*;
import org.progeeks.cmd.swing.*;
import org.progeeks.util.log.*;
import org.progeeks.util.*;

import org.progeeks.nwn.model.*;
import org.progeeks.nwn.ui.*;

/**
 *  Action to save the current project.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class SaveProjectAction extends AbstractAction
{
    static Log log = Log.getLog( OpenProjectAction.class );

    private WindowContext context;

    public SaveProjectAction( WindowContext context )
    {
        super( "Save project" );
        this.context = context;
    }

    public void actionPerformed( ActionEvent event )
    {
        try
            {
            System.out.println( "Save Project" );
            context.saveProject();
            }
        catch( IOException e )
            {
            log.error( "Error saving project", e );
            context.getRequestHandler().requestShowMessage( "Error saving project:\n" + e.getMessage() );
            }
    }
}


