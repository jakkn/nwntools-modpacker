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


package org.progeeks.nwn;

import java.util.*;

/**
 *  Various utilities for NWN data types.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceUtils
{
    public static final int RES_RES = 0x0000;
    public static final int RES_BMP = 0x0001;
    public static final int RES_MVE = 0x0002;
    public static final int RES_TGA = 0x0003;
    public static final int RES_WAV = 0x0004;
    public static final int RES_PLT = 0x0006;
    public static final int RES_INI = 0x0007;
    public static final int RES_BMU = 0x0008;
    public static final int RES_MPG = 0x0009;
    public static final int RES_TXT = 0x000A;
    public static final int RES_PLH = 0x07D0;
    public static final int RES_TEX = 0x07D1;
    public static final int RES_MDL = 0x07D2;
    public static final int RES_THG = 0x07D3;
    public static final int RES_FNT = 0x07D5;
    public static final int RES_LUA = 0x07D7;
    public static final int RES_SLT = 0x07D8;
    public static final int RES_NSS = 0x07D9;
    public static final int RES_NCS = 0x07DA;
    public static final int RES_MOD = 0x07DB;
    public static final int RES_ARE = 0x07DC;
    public static final int RES_SET = 0x07DD;
    public static final int RES_IFO = 0x07DE;
    public static final int RES_BIC = 0x07DF;
    public static final int RES_WOK = 0x07E0;
    public static final int RES_2DA = 0x07E1;
    public static final int RES_TLK = 0x07E2;
    public static final int RES_TXI = 0x07E6;
    public static final int RES_GIT = 0x07E7;
    public static final int RES_BTI = 0x07E8;
    public static final int RES_UTI = 0x07E9;
    public static final int RES_BTC = 0x07EA;
    public static final int RES_UTC = 0x07EB;
    public static final int RES_DLG = 0x07ED;
    public static final int RES_ITP = 0x07EE;
    public static final int RES_BTT = 0x07EF;
    public static final int RES_UTT = 0x07F0;
    public static final int RES_DDS = 0x07F1;
    public static final int RES_UTS = 0x07F3;
    public static final int RES_LTR = 0x07F4;
    public static final int RES_GFF = 0x07F5;
    public static final int RES_FAC = 0x07F6;
    public static final int RES_BTE = 0x07F7;
    public static final int RES_UTE = 0x07F8;
    public static final int RES_BTD = 0x07F9;
    public static final int RES_UTD = 0x07FA;
    public static final int RES_BTP = 0x07FB;
    public static final int RES_UTP = 0x07FC;
    public static final int RES_DTF = 0x07FD;
    public static final int RES_GIC = 0x07FE;
    public static final int RES_GUI = 0x07FF;
    public static final int RES_CSS = 0x0800;
    public static final int RES_CCS = 0x0801;
    public static final int RES_BTM = 0x0802;
    public static final int RES_UTM = 0x0803;
    public static final int RES_DWK = 0x0804;
    public static final int RES_PWK = 0x0805;
    public static final int RES_BTG = 0x0806;
    public static final int RES_UTG = 0x0807;
    public static final int RES_JRL = 0x0808;
    public static final int RES_SAV = 0x0809;
    public static final int RES_UTW = 0x080A;
    public static final int RES_4PC = 0x080B;
    public static final int RES_SSF = 0x080C;
    public static final int RES_HAK = 0x080D;
    public static final int RES_NWM = 0x080E;
    public static final int RES_BIK = 0x080F;
    public static final int RES_NDB = 0x0810;
    public static final int RES_PTM = 0x0811;
    public static final int RES_PTT = 0x0812;
    public static final int RES_ERF = 0x270D;
    public static final int RES_BIF = 0x270E;
    public static final int RES_KEY = 0x270F;

    /**
     *  Maps file extensions to the appropriate resource type code.
     */
    private static Map extensionMap = new HashMap();

    static
    {
        // Setup the extension mappings
        extensionMap.put( "res", new Integer(0x0000) );
        extensionMap.put( "bmp", new Integer(0x0001) );
        extensionMap.put( "mve", new Integer(0x0002) );
        extensionMap.put( "tga", new Integer(0x0003) );
        extensionMap.put( "wav", new Integer(0x0004) );
        extensionMap.put( "plt", new Integer(0x0006) );
        extensionMap.put( "ini", new Integer(0x0007) );
        extensionMap.put( "bmu", new Integer(0x0008) );
        extensionMap.put( "mpg", new Integer(0x0009) );
        extensionMap.put( "txt", new Integer(0x000a) );
        extensionMap.put( "plh", new Integer(0x07d0) );
        extensionMap.put( "tex", new Integer(0x07d1) );
        extensionMap.put( "mdl", new Integer(0x07d2) );
        extensionMap.put( "thg", new Integer(0x07d3) );
        extensionMap.put( "fnt", new Integer(0x07d5) );
        extensionMap.put( "lua", new Integer(0x07d7) );
        extensionMap.put( "slt", new Integer(0x07d8) );
        extensionMap.put( "nss", new Integer(0x07d9) );
        extensionMap.put( "ncs", new Integer(0x07da) );
        extensionMap.put( "mod", new Integer(0x07db) );
        extensionMap.put( "are", new Integer(0x07dc) );
        extensionMap.put( "set", new Integer(0x07dd) );
        extensionMap.put( "ifo", new Integer(0x07de) );
        extensionMap.put( "bic", new Integer(0x07df) );
        extensionMap.put( "wok", new Integer(0x07e0) );
        extensionMap.put( "2da", new Integer(0x07e1) );
        extensionMap.put( "tlk", new Integer(0x07e2) );
        extensionMap.put( "txi", new Integer(0x07e6) );
        extensionMap.put( "git", new Integer(0x07e7) );
        extensionMap.put( "bti", new Integer(0x07e8) );
        extensionMap.put( "uti", new Integer(0x07e9) );
        extensionMap.put( "btc", new Integer(0x07ea) );
        extensionMap.put( "utc", new Integer(0x07eb) );
        extensionMap.put( "dlg", new Integer(0x07ed) );
        extensionMap.put( "itp", new Integer(0x07ee) );
        extensionMap.put( "btt", new Integer(0x07ef) );
        extensionMap.put( "utt", new Integer(0x07f0) );
        extensionMap.put( "dds", new Integer(0x07f1) );
        extensionMap.put( "uts", new Integer(0x07f3) );
        extensionMap.put( "ltr", new Integer(0x07f4) );
        extensionMap.put( "gff", new Integer(0x07f5) );
        extensionMap.put( "fac", new Integer(0x07f6) );
        extensionMap.put( "bte", new Integer(0x07f7) );
        extensionMap.put( "ute", new Integer(0x07f8) );
        extensionMap.put( "btd", new Integer(0x07f9) );
        extensionMap.put( "utd", new Integer(0x07fa) );
        extensionMap.put( "btp", new Integer(0x07fb) );
        extensionMap.put( "utp", new Integer(0x07fc) );
        extensionMap.put( "dtf", new Integer(0x07fd) );
        extensionMap.put( "gic", new Integer(0x07fe) );
        extensionMap.put( "gui", new Integer(0x07ff) );
        extensionMap.put( "css", new Integer(0x0800) );
        extensionMap.put( "ccs", new Integer(0x0801) );
        extensionMap.put( "btm", new Integer(0x0802) );
        extensionMap.put( "utm", new Integer(0x0803) );
        extensionMap.put( "dwk", new Integer(0x0804) );
        extensionMap.put( "pwk", new Integer(0x0805) );
        extensionMap.put( "btg", new Integer(0x0806) );
        extensionMap.put( "utg", new Integer(0x0807) );
        extensionMap.put( "jrl", new Integer(0x0808) );
        extensionMap.put( "sav", new Integer(0x0809) );
        extensionMap.put( "utw", new Integer(0x080a) );
        extensionMap.put( "4pc", new Integer(0x080b) );
        extensionMap.put( "ssf", new Integer(0x080c) );
        extensionMap.put( "hak", new Integer(0x080d) );
        extensionMap.put( "nwm", new Integer(0x080e) );
        extensionMap.put( "bik", new Integer(0x080f) );
        extensionMap.put( "ndb", new Integer(0x0810) );
        extensionMap.put( "ptm", new Integer(0x0811) );
        extensionMap.put( "ptt", new Integer(0x0812) );
        extensionMap.put( "erf", new Integer(0x270d) );
        extensionMap.put( "bif", new Integer(0x270e) );
        extensionMap.put( "key", new Integer(0x270f) );
    }

    public static int getTypeForExtension( String extension )
    {
        Integer i = (Integer)extensionMap.get( extension );
        if( i == null )
            {
            // Check for encoded unknown extensions
            if( extension.startsWith( "0x" ) )
                {
                i = Integer.valueOf( extension.substring( 2 ), 16 );
                System.out.println( "Decoded extension:" + extension + " as type:" + i );
                }
            }
        if( i == null )
            return( -1 );
        return( i.intValue() );
    }

    public static String getExtensionForType( int type )
    {
        switch( type )
            {
            case RES_RES:
                return( "RES" );
            case RES_BMP:
                return( "BMP" );
            case RES_MVE:
                return( "MVE" );
            case RES_TGA:
                return( "TGA" );
            case RES_WAV:
                return( "WAV" );
            case RES_PLT:
                return( "PLT" );
            case RES_INI:
                return( "INI" );
            case RES_BMU:
                return( "BMU" );
            case RES_MPG:
                return( "MPG" );
            case RES_TXT:
                return( "TXT" );
            case RES_PLH:
                return( "PLH" );
            case RES_TEX:
                return( "TEX" );
            case RES_MDL:
                return( "MDL" );
            case RES_THG:
                return( "THG" );
            case RES_FNT:
                return( "FNT" );
            case RES_LUA:
                return( "LUA" );
            case RES_SLT:
                return( "SLT" );
            case RES_NSS:
                return( "NSS" );
            case RES_NCS:
                return( "NCS" );
            case RES_MOD:
                return( "MOD" );
            case RES_ARE:
                return( "ARE" );
            case RES_SET:
                return( "SET" );
            case RES_IFO:
                return( "IFO" );
            case RES_BIC:
                return( "BIC" );
            case RES_WOK:
                return( "WOK" );
            case RES_2DA:
                return( "2DA" );
            case RES_TLK:
                return( "TLK" );
            case RES_TXI:
                return( "TXI" );
            case RES_GIT:
                return( "GIT" );
            case RES_BTI:
                return( "BTI" );
            case RES_UTI:
                return( "UTI" );
            case RES_BTC:
                return( "BTC" );
            case RES_UTC:
                return( "UTC" );
            case RES_DLG:
                return( "DLG" );
            case RES_ITP:
                return( "ITP" );
            case RES_BTT:
                return( "BTT" );
            case RES_UTT:
                return( "UTT" );
            case RES_DDS:
                return( "DDS" );
            case RES_UTS:
                return( "UTS" );
            case RES_LTR:
                return( "LTR" );
            case RES_GFF:
                return( "GFF" );
            case RES_FAC:
                return( "FAC" );
            case RES_BTE:
                return( "BTE" );
            case RES_UTE:
                return( "UTE" );
            case RES_BTD:
                return( "BTD" );
            case RES_UTD:
                return( "UTD" );
            case RES_BTP:
                return( "BTP" );
            case RES_UTP:
                return( "UTP" );
            case RES_DTF:
                return( "DTF" );
            case RES_GIC:
                return( "GIC" );
            case RES_GUI:
                return( "GUI" );
            case RES_CSS:
                return( "CSS" );
            case RES_CCS:
                return( "CCS" );
            case RES_BTM:
                return( "BTM" );
            case RES_UTM:
                return( "UTM" );
            case RES_DWK:
                return( "DWK" );
            case RES_PWK:
                return( "PWK" );
            case RES_BTG:
                return( "BTG" );
            case RES_UTG:
                return( "UTG" );
            case RES_JRL:
                return( "JRL" );
            case RES_SAV:
                return( "SAV" );
            case RES_UTW:
                return( "UTW" );
            case RES_4PC:
                return( "4PC" );
            case RES_SSF:
                return( "SSF" );
            case RES_HAK:
                return( "HAK" );
            case RES_NWM:
                return( "NWM" );
            case RES_BIK:
                return( "BIK" );
            case RES_NDB:
                return( "NDB" );
            case RES_PTM:
                return( "PTM" );
            case RES_PTT:
                return( "PTT" );
            case RES_ERF:
                return( "ERF" );
            case RES_BIF:
                return( "BIF" );
            case RES_KEY:
                return( "KEY" );
            }
        String ext = "0x" + Integer.toHexString( type );

        System.out.println( "Warning: encountered unknown type:" + type + "  Using extension:" + ext );
        return( ext );
    }

    /**
     *  Returns true if the specified resource type is stored as
     *  a GFF file.
     */
    public static boolean isGffType( int type )
    {
        switch( type )
            {
            case RES_IFO: // module info
            case RES_ARE: // area
            case RES_GIC: // area comments
            case RES_GIT: // area objects
            case RES_UTC: // creature blueprint
            case RES_UTD: // door blueprint
            case RES_UTE: // encounter blueprint
            case RES_UTI: // item blueprint
            case RES_UTP: // placeable blueprint
            case RES_UTS: // sound blueprint
            case RES_UTM: // store blueprint
            case RES_UTT: // trigger blueprint
            case RES_UTW: // waypoint blueprint
            case RES_DLG: // conversation
            case RES_JRL: // journal
            case RES_FAC: // faction
            case RES_ITP: // palette
            case RES_PTM: // plot manager
            case RES_PTT: // plot wizard blueprint
            case RES_BIC: // Character/Creature file
                return( true );
            }
        return( false );
    }
}

