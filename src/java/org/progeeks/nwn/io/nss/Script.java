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
 * 3) Neither the names "Progeeks", "NWN Tools", nor the names of its contributors
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

package org.progeeks.nwn.io.nss;

import java.io.File;
import java.util.*;

/**
 *  Represents the information about a parsed script including
 *  its parsed blocks.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class Script
{
    private String name;
    private File file;
    private List blocks;

    public Script( String name, File file, List blocks )
    {
        int split = name.lastIndexOf( "." );
        if( split > 0 )
            name = name.substring( 0, split );
        this.name = name;
        this.file = file;
        this.blocks = blocks;
    }

    public String getName()
    {
        return( name );
    }

    public File getFile()
    {
        return( file );
    }

    public List getBlocks()
    {
        return( blocks );
    }

    /**
     *  Finds a declaration block of the specified name and type.
     */
    public DeclarationBlock findDeclaration( String type, String name )
    {
        for( Iterator i = blocks.iterator(); i.hasNext(); )
            {
            ScriptBlock block = (ScriptBlock)i.next();
            if( block.getType() != ScriptBlock.DECLARATION )
                continue;

            DeclarationBlock db = (DeclarationBlock)block;
            if( name.equals( db.getName() ) && type.equals( db.getDeclarationType() ) )
                return( db );
            }
        return( null );
    }

    public String toString()
    {
        return( "Script[" + name + "]" );
    }
}
