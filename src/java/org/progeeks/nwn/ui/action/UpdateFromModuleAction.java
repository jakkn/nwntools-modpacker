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

    protected void convertResources( File moduleFile, ModuleImporter importer,
                                     ProgressReporter pr ) throws IOException
    {
        // Even though we just copied the files to a directory,
        // we'll read the originals back out of the module.
        // This won't be reusable by the main system, but it's
        // easier because we don't have nice ResourceIndex objects
        // to help us yet.
        FileInputStream fIn = new FileInputStream( moduleFile );
        BufferedInputStream in = new BufferedInputStream( fIn, 65536 );
        try
            {
            ModReader reader = new ModReader( in );

            pr.setMaximum( reader.getResourceCount() );

            ModReader.ResourceInputStream rIn = null;
            int count = 0;
            while( (rIn = reader.nextResource()) != null )
                {
                String name = rIn.getResourceName();
                pr.setProgress( count++ );
                if( pr.isCanceled() )
                    {
                    log.info( "User aborted." );
                    return;
                    }

                if( name.length() == 0 )
                    {
                    pr.setMessage( "Skipping empty resource entry." );
                    continue;
                    }
                int type = rIn.getResourceType();

                ResourceKey key = new ResourceKey( name, type );

                // We already know about some resources
                ResourceIndex ri = (ResourceIndex)importer.getGraph().getResourceIndex( key );
                if( ri != null )
                    {
                    pr.setMessage( "Updating: " + name );
                    importer.updateSource( rIn, ri );
                    }
                else
                    {
                    // It's a new resource
                    pr.setMessage( "Importing: " + name );
                    importer.importResource( key, rIn );
                    }
                }
            }
        finally
            {
            in.close();
            }
    }

    public void actionPerformed( ActionEvent event )
    {
        System.out.println( "Update from module." );
        UserRequestHandler reqHandler = context.getRequestHandler();
        File module = reqHandler.requestFile( "Update From Module",  "NWN Module File",
                                              "mod", true );
        if( module == null )
            return;

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

        context.getCommandProcessor().execute( new UpdateCommand( context.getProject(), module, rules ), false );
    }

    private class UpdateCommand extends AbstractViewCommand
    {
        private Project project;
        private File module;
        private List rules;

        public UpdateCommand( Project project, File module, List rules )
        {
            super( false );

            this.project = project;
            this.module = module;
            this.rules = rules;
        }

        public Result execute( Environment env )
        {
            try
                {
                File unpackedDir = project.getModuleFilesDirectory().getFile( project );

                UserRequestHandler reqHandler = context.getRequestHandler();
                ProgressReporter pr;
                pr = reqHandler.requestProgressReporter( "Updating from module:" + module.getName(),
                                                         "Extracting files...", 0, 100 );

                // Extract the module resources into the build directory.
                ModReader.extractModule( module, unpackedDir, pr );
                if( pr.isCanceled() )
                    throw new RuntimeException( "Operation Canceled" );
                pr.done();

                pr = reqHandler.requestProgressReporter( "Updating from module:" + module.getName(),
                                                         "Converting files...", 0, 100 );

                // Go through all of the resources and add them to the source
                // directories.
                ModuleImporter importer = new ModuleImporter( project, rules );
                convertResources( module, importer, pr );
                if( pr.isCanceled() )
                    throw new RuntimeException( "Operation Canceled" );
                pr.done();

                pr = reqHandler.requestProgressReporter( "Updating from module:" + module.getName(),
                                                         "Saving project:" + project.getName(), 0, 100 );
                pr.setProgress( 50 );
                // Try to save the project
                context.saveProject();
                pr.done();
                }
            catch( IOException e )
                {
                context.getRequestHandler().requestShowMessage( "Error loading module file:" + module
                                                                + "\n" + e.getMessage() );
                }
            catch( RuntimeException e )
                {
                log.error( "Error importing module", e );
                context.getRequestHandler().requestShowMessage( "Error importing module:\n" + e.getMessage() );
                }
            return( null );
        }
    }
}


