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

        public BuildCommand( Project project )
        {
            super( false );

            this.project = project;
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
            if( ri.isSourceNewer() )
                {
                staleResources.add( ri );
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
                        staleResources.add( ri );
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

        public Result execute( Environment env )
        {
            graph = project.getProjectGraph();
            int nodeCount = graph.nodeSize();

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

System.out.println( "Stale:" + staleResources );



            return( null );
        }
    }
}



