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
import org.progeeks.util.thread.*;


/**
 *  Reads XML files and saves them back out as GFF files.  This is
 *  the opposite of the GffToXml class.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class XmlToGff
{
    private File outDir;
    private byte[] transferBuff = new byte[65536];
    private GffXmlReader xmlReader = new GffXmlReader();
    private boolean compressGff = false;
    private boolean verbose = true;

    public XmlToGff()
    {
    }

    public XmlToGff( File outDir )
    {
        this.outDir = outDir;
    }

    public void setVerbose( boolean flag )
    {
        this.verbose = flag;
    }

    public void setCompressGff( boolean flag )
    {
        this.compressGff = flag;
    }

    public void setOutputDirectory( File outDir )
    {
        this.outDir = outDir;
    }

    public File getOutputFile( String name, int type )
    {
        return( new File( outDir, name ) );
    }

    public void processFile( File f ) throws IOException
    {
        if( f.isDirectory() )
            {
            return; // for now
            }

        int type = ResourceUtils.getTypeForFileName( f.getName() );
        //System.out.println( "type:" + type );
        //if( type == ResourceTypes.TYPE_MOD )
        //    {
        //    processModFile( f );
        //    return;
        //    }

        // Is it an XML file
        if( type == -1 && f.getName().toLowerCase().endsWith( ".xml" ) )
            {
            if( verbose )
                System.out.println( "Reading:" + f.getName() );
            FileReader fIn = new FileReader( f );
            BufferedReader bIn = new BufferedReader( fIn, 65536 );

            try
                {
                // Read the XML file
                Object obj = xmlReader.readObject( bIn );
                if( !(obj instanceof Struct) )
                    throw new RuntimeException( "Invalid XML GFF file:" + f );

                Struct root = (Struct)obj;
                String name = xmlReader.getName();

                if( verbose )
                    System.out.println( "Writing:" + name );

                File outFile = new File( outDir, name );

                FileOutputStream fOut = new FileOutputStream( outFile );
                BufferedOutputStream bOut = new BufferedOutputStream( fOut, 65536 );
                GffWriter out = new GffWriter( xmlReader.getType(), xmlReader.getVersion(), bOut );
                out.setShouldCompress( compressGff );
                out.writeStruct( root );
                }
            finally
                {
                bIn.close();
                }
            }
        else
            {
            File outFile = getOutputFile( f.getName(), type );

            FileInputStream fIn = new FileInputStream( f );
            BufferedInputStream bIn = new BufferedInputStream( fIn, 65536 );
            try
                {
                saveStream( outFile, bIn );
                }
            finally
                {
                bIn.close();
                }
            }
    }

    public long saveStream( File f, InputStream in ) throws IOException
    {
        System.out.println( "Copying:" + f.getName() );

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
/*
    public void writeFile( String name, InputStream in, int type ) throws IOException
    {
        File f = getOutputFile( name, type );
        if( !ResourceUtils.isGffType( type ) )
            {
            saveStream( f, in );
            return;
            }

        System.out.println( "Reading:" + name );

        // Process the GFF file using the GffReader
        GffReader reader = new GffReader( in );
        System.out.println( "Writing:" + f.getName() );
        FileOutputStream fOut = new FileOutputStream( f );
        BufferedOutputStream bOut = new BufferedOutputStream( fOut, 65536 );
        GffWriter out = new GffWriter( reader.getHeader().getType(),
                                       reader.getHeader().getVersion(),
                                       bOut );

        out.writeStruct( reader.getRootStruct() );
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
    }*/

    public static void main( String[] args ) throws Exception
    {
        if( args.length < 2 )
            {
            System.out.println( "Usage: XmlToGff [options] <destionation dir> <files>" );
            System.out.println();
            System.out.println( "Options:" );
            System.out.println( "    -compress  Will conslidate redundant elements within the file to" );
            System.out.println( "               reduce the resulting file size." );
            System.out.println();
            System.out.println( "Arguments:" );
            System.out.println( "    <destionation dir> is the output directory." );
            System.out.println( "    <files> is a collection of modules files that will be converted" );
            System.out.println( "        differently depending on their extension.  XML files will be" );
            System.out.println( "        converted to GFF and any non-XML files will be copied." );
            System.out.println( "        Sub-directories are currently ignored." );
            return;
            }

        //org.progeeks.util.log.Log.initialize();

        System.out.println( "--- XML To GFF version 0.6 ---" );

        long start = System.currentTimeMillis();

        boolean compress = false;
        int index;
        for( index = 0; index < args.length; index++ )
            {
            if( !args[index].startsWith( "-" ) )
                break;

            if( "-compress".equals( args[index] ) )
                {
                System.out.println( "Compression: on." );
                compress = true;
                }
            }
        File outDir = new File( args[index] );
        XmlToGff converter = new XmlToGff( outDir );
        converter.setCompressGff( compress );

        for( int i = index; i < args.length; i++ )
            {
            converter.processFile( new File( args[i] ) );
            }

        long totalTime = System.currentTimeMillis() - start;
        double secs = (double)totalTime / 1000.0;

        System.out.println( "\nTotal time:" + secs + " seconds." );
    }
}
