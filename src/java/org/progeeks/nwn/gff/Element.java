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
 *  Base class for all element types.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public abstract class Element
{
    public static final int TYPE_UINT8 = 0;
    public static final int TYPE_INT8 = 1;
    public static final int TYPE_UINT16 = 2;
    public static final int TYPE_INT16 = 3;
    public static final int TYPE_UINT32 = 4;
    public static final int TYPE_INT32 = 5;
    public static final int TYPE_UINT64 = 6;
    public static final int TYPE_INT64 = 7;
    public static final int TYPE_FLOAT = 8;
    public static final int TYPE_DOUBLE = 9;
    public static final int TYPE_STRING = 10;
    public static final int TYPE_RESREF = 11;
    public static final int TYPE_STRREF = 12;
    public static final int TYPE_DATREF = 13;
    public static final int TYPE_STRUCTREF = 14;
    public static final int TYPE_LIST = 15;
    public static final int TYPE_STRUCT = 16;

    public static final String[] TYPES = { "UBYTE",
                                           "BYTE",
                                           "UINT16",
                                           "INT16",
                                           "UINT32",
                                           "INT32",
                                           "UINT64",
                                           "INT64",
                                           "FLOAT",
                                           "DOUBLE",
                                           "STRING",
                                           "RESREF",
                                           "STRINGREF",
                                           "DATAREF",
                                           "STRUCTREF",
                                           "<list of child entries>",
                                           "STRUCT" };

    private String name;
    private int type;

    protected Element( String name, int type )
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return( name );
    }

    public int getType()
    {
        return( type );
    }

    public abstract void setStringValue( String value );

    public abstract String getStringValue();
}
