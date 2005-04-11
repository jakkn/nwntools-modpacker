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

package org.progeeks.nwn.resource;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.*;
import org.progeeks.util.log.*;

/**
 *  Provides resource lookup support.  Resource objects can be grabbed
 *  singly or in groups using their ResourceKeys as reference.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceManager implements Cloneable
{
    static Log log = Log.getLog( ResourceManager.class );

    public static final File DEFAULT_NWN_DIR = new File( "/NeverwinterNights/NWN" );

    /**
     *  Maps loaders to resource types.
     */
    private Map loaderMap = new HashMap();

    /**
     *  Maps resource keys to the index implementations that can load them.
     */
    private ResourceStreamer streamer = new ResourceStreamer();

    public ResourceManager()
    {
        setupDefaultLoaders();
    }

    /**
     *  Returns a clone of this resource manager and the map of
     *  handlers/resource loaders.
     */
    public Object clone()
    {
        ResourceManager clone = new ResourceManager();
        clone.loaderMap.putAll( loaderMap );
        clone.streamer = (ResourceStreamer)streamer.clone();

        return( clone );
    }

    /**
     *  Registers a default set of resource loaders.
     */
    protected void setupDefaultLoaders()
    {
        registerLoader( ResourceTypes.TYPE_SET, new TilesetLoader() );
        registerLoader( ResourceTypes.TYPE_TGA, new TargaLoader() );

        // Go through all of the resource types and register the gff
        // loader with any GFF based type.
        GffLoader gffLoader = new GffLoader();
        for( Iterator i = ResourceUtils.getResourceTypes().iterator(); i.hasNext(); )
            {
            Integer type = (Integer)i.next();

            if( ResourceUtils.isGffType( type.intValue() ) )
                registerLoader( type, gffLoader );
            }
    }

    /**
     *  Returns a string indicator for the resource location.  Useful in
     *  debugging look-up failures for overrides, etc..
     */
    public String getResourceLocationInfo( ResourceKey key )
    {
        return( streamer.getResourceLocationInfo( key ) );
    }

    /**
     *  Returns a type-specific object for the specified resource key.
     */
    public Object getResource( ResourceKey key )
    {
        ResourceLoader loader = getLoader( key.getType() );
        if( loader == null )
            {
            log.error( "No loader for key type, key:" + key );
            return( null );
            }

        InputStream in = getResourceStream( key );
        if( in == null )
            {
            log.error( "Could not get data stream for key:" + key );
            return( null );
            }

        try
            {
            return( loader.loadResource( key, in ) );
            }
        catch( IOException e )
            {
            log.error( "Error loading resource for key:" + key, e );
            return( null );
            }
    }

    /**
     *  Registers a specific loader implementation with the specified resource type.
     */
    public void registerLoader( int type, ResourceLoader loader )
    {
        registerLoader( new Integer(type), loader );
    }

    /**
     *  Registers a specific loader implementation with the specified resource type.
     */
    public void registerLoader( Integer type, ResourceLoader loader )
    {
        loaderMap.put( type, loader );
    }

    /**
     *  Returns a loader implementation for the specified type.
     */
    public ResourceLoader getLoader( int type )
    {
        return( (ResourceLoader)loaderMap.get( new Integer(type) ) );
    }

    /**
     *  Returns the raw InputStream for the specified resource key.
     */
    public InputStream getResourceStream( ResourceKey key )
    {
        return( streamer.getResourceStream( key ) );
    }

    /**
     *  Adds the specified key file to this resource manager's
     *  resource index.
     */
    public void addKeyFile( File keyFile ) throws IOException
    {
        streamer.addKeyFile( keyFile );
    }

    /**
     *  Adds the specified directory to this resource manager's
     *  resource index.
     */
    public void addResourceDirectory( File directory )
    {
        streamer.addResourceDirectory( directory );
    }

    /**
     *  Adds the specified resource file to this resource manager's
     *  resource index determining the type from the file's extension.
     */
    public void addResourceFile( File resource )
    {
        streamer.addResourceFile( resource );
    }

    /**
     *  Adds the specified resource file to this resource manager's
     *  resource index using the specified resource type.
     */
    public void addResourceFile( File resource, int type )
    {
        streamer.addResourceFile( resource, type );
    }

    /**
     *  Adds the specified encapsulated resource file (and all of its contained
     *  resources) to this resource manager's resource index.
     */
    public void addEncapsulatedResourceFile( File erf ) throws IOException
    {
        streamer.addEncapsulatedResourceFile( erf );
    }

    public void loadDefaultKeys( File nwn ) throws IOException
    {
        streamer.loadDefaultKeys( nwn );
    }

    public void loadDefaultKeys() throws IOException
    {
        streamer.loadDefaultKeys();
    }

}
