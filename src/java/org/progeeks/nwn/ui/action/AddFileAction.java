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
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.AbstractAction;

import org.progeeks.util.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.*;
import org.progeeks.nwn.model.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.nwn.ui.*;

/**
 *  Action used to add an existing file to the project.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class AddFileAction extends AbstractAction
{
    private WindowContext context;

    public AddFileAction( WindowContext context )
    {
        super( "Import Resource File..." );
        this.context = context;
        PropertyActionConnector.connect( WindowContext.PROP_PROJECT, this, context );
    }

    protected void importResourceFile( File resource ) throws IOException
    {
        Project project = context.getProject();
        ProjectGraph graph = project.getProjectGraph();
        ResourceKey key = ResourceUtils.getKeyForFileName( resource.getName() );

        // Get the project source root
        File root = project.getProjectFile().getParentFile();

        // See if the resource file is already in our hierarchy
        FileIndex destination = null;
        if( resource.getCanonicalPath().startsWith( root.getCanonicalPath() ) )
            {
            String s = resource.getCanonicalPath();
            destination = new FileIndex( s.substring( root.getCanonicalPath().length() + 1 ) );
            destination = destination.getParent();
            }

        // If it's already in the project hierarchy and is
        // an XML or NSS file, then just add it to the project.
        String name = resource.getName().toLowerCase();
        if( name.endsWith( ".xml" ) && destination != null )
            {
            Struct struct = GffUtils.readGffXml( resource );
            ResourceIndex ri = ResourceIndexFactory.createResourceIndex( key, struct, destination,
                                                                         project.getModuleFilesDirectory() );

            graph.addDirectory( destination );
            graph.addNode( ri );
            graph.addEdge( ProjectGraph.EDGE_FILE, destination, ri, true );
            }
        else if( name.endsWith( ".nss" ) && destination != null )
            {
            ResourceIndex ri = ResourceIndexFactory.createResourceIndex( key, destination,
                                                                         project.getModuleFilesDirectory() );

            graph.addDirectory( destination );
            graph.addNode( ri );
            graph.addEdge( ProjectGraph.EDGE_FILE, destination, ri, true );
            }
        else
            {
            // Need to do a real full import

            // FIXME - these should be part of the project itself
            List rules = new ArrayList();
            rules.add( new ResourceRegexRule( "ats_.*", "Tools/ATS Tradeskills" ) );
            rules.add( new ResourceRegexRule( "cohort.*", "Tools/Cohort System" ) );
            rules.add( new ResourceRegexRule( "dlg_.*", "Tools/Dialog Utils" ) );
            rules.add( new ResourceRegexRule( "dmw_.*", "Tools/Dungeon Master Wand" ) );
            rules.add( new ResourceRegexRule( "dmwand", "Tools/Dungeon Master Wand" ) );
            rules.add( new ResourceRegexRule( "ew_.*", "Tools/Emote Wand" ) );
            rules.add( new ResourceRegexRule( "emotewand", "Tools/Emote Wand" ) );
            rules.add( new ResourceRegexRule( "fxw_.*", "Tools/FX Wand" ) );
            rules.add( new ResourceRegexRule( "fxwand", "Tools/FX Wand" ) );
            rules.add( new ResourceRegexRule( "hc_.*", "Tools/Hard-core Rules" ) );
            rules.add( new ResourceRegexRule( "hcr.*", "Tools/Hard-core Rules" ) );
            rules.add( new ResourceRegexRule( "hchtf_.*", "Tools/Hard-core Rules" ) );
            rules.add( new ResourceRegexRule( "cb_.*", "Tools/Memetic Toolkit" ) );
            rules.add( new ResourceRegexRule( "h_.*", "Tools/Memetic Toolkit" ) );
            rules.add( new ResourceRegexRule( "lib_.*", "Tools/Memetic Toolkit" ) );
            rules.add( new ResourceRegexRule( "nw_.*", "Tools/Overrides" ) );
            rules.add( new ResourceRegexRule( "sei_.*", "Tools/Subraces" ) );
            rules.add( new StandardTypeFilterRule() );
            ModuleImporter importer = new ModuleImporter( context.getProject(), rules );

            FileInputStream in = new FileInputStream( resource );
            try
                {
                importer.importResource( key, in );
                }
            finally
                {
                in.close();
                }
            }
    }

    public void actionPerformed( ActionEvent event )
    {
        UserRequestHandler reqHandler = context.getRequestHandler();

        // Get the file from the user
        File resource = reqHandler.requestFile( "Import Resource",  "Resource File",
                                                "xml,2da,nss", true );
        if( resource == null )
            return;

        try
            {
            importResourceFile( resource );
            }
        catch( IOException e )
            {
            reqHandler.requestShowError( "Importing Resource", "Error importing resource:" + e.getMessage() );
            }
    }
}


