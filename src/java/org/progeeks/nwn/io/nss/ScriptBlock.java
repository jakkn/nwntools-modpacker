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


/**
 *  Represents a block of script code.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ScriptBlock
{
    public static final int WHITESPACE = 0;
    public static final int COMMENT = 1;
    public static final int INCLUDE = 2;
    public static final int CODE = 3;

    private int startLine;
    private int endLine;
    private int type;
    private StringBuffer blockText;

    public ScriptBlock( StringBuffer block )
    {
        this.blockText = block;
    }

    public ScriptBlock( int type, StringBuffer blockText, int start, int end )
    {
        this.startLine = start;
        this.endLine = end;
        this.type = type;
        this.blockText = blockText;
    }

    public void setType( int type )
    {
        this.type = type;
    }

    public int getType()
    {
        return( type );
    }

    public void setBlockText( StringBuffer blockText )
    {
        this.blockText = blockText;
    }

    public StringBuffer getBlockText()
    {
        return( blockText );
    }

    public void setStartLine( int line )
    {
        this.startLine = line;
    }

    public int getStartLine()
    {
        return( startLine );
    }

    public void setEndLine( int line )
    {
        this.endLine = line;
    }

    public int getEndLine()
    {
        return( endLine );
    }

    public void appendBlock( ScriptBlock block )
    {
        if( this.type != block.type )
            throw new RuntimeException( "Blocks cannot be merged." );

        blockText.append( block.blockText );
        this.endLine = block.endLine;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "Block[" );
        switch( type )
            {
            case WHITESPACE:
                sb.append( "Whitespace" );
                break;
            case COMMENT:
                sb.append( "Comment" );
                break;
            case INCLUDE:
                sb.append( "Include" );
                break;
            case CODE:
                sb.append( "Code" );
                break;
            default:
                sb.append( "Unknown:" + type );
                break;
            }

        sb.append( "] (" + startLine + " -> " + endLine + ") = " );
        sb.append( blockText );

        return( sb.toString() );
    }
}
