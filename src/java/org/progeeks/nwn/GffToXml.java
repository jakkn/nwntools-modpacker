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

import org.progeeks.meta.xml.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.nwn.io.xml.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.util.StringUtils;
import org.progeeks.util.thread.*;


/**
 *  Converts GFF files from a module or frome files into
 *  individual XML representations.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GffToXml
{
    private File outDir;
    private byte[] transferBuff = new byte[65536];

    public GffToXml( File outDir )
    {
        this.outDir = outDir;
    }

    public File getOutputFile( String name, int type )
    {
        if( ResourceUtils.isGffType( type ) )
            name = name + ".xml";

        return( new File( outDir, name ) );
    }

    public void processFile( File f ) throws IOException
    {
        if( f.isDirectory() )
            {
            return; // for now
            }

        int type = ResourceUtils.getTypeForFileName( f.getName() );
        if( type == ResourceTypes.TYPE_MOD
            || type == ResourceTypes.TYPE_ERF
            || type == ResourceTypes.TYPE_HAK )
            {
            processModFile( f );
            return;
            }

        FileInputStream fIn = new FileInputStream( f );
        BufferedInputStream bIn = new BufferedInputStream( fIn, 65536 );
        try
            {
            writeFile( f.getName(), bIn, type );
            }
        finally
            {
            bIn.close();
            }
    }

    public long saveStream( File f, InputStream in ) throws IOException
    {
        System.out.println( "Writing:" + f.getName() );

        FileOutputStream fOut = new FileOutputStream(f);
        BufferedOutputStream out = new BufferedOutputStream( fOut, 65536 );
        try
            {
            int count = 0;
            int total = 0;
            while( (count = in.read(transferBuff)) >= 0 )
                {
                out.write( transferBuff, 0, count );
                total += count;
                }

            return( total );
            }
        finally
            {
            out.close();
            }

    }

    public void writeFile( String name, InputStream in, int type ) throws IOException
    {
        File f = getOutputFile( name, type );
        if( !ResourceUtils.isGffType( type ) )
            {
            saveStream( f, in );
            return;
            }

        System.out.println( "Writing:" + f.getName() );

        // Process the GFF file using the GffReader
        GffReader reader = new GffReader( in );
        FileWriter fOut = new FileWriter( f );
        BufferedWriter bOut = new BufferedWriter( fOut, 65536 );
        GffXmlWriter out = new GffXmlWriter( name, reader.getHeader().getType(),
                                             reader.getHeader().getVersion(),
                                             bOut );
        try
            {
            out.writeStruct( reader.readRootStruct() );
            }
        finally
            {
            reader.close();
            out.close();
            }
    }

    public void processModFile( File f ) throws IOException
    {
        FileInputStream fIn = new FileInputStream( f );
        BufferedInputStream in = new BufferedInputStream( fIn, 65536 );
        int count = 0;
        long total = 0;

        System.out.println( "Extracting files from MOD file:" + f );
        try
            {
            ModReader m = new ModReader( in );

            // If it has a description and is a HAK file then write the description
            // to a special file since .haks don't normally have anything like that.
            String fileType = m.getType();
            String description = m.getDescription();
            if( "HAK".equals(fileType) && description != null && description.length() > 0 )
                {
                String name = fileType.toLowerCase() + ".description";
                File descFile = getOutputFile( name, -1 );
                System.out.println( "Including " + name + " file containing the description." );
                StringUtils.writeFile( description, descFile );
                }

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
                name += "." + ResourceUtils.getExtensionForType(rIn.getResourceType()).toLowerCase();

                //System.out.println( "-------Resource:" + name + "    size:" + rIn.getBytesLeft() );

                try
                    {
                    writeFile( name, rIn, type );
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
    }

    public static void main( String[] args ) throws Exception
    {
        if( args.length < 2 )
            {
            System.out.println( "Usage: GffToXml <destionation dir> <files>" );
            System.out.println();
            System.out.println( "Where: <destionation dir> is the output directory." );
            System.out.println( "<files> is a collection of modules files that will be" );
            System.out.println( "differently depending on their extension.  Any module files" );
            System.out.println( "will be extracted into individual XML files as if they were " );
            System.out.println( "passed on the command line.  GFF files will be converted to XML." );
            System.out.println( "All other files will be copied." );
            return;
            }

        System.out.println( "--- Gff To XML version 1.0.0 ---" );

        long start = System.currentTimeMillis();

        File outDir = new File( args[0] );
        GffToXml converter = new GffToXml( outDir );

        for( int i = 1; i < args.length; i++ )
            {
            converter.processFile( new File( args[i] ) );
            }

        long totalTime = System.currentTimeMillis() - start;
        double secs = (double)totalTime / 1000.0;

        System.out.println( "\nTotal time:" + secs + " seconds." );
    }
}
