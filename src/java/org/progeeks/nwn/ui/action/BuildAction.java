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
import java.util.*;
import javax.swing.AbstractAction;

import org.progeeks.cmd.*;
import org.progeeks.cmd.swing.*;
import org.progeeks.util.log.*;
import org.progeeks.util.*;

import org.progeeks.nwn.*;
import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.*;
import org.progeeks.nwn.io.nss.*;
import org.progeeks.nwn.model.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.nwn.ui.*;

/**
 *  Attempts to build the current module by building any stale
 *  resources and then combining them into the module file.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class BuildAction extends AbstractAction
{
    static Log log = Log.getLog( BuildAction.class );

    private WindowContext context;

    public BuildAction( WindowContext context )
    {
        super( "Build Module" );
        this.context = context;
    }

    public void actionPerformed( ActionEvent event )
    {
        System.out.println( "Build project." );
        context.getCommandProcessor().execute( new BuildCommand( context.getProject() ), false );
    }

    private class BuildCommand extends AbstractViewCommand
    {
        private Project project;
        private ProjectGraph graph;
        private Set staleResources = new HashSet();
        private Set checkedScripts = new HashSet();
        private int scriptCount = 0;
        private int xmlCount = 0;

        public BuildCommand( Project project )
        {
            super( false );

            this.project = project;
        }

        private void addStaleResource( ResourceIndex ri )
        {
            staleResources.add( ri );

            ResourceKey key = ri.getKey();
            if( key.isGffType() )
                xmlCount++;
            else if( key.getType() == ResourceTypes.TYPE_NSS )
                scriptCount++;
        }

        private boolean checkScriptDependencies( ResourceIndex ri ) throws IOException
        {
            File f = ri.getSource().getFile( project );
            checkedScripts.add( ri );
            DependencyReader reader = new DependencyReader( new FileReader( f ) );
            try
                {
                List deps = reader.readDependencies();
                for( Iterator i = deps.iterator(); i.hasNext(); )
                    {
                    ResourceKey key = (ResourceKey)i.next();
                    ResourceIndex script = graph.getResourceIndex( key );
                    if( script == null )
                        {
                        // For now, just assume its a Bioware script and ignore it
                        // FIXME - should really check with the resource manager.
                        continue;
                        //throw new RuntimeException( "Resource not found for key:" + key );
                        }

                    if( checkStaleness( script ) )
                        return( true );
                    }
                return( false );
                }
            finally
                {
                reader.close();
                }
        }

        private boolean checkStaleness( ResourceIndex ri )
        {
            if( staleResources.contains( ri ) )
                return( true );

            ri.makeSourceCurrent( project );
            ri.makeDestinationCurrent( project );
            if( ri.isSourceNewer() )
                {
                addStaleResource( ri );
                return( true );
                }

            if( ri.getKey().getType() == ResourceTypes.TYPE_NSS )
                {
                if( checkedScripts.contains( ri ) )
                    return( false );

                try
                    {
                    // Check for dependencies
                    if( checkScriptDependencies( ri ) )
                        {
                        addStaleResource( ri );
                        return( true );
                        }
                    }
                catch( IOException e )
                    {
                    log.error( "Error checking script dependencies:" + ri, e );
                    }
                }

            return( false );
        }

        protected void compileScripts( ProgressReporter pr, File compileDir ) throws IOException
        {
            if( scriptCount == 0 )
                return;

            ScriptCompiler scriptCompiler = context.getGlobalContext().getScriptCompiler();

            System.out.println( "Compiling..." );
            int index = 0;

            boolean hasCompiler = scriptCompiler.hasCompiler();

            // Compile the scripts first
            for( Iterator i = staleResources.iterator(); i.hasNext(); index++ )
                {
                pr.setProgress( index );
                ResourceIndex ri = (ResourceIndex)i.next();
                if( ri.getKey().getType() != ResourceTypes.TYPE_NSS )
                    continue;

                if( !hasCompiler )
                    {
                    ri.makeDirty( project );
                    continue;
                    }

                pr.setMessage( "Compiling:" + ri.getKey().getFileName() );

                // Copy the new script into the build directory
                File src = ri.getSource().getFile( project );
                File dest = ri.getDestination().getFile( project );

                //log.debug( "Copying:" + src + " to:" + dest );
                //FileUtils.copyFile( src, dest );

                // Errors have to be handled a different way, but at this
                // point the src and destination _are_ up-to-date since the
                // destination is just the script copy.  Errors need to be
                // added to the graph or resource or something.
                ri.makeAllUpToDate( project );

                // Compile the script
                scriptCompiler.compileScript( ri.getKey().getFileName(), compileDir );

                i.remove();
                }

            try
                {
                // Wait for the last compiles to finish.
                scriptCompiler.waitForAll();
                }
            catch( InterruptedException e )
                {
                log.error( "Error waiting for compiles to finish", e );
                }

            if( !hasCompiler )
                {
                log.error( "No compiler configured." );
                }
        }

        protected void convertGffResources( ProgressReporter pr ) throws IOException
        {
            int index = 0;
            for( Iterator i = staleResources.iterator(); i.hasNext(); index++ )
                {
                pr.setProgress( index );
                ResourceIndex ri = (ResourceIndex)i.next();
                if( !ri.getKey().isGffType() )
                    continue;

                File src = ri.getSource().getFile( project );
                File dest = ri.getDestination().getFile( project );
                log.debug( "Converting:" + src + " to:" + dest );
                pr.setMessage( "Converting:" + ri.getKey().getFileName() );

                // Easy, load the XML.
                Struct gff = GffUtils.readGffXml( src );

                // Save the GFF
                GffUtils.writeGff( ri.getKey(), gff, dest );

                i.remove();
                }
        }

        protected void copyResources( ProgressReporter pr ) throws IOException
        {
            int index = 0;
            // Copy the rest of the files
            for( Iterator i = staleResources.iterator(); i.hasNext(); index++ )
                {
                ResourceIndex ri = (ResourceIndex)i.next();
                pr.setMessage( "Copying:" + ri.getKey().getFileName() );
                pr.setProgress( index );

                // Copy the new script into the build directory
                File src = ri.getSource().getFile( project );
                File dest = ri.getDestination().getFile( project );

                log.debug( "Copying:" + src + " to:" + dest );
                FileUtils.copyFile( src, dest );

                ri.makeAllUpToDate( project );
                }
        }

        public void packModule( File buildDir, File unpackedDir, ProgressReporter pr ) throws IOException
        {
            File module = new File( buildDir, project.getTargetModuleName() );

            // For now we cheat and use the project description.  We
            // should really be pulling this from the module.ifo resource.
            String description = project.getProjectDescription();

            ModPacker packer = new ModPacker( module, description, pr );
            File[] list = unpackedDir.listFiles();
            for( int i = 0; i < list.length; i++ )
                {
                if( list[i].isDirectory() )
                    continue;
                packer.addFile( list[i] );
                }

            System.out.println( "Writing resources to module file:" + module );

            // And go
            long size = packer.writeModule();
            System.out.println( size + " bytes written from " + packer.getResourceCount()
                                + " resources." );
        }

        public Result execute( Environment env )
        {
            graph = project.getProjectGraph();
            int nodeCount = graph.nodeSize();

            File buildDir = project.getBuildDirectory().getFile( project );
            File unpackedDir = project.getModuleFilesDirectory().getFile( project );

            String prName = "Build Module:" + project.getTargetModuleName();
            UserRequestHandler reqHandler = context.getRequestHandler();
            ProgressReporter pr;
            pr = reqHandler.requestProgressReporter( prName, "Scanning dependencies...", 0, nodeCount );
            try
                {
                int index = 0;
                for( Iterator i = graph.nodeIterator(); i.hasNext(); index++ )
                    {
                    Object obj = i.next();
                    pr.setProgress( index );
                    if( obj instanceof ResourceIndex )
                        {
                        checkStaleness( (ResourceIndex)obj );
                        }
                    }
                }
            finally
                {
                pr.done();
                }

            // Convert the XML first
            pr = reqHandler.requestProgressReporter( prName, "Converting XML...", 0, staleResources.size() );
            System.out.println( "Converting..." );
            try
                {
                convertGffResources( pr );
                }
            catch( IOException e )
                {
                log.error( "Error converting XML resources", e );
                }
            finally
                {
                pr.done();
                }

            // Copy any resources (including the script files)
            pr = reqHandler.requestProgressReporter( prName, "Copying resources...", 0, staleResources.size() );
            try
                {
                copyResources( pr );
                }
            catch( IOException e )
                {
                log.error( "Error copying modified resources", e );
                }
            finally
                {
                pr.done();
                }

            pr = reqHandler.requestProgressReporter( prName, "Compiling scripts...", 0, staleResources.size() );
            try
                {
                compileScripts( pr, unpackedDir );
                }
            catch( IOException e )
                {
                log.error( "Error compiling scripts", e );
                }
            finally
                {
                pr.done();
                }

            // Now build the module.
            pr = reqHandler.requestProgressReporter( prName, "Packing module...", 0, 0 );
            try
                {
                packModule( buildDir, unpackedDir, pr );
                }
            catch( IOException e )
                {
                log.error( "Error packing module", e );
                }
            finally
                {
                pr.done();
                }

            try
                {
                // Store a cached version of the project with all of the changes
                // we just made above.
                context.cacheProject();
                }
            catch( IOException e )
                {
                log.error( "Error compiling scripts", e );
                }

            return( null );
        }
    }
}



