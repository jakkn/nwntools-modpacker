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
 *  Element containing a String reference to a localized string value
 *  and/or a list of language-dependent strings.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class LocalizedStringElement extends Element
{
    private int refId;
    private HashMap map;
    private String value;

    public LocalizedStringElement( String name, int type, int id )
    {
        super( name, type );
        this.refId = id;
    }

    public int getReferenceId()
    {
        return( refId );
    }

    public void addLocalString( int languageId, String s )
    {
        if( value == null )
            value = s;
        if( map == null )
            map = new HashMap();
        map.put( new Integer( languageId ), s );
    }

    public String getValue()
    {
        return( value );
    }

    public String getLocalString( int languageId )
    {
        return( (String)map.get( new Integer( languageId ) ) );
    }

    public Map getLocalStrings()
    {
        if( map == null )
            return( Collections.EMPTY_MAP );
        return( map );
    }

    public void setStringValue( String value )
    {
    }

    public String getStringValue()
    {
        return( value );
    }

    public String toString()
    {
        return( getName() + " = " + TYPES[getType()] + ":" + value );
    }
}

