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
 *  Place-holder element refering to an object-based value.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ObjectElement extends Element
{
    private Object value;

    public ObjectElement( String name, int type, Object value )
    {
        super( name, type );
        this.value = value;
    }

    public void setValue( Object value )
    {
        this.value = value;
    }

    public Object getValue()
    {
        return( value );
    }

    public void setStringValue( String value )
    {
    }

    public String getStringValue()
    {
        if( value instanceof byte[] )
            {
            StringBuffer sb = new StringBuffer( "byte[]" );
            byte[] buff = (byte[])value;
            for( int i = 0; i < buff.length; i++ )
                {
                sb.append( "[" + Integer.toHexString( buff[i] & 0xff ) + "(" + (char)buff[i] + ")]" );
                }
            return( sb.toString() );
            }
        return( String.valueOf( value ) );
    }

    public String toString()
    {
        if( value instanceof byte[] )
            {
            StringBuffer sb = new StringBuffer( getName() + " = " + TYPES[getType()] + ":" );
            byte[] buff = (byte[])value;
            for( int i = 0; i < buff.length; i++ )
                {
                sb.append( "[" + Integer.toHexString( buff[i] & 0xff ) + "(" + (char)buff[i] + ")]" );
                }
            return( sb.toString() );
            }
        return( getName() + " = " + TYPES[getType()] + ":" + value );
    }
}
