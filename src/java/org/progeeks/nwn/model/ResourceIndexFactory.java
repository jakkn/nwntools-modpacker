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

package org.progeeks.nwn.model;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.resource.*;

/**
 *  Creates ResourceIndex objects from supplied parameters.
 *  This is primarily used for finding the names of GFF files
 *  since it's different for every type... but it will do any
 *  index.  Every resource type potentially has some custom
 *  behavior.  By hard-coding it in this class today, we can
 *  always implement something fancier in the future.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceIndexFactory
{
    /**
     *  Creates a resource index from the specified parameters supplying
     *  any type-specific processing to generate target and destination
     *  files.
     */
    public static ResourceIndex createResourceIndex( ResourceKey key, FileIndex sourceDir,
                                                     FileIndex targetDir )
    {
        return( new ResourceIndex( key, new FileIndex( sourceDir, getSourceFile(key) ),
                                        new FileIndex( targetDir, getDestinationFile(key) ) ) );
    }

    /**
     *  Creates a resource index from the specified parameters supplying
     *  any type-specific processing to generate target and destination
     *  files.  It will use the type to retrieve a human-readable name
     *  from the specified GFF Struct.
     */
    public static ResourceIndex createResourceIndex( ResourceKey key, Struct gff, FileIndex sourceDir,
                                                     FileIndex targetDir )
    {

        String name = getNameFromStruct( key, gff );

        return( new ResourceIndex( name, key, new FileIndex( sourceDir, getSourceFile(key) ),
                                        new FileIndex( targetDir, getDestinationFile(key) ) ) );
    }

    /**
     *  Locates the name within the specified struct based on the supplied
     *  type and returns it.
     */
    public static String getNameFromStruct( ResourceKey key, Struct struct )
    {
        String s = null;

        switch( key.getType() )
            {
            case ResourceTypes.TYPE_IFO: // module info
                s = "Module Information";
                break;
            case ResourceTypes.TYPE_ARE: // area
                s = struct.getString( "Name" );
                break;
            case ResourceTypes.TYPE_GIC: // area comments
            case ResourceTypes.TYPE_GIT: // area objects
                break;
            case ResourceTypes.TYPE_UTC: // creature blueprint
                String first = struct.getString( "FirstName" );
                String last = struct.getString( "LastName" );
                if( first != null && last != null )
                    s = first + " " + last;
                else if( last != null )
                    s = last;
                else
                    s = first;
                s.trim();
                break;
            case ResourceTypes.TYPE_UTD: // door blueprint
                s = struct.getString( "LocName" );
                break;
            case ResourceTypes.TYPE_UTE: // encounter blueprint
                s = struct.getString( "LocalizedName" );
                break;
            case ResourceTypes.TYPE_UTI: // item blueprint
                s = struct.getString( "LocalizedName" );
                break;
            case ResourceTypes.TYPE_UTP: // placeable blueprint
                s = struct.getString( "LocName" );
                break;
            case ResourceTypes.TYPE_UTS: // sound blueprint
                s = struct.getString( "LocName" );
                break;
            case ResourceTypes.TYPE_UTM: // store blueprint
                s = struct.getString( "LocName" );
                break;
            case ResourceTypes.TYPE_UTT: // trigger blueprint
                s = struct.getString( "LocalizedName" );
                break;
            case ResourceTypes.TYPE_UTW: // waypoint blueprint
                s = struct.getString( "LocalizedName" );
                break;
            case ResourceTypes.TYPE_DLG: // conversation
                // Conversations have no name
            case ResourceTypes.TYPE_JRL: // journal
                // Neither do journals, besides they don't go into
                // the tree anyway.
            case ResourceTypes.TYPE_FAC: // faction
                // Likewise
            case ResourceTypes.TYPE_ITP: // palette
                // No name.
            case ResourceTypes.TYPE_PTM: // plot manager
            case ResourceTypes.TYPE_PTT: // plot wizard blueprint
            case ResourceTypes.TYPE_BIC: // Character/Creature file
                break;
            }

        if( "".equals( s ) )
            s = null;

        return( s );
    }

    public static String getSourceFile( ResourceKey key )
    {
        String ext = key.getTypeString().toLowerCase();

        if( key.isGffType() )
            {
            ext += ".xml";
            }

        return( key.getName() + "." + ext );
    }

    public static String getDestinationFile( ResourceKey key )
    {
        String ext = null;

        // Some types have altogether different destinations.
        switch( key.getType() )
            {
            case ResourceTypes.TYPE_NSS:
                //ext = "ncs";
                ext = "nss";
                // Note: for scripts we use the NSS extension because
                // the source files will always be copied into the build directory.
                // Right now this is really the only way we can build and
                // it's also the only way we can tell if the source file has
                // changed since not all .nss files will have a .ncs file (includes).
                // If we ever want to not store scripts in the module then it can
                // be filtered out at module construction time... but not here.
                break;
            default:
                ext = key.getTypeString().toLowerCase();
                break;
            }

        return( key.getName() + "." + ext );
    }
}
