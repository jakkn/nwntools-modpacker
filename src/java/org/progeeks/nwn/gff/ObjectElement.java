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

    public ObjectElement( String name, int type )
    {
        super( name, type );
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
        if( !value.startsWith( "byte[" ) )
            throw new RuntimeException( "Can't decode string:" + value );

        int split = value.indexOf( "]" );
        int size = Integer.parseInt( value.substring( "byte[".length(), split ) );
        byte[] val = new byte[size];
        this.value = val;
        int length = value.length();
        int current = 0;
        int index = 0;
        for( int i = split + 1; i < length; i++ )
            {
            char c = value.charAt( i );
            int nibble = 0;
            switch( c )
                {
                case '[':
                    current = 0;
                    break;
                case ']':
                    val[index] = (byte)current;
                    index++;
                    break;

                // Not the most efficient way, but it will do.
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    nibble = c - '0';
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    nibble = 10 + (c - 'a');
                    break;
                default:
                    break;
                }
            if( nibble > 0 )
                {
                current = current << 4;
                current += nibble;
                }
            }

System.out.println( "Passed value:" + value );
System.out.println( "     decoded:" + getStringValue() );
    }

    public String getStringValue()
    {
        if( value instanceof byte[] )
            {
            byte[] buff = (byte[])value;
            StringBuffer sb = new StringBuffer( "byte[" + buff.length + "]" );
            for( int i = 0; i < buff.length; i++ )
                {
                sb.append( "[" + Integer.toHexString( buff[i] & 0xff ) + "]" );
                }
            return( sb.toString() );
            }
        return( String.valueOf( value ) );
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
            case TYPE_DATREF:
                if( value instanceof byte[] )
                    return( ((byte[])value).length + 4 ); // size + buffer
            default:
                throw new RuntimeException( "Cannot calculate size for type." );
            }
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
