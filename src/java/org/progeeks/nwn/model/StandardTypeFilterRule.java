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

import org.progeeks.nwn.ResourceKey;

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
            case ResourceKey.TYPE_ARE:
                subDir = "Areas";
                break;
            case ResourceKey.TYPE_NSS:
                subDir = "Scripts";
                break;
            case ResourceKey.TYPE_DLG:
                subDir = "Conversations";
                break;
            case ResourceKey.TYPE_UTM:
                subDir = "Blueprints/Merchants";
                break;
            case ResourceKey.TYPE_UTS:
                subDir = "Blueprints/Sounds";
                break;
            case ResourceKey.TYPE_UTP:
                subDir = "Blueprints/Placeables";
                break;
            case ResourceKey.TYPE_UTE:
                subDir = "Blueprints/Encounters";
                break;
            case ResourceKey.TYPE_UTI:
                subDir = "Blueprints/Items";
                break;
            case ResourceKey.TYPE_UTC:
                subDir = "Blueprints/Creatures";
                break;
            case ResourceKey.TYPE_UTW:
                subDir = "Blueprints/Waypoints";
                break;
            case ResourceKey.TYPE_UTT:
                subDir = "Blueprints/Triggers";
                break;
            case ResourceKey.TYPE_UTD:
                subDir = "Blueprints/Doors";
                break;

            // Ignored types
            case ResourceKey.TYPE_NCS: // compiled script files
            case ResourceKey.TYPE_NDB: // debug files
            case ResourceKey.TYPE_GIC: // area comments
            case ResourceKey.TYPE_GIT: // area items
            case ResourceKey.TYPE_IFO: // module info file
            case ResourceKey.TYPE_ITP: // palette files
            case ResourceKey.TYPE_FAC: // factions
            case ResourceKey.TYPE_JRL: // journal
                return( null );

            default:
                break;
            }

        if( subDir == null )
            return( new MappingResult( currentParent, false ) );

        return( new MappingResult( new FileIndex( (FileIndex)currentParent, subDir ), false ) );
    }
}

