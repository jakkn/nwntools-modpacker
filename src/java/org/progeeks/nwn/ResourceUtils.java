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
            return( -1 );
        return( i.intValue() );
    }

    public static String getExtensionForType( int type )
    {
        switch( type )
            {
            case 0x0000:
                return( "RES" );
            case 0x0001:
                return( "BMP" );
            case 0x0002:
                return( "MVE" );
            case 0x0003:
                return( "TGA" );
            case 0x0004:
                return( "WAV" );
            case 0x0006:
                return( "PLT" );
            case 0x0007:
                return( "INI" );
            case 0x0008:
                return( "BMU" );
            case 0x0009:
                return( "MPG" );
            case 0x000A:
                return( "TXT" );
            case 0x07D0:
                return( "PLH" );
            case 0x07D1:
                return( "TEX" );
            case 0x07D2:
                return( "MDL" );
            case 0x07D3:
                return( "THG" );
            case 0x07D5:
                return( "FNT" );
            case 0x07D7:
                return( "LUA" );
            case 0x07D8:
                return( "SLT" );
            case 0x07D9:
                return( "NSS" );
            case 0x07DA:
                return( "NCS" );
            case 0x07DB:
                return( "MOD" );
            case 0x07DC:
                return( "ARE" );
            case 0x07DD:
                return( "SET" );
            case 0x07DE:
                return( "IFO" );
            case 0x07DF:
                return( "BIC" );
            case 0x07E0:
                return( "WOK" );
            case 0x07E1:
                return( "2DA" );
            case 0x07E2:
                return( "TLK" );
            case 0x07E6:
                return( "TXI" );
            case 0x07E7:
                return( "GIT" );
            case 0x07E8:
                return( "BTI" );
            case 0x07E9:
                return( "UTI" );
            case 0x07EA:
                return( "BTC" );
            case 0x07EB:
                return( "UTC" );
            case 0x07ED:
                return( "DLG" );
            case 0x07EE:
                return( "ITP" );
            case 0x07EF:
                return( "BTT" );
            case 0x07F0:
                return( "UTT" );
            case 0x07F1:
                return( "DDS" );
            case 0x07F3:
                return( "UTS" );
            case 0x07F4:
                return( "LTR" );
            case 0x07F5:
                return( "GFF" );
            case 0x07F6:
                return( "FAC" );
            case 0x07F7:
                return( "BTE" );
            case 0x07F8:
                return( "UTE" );
            case 0x07F9:
                return( "BTD" );
            case 0x07FA:
                return( "UTD" );
            case 0x07FB:
                return( "BTP" );
            case 0x07FC:
                return( "UTP" );
            case 0x07FD:
                return( "DTF" );
            case 0x07FE:
                return( "GIC" );
            case 0x07FF:
                return( "GUI" );
            case 0x0800:
                return( "CSS" );
            case 0x0801:
                return( "CCS" );
            case 0x0802:
                return( "BTM" );
            case 0x0803:
                return( "UTM" );
            case 0x0804:
                return( "DWK" );
            case 0x0805:
                return( "PWK" );
            case 0x0806:
                return( "BTG" );
            case 0x0807:
                return( "UTG" );
            case 0x0808:
                return( "JRL" );
            case 0x0809:
                return( "SAV" );
            case 0x080A:
                return( "UTW" );
            case 0x080B:
                return( "4PC" );
            case 0x080C:
                return( "SSF" );
            case 0x080D:
                return( "HAK" );
            case 0x080E:
                return( "NWM" );
            case 0x080F:
                return( "BIK" );
            case 0x0810:
                return( "NDB" );
            case 0x0811:
                return( "PTM" );
            case 0x0812:
                return( "PTT" );
            case 0x270D:
                return( "ERF" );
            case 0x270E:
                return( "BIF" );
            case 0x270F:
                return( "KEY" );
            }
        throw new RuntimeException( "Unknown file type:" + type + "  0x" + Integer.toHexString( type ) );
    }
}
