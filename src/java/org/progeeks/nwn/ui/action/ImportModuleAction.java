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
import javax.swing.*;

import org.progeeks.meta.*;
import org.progeeks.meta.beans.*;
import org.progeeks.meta.swing.*;
import org.progeeks.meta.swing.wizard.*;
import org.progeeks.util.*;
import org.progeeks.util.log.*;

import org.progeeks.nwn.*;
import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.nwn.model.*;
import org.progeeks.nwn.ui.*;

/**
 *  Action to create a new project using a specified module
 *  as the basis.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ImportModuleAction extends AbstractAction
{
    static Log log = Log.getLog( ImportModuleAction.class );

    private WindowContext context;

    public ImportModuleAction( WindowContext context )
    {
        super( "Import module..." );
        this.context = context;
    }

    protected Struct getModuleInfo( File moduleFile ) throws IOException
    {
        FileInputStream in = new FileInputStream( moduleFile );
        try
            {
            ModReader modReader = new ModReader( in );

            // Find the input stream for the module.ifo resource
            ModReader.ResourceInputStream rIn = null;
            while( (rIn = modReader.nextResource()) != null )
                {
                if( rIn.getResourceType() == ResourceUtils.RES_IFO
                    && rIn.getResourceName().equals( "module" ) )
                    {
                    break;
                    }
                }

            if( rIn == null )
                {
                throw new RuntimeException( "The specified module does not contain a valid module.ifo resource."
                                             + "\nModule file:" + moduleFile );
                }

            GffReader reader = new GffReader( rIn );
            return( (Struct)reader.readRootStruct() );
            }
        finally
            {
            in.close();
            }
    }

    public void actionPerformed( ActionEvent event )
    {
        System.out.println( "Import" );
        UserRequestHandler reqHandler = context.getRequestHandler();
        File module = reqHandler.requestFile( "Import Module",  "NWN Module File",
                                              "mod", true );
        if( module == null )
            return;

        File projectDirectory = reqHandler.requestDirectory( "Project Directory" );
        if( projectDirectory == null )
            return;

        System.out.println( "Import:" + module );
        try
            {
            Struct info = getModuleInfo( module );
            System.out.println( "Info:" + info );

            String name = info.getString( "Mod_Name" );
            String description = info.getString( "Mod_Description" );
            String minGameVer = info.getString( "Mod_MinGameVer" );

            System.out.println( "Module:" + name );
            System.out.println( "Description:" + description );
            System.out.println( "Minimum game version:" + minGameVer );

            Project project = new Project();
            project.setName( name );
            project.setTargetModuleName( module.getName() );
            project.setProjectDescription( description );

            project.setBuildDirectory( new File( projectDirectory, "build" ) );
            project.setWorkDirectory( new File( projectDirectory, "cache" ) );
            project.setSourceDirectory( new File( projectDirectory, "source" ) );

            // Need to create wizard pages with the following configuration
            // Page 1:
            //  Confirmation page that shows the project directory and module
            //  to import... as read-only fields.  So they can actually just
            //  be a part of the text description.
            //
            // Page 2:
            //  Project name, module name, project description.
            //
            // Page 3:
            //  Directory settings.
            //
            // Page 4:
            //  Source tree and filters.
            //
            // Any plug-in added pages (how's that work?)
            //  ...CVS, etc...
            //
            // The final page has the final confirmation with
            // a list of what will be done.

            // The data can all be bundled into a single ImportConfiguration object...
            // or can we combing new project with import module into one common config.
            // ProjectConfiguration.
            // Additional objects can be added by name... so maybe it's a map.
            // Or everything is just a map that gets put together into a CompositeMetaObject.

            // Page configurations should come from a list in the global context.
            // Plugins can then add their own page configurations to correspond to the
            // project configuration objects they added.

            MetaWizardDialog dlg = new MetaWizardDialog( null, "Import Module", true );
            dlg.setSize( 700, 450 );

            MetaKit kit = BeanUtils.getMetaKit();
            MetaObject mProject = kit.wrapObject( project, kit.getMetaClassForObject( project ) );

            String d1 = StringUtils.readStringResource( getClass(), "ImportModule.html" );
            d1 = d1.replaceAll( "@module@", module.getName() );
            d1 = d1.replaceAll( "@project@", name );
            d1 = d1.replaceAll( "@directory@", projectDirectory.getPath().replace( '\\', '/' ) );

            PageConfiguration page;
            ImageIcon icon = new ImageIcon( getClass().getResource( "ImportModule.png" ) );
            page = new PageConfiguration( null, d1, icon );

            dlg.addPage( page );


            List fields = new ArrayList();
            fields.add( "name" );
            fields.add( "targetModuleName" );
            fields.add( "projectDescription" );
            //page = new MetaWizardDialog.PageConfiguration( "Project Information", (String)null, fields );
            String d2 = StringUtils.readStringResource( getClass(), "ImportModule-info.html" );
            page = new PageConfiguration( "Project Information", d2, fields );

            dlg.addPage( page, mProject );

            // Page three configuration
            fields = new ArrayList();
            fields.add( "buildDirectory" );
            fields.add( "workDirectory" );
            fields.add( "sourceDirectory" );
            String d3 = StringUtils.readStringResource( getClass(), "ImportModule-dirs.html" );
            d3 = d3.replaceAll( "@directory@", projectDirectory.getPath().replace( '\\', '/' ) );
            page = new PageConfiguration( "Directories", d3, fields );
            page.setPageEvaluator( new EndEvaluator() );
            dlg.addPage( page, mProject );

            dlg.setLocationRelativeTo( null );
            dlg.show();

            System.out.println( "Canceled:" + dlg.isCanceled() );
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

    }

    private class EndEvaluator extends DefaultPageEvaluator
    {
        public int pageChanged( int buttonFlags, MetaObject object, PageConfiguration page )
        {
            buttonFlags |= MetaWizardDialog.BUTTON_FINISH;
            return( buttonFlags );
        }
    }
}


