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

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.progeeks.util.*;
import org.progeeks.util.log.*;

import org.progeeks.nwn.*;
import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.*;
import org.progeeks.nwn.model.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.nwn.ui.*;

/**
 *  Performs the nuts and bolts work of importing a module
 *  and its resources.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ModuleImporter
{
    static Log log = Log.getLog( ModuleImporter.class );

    private byte[] transferBuff = new byte[65536];

    private Project project;
    private ProjectGraph graph;
    private List rules;

    public ModuleImporter( Project project, List rules )
    {
        this.project = project;
        this.graph = project.getProjectGraph();
        this.rules = rules;
    }

    public void setRules( List rules )
    {
        this.rules = rules;
    }

    public List getRules()
    {
        return( rules );
    }

    public static Struct getModuleInfo( File moduleFile ) throws IOException
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

            return( GffUtils.readGff( rIn ) );
            }
        finally
            {
            in.close();
            }
    }

    public static List getModuleResourceList( File moduleFile ) throws IOException
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

    public FileIndex getPathForResource( ResourceKey key, FileIndex start )
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

    public ResourceIndex createSource( InputStream in, ResourceKey key, Project project,
                                       FileIndex destination ) throws IOException
    {
        ResourceIndex ri = null;

        // If it's a GFF file, the read it's struct
        if( key.isGffType() )
            {
            Struct struct = GffUtils.readGff( in );
            ri = ResourceIndexFactory.createResourceIndex( key, struct, destination,
                                                           project.getModuleFilesDirectory() );

            // Now write out the struct
            File f = ri.getSource().getFile( project );
            GffUtils.writeGffXml( key, struct, f );

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
                                                               project.getModuleFilesDirectory() );

                File f = ri.getSource().getFile( project );
                FileUtils.saveStream( f, in );

                // Go ahead and make the source and target times the same
                File df = ri.getDestination().getFile( project );
                df.setLastModified( f.lastModified() );

                // Make sure the file indexes are up-to-date.
                ri.getSource().updateLastModified( project );
                ri.getDestination().updateLastModified( project );
                }
            finally
                {
                in.close();
                }
            }
        return( ri );
    }

    public void importResource( ResourceKey key, InputStream in ) throws IOException
    {
        FileIndex destination;
        boolean   shouldHaveParent = false;
        Object    parentNode = null;
        List      children = null;

        // Certain file types piggy-back on other resources
        switch( key.getType() )
            {
            case ResourceTypes.TYPE_GIC: // area comments
            case ResourceTypes.TYPE_GIT: // area items
                // These get attached to the are resource and
                // inherit its directory.
                ResourceKey parent = new ResourceKey( key.getName(), ResourceTypes.TYPE_ARE );
                destination = getPathForResource( parent, project.getSourceDirectory() );

                parentNode = graph.getResourceIndex( parent );

                if( parentNode == null )
                    log.warn( "Resource has no parent:" + key );

                shouldHaveParent = true;
                break;

            case ResourceTypes.TYPE_ARE:
                // We do an extra check here just in case the GIC or GIT
                // resources were added before us
                ResourceKey k = new ResourceKey( key.getName(), ResourceTypes.TYPE_GIC );
                ResourceIndex temp = graph.getResourceIndex( k );
                if( temp != null )
                    {
                    children = new ArrayList();
                    // Need to connect this one to it
                    children.add( temp );
                    }
                k = new ResourceKey( key.getName(), ResourceTypes.TYPE_GIT );
                temp = graph.getResourceIndex( k );
                if( temp != null )
                    {
                    if( children == null )
                        children = new ArrayList();

                    // Need to connect this one to it
                    children.add( temp );
                    }
                destination = getPathForResource( key, project.getSourceDirectory() );
                break;

            case ResourceTypes.TYPE_IFO: // module info file
            case ResourceTypes.TYPE_ITP: // palette files
            case ResourceTypes.TYPE_FAC: // factions
            case ResourceTypes.TYPE_JRL: // journal
                parentNode = graph.getRoot();
                destination = getPathForResource( key, project.getSourceDirectory() );
                shouldHaveParent = true;
                break;

            default:
                destination = getPathForResource( key, project.getSourceDirectory() );
                break;
            }

        if( destination == null )
            {
            // Skipping it because it doesn't convert or copy to a source
            // file
            return;
            }

        // Construct the path if it doesn't exist
        File path = destination.getFile( project );
        if( !path.exists() )
            path.mkdirs();

        ResourceIndex ri = createSource( in, key, project, destination );

        // Now that we have a fully built out ResourceIndex,
        // add it to the graph
        graph.addNode( ri );
        if( shouldHaveParent )
            {
            if( parentNode != null )
                graph.addEdge( ProjectGraph.EDGE_AGGREGATE, parentNode, ri, true );
            }
        else
            {
            graph.addDirectory( destination );
            graph.addEdge( ProjectGraph.EDGE_FILE, destination, ri, true );
            }

        if( children != null )
            {
            for( Iterator i = children.iterator(); i.hasNext(); )
                {
                graph.addEdge( ProjectGraph.EDGE_AGGREGATE, ri, i.next(), true );
                }
            }
    }
}


