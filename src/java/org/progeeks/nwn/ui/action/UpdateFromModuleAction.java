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

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

import org.progeeks.cmd.*;
import org.progeeks.cmd.swing.*;
import org.progeeks.meta.*;
import org.progeeks.meta.beans.*;
import org.progeeks.meta.swing.*;
import org.progeeks.meta.swing.wizard.*;
import org.progeeks.util.*;
import org.progeeks.util.log.*;

import org.progeeks.nwn.*;
import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.nwn.io.xml.*;
import org.progeeks.nwn.model.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.nwn.ui.*;

/**
 *  Action to update the current project using the binaries
 *  in a module file.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class UpdateFromModuleAction extends AbstractAction
{
    static Log log = Log.getLog( UpdateFromModuleAction.class );

    private WindowContext context;

    public UpdateFromModuleAction( WindowContext context )
    {
        super( "Update from module..." );
        this.context = context;
        PropertyActionConnector.connect( WindowContext.PROP_PROJECT, this, context );
    }

    public void actionPerformed( ActionEvent event )
    {
        System.out.println( "Update from module." );
        UserRequestHandler reqHandler = context.getRequestHandler();
        File module = reqHandler.requestFile( "Update From Module",  "NWN Module File",
                                              "mod", true );
        if( module == null )
            return;

    }
}


