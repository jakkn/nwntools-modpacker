/*
 * $Id$
 *
 * Copyright (c) 2003, Paul Speed
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

import java.io.*;
import java.util.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.util.thread.*;


/**
 *  Quick and dirty util to dump all module GFF resources as a bunch of
 *  text files.  More of a test program really.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ModDumper
{
    public static void main( String[] args ) throws Exception
    {
        if( args.length < 1 )
            {
            System.out.println( "Usage: ModDumper <mod file>" );
            System.out.println();
            System.out.println( "Where: <mod file> is the module file to dump." );
            return;
            }

        System.out.println( "--- Module Dumper version 0.1 ---" );

        long start = System.currentTimeMillis();

        FileInputStream fIn = new FileInputStream( args[0] );
        BufferedInputStream in = new BufferedInputStream( fIn, 65536 );
        int count = 0;
        long total = 0;

        try
            {
            ModReader m = new ModReader( in );

            ModReader.ResourceInputStream rIn = null;
            while( (rIn = m.nextResource()) != null )
                {
                String name = rIn.getResourceName();
                if( name.length() == 0 )
                    {
                    System.out.println( "Skipping empty resource entry.                         " );
                    continue;
                    }
                int type = rIn.getResourceType();
                if( !ResourceUtils.isGffType( type ) )
                    continue;

                name += "." + ResourceUtils.getExtensionForType(rIn.getResourceType()).toLowerCase();

                System.out.println( "-------Resource:" + name + "    size:" + rIn.getBytesLeft() );

                try
                    {
                    GffReader reader = new GffReader( rIn );

                    Struct root = reader.getRootStruct();
                    for( Iterator it = root.getValues().iterator(); it.hasNext(); )
                        {
                        Element e = (Element)it.next();
                        reader.printElement( e, "" );
                        }
                    }
                finally
                    {
                    rIn.close();
                    }
                //File f = new File( root, name );
                //System.out.print( "Writing:" + f + "  size:" + rIn.getBytesLeft()
                //                            + "                   \r" );
                //int size = dumpStream( f, rIn );

                //total += size;
                count++;
                }

            System.out.println( "Extracted " + count + " resources for a total of " + total + " bytes read.     " );
            }
        finally
            {
            in.close();
            }

        long totalTime = System.currentTimeMillis() - start;
        double secs = (double)totalTime / 1000.0;

        System.out.println( "\nTotal time:" + secs + " seconds." );
    }
}
