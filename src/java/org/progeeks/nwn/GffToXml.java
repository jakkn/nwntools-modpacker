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

import org.progeeks.meta.xml.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.*;
import org.progeeks.nwn.io.gff.*;
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
    private XmlPrintWriter writer;
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
        if( type == ResourceUtils.RES_MOD )
            {
            processModFile( f );
            return;
            }

        FileInputStream fIn = new FileInputStream( f );
        try
            {
            writeFile( f.getName(), fIn, type );
            }
        finally
            {
            fIn.close();
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

    protected void writeElements( List elements )
    {
        for( Iterator it = elements.iterator(); it.hasNext(); )
            {
            Object obj = it.next();
            if( obj instanceof Element )
                writeElement( (Element)obj );
            else if( obj instanceof Struct )
                writeStruct( (Struct)obj );
            else
                System.out.println( "Unknown object type: " + obj );
            }
    }

    protected void writeElement( Element el )
    {
        writer.pushTag( "element" );
        writer.printAttribute( "name", el.getName() );
        writer.printAttribute( "type", String.valueOf( el.getType() ) );

        if( el instanceof StructElement )
            {
            StructElement se = (StructElement)el;
            writeStruct( se.getStruct() );
            }
        else if( el instanceof ListElement )
            {
            ListElement ll = (ListElement)el;
            writeElements( ll.getValue() );
            }
        else if( el instanceof StringElement )
            {
            StringElement se = (StringElement)el;
            String val = se.getValue();
            // no attribute means no value
            if( val != null )
                {
                if( val.indexOf( '\r' ) < 0 && val.indexOf( '\n' ) < 0 )
                    {
                    writer.printAttribute( "value", val );
                    }
                else
                    {
                    writer.pushTag( "value" );
                    writer.startDataBlock();
                    writer.print( val );
                    writer.closeDataBlock();
                    writer.popTag();
                    }
                }
            }
        else
            {
            writer.printAttribute( "value", el.getStringValue() );
            }

        writer.popTag();
    }

    protected void writeStruct( Struct s )
    {
        writer.pushTag( "struct" );
        writer.printAttribute( "id", String.valueOf( s.getId() ) );

        writeElements( s.getValues() );

        writer.popTag();
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

        this.writer = new XmlPrintWriter( new FileWriter( f ) );

        try
            {
            writer.pushTag( ResourceUtils.getExtensionForType( type ).toLowerCase() );
            writer.printAttribute( "name", name );
            writeStruct( reader.getRootStruct() );
            writer.popTag();
            }
        finally
            {
            writer.close();
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

        System.out.println( "--- Gff To XML version 0.1 ---" );

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
