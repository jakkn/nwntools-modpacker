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

package org.progeeks.nwn;


/**
 *  Contains a resource name and a type.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceKey implements java.io.Serializable
{
    static final long serialVersionUID = 42L;

    private String name;
    private int type;

    /**
     *  Creates a key that refers to any resource with the
     *  specified name.
     */
    public ResourceKey( String name )
    {
        this( name, -1 );
    }

    /**
     *  Creates a key that refers to the resource with the
     *  specified name and type.
     */
    public ResourceKey( String name, int type )
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return( name );
    }

    public int getType()
    {
        return( type );
    }

    public int hashCode()
    {
        return( name.hashCode() + type );
    }

    public boolean equals( ResourceKey key )
    {
        if( key == this )
            return( true );
        if( key.type != type )
            return( false );
        if( key.name != name && !key.name.equals( name ) )
            return( false );
        return( true );
    }

    public boolean equals( Object obj )
    {
        if( obj instanceof ResourceKey )
            return( equals( (ResourceKey)obj ) );
        return( false );
    }

    public String toString()
    {
        return( "ResourceKey[" + name + ":" + ResourceUtils.getTypeString( type ) + "]" );
    }
}
