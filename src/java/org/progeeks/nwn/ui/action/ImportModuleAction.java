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
 *  Action to create a new project using a specified module
 *  as the basis.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ImportModuleAction extends AbstractAction
{
    static Log log = Log.getLog( ImportModuleAction.class );

    private byte[] transferBuff = new byte[65536];
    private static String[] defaultStructure = new String[] {
                                                    "Areas",
                                                    "Blueprints",
                                                    "Blueprints/Creatures",
                                                    "Blueprints/Doors",
                                                    "Blueprints/Encounters",
                                                    "Blueprints/Items",
                                                    "Blueprints/Merchants",
                                                    "Blueprints/Placeables",
                                                    "Blueprints/Sounds",
                                                    "Blueprints/Triggers",
                                                    "Blueprints/Waypoints",
                                                    "Conversations",
                                                    "Scripts"
                                                };

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
                if( rIn.getResourceType() == ResourceTypes.TYPE_IFO
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

    // FIXME - This should DEFINITELY be a utility method somewhere
    public long saveStream( File f, InputStream in ) throws IOException
    {
        FileOutputStream fOut = new FileOutputStream(f);
        BufferedOutputStream out = new BufferedOutputStream( fOut, 65536 );
        try
            {
            int count = 0;
            int total = 0;
            while( (count = in.read(transferBuff)) >= 0 )
                {
                out.write( transferBuff, 0, count );
                total += count;
                }

            return( total );
            }
        finally
            {
            out.close();
            }

    }

    protected void writeGffXml( File f, Struct struct, ResourceKey key ) throws IOException
    {
        String type = key.getTypeString() + " ";
        String version = "V3.2";

        FileWriter fOut = new FileWriter( f );
        BufferedWriter bOut = new BufferedWriter( fOut, 65536 );
        GffXmlWriter out = new GffXmlWriter( key.getName(), type, version, bOut );
        try
            {
            out.writeStruct( struct );
            }
        finally
            {
            out.close();
            }
    }

    protected FileIndex getPathForResource( List rules, ResourceKey key, FileIndex start )
    {
        FileIndex currentParent = start;

        for( Iterator j = rules.iterator(); j.hasNext(); )
            {
            MappingRule rule = (MappingRule)j.next();
            MappingResult result = rule.performMapping( currentParent, key );
            if( result == null )
                {
                // The rule swallowed it
                return( null );
                }

            currentParent = (FileIndex)result.getParent();

            if( result.shouldTerminate() )
                {
                // The rule says we're done
                break;
                }
            }

        return( currentParent );
    }


    protected void convertResources( File moduleFile, Project project, List rules,
                                     ProgressReporter pr ) throws IOException
    {
        ProjectGraph graph = project.getProjectGraph();

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
                pr.setMessage( "Importing: " + name );
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

                FileIndex destination = getPathForResource( rules, key, project.getSourceDirectory() );
                if( destination == null )
                    {
                    // Skipping it because it doesn't convert or copy to a source
                    // file
                    continue;
                    }

                // Construct the path if it doesn't exist
                File path = destination.getFile( project );
                if( !path.exists() )
                    path.mkdirs();

                ResourceIndex ri = null;

                // If it's a GFF file, the read it's struct
                if( key.isGffType() )
                    {
                    ResourceLoader loader = context.getResourceManager().getLoader( key.getType() );
                    Struct struct = (Struct)loader.loadResource( key, rIn );
                    ri = ResourceIndexFactory.createResourceIndex( key, struct, destination,
                                                                   project.getBuildDirectory() );

                    // Now write out the struct
                    File f = ri.getSource().getFile( project );
                    pr.setMessage( "Storing: " + f.getName() );
                    writeGffXml( f, struct, key );

                    // Go ahead and make the source and target times the same
                    File df = ri.getDestination().getFile( project );
                    df.setLastModified( f.lastModified() );

                    // Make sure the file indexes are up-to-date.
                    ri.getSource().updateLastModified( project );
                    ri.getDestination().updateLastModified( project );
                    }
                else
                    {
                    try
                        {
                        // Just copy the file
                        ri = ResourceIndexFactory.createResourceIndex( key, destination,
                                                                       project.getBuildDirectory() );

                        File f = ri.getSource().getFile( project );
                        pr.setMessage( "Storing: " + f.getName() );
                        saveStream( f, rIn );

                        // Make sure the file indexes are up-to-date.
                        ri.getSource().updateLastModified( project );
                        ri.getDestination().updateLastModified( project );
                        }
                    finally
                        {
                        rIn.close();
                        }
                    }

                // Now that we have a fully built out ResourceIndex,
                // add it to the graph
                graph.addDirectory( destination );
                graph.addNode( ri );
                graph.addEdge( ProjectGraph.EDGE_FILE, destination, ri, true );
                }
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

            project.setBuildDirectory( new FileIndex( "build" ) );
            project.setWorkDirectory( new FileIndex( "cache" ) );
            project.setSourceDirectory( new FileIndex( "source" ) );

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

            MetaWizardDialog dlg = new MetaWizardDialog( null, "Import Module", true,
                                                         context.getGlobalContext().getFactoryRegistry() );
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
            d4 = d4.replaceAll( "@build-directory@", project.getBuildDirectory().getFullPath() );
            d4 = d4.replaceAll( "@work-directory@", project.getWorkDirectory().getFullPath() );
            d4 = d4.replaceAll( "@source-directory@", project.getSourceDirectory().getFullPath() );

            page = new PageConfiguration( "Summary", d4, icon );
            page.setPageEvaluator( new EndEvaluator() );
            dlg.addPage( page );

            dlg.setLocationRelativeTo( null );
            dlg.show();

            System.out.println( "Canceled:" + dlg.isCanceled() );
            if( dlg.isCanceled() )
                return;

            ProjectGraph graph = project.getProjectGraph();

            // Always add the standard directories to make sure
            // they exist.  Eventually the user will define a custom
            // starting folder set, for now, this will do.
            for( int i = 0; i < defaultStructure.length; i++ )
                {
                FileIndex f = new FileIndex( project.getSourceDirectory(), defaultStructure[i] );
                graph.addDirectory( f );
                }

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

            context.getCommandProcessor().execute( new ImportCommand( project, module, rules ), false );
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

    private class ImportCommand extends AbstractViewCommand
    {
        private Project project;
        private File module;
        private List rules;

        public ImportCommand( Project project, File module, List rules )
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
                File buildDir = project.getBuildDirectory().getFile( project );

                // Make sure the build directory exists
                if( !buildDir.exists() )
                    buildDir.mkdirs();

                UserRequestHandler reqHandler = context.getRequestHandler();
                ProgressReporter pr;
                pr = reqHandler.requestProgressReporter( "Importing module:" + module.getName(),
                                                         "Extracting files...", 0, 100 );

                // Extract the module resources into the build directory.
                ModReader.extractModule( module, buildDir, pr );
                if( pr.isCanceled() )
                    throw new RuntimeException( "Operation Canceled" );
                pr.done();

                pr = reqHandler.requestProgressReporter( "Importing module:" + module.getName(),
                                                         "Converting files...", 0, 100 );

                // Go through all of the resources and add them to the source
                // directories.
                // This should probably be moved to a separate class.
                convertResources( module, project, rules, pr );
                if( pr.isCanceled() )
                    throw new RuntimeException( "Operation Canceled" );
                pr.done();

                // Set the current project to the newly imported project
                context.setProject( project );

                // It would be nice to do the above two operations in one step,
                // but it would require that we push the data to two different writers
                // rather than doing the standard read/write loop.
                //
                // Plus, by doing the processing separately, it makes it
                // easier to do things like update the source from the
                // binaries and such.

                pr = reqHandler.requestProgressReporter( "Importing module:" + module.getName(),
                                                         "Saving project:" + project.getName(), 0, 100 );
                // Try to save the project
                context.getGlobalContext().saveProject( project );
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


