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


package org.progeeks.nwn.resource;

import java.util.*;

/**
 *  Various utilities for NWN data types.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceUtils
{
    private static final String WILDCARD = "*";

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
        extensionMap.put( "270C", new Integer(0x270c) );
        extensionMap.put( "erf", new Integer(0x270d) );
        extensionMap.put( "bif", new Integer(0x270e) );
        extensionMap.put( "key", new Integer(0x270f) );
    }

    /**
     *  Returns a collection containing all resource types as Integers.
     *  In no particular order.
     */
    public static Collection getResourceTypes()
    {
        return( Collections.unmodifiableCollection( extensionMap.values() ) );
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

    public static int getTypeForFileName( String name )
    {
        String ext = "";
        int split = name.lastIndexOf( '.' );
        if( split >= 0 )
            {
            ext = name.substring( split + 1 );
            name = name.substring( 0, split );
            }
        return( getTypeForExtension( ext ) );
    }

    public static ResourceKey getKeyForFileName( String name )
    {
System.out.println( "getKeyForFileName(" + name + ")" );
        String ext = "";
        int split = name.lastIndexOf( '.' );
        if( split >= 0 )
            {
            ext = name.substring( split + 1 );
            name = name.substring( 0, split );
            }
System.out.println( "Name:" + name + "   ext:" + ext );
        ResourceKey key = new ResourceKey( name, getTypeForExtension( ext.toLowerCase() ) );
System.out.println( "  key:" + key );
        return( key );
    }

    public static String getTypeString( int type )
    {
        switch( type )
            {
            case ResourceTypes.TYPE_RES:
                return( "RES" );
            case ResourceTypes.TYPE_BMP:
                return( "BMP" );
            case ResourceTypes.TYPE_MVE:
                return( "MVE" );
            case ResourceTypes.TYPE_TGA:
                return( "TGA" );
            case ResourceTypes.TYPE_WAV:
                return( "WAV" );
            case ResourceTypes.TYPE_PLT:
                return( "PLT" );
            case ResourceTypes.TYPE_INI:
                return( "INI" );
            case ResourceTypes.TYPE_BMU:
                return( "BMU" );
            case ResourceTypes.TYPE_MPG:
                return( "MPG" );
            case ResourceTypes.TYPE_TXT:
                return( "TXT" );
            case ResourceTypes.TYPE_PLH:
                return( "PLH" );
            case ResourceTypes.TYPE_TEX:
                return( "TEX" );
            case ResourceTypes.TYPE_MDL:
                return( "MDL" );
            case ResourceTypes.TYPE_THG:
                return( "THG" );
            case ResourceTypes.TYPE_FNT:
                return( "FNT" );
            case ResourceTypes.TYPE_LUA:
                return( "LUA" );
            case ResourceTypes.TYPE_SLT:
                return( "SLT" );
            case ResourceTypes.TYPE_NSS:
                return( "NSS" );
            case ResourceTypes.TYPE_NCS:
                return( "NCS" );
            case ResourceTypes.TYPE_MOD:
                return( "MOD" );
            case ResourceTypes.TYPE_ARE:
                return( "ARE" );
            case ResourceTypes.TYPE_SET:
                return( "SET" );
            case ResourceTypes.TYPE_IFO:
                return( "IFO" );
            case ResourceTypes.TYPE_BIC:
                return( "BIC" );
            case ResourceTypes.TYPE_WOK:
                return( "WOK" );
            case ResourceTypes.TYPE_2DA:
                return( "2DA" );
            case ResourceTypes.TYPE_TLK:
                return( "TLK" );
            case ResourceTypes.TYPE_TXI:
                return( "TXI" );
            case ResourceTypes.TYPE_GIT:
                return( "GIT" );
            case ResourceTypes.TYPE_BTI:
                return( "BTI" );
            case ResourceTypes.TYPE_UTI:
                return( "UTI" );
            case ResourceTypes.TYPE_BTC:
                return( "BTC" );
            case ResourceTypes.TYPE_UTC:
                return( "UTC" );
            case ResourceTypes.TYPE_DLG:
                return( "DLG" );
            case ResourceTypes.TYPE_ITP:
                return( "ITP" );
            case ResourceTypes.TYPE_BTT:
                return( "BTT" );
            case ResourceTypes.TYPE_UTT:
                return( "UTT" );
            case ResourceTypes.TYPE_DDS:
                return( "DDS" );
            case ResourceTypes.TYPE_UTS:
                return( "UTS" );
            case ResourceTypes.TYPE_LTR:
                return( "LTR" );
            case ResourceTypes.TYPE_GFF:
                return( "GFF" );
            case ResourceTypes.TYPE_FAC:
                return( "FAC" );
            case ResourceTypes.TYPE_BTE:
                return( "BTE" );
            case ResourceTypes.TYPE_UTE:
                return( "UTE" );
            case ResourceTypes.TYPE_BTD:
                return( "BTD" );
            case ResourceTypes.TYPE_UTD:
                return( "UTD" );
            case ResourceTypes.TYPE_BTP:
                return( "BTP" );
            case ResourceTypes.TYPE_UTP:
                return( "UTP" );
            case ResourceTypes.TYPE_DTF:
                return( "DTF" );
            case ResourceTypes.TYPE_GIC:
                return( "GIC" );
            case ResourceTypes.TYPE_GUI:
                return( "GUI" );
            case ResourceTypes.TYPE_CSS:
                return( "CSS" );
            case ResourceTypes.TYPE_CCS:
                return( "CCS" );
            case ResourceTypes.TYPE_BTM:
                return( "BTM" );
            case ResourceTypes.TYPE_UTM:
                return( "UTM" );
            case ResourceTypes.TYPE_DWK:
                return( "DWK" );
            case ResourceTypes.TYPE_PWK:
                return( "PWK" );
            case ResourceTypes.TYPE_BTG:
                return( "BTG" );
            case ResourceTypes.TYPE_UTG:
                return( "UTG" );
            case ResourceTypes.TYPE_JRL:
                return( "JRL" );
            case ResourceTypes.TYPE_SAV:
                return( "SAV" );
            case ResourceTypes.TYPE_UTW:
                return( "UTW" );
            case ResourceTypes.TYPE_4PC:
                return( "4PC" );
            case ResourceTypes.TYPE_SSF:
                return( "SSF" );
            case ResourceTypes.TYPE_HAK:
                return( "HAK" );
            case ResourceTypes.TYPE_NWM:
                return( "NWM" );
            case ResourceTypes.TYPE_BIK:
                return( "BIK" );
            case ResourceTypes.TYPE_NDB:
                return( "NDB" );
            case ResourceTypes.TYPE_PTM:
                return( "PTM" );
            case ResourceTypes.TYPE_PTT:
                return( "PTT" );
            case ResourceTypes.TYPE_270C:
                return( "270C" );
            case ResourceTypes.TYPE_ERF:
                return( "ERF" );
            case ResourceTypes.TYPE_BIF:
                return( "BIF" );
            case ResourceTypes.TYPE_KEY:
                return( "KEY" );
            }

        return( WILDCARD );
    }

    public static String getExtensionForType( int type )
    {
        String ext = getTypeString( type );
        if( ext != WILDCARD )
            return( ext );

        ext = "0x" + Integer.toHexString( type );
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
            case ResourceTypes.TYPE_IFO: // module info
            case ResourceTypes.TYPE_ARE: // area
            case ResourceTypes.TYPE_GIC: // area comments
            case ResourceTypes.TYPE_GIT: // area objects
            case ResourceTypes.TYPE_UTC: // creature blueprint
            case ResourceTypes.TYPE_UTD: // door blueprint
            case ResourceTypes.TYPE_UTE: // encounter blueprint
            case ResourceTypes.TYPE_UTI: // item blueprint
            case ResourceTypes.TYPE_UTP: // placeable blueprint
            case ResourceTypes.TYPE_UTS: // sound blueprint
            case ResourceTypes.TYPE_UTM: // store blueprint
            case ResourceTypes.TYPE_UTT: // trigger blueprint
            case ResourceTypes.TYPE_UTW: // waypoint blueprint
            case ResourceTypes.TYPE_DLG: // conversation
            case ResourceTypes.TYPE_JRL: // journal
            case ResourceTypes.TYPE_FAC: // faction
            case ResourceTypes.TYPE_ITP: // palette
            case ResourceTypes.TYPE_PTM: // plot manager
            case ResourceTypes.TYPE_PTT: // plot wizard blueprint
            case ResourceTypes.TYPE_BIC: // Character/Creature file
                return( true );
            }
        return( false );
    }
}

