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

package org.progeeks.nwn.io;

import java.io.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.nwn.io.xml.*;
import org.progeeks.nwn.resource.*;

/**
 *  Some nice utility methods for loading and saving GFF resources
 *  as either gff or XML.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GffUtils
{
    public static Struct readGffXml( Reader in ) throws IOException
    {
        // We recreate every time to be thread-safe.
        GffXmlReader xmlReader = new GffXmlReader();

        // Read the XML file
        Object obj = xmlReader.readObject( in );
        if( !(obj instanceof Struct) )
            throw new RuntimeException( "Invalid XML GFF." );

        return( (Struct)obj );
    }

    public static Struct readGffXml( File f ) throws IOException
    {
        FileReader fIn = new FileReader( f );
        BufferedReader bIn = new BufferedReader( fIn, 65536 );
        try
            {
            return( readGffXml( bIn ) );
            }
        finally
            {
            bIn.close();
            }
    }

    public static void writeGffXml( ResourceKey key, Struct struct, Writer out ) throws IOException
    {
        String type = key.getTypeString() + " ";
        String version = GffWriter.GFF_VERSION;

        GffXmlWriter writer = new GffXmlWriter( key.getName(), type, version, out );
        try
            {
            writer.writeStruct( struct );
            }
        finally
            {
            out.close();
            }
    }

    public static void writeGffXml( ResourceKey key, Struct struct, File f ) throws IOException
    {
        FileWriter fOut = new FileWriter( f );
        BufferedWriter bOut = new BufferedWriter( fOut, 65536 );
        writeGffXml( key, struct, bOut );
    }

    public static Struct readGff( InputStream in ) throws IOException
    {
        GffReader reader = new GffReader( in );
        try
            {
            return( reader.readRootStruct() );
            }
        finally
            {
            reader.close();
            }
    }

    public static void writeGff( ResourceKey key, Struct struct, OutputStream out ) throws IOException
    {
        String type = key.getTypeString() + " ";
        type = type.substring( 0, 4 );

        GffWriter gff = new GffWriter( type, out );
        gff.writeStruct( struct );
    }

    public static void writeGff( ResourceKey key, Struct struct, File f ) throws IOException
    {
        FileOutputStream fOut = new FileOutputStream( f );
        BufferedOutputStream bOut = new BufferedOutputStream( fOut, 65536 );
        try
            {
            writeGff( key, struct, bOut );
            }
        finally
            {
            bOut.close();
            }
    }

}
