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

package org.progeeks.nwn;


/**
 *  Contains a resource name and a type.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceKey implements java.io.Serializable, Comparable
{
    static final long serialVersionUID = 42L;

    public static final int TYPE_RES = 0x0000;
    public static final int TYPE_BMP = 0x0001;
    public static final int TYPE_MVE = 0x0002;
    public static final int TYPE_TGA = 0x0003;
    public static final int TYPE_WAV = 0x0004;
    public static final int TYPE_PLT = 0x0006;
    public static final int TYPE_INI = 0x0007;
    public static final int TYPE_BMU = 0x0008;
    public static final int TYPE_MPG = 0x0009;
    public static final int TYPE_TXT = 0x000A;
    public static final int TYPE_PLH = 0x07D0;
    public static final int TYPE_TEX = 0x07D1;
    public static final int TYPE_MDL = 0x07D2;
    public static final int TYPE_THG = 0x07D3;
    public static final int TYPE_FNT = 0x07D5;
    public static final int TYPE_LUA = 0x07D7;
    public static final int TYPE_SLT = 0x07D8;
    public static final int TYPE_NSS = 0x07D9;
    public static final int TYPE_NCS = 0x07DA;
    public static final int TYPE_MOD = 0x07DB;
    public static final int TYPE_ARE = 0x07DC;
    public static final int TYPE_SET = 0x07DD;
    public static final int TYPE_IFO = 0x07DE;
    public static final int TYPE_BIC = 0x07DF;
    public static final int TYPE_WOK = 0x07E0;
    public static final int TYPE_2DA = 0x07E1;
    public static final int TYPE_TLK = 0x07E2;
    public static final int TYPE_TXI = 0x07E6;
    public static final int TYPE_GIT = 0x07E7;
    public static final int TYPE_BTI = 0x07E8;
    public static final int TYPE_UTI = 0x07E9;
    public static final int TYPE_BTC = 0x07EA;
    public static final int TYPE_UTC = 0x07EB;
    public static final int TYPE_DLG = 0x07ED;
    public static final int TYPE_ITP = 0x07EE;
    public static final int TYPE_BTT = 0x07EF;
    public static final int TYPE_UTT = 0x07F0;
    public static final int TYPE_DDS = 0x07F1;
    public static final int TYPE_UTS = 0x07F3;
    public static final int TYPE_LTR = 0x07F4;
    public static final int TYPE_GFF = 0x07F5;
    public static final int TYPE_FAC = 0x07F6;
    public static final int TYPE_BTE = 0x07F7;
    public static final int TYPE_UTE = 0x07F8;
    public static final int TYPE_BTD = 0x07F9;
    public static final int TYPE_UTD = 0x07FA;
    public static final int TYPE_BTP = 0x07FB;
    public static final int TYPE_UTP = 0x07FC;
    public static final int TYPE_DTF = 0x07FD;
    public static final int TYPE_GIC = 0x07FE;
    public static final int TYPE_GUI = 0x07FF;
    public static final int TYPE_CSS = 0x0800;
    public static final int TYPE_CCS = 0x0801;
    public static final int TYPE_BTM = 0x0802;
    public static final int TYPE_UTM = 0x0803;
    public static final int TYPE_DWK = 0x0804;
    public static final int TYPE_PWK = 0x0805;
    public static final int TYPE_BTG = 0x0806;
    public static final int TYPE_UTG = 0x0807;
    public static final int TYPE_JRL = 0x0808;
    public static final int TYPE_SAV = 0x0809;
    public static final int TYPE_UTW = 0x080A;
    public static final int TYPE_4PC = 0x080B;
    public static final int TYPE_SSF = 0x080C;
    public static final int TYPE_HAK = 0x080D;
    public static final int TYPE_NWM = 0x080E;
    public static final int TYPE_BIK = 0x080F;
    public static final int TYPE_NDB = 0x0810;
    public static final int TYPE_PTM = 0x0811;
    public static final int TYPE_PTT = 0x0812;
    public static final int TYPE_270C = 0x270C;
    public static final int TYPE_ERF = 0x270D;
    public static final int TYPE_BIF = 0x270E;
    public static final int TYPE_KEY = 0x270F;

    private String name;
    private int type;

    /**
     *  Creates a key that refers to any resource with the
     *  specified name.
     */
    public ResourceKey( String name )
    {
        this( name, -1 );
    }

    /**
     *  Creates a key that refers to the resource with the
     *  specified name and type.
     */
    public ResourceKey( String name, int type )
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

    public boolean isGffType()
    {
        return( ResourceUtils.isGffType( type ) );
    }

    public String getTypeString()
    {
        return( ResourceUtils.getTypeString( type ) );
    }

    public int hashCode()
    {
        return( name.hashCode() + type );
    }

    public boolean equals( ResourceKey key )
    {
        if( key == this )
            return( true );
        if( key.type != type )
            return( false );
        if( key.name != name && !key.name.equals( name ) )
            return( false );
        return( true );
    }

    public boolean equals( Object obj )
    {
        if( obj instanceof ResourceKey )
            return( equals( (ResourceKey)obj ) );
        return( false );
    }

    public int compareTo( Object obj )
    {
        if( !(obj instanceof ResourceKey) )
            return( 1 );
        ResourceKey key = (ResourceKey)obj;
        return( name.compareTo( key.name ) );
    }

    public String toString()
    {
        return( "ResourceKey[" + name + ":" + ResourceUtils.getTypeString( type ) + "]" );
    }
}
