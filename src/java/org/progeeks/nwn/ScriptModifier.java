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

package org.progeeks.nwn;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.io.nss.*;

/**
 *  Reads and parses scripts, outputting a possibly modified
 *  version doing constant additions, variable substitution, etc..
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ScriptModifier
{
    private File output = null;
    private Map constants = new LinkedHashMap();
    private List scriptOperators = new ArrayList();

    public ScriptModifier()
    {
    }

    public void addScriptOperator( ScriptOperator operator )
    {
        scriptOperators.add( operator );
    }

    public void removeScriptOperator( ScriptOperator operator )
    {
        scriptOperators.remove( operator );
    }

    public void setOutputDirectory( File f )
    {
        this.output = f;
        System.out.println( "Sending processed files to:" + f );
    }

    public void setConstant( String constant )
    {
        String c = constant.trim();
        if( !c.startsWith( "const" ) )
            {
            throw new RuntimeException( "Invalid constant format:" + constant );
            }
        c = c.substring( "const".length() + 1 );
        int split = c.indexOf( ' ' );
        if( split < 0 )
            {
            throw new RuntimeException( "Invalid constant format:" + constant );
            }
        String type = c.substring( 0, split );
        c = c.substring( split + 1 );

        split = c.indexOf( '=' );
        if( split < 0 )
            {
            throw new RuntimeException( "Invalid constant format:" + constant );
            }
        String name = c.substring( 0, split ).trim();
        c = c.substring( split + 1 );

        if( !constant.endsWith( ";" ) )
            constant += ";";
        constant += "\n";

        System.out.println( "Setting constant:" + name + " to:" + c );
        constants.put( name, constant );
    }

    protected Script readScript( File scriptFile ) throws IOException
    {
        System.out.println( "    parsing:" + scriptFile );
        return( ScriptReader.readScript( scriptFile ) );
    }

    protected void writeScript( Script script ) throws IOException
    {
        File scriptFile;
        if( output != null )
            scriptFile = new File( output, script.getName() + ".nss" );
        else
            scriptFile = new File( script.getName() + ".nss" );
        System.out.println( "    writing:" + scriptFile );
        FileWriter fw = new FileWriter(scriptFile);
        PrintWriter out = new PrintWriter(fw);
        try
            {
            for( Iterator i = script.getBlocks().iterator(); i.hasNext(); )
                {
                ScriptBlock block = (ScriptBlock)i.next();
                out.print( block.getBlockText() );
                }
            }
        finally
            {
            out.close();
            }
    }

    protected void setConstants( List blocks )
    {
        Set used = new HashSet();

        // Go through the blocks to see if there are constants
        // already set that we need to replace.  While we're in
        // there, go ahead and keep track of the index of the
        // first include or declaration.
        int insertIndex = -1;
        int index = 0;
        boolean isRunnable = false;
        for( Iterator i = blocks.iterator(); i.hasNext(); index++ )
            {
            ScriptBlock block = (ScriptBlock)i.next();
            switch( block.getType() )
                {
                case ScriptBlock.COMMENT:
                case ScriptBlock.MULTICOMMENT:
                case ScriptBlock.WHITESPACE:
                    break;
                case ScriptBlock.INCLUDE:
                    if( insertIndex < 0 )
                        insertIndex = index;
                    break;
                case ScriptBlock.DECLARATION:
                    if( insertIndex < 0 )
                        insertIndex = index;
                    DeclarationBlock db = (DeclarationBlock)block;
                    if( "main".equals( db.getName() ) || "StartingConditional".equals( db.getName() ) )
                        isRunnable = true;
                    break;
                case ScriptBlock.CONSTANT:
                    if( insertIndex < 0 )
                        insertIndex = index;

                    ConstantBlock cb = (ConstantBlock)block;

                    // See if we've defined a value for this name
                    String text = (String)constants.get( cb.getName() );
                    if( text != null )
                        {
                        cb.setBlockText( new StringBuffer( text ) );
                        used.add( cb.getName() );
                        }
                    break;
                }
            }

        if( !isRunnable )
            {
            // We don't insert constants in include files.
            return;
            }

        // Now, go through the constants map adding any
        // that we haven't set yet.
        int insertCount = 0;
        for( Iterator i = constants.entrySet().iterator(); i.hasNext(); )
            {
            Map.Entry e = (Map.Entry)i.next();
            String name = (String)e.getKey();
            String text = (String)e.getValue();

            if( used.contains( name ) )
                continue;

            ConstantBlock newBlock = new ConstantBlock( new StringBuffer( text ) );
            blocks.add( insertIndex + insertCount, newBlock );
            insertCount++;
            }

        // If we've inserted any new constants then add some whitespace
        // before and after.
        if( insertCount > 0 )
            {
            blocks.add( insertIndex + insertCount, new ScriptBlock( new StringBuffer( "\n" ) ) );
            blocks.add( insertIndex, new ScriptBlock( new StringBuffer( "\n" ) ) );
            }
    }

    /**
     *  Goes through all of the blocks, searching and replacing
     *  the standard variable references.
     */
    protected void replaceVariables( Script script )
    {
        String scriptName = script.getName();
        String scriptFile = script.getFile().getName();

        for( Iterator i = script.getBlocks().iterator(); i.hasNext(); )
            {
            ScriptBlock block = (ScriptBlock)i.next();
            String text = block.getBlockText().toString();
            String processed = text.replaceAll( "\\$\\{script_name\\}", scriptName );
            processed = processed.replaceAll( "\\$\\{script_file\\}", scriptFile );
            if( text.equals( processed ) )
                continue;
            block.setBlockText( new StringBuffer(processed) );
            }
    }

    protected void processOperators( Script script )
    {
        for( Iterator i = scriptOperators.iterator(); i.hasNext(); )
            {
            ScriptOperator op = (ScriptOperator)i.next();
            op.processScript( script );
            }
    }

    public void processScript( File scriptFile ) throws IOException
    {
        System.out.println( "Processing:" + scriptFile );
        Script script = readScript( scriptFile );

        // Set any constants
        setConstants( script.getBlocks() );

        // Perform any processing
        replaceVariables( script );

        // Perform any other custom operations
        processOperators( script );

        // Write it back out
        writeScript( script );
    }

    public static void main( String[] args ) throws IOException
    {
        ScriptModifier scriptModder = new ScriptModifier();

        for( int i = 0; i < args.length; i++ )
            {
            if( args[i].startsWith( "-" ) )
                {
                if( args[i].equals( "-destination" ) && i + 1 < args.length )
                    {
                    scriptModder.setOutputDirectory( new File(args[i + 1]) );
                    i++;
                    }
                else if( args[i].equals( "-setConstant" ) && i + 1 < args.length )
                    {
                    scriptModder.setConstant( args[i + 1] );
                    i++;
                    }
                else
                    {
                    System.out.println( "Error processing argument:" + args[i] );
                    }
                }
            else if( args[i].toLowerCase().endsWith( ".nss" ) )
                {
                // Treat is as a script file
                scriptModder.processScript( new File(args[i]) );
                }
            else
                {
                System.out.println( "Don't know what to do with argument:" + args[i] );
                }
            }
    }
}
