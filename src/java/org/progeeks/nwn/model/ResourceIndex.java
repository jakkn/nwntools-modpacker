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

package org.progeeks.nwn.model;

import org.progeeks.nwn.ResourceKey;

/**
 *  Provides the project-level view of a resource.  This includes
 *  information about build book-keeping, etc..
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceIndex implements Comparable
{
    private String name;
    private ResourceKey key;
    private FileIndex source;
    private FileIndex destination;

    /**
     *  Creates a resource index that points to the specified resource.
     *  The index can be provided source and destination files that are used by
     *  the build to determine if all files are up-to-date, etc.. and a user-readable
     *  name for display.
     */
    public ResourceIndex( ResourceKey key, FileIndex source, FileIndex destination )
    {
        this( null, key, source, destination );
    }

    /**
     *  Creates a resource index that points to the specified resource.
     *  The index can be provided source and destination files that are used by
     *  the build to determine if all files are up-to-date, etc.. and a user-readable
     *  name for display.
     */
    public ResourceIndex( String name, ResourceKey key, FileIndex source, FileIndex destination )
    {
        this.name = name;
        this.key = key;
        this.source = source;
        this.destination = destination;
    }

    /**
     *  Sets the user-friendly name of this resource.  Null causes
     *  getName() to return key.getName().
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     *  Returns the user-friendly name of this resource, or key.getName()
     *  if there is no user-friendly name associated with this resource.
     */
    public String getName()
    {
        return( name );
    }

    public ResourceKey getKey()
    {
        return( key );
    }

    public FileIndex getSource()
    {
        return( source );
    }

    public FileIndex getDestination()
    {
        return( destination );
    }

    public boolean equals( Object obj )
    {
        if( !(obj instanceof ResourceIndex) )
            return( false );
        return( key.equals( ((ResourceIndex)obj).key ) );
    }

    public int hashCode()
    {
        return( key.hashCode() );
    }

    public int compareTo( Object obj )
    {
        if( !(obj instanceof ResourceIndex) )
            return( 1 );
        ResourceIndex ri = (ResourceIndex)obj;
        return( key.compareTo( ri.key ) );
    }

    public String toString()
    {
        return( "ResourceIndex[" + name + ", " + key + "]" );
    }
}


