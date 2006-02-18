/*
 * $Id$
 *
 * Copyright (c) 2001-2002, Paul Speed
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

import org.progeeks.util.*;

import org.progeeks.nwn.io.*;
import org.progeeks.nwn.io.BinaryDataOutputStream;
import org.progeeks.nwn.io.itp.*;
import org.progeeks.nwn.itp.*;
import org.progeeks.nwn.resource.*;

/**
 *  Packs a set of files into a .mod file.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ModPacker
{
    /**
     *  The file to which the module will be written.
     */
    private File module;
    private BinaryDataOutputStream out;

    /**
     *  The type string for the type of file we're writing: HAK, ERF, or MOD
     */
    private String type;

    /**
     *  The description string that will be stored with the module.
     */
    private String description;

    /**
     *  The list of resource entries that will be saved.
     */
    private List   resources = new ArrayList();

    /**
     *  The total number of bytes in resources.
     */
    private int    resourceSize = 0;

    // Some variables that get written to the header.
    // We'll keep them for possible sanity checking
    private int    stringOffset;
    private int    resourceIndexOffset;
    private int    positionStructureOffset;

    private byte[] transferBuff = new byte[65536];

    private ProgressReporter reporter;

    public ModPacker( File module, String description )
    {
        this( module, description, null );
    }

    public ModPacker( File module, String description, ProgressReporter pr )
    {
        this.module = module;
        this.description = description;
        this.reporter = pr;

        // Get the file extension for the type
        String fileName = module.getName();
        int split = fileName.lastIndexOf( '.' );
        if( split > 0 )
            {
            type = fileName.substring( split + 1 ).toUpperCase();
            // Check to make sure it's a valid type
            if( !"MOD".equals( type ) && !"ERF".equals( type ) && !"HAK".equals(type)
                && !"SAV".equals( type ) )
                {
                // Not a valid type
                System.out.println( "Unknown type:" + type + "  Treating it as a MOD file." );
                type = null;
                }
            }

        if( type == null || type.length() < 3 )
            {
            type = "MOD";
            }
    }

    public void addFile( File f )
    {
        if( f.isDirectory() )
            {
            System.out.println( "Skipping:" + f );
            return;
            }
        ResourceIndex ri = new ResourceIndex( f, resources.size(), resourceSize );
        if( ri.type == -1 )
            {
            System.out.println( "Skipping:" + f + "  Unknown type." );
            return;
            }
        resourceSize += ri.size;
//System.out.println( "Adding:" + ri );
        resources.add( ri );
    }

    public int getResourceCount()
    {
        return( resources.size() );
    }

    /**
     *  Writes the module and its resources to disk.
     */
    public long writeModule() throws IOException
    {
        if( reporter != null )
            {
            reporter.setMaximum( resources.size() + 1 );
            reporter.setProgress( 1 );
            }

        BufferedOutputStream bOut = new BufferedOutputStream( new FileOutputStream(module), 65536 );
        out = new BinaryDataOutputStream( bOut );
        try
            {
            writeHeader();
            writeStrings();
            writeResourceIndex();
            writePositionTable();
            writeResources();

            long totalSize = out.getFilePosition();
            return( totalSize );
            }
        finally
            {
            out.close();
            }
    }

    private void writeHeader() throws IOException
    {
        byte[] temp = type.getBytes();
        byte[] typeBytes = new byte[] { temp[0], temp[1], temp[2], ' ' };
        out.write( typeBytes );

        byte[] ver = new byte[] { 'V', '1', '.', '0' };
        out.write( ver );

        int stringSize;
        if( description == null )
            {
            // No strings
            out.writeInt( 0 );
            stringSize = 0;
            out.writeInt( 0 ); // 0 strings, 0 size block
            }
        else
            {
            // Write the string count
            out.writeInt( 1 );

            // Write the string size (includes the 8 bytes for the
            // string structure
            stringSize = 0x8 + description.length();
            out.writeInt( stringSize );
            }

        out.writeInt( resources.size() );

        // Write the string offset, always the header size which
        // should always be 160.
        stringOffset = 160;
//System.out.println( "String offset:" + stringOffset );
        out.writeInt( stringOffset );

        // The resource offset will be after the strings
        resourceIndexOffset = stringOffset + stringSize;
//System.out.println( "Resource index offset:" + resourceIndexOffset );
        out.writeInt( resourceIndexOffset );

        // The resource index size is 32 bytes per resource
        // index entry.  Its after this that the position structures
        // start.
        int resourceIndexSize = resources.size() * 32;
        positionStructureOffset = resourceIndexOffset + resourceIndexSize;
//System.out.println( "Position offset:" + positionStructureOffset );
        out.writeInt( positionStructureOffset );

        // Now the year and day and crap
        Calendar cal = Calendar.getInstance();
//System.out.println( "Year:" + cal.get( Calendar.YEAR ) );
        out.writeInt( cal.get( Calendar.YEAR ) - 1900 );
//System.out.println( "Day:" + cal.get( Calendar.DAY_OF_YEAR ) );
        out.writeInt( cal.get( Calendar.DAY_OF_YEAR ) );

        // Don't know what this is for
        out.writeInt( -1 );

        // And padd out the header with another 116 bytes
        // AKA 29 UINTs ala the Torlack docs
        for( int i = 0; i < 116; i++ )
            out.write( (byte)0 );
    }

    private void writeString( String s, int language ) throws IOException
    {
        out.writeInt( language );
        out.writeInt( s.length() );
        out.writeBytes( s );
    }

    private void writeStrings() throws IOException
    {
        if( description == null )
            return;

        if( out.getFilePosition() != stringOffset )
            {
            throw new IOException( "Error: file position not where expected for string block.  "
                                   + out.getFilePosition() + " instead of " + stringOffset );
            }
        // Only one string to write.
        writeString( description, 0 );  // language - english
    }

    private void writeResourceIndex() throws IOException
    {
        if( out.getFilePosition() != resourceIndexOffset )
            {
            throw new IOException( "Error: file position not where expected for resource index.  "
                                   + out.getFilePosition() + " instead of " + resourceIndexOffset );
            }

        byte[] sBuff = new byte[16];

        for( Iterator i = resources.iterator(); i.hasNext(); )
            {
            ResourceIndex res = (ResourceIndex)i.next();

            // No matter how long the name actually is, we
            // write 16 bytes.

            byte[] nameBuff = res.name.getBytes();
            Arrays.fill( sBuff, (byte)0x0 );
            System.arraycopy( nameBuff, 0, sBuff, 0, Math.min( 16, nameBuff.length ) );

            out.write( sBuff );
            out.writeInt( res.index );
            out.writeInt( res.type );
            }

        // And for every resource index we wrote out, there is an
        // extra 8 bytes worth of padding.  Don't know what it's for.
        int pad = resources.size() * 8;
        for( int i = 0; i < pad; i++ )
            out.write( (byte)0 );
    }

    private void writePositionTable() throws IOException
    {
        if( out.getFilePosition() != positionStructureOffset )
            {
            throw new IOException( "Error: file position not where expected for position table.  "
                                   + out.getFilePosition() + " instead of " + positionStructureOffset );
            }

        // Each position structure is 8 bytes.  The total of
        // this block plus the current file position is needed
        // since the position table entries reference absolute file
        // positions.
        int tableSize = 8 * resources.size();
        int dataOffset = positionStructureOffset + tableSize;

        for( Iterator i = resources.iterator(); i.hasNext(); )
            {
            ResourceIndex res = (ResourceIndex)i.next();

            out.writeInt( res.offset + dataOffset );
            out.writeInt( res.size );
            }
    }

    private void writeResources() throws IOException
    {
        int index = 1;
        for( Iterator i = resources.iterator(); i.hasNext(); index++ )
            {
            ResourceIndex res = (ResourceIndex)i.next();
            if( reporter != null )
                {
                if( reporter.isCanceled() )
                    {
                    throw new InterruptedIOException( "Aborted by user." );
                    }
                reporter.setProgress( index );
                reporter.setMessage( "Storing:" + res.file.getName() );
                }

            FileInputStream fIn = new FileInputStream( res.file );
            BufferedInputStream in = new BufferedInputStream( fIn, 65536 );
            try
                {
                int bytes = writeResourceData( in );
                if( reporter == null )
                    System.out.print( "Packed resource file:" + res.file + "   " + bytes + " bytes written.        \r" );
                }
            finally
                {
                in.close();
                }
            }
    }

    private int writeResourceData( InputStream in ) throws IOException
    {
        int len = 0;
        int total = 0;
        while( (len = in.read( transferBuff )) >= 0 )
            {
            out.write( transferBuff, 0, len );
            total += len;
            }
        return( total );
    }

    public static String getModuleDescription( File ifo ) throws IOException
    {
        FileInputStream in = new FileInputStream(ifo);
        try
            {
            ItpReader itp = new ItpReader( in );
            ModuleInfo info = new ModuleInfo( itp.getRootElements() );

            return( info.getDescription() );
            }
        finally
            {
            in.close();
            }
    }

    public static void main( String[] args ) throws IOException
    {
        if( args.length < 2 )
            {
            System.out.println( "Usage: ModPacker <directory> <mod file>" );
            System.out.println();
            System.out.println( "Where: <directory> is the location of the files to pack." );
            System.out.println( "    and <mod file> is the file to create." );
            return;
            }

        System.out.println( "--- Module Packer version 1.0.0 ---" );

        long start = System.currentTimeMillis();

        File root = new File( args[0] );
        File module = new File( args[1] );

        // Check for the module.ifo resource
        File ifo = new File( root, "module.ifo" );
        File hakDesc = new File( root, "hak.description" );
        String description = "Test: Module packed with ModPacker.";
        if( ifo.exists() )
            {
            description = getModuleDescription( ifo );
            }
        else if( hakDesc.exists() )
            {
            description = StringUtils.readFile( hakDesc );
            }

        ModPacker packer = new ModPacker( module, description );
        File[] list = root.listFiles();
        for( int i = 0; i < list.length; i++ )
            {
            if( list[i].isDirectory() )
                continue;
            if( "hak.description".equals( list[i].getName() ) )
                continue;
            packer.addFile( list[i] );
            }

        System.out.println( "Writing resources to module file:" + module );

        // And go
        long size = packer.writeModule();
        System.out.println( size + " bytes written from " + packer.getResourceCount()
                            + " resources.                                 " );

        long total = System.currentTimeMillis() - start;
        double secs = (double)total / 1000.0;

        System.out.println( "\nTotal time:" + secs + " seconds." );
    }

    private class ResourceIndex
    {
        File   file;
        String name;
        int    index;
        int    type;
        int    offset;
        int    size;

        public ResourceIndex( File f, int index, int offset )
        {
            this.file = f;
            this.name = f.getName();
            String ext = "";
            int split = name.lastIndexOf( '.' );
            if( split >= 0 )
                {
                ext = name.substring( split + 1 ).toLowerCase();
                name = name.substring( 0, split );
                }
            this.index = index;
            this.type = ResourceUtils.getTypeForExtension( ext );
            this.size = (int)f.length();
            this.offset = offset;
        }

        public String toString()
        {
            return( "Resource:" + name + "  type:" + type + "  index:" + index + "  offset:" + offset
                        + "  size:" + size );
        }
    }
}

