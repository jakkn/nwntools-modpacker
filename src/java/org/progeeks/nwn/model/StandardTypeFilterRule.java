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

import java.io.File;

import org.progeeks.nwn.resource.*;

/**
 *  Rule for moving the resource into a type-specific sub-directory.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class StandardTypeFilterRule implements MappingRule
{
    public MappingResult performMapping( Object currentParent, Object newNode )
    {
        ResourceKey key = (ResourceKey)newNode;
        String subDir = null;

        switch( key.getType() )
            {
            case ResourceTypes.TYPE_ARE:
            case ResourceTypes.TYPE_GIC: // area comments
            case ResourceTypes.TYPE_GIT: // area items
                subDir = "Areas";
                break;
            case ResourceTypes.TYPE_NSS:
                subDir = "Scripts";
                break;
            case ResourceTypes.TYPE_DLG:
                subDir = "Conversations";
                break;
            case ResourceTypes.TYPE_UTM:
                subDir = "Blueprints/Merchants";
                break;
            case ResourceTypes.TYPE_UTS:
                subDir = "Blueprints/Sounds";
                break;
            case ResourceTypes.TYPE_UTP:
                subDir = "Blueprints/Placeables";
                break;
            case ResourceTypes.TYPE_UTE:
                subDir = "Blueprints/Encounters";
                break;
            case ResourceTypes.TYPE_UTI:
                subDir = "Blueprints/Items";
                break;
            case ResourceTypes.TYPE_UTC:
                subDir = "Blueprints/Creatures";
                break;
            case ResourceTypes.TYPE_UTW:
                subDir = "Blueprints/Waypoints";
                break;
            case ResourceTypes.TYPE_UTT:
                subDir = "Blueprints/Triggers";
                break;
            case ResourceTypes.TYPE_UTD:
                subDir = "Blueprints/Doors";
                break;
            case ResourceTypes.TYPE_IFO: // module info file
            case ResourceTypes.TYPE_ITP: // palette files
            case ResourceTypes.TYPE_FAC: // factions
            case ResourceTypes.TYPE_JRL: // journal
                // Let these go into the sources directory for
                // now.  Eventually we'll roll them together or
                // something but at least now they'll get copied
                // over.
                break;

            // Ignored types
            case ResourceTypes.TYPE_NCS: // compiled script files
            case ResourceTypes.TYPE_NDB: // debug files
                return( null );

            default:
                break;
            }

        if( subDir == null )
            return( new MappingResult( currentParent, false ) );

        return( new MappingResult( new FileIndex( (FileIndex)currentParent, subDir ), false ) );
    }
}

