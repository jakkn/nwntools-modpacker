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

    protected List getModuleResourceList( File moduleFile ) throws IOException
    {
        List results = new ArrayList();
        FileInputStream in = new FileInputStream( moduleFile );
        try
            {
            ModReader modReader = new ModReader( in );

            // Find the input stream for the module.ifo resource
            ModReader.ResourceInputStream rIn = null;
            while( (rIn = modReader.nextResource()) != null )
                {
                results.add( new ResourceKey( rIn.getResourceName(), rIn.getResourceType() ) );
                }

            return( results );
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
            dlg.addPage( page, mProject );

            // And the summary page
            File projectFile = new File( projectDirectory, name + ".xml" );
            project.setProjectFile( projectFile );
            String d4 = StringUtils.readStringResource( getClass(), "ImportModule-summary.html" );
            d4 = d4.replaceAll( "@project@", name );
            d4 = d4.replaceAll( "@module@", module.getName() );
            d4 = d4.replaceAll( "@module-file@", module.getPath().replace( '\\', '/' ) );
            d4 = d4.replaceAll( "@project-file@", projectFile.getPath().replace( '\\', '/' ) );
            d4 = d4.replaceAll( "@build-directory@", project.getBuildDirectory().getPath().replace( '\\', '/' ) );
            d4 = d4.replaceAll( "@work-directory@", project.getWorkDirectory().getPath().replace( '\\', '/' ) );
            d4 = d4.replaceAll( "@source-directory@", project.getSourceDirectory().getPath().replace( '\\', '/' ) );

            page = new PageConfiguration( "Summary", d4, icon );
            page.setPageEvaluator( new EndEvaluator() );
            dlg.addPage( page );

            dlg.setLocationRelativeTo( null );
            dlg.show();

            System.out.println( "Canceled:" + dlg.isCanceled() );
            if( dlg.isCanceled() )
                return;

            // Build out the resource graph as a test.
            File areaDir = new File( projectDirectory, "Areas" );
            File scriptDir = new File( projectDirectory, "Scripts" );
            File dialogDir = new File( projectDirectory, "Conversations" );
            File blueprintDir = new File( projectDirectory, "Blueprints" );
            File merchantDir = new File( blueprintDir, "Merchants" );
            File soundDir = new File( blueprintDir, "Sounds" );
            File encounterDir = new File( blueprintDir, "Encounters" );
            File placeableDir = new File( blueprintDir, "Placeables" );
            File itemDir = new File( blueprintDir, "Items" );
            File creatureDir = new File( blueprintDir, "Creatures" );
            File waypointDir = new File( blueprintDir, "Waypoints" );
            File triggerDir = new File( blueprintDir, "Triggers" );
            File doorDir = new File( blueprintDir, "Doors" );

            ProjectGraph graph = project.getProjectGraph();
            graph.addNode( areaDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, project, areaDir, true );
            graph.addNode( scriptDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, project, scriptDir, true );
            graph.addNode( dialogDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, project, dialogDir, true );
            graph.addNode( blueprintDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, project, blueprintDir, true );
            graph.addNode( merchantDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, merchantDir, true );
            graph.addNode( soundDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, soundDir, true );
            graph.addNode( encounterDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, encounterDir, true );
            graph.addNode( placeableDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, placeableDir, true );
            graph.addNode( itemDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, itemDir, true );
            graph.addNode( creatureDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, creatureDir, true );
            graph.addNode( waypointDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, waypointDir, true );
            graph.addNode( triggerDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, triggerDir, true );
            graph.addNode( doorDir );
            graph.addEdge( ProjectGraph.EDGE_FILE, blueprintDir, doorDir, true );

            // Now go through all of the resources and add them too
            List resources = getModuleResourceList( module );
            for( Iterator i = resources.iterator(); i.hasNext(); )
                {
                ResourceKey key = (ResourceKey)i.next();
                switch( key.getType() )
                    {
                    case ResourceKey.TYPE_ARE:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, areaDir, key, true );
                        break;

                    case ResourceKey.TYPE_NSS:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, scriptDir, key, true );
                        break;

                    case ResourceKey.TYPE_DLG:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, dialogDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTM:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, merchantDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTS:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, soundDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTP:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, placeableDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTE:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, encounterDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTI:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, itemDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTC:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, creatureDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTW:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, waypointDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTT:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, triggerDir, key, true );
                        break;

                    case ResourceKey.TYPE_UTD:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, doorDir, key, true );
                        break;

                    // Ignored types
                    case ResourceKey.TYPE_NCS: // compiled script files
                    case ResourceKey.TYPE_NDB: // debug files
                    case ResourceKey.TYPE_GIC: // area comments
                    case ResourceKey.TYPE_GIT: // area items
                    case ResourceKey.TYPE_IFO: // module info file
                    case ResourceKey.TYPE_ITP: // palette files
                    case ResourceKey.TYPE_FAC: // factions
                    case ResourceKey.TYPE_JRL: // journal
                        break;

                    case ResourceKey.TYPE_BTC:
                    case ResourceKey.TYPE_RES:
                    case ResourceKey.TYPE_BMP:
                    case ResourceKey.TYPE_MVE:
                    case ResourceKey.TYPE_TGA:
                    case ResourceKey.TYPE_WAV:
                    case ResourceKey.TYPE_PLT:
                    case ResourceKey.TYPE_INI:
                    case ResourceKey.TYPE_BMU:
                    case ResourceKey.TYPE_MPG:
                    case ResourceKey.TYPE_TXT:
                    case ResourceKey.TYPE_PLH:
                    case ResourceKey.TYPE_TEX:
                    case ResourceKey.TYPE_MDL:
                    case ResourceKey.TYPE_THG:
                    case ResourceKey.TYPE_FNT:
                    case ResourceKey.TYPE_LUA:
                    case ResourceKey.TYPE_SLT:
                    case ResourceKey.TYPE_MOD:
                    case ResourceKey.TYPE_SET:
                    case ResourceKey.TYPE_BIC:
                    case ResourceKey.TYPE_WOK:
                    case ResourceKey.TYPE_2DA:
                    case ResourceKey.TYPE_TLK:
                    case ResourceKey.TYPE_TXI:
                    case ResourceKey.TYPE_BTI:
                    case ResourceKey.TYPE_BTT:
                    case ResourceKey.TYPE_DDS:
                    case ResourceKey.TYPE_LTR:
                    case ResourceKey.TYPE_GFF:
                    case ResourceKey.TYPE_BTE:
                    case ResourceKey.TYPE_BTD:
                    case ResourceKey.TYPE_BTP:
                    case ResourceKey.TYPE_DTF:
                    case ResourceKey.TYPE_GUI:
                    case ResourceKey.TYPE_CSS:
                    case ResourceKey.TYPE_CCS:
                    case ResourceKey.TYPE_BTM:
                    case ResourceKey.TYPE_DWK:
                    case ResourceKey.TYPE_PWK:
                    case ResourceKey.TYPE_BTG:
                    case ResourceKey.TYPE_UTG:
                    case ResourceKey.TYPE_SAV:
                    case ResourceKey.TYPE_4PC:
                    case ResourceKey.TYPE_SSF:
                    case ResourceKey.TYPE_HAK:
                    case ResourceKey.TYPE_NWM:
                    case ResourceKey.TYPE_BIK:
                    case ResourceKey.TYPE_PTM:
                    case ResourceKey.TYPE_PTT:
                    case ResourceKey.TYPE_270C:
                    case ResourceKey.TYPE_ERF:
                    case ResourceKey.TYPE_BIF:
                    case ResourceKey.TYPE_KEY:
                    default:
                        graph.addNode( key );
                        graph.addEdge( ProjectGraph.EDGE_FILE, project, key, true );
                        break;
                    }
                }

            // Just the areas for now
            //List areas = (List)info.getList( "Mod_Area_list" );
            //for( Iterator i = areas.iterator(); i.hasNext(); )
            //    {
            //    Struct a = (Struct)i.next();
            //    String areaName = a.getString( "Area_Name" );
            //    ResourceKey key = new ResourceKey( areaName, ResourceUtils.RES_ARE );
            //    System.out.println( "   :" + key );
            //    graph.addNode( key );
            //    graph.addEdge( ProjectGraph.EDGE_FILE, areaDir, key, true );
            //    }

            //System.out.println( "Graph:" + graph );
            context.getFileTreeModel().setFileTreeView( new FileTreeView( graph ) );
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


