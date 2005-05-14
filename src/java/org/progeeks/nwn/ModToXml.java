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

package org.progeeks.nwn;

import java.io.*;
import java.util.*;

/**
 *  Converts the files from a module into
 *  individual XML representations.  This is just a convenience
 *  wrapper for the GffToXml tool.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ModToXml
{
    public static void main( String[] args ) throws Exception
    {
        if( args.length < 2 )
            {
            System.out.println( "Usage: ModToXml <module file> <destionation dir>" );
            System.out.println();
            System.out.println( "Where: <mod file> is the module file to unpack and convert." );
            System.out.println( "    and <destination directory> is location where the" );
            System.out.println( "    files will be unpacked." );
            System.out.println( "    GFF files will be converted to XML." );
            System.out.println( "    All other files will be copied." );
            return;
            }

        System.out.println( "--- Gff To XML version 1.0.0 ---" );

        long start = System.currentTimeMillis();

        File inFile = new File( args[0] );
        File outDir = new File( args[1] );
        GffToXml converter = new GffToXml( outDir );

        converter.processFile( inFile );

        long totalTime = System.currentTimeMillis() - start;
        double secs = (double)totalTime / 1000.0;

        System.out.println( "\nTotal time:" + secs + " seconds." );
    }
}
