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
 *  Element containing a list of other elements.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ListElement extends Element
{
    private List value;

    public ListElement( String name, int type )
    {
        super( name, type );
        value = new ArrayList();
    }

    public ListElement( String name, int type, List value )
    {
        super( name, type );
        this.value = value;
    }

    public List getValue()
    {
        return( value );
    }

    public void addValue( Struct struct )
    {
        value.add( struct );
    }

    public void setValue( List value )
    {
        this.value = value;
    }

    public void setStringValue( String value )
    {
    }

    public String getStringValue()
    {
        return( String.valueOf( value ) );
    }

    public int hashCode()
    {
        // Not ideal, but good enough
        return( getName().hashCode() );
    }

    public boolean equals( Object obj )
    {
        if( obj == null )
            return( false );

        if( !getClass().equals( obj.getClass() ) )
            return( false );

        ListElement el = (ListElement)obj;

        if( getType() != el.getType() )
            return( false );

        if( !getName().equals( el.getName() ) )
            return( false );

        return( value.equals( el.value ) );
    }

    public String toString()
    {
        return( getName() + " = " + TYPES[getType()] + ":" + value );
    }
}
