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

import org.progeeks.util.ErrorInfo;

/**
 *  Specialization of ErrorInfo containing script compile error
 *  information.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class CompileError extends ErrorInfo
{
    private String scriptName;
    private int line;

    public CompileError( String message, String scriptName, int line )
    {
        super( "Compiling", message );
        this.scriptName = scriptName;
        this.line = line;
    }

    public void setScriptName( String scriptName )
    {
        this.scriptName = scriptName;
    }

    public String getScriptName()
    {
        return( scriptName );
    }

    public void setLineNumber( int line )
    {
        this.line = line;
    }

    public int getLineNumber()
    {
        return( line );
    }

    public String toString()
    {
        return( "Error " + getTask() + ":" + scriptName + "(" + line + "):" + getMessage() );
    }
}
