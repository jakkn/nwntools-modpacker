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

package org.progeeks.nwn.io.key;

import java.io.*;

import org.progeeks.nwn.io.*;

/**
 *  Header information from a Key file.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class Header
{
    private String type;
    private String version;
    private int fileCount;
    private int keyCount;
    private int fileTableOffset;
    private int keyTableOffset;
    private int buildYear;
    private int buildDay;

    public Header( BinaryDataInputStream in ) throws IOException
    {
        byte[] buff = new byte[4];
        in.readFully( buff );
        type = new String(buff);
        in.readFully( buff );
        version = new String(buff);

        fileCount = in.readInt();
        keyCount = in.readInt();
        fileTableOffset = in.readInt();
        keyTableOffset = in.readInt();
        buildYear = in.readInt();
        buildDay = in.readInt();
    }

    public String getType()
    {
        return( type );
    }

    public String getVersion()
    {
        return( version );
    }

    public int getFileCount()
    {
        return( fileCount );
    }

    public int getKeyCount()
    {
        return( keyCount );
    }

    public int getFileTableOffset()
    {
        return( fileTableOffset );
    }

    public int getKeyTableOffset()
    {
        return( keyTableOffset );
    }

    public int getBuildYear()
    {
        return( buildYear );
    }

    public int getBuildDay()
    {
        return( buildDay );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer( "Header[" );

        sb.append( "\ntype = " );
        sb.append( type );
        sb.append( ",\nversion = " );
        sb.append( version );

        sb.append( ",\nfile count = " ).append( fileCount );
        sb.append( ",\nkey count = " ).append( keyCount );
        sb.append( ",\nfile table offset = " ).append( fileTableOffset );
        sb.append( ",\nkey table offset = " ).append( keyTableOffset );
        sb.append( ",\nbuild year = " ).append( buildYear );
        sb.append( "  (" + (1900 + buildYear) + ")" );
        sb.append( ",\nbuild day = " ).append( buildDay );

        sb.append( "]" );
        return( sb.toString() );
    }
}
