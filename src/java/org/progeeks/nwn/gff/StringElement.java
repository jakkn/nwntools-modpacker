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

/**
 *  Element containing a String value.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class StringElement extends Element
{
    private String value;

    public StringElement( String name, int type )
    {
        super( name, type );
    }

    public StringElement( String name, int type, String value )
    {
        super( name, type );
        this.value = value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    public String getValue()
    {
        return( value );
    }

    public void setStringValue( String value )
    {
        this.value = value;
    }

    public String getStringValue()
    {
        return( value );
    }

    /**
     *  Returns the amount of space this element will take up in the
     *  data block.  Will be 0 for all but the types that are actually
     *  stored in the data block.
     */
    public int getSize()
    {
        switch( getType() )
            {
            case TYPE_STRING:
                return( 4 + value.length() );
            case TYPE_RESREF:
                return( 1 + value.length() );
            default:
                throw new RuntimeException( "Cannot calculate size for type." );
            }
    }

    public int hashCode()
    {
        if( value == null )
            return( 0 );
        return( value.hashCode() );
    }

    public boolean equals( Object obj )
    {
        if( obj == null )
            return( false );

        if( !getClass().equals( obj.getClass() ) )
            return( false );

        StringElement el = (StringElement)obj;

        if( getType() != el.getType() )
            return( false );

        if( !getName().equals( el.getName() ) )
            return( false );

        return( value == el.value || (value != null && value.equals( el.value )) );
    }

    public String toString()
    {
        return( getName() + " = " + TYPES[getType()] + ":" + value );
    }
}
