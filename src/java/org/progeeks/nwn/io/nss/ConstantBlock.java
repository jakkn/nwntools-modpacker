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
 * 3) Neither the names "Progeeks", "NWN Tools", nor the names of its contributors
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

package org.progeeks.nwn.io.nss;


/**
 *  Represents a block of script code containing a constant definition.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ConstantBlock extends ScriptBlock
{
    private String type;
    private String name;
    private String value;

    public ConstantBlock( StringBuffer block )
    {
        super( block );
        setType( CONSTANT );
    }

    public String getConstantType()
    {
        return( type );
    }

    public String getName()
    {
        return( name );
    }

    public String getValue()
    {
        return( value );
    }

    public void append( String text )
    {
        super.append( text );

        if( text.equals( "=" ) )
            {
            if( type == null || name == null )
                throw new RuntimeException( "Format error parsing constant declaration." );
            return;
            }
        else if( text.equals( ";" ) )
            {
            if( type == null || name == null || value == null )
                throw new RuntimeException( "Format error parsing constant declaration." );
            return;
            }
        else if( text.trim().length() == 0 )
            {
            return;
            }

        if( type == null )
            type = text;
        else if( name == null )
            name = text;
        else if( value == null )
            value = text;
    }

    public void setType( int type )
    {
        if( type != CONSTANT )
            throw new IllegalArgumentException( "ConstantBlock must be of type constant." );
        super.setType( type );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "ConstantBlock[" );
        sb.append( type );
        sb.append( ", " );
        sb.append( name );
        sb.append( " = " );
        sb.append( value );
        sb.append( "] (" + getStartLine() + " -> " + getEndLine() + ") = " );
        sb.append( getBlockText() );

        return( sb.toString() );
    }
}
