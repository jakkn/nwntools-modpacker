/*
 * $Id$
 *
 * Copyright (c) 2001-2002, Paul Speed
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

package org.progeeks.nwn.io.gff;

import java.io.*;

import org.progeeks.nwn.io.*;

/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class Header
{
    private String type;
    private String version;
    private BlockIndex structs;
    private BlockIndex fields;
    private BlockIndex labels;
    private BlockIndex values;
    private BlockIndex fieldIndices;
    private BlockIndex lists;

    public Header( BinaryDataInputStream in ) throws IOException
    {
        byte[] buff = new byte[4];
        in.readFully( buff );
        type = new String(buff);
        in.readFully( buff );
        version = new String(buff);

        structs = new BlockIndex(in);
        fields = new BlockIndex(in);
        labels = new BlockIndex(in);
        values = new BlockIndex(in);
        fieldIndices = new BlockIndex(in);
        lists = new BlockIndex(in);
    }

    public String getType()
    {
        return( type );
    }

    public String getVersion()
    {
        return( version );
    }

    public BlockIndex getStructs()
    {
        return( structs );
    }

    public BlockIndex getFields()
    {
        return( fields );
    }

    public BlockIndex getLabels()
    {
        return( labels );
    }

    public BlockIndex getValues()
    {
        return( values );
    }

    public BlockIndex getFieldIndices()
    {
        return( fieldIndices );
    }

    public BlockIndex getLists()
    {
        return( lists );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer( "Header[" );

        sb.append( "\ntype = " );
        sb.append( type );
        sb.append( ",\nversion = " );
        sb.append( version );

        sb.append( ",\nStructs = " ).append( structs );
        sb.append( ",\nFields = " ).append( fields );
        sb.append( ",\nLabels = " ).append( labels );
        sb.append( ",\nValues = " ).append( values );
        sb.append( ",\nFieldIndices = " ).append( fieldIndices );
        sb.append( ",\nLists = " ).append( lists );

        sb.append( "]" );
        return( sb.toString() );
    }
}
