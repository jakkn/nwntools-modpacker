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

/**
 *  Provides resource lookup support.  Resource objects can be grabbed
 *  singly or in groups using their ResourceKeys as reference.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceManager
{
    /**
     *  Maps loaders to resource types.
     */
    private Map loaderMap = new HashMap();

    /**
     *  Maps resource keys to the index implementations that can load them.
     */
    private ResourceStreamer streamer = new ResourceStreamer();


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

    public void loadDefaultKeys() throws IOException
    {
        streamer.loadDefaultKeys();
    }

}
