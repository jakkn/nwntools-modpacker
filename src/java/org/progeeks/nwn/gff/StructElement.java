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

package org.progeeks.nwn.gff;

import java.util.*;

/**
 *  An element that contains a list of other elements accessible
 *  by name.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class StructElement extends Element
{
    private Struct struct;

    public StructElement( String name, int type, Struct struct )
    {
        super( name, type );
        this.struct = struct;
    }

    public void setStruct( Struct struct )
    {
        this.struct = struct;
    }

    public Struct getStruct()
    {
        return( struct );
    }

    public void setStringValue( String value )
    {
    }

    public String getStringValue()
    {
        return( String.valueOf( struct ) );
    }

    public int hashCode()
    {
        if( struct == null )
            return( 0 );
        return( struct.hashCode() );
    }

    public boolean equals( Object obj )
    {
        if( obj == null )
            return( false );

        if( !getClass().equals( obj.getClass() ) )
            return( false );

        StructElement el = (StructElement)obj;

        if( getType() != el.getType() )
            return( false );

        if( !getName().equals( el.getName() ) )
            return( false );

        return( struct == el.struct || (struct != null && struct.equals( el.struct ) ) );
    }


    public String toString()
    {
        return( getName() + " = " + TYPES[getType()] + ":" + struct );
    }
}
