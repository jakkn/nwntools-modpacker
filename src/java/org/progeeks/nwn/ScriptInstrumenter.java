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
 *  Instrumentes a script in a standard way by replacing all of the
 *  main() and StartingConditional() functions with debug versions that
 *  call the originals with a modified name.  The process can also be
 *  reversed.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ScriptInstrumenter
{
    public static void main( String[] args ) throws Exception
    {
        ScriptModifier scriptModder = new ScriptModifier();

        ScriptOperator adder = new AddInstrumentation();
        ScriptOperator remover = new RemoveInstrumentation();

        // Defaults to adding
        scriptModder.addScriptOperator( adder );

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
                else if( args[i].equals( "-add" ) )
                    {
                    scriptModder.removeScriptOperator( remover );
                    scriptModder.addScriptOperator( adder );
                    }
                else if( args[i].equals( "-remove" ) )
                    {
                    scriptModder.removeScriptOperator( adder );
                    scriptModder.addScriptOperator( remover );
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

    private static final String REAL_FUNCTION_NAME = "pgUninstrumentedMain";

    /**
     *  Looks for certain functions and replaces/recalls them with
     *  instrumented versions.
     */
    private static class AddInstrumentation implements ScriptOperator
    {
        public AddInstrumentation()
        {
        }

        public void processScript( Script script )
        {
            List blocks = script.getBlocks();

            // Look for the block we're interested in... just main() for now.
            DeclarationBlock main = script.findDeclaration( "void", "main" );
            if( main == null )
                {
                System.out.println( "No main function found, skipping." );
                return;
                }

            DeclarationBlock instrumentedMain;
            StringBuffer sb = new StringBuffer();
            sb.append( "void main()\n" );
            sb.append( "{\n" );
            sb.append( "    WriteTimestampedLogEntry( \"+Script [" + script.getName() + "] starting. \" );\n" );
            sb.append( "    " + REAL_FUNCTION_NAME + "();\n" );
            sb.append( "    WriteTimestampedLogEntry( \"-Script [" + script.getName() + "] finished. \" );\n" );
            sb.append( "}\n" );
            instrumentedMain = new DeclarationBlock( sb, "void", "main" );

            // Go ahead and see if we might have already instrumented this code
            DeclarationBlock renamedMain = script.findDeclaration( "void", REAL_FUNCTION_NAME );
            if( renamedMain != null )
                {
                System.out.println( "Replacing instrumentation in:" + script );
                int index = blocks.indexOf( main );
                blocks.set( index, instrumentedMain );
                return;
                }

            System.out.println( "Adding instrumentation to:" + script );

            int insert = blocks.indexOf( main );

            // Modify the original main to have a new name
            main.setName( "pgUninstrumentedMain" );

            // Put our instrumented version just after the renamed main
            insert++;
            blocks.add( insert, instrumentedMain );
        }
    }

    /**
     *  Reverses what AddInstrumentation does.
     */
    private static class RemoveInstrumentation implements ScriptOperator
    {
        public void processScript( Script script )
        {
            List blocks = script.getBlocks();

            // Look for the block we're interested in... just main() for now.
            DeclarationBlock main = script.findDeclaration( "void", "main" );
            if( main == null )
                {
                System.out.println( "No main function found, skipping." );
                return;
                }

            // Go ahead and see if we might have already instrumented this code
            DeclarationBlock renamedMain = script.findDeclaration( "void", REAL_FUNCTION_NAME );
            if( renamedMain == null )
                {
                System.out.println( "No instrumentation found, skipping." );
                }

            System.out.println( "Removing instrumentation from:" + script );

            // Remove the instrumentation and change the name back on the real
            // main method.
            blocks.remove( main );
            renamedMain.setName( "main" );
        }
    }
}
