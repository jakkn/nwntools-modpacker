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

package org.progeeks.nwn.io.bif;

import java.io.*;

import org.progeeks.nwn.io.*;

/**
 *  Header information for a BIF file.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class Header
{
    private String type;
    private String version;
    private int variableResourceCount;
    private int fixedResourceCount;
    private int variableResourceOffset;

    public Header( BinaryDataInputStream in ) throws IOException
    {
        byte[] buff = new byte[4];
        in.readFully( buff );
        type = new String(buff);
        in.readFully( buff );
        version = new String(buff);

        variableResourceCount = in.readInt();
        fixedResourceCount = in.readInt();
        variableResourceOffset = in.readInt();
    }

    public String getType()
    {
        return( type );
    }

    public String getVersion()
    {
        return( version );
    }

    public int getVariableResourceCount()
    {
        return( variableResourceCount );
    }

    public int getFixedResourceCount()
    {
        return( fixedResourceCount );
    }

    public int getVariableResourceOffset()
    {
        return( variableResourceOffset );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer( "Header[" );

        sb.append( "\ntype = " );
        sb.append( type );
        sb.append( ",\nversion = " );
        sb.append( version );

        sb.append( ",\nvariable resource count = " ).append( variableResourceCount );
        sb.append( ",\nfixed resource count = " ).append( fixedResourceCount );
        sb.append( ",\nvariable resource offset = " ).append( variableResourceOffset );

        sb.append( "]" );
        return( sb.toString() );
    }
}

