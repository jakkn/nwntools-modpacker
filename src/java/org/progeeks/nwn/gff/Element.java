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

    /**
     *  Returns the amount of space this element will take up in the
     *  data block.  Will be 0 for all but the types that are actually
     *  stored in the data block.
     */
    public int getSize()
    {
        switch( type )
            {
            case TYPE_UINT8:
            case TYPE_INT8:
            case TYPE_UINT16:
            case TYPE_INT16:
            case TYPE_UINT32:
            case TYPE_INT32:
            case TYPE_FLOAT:
            case TYPE_STRUCTREF:
            case TYPE_LIST:
            case TYPE_STRUCT:
                return( 0 );

            case TYPE_UINT64:
                return( 8 );
            case TYPE_INT64:
                return( 8 );
            case TYPE_DOUBLE:
                return( 8 );

            case TYPE_STRING:
            case TYPE_RESREF:
            case TYPE_STRREF:
            case TYPE_DATREF:
            default:
                throw new RuntimeException( "Cannot calculate size for type." );
            }
    }

    public abstract void setStringValue( String value );

    public abstract String getStringValue();
}
