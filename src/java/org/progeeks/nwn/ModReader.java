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

import org.progeeks.nwn.io.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.util.ProgressReporter;
import org.progeeks.util.StringUtils;
import org.progeeks.util.thread.*;

/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ModReader
{
    private BinaryDataInputStream in;

    private byte[] type = new byte[4];
    private byte[] version = new byte[4];
    private int    stringCount = 0;
    private int    stringSize = 0;
    private int    resourceCount = 0;
    private int    stringOffset = 0;
    private int    resourceOffset = 0;
    private int    positionOffset = 0;
    private int    year;
    private int    day;
    private int    extra;
    private String description;
    private List   strings = new ArrayList();
    private List   resources = new ArrayList();
    private int    currentResource = 0;

    private static byte[] transferBuff = new byte[65536];

    public ModReader( InputStream in ) throws IOException
    {
        this.in = new BinaryDataInputStream( in );

        readHeader();
        readStrings();
        readResourceIndices();
        readPositions();
    }

    public int getResourceCount()
    {
        return( resources.size() );
    }

    public String getDescription()
    {
        return( description );
    }

    public String getType()
    {
        return( new String(type).trim() );
    }

    private void readHeader() throws IOException
    {
        in.readFully( type );
        in.readFully( version );

        stringCount = in.readInt();
        stringSize = in.readInt();
        resourceCount = in.readInt();
        stringOffset = in.readInt();
        resourceOffset = in.readInt();
        positionOffset = in.readInt();

        year = in.readInt();
        day = in.readInt();

        extra = in.readInt();
    }

    private void readStrings() throws IOException
    {
        // Go to the string location
        in.gotoPosition( stringOffset );

        for( int i = 0; i < stringCount; i++ )
            {
            int lang = in.readInt();
            int length = in.readInt();

            if( length + in.getFilePosition() > resourceOffset )
                {
                int oldLength = length;
                length = (int)(resourceOffset - in.getFilePosition());
                System.out.println( "Warning: File is corrupt.  String table does not fit in string block." );
                System.out.println( "         String[" + i + "] has been truncated from:" + oldLength + " to:"
                                    + length );
                }
            if( length == 0 )
                {
                System.out.println( "Warning: End of string table reached early." );
                return;
                }

            byte[] data = new byte[length];
            in.readFully( data );

            String s = new String(data);

            // The only reliable way to get the description.
            if( description == null )
                description = s;

            strings.add( s );
            }
    }

    private void readResourceIndices() throws IOException
    {
        // Go to the proper location
        in.gotoPosition( resourceOffset );

        byte[] buff = new byte[16];

        for( int i = 0; i < resourceCount; i++ )
            {
            in.readFully( buff );

            ResourceIndex ri = new ResourceIndex();
            ri.name = new String(buff).trim();
            ri.index = in.readInt();

            ri.type = in.readInt();

            if( ri.type == 0 )
                {
                System.out.println( "Warning: empty resource entry at index:" + i );
                System.out.println( "         name:" + ri.name );
                ri.index = i;
                }

            resources.add( ri );
            }

        // Sort the list
        //Collections.sort( resources );
    }

    private void readPositions() throws IOException
    {
        // Go to the proper location
        in.gotoPosition( positionOffset );

        int count = 0;
        for( Iterator i = resources.iterator(); i.hasNext(); )
            {
            ResourceIndex ri = (ResourceIndex)i.next();

            if( ri.index != count )
                {
                throw new IOException( "Ooops.  We're reading resource positions out of order:"
                                        + "ri.index:" + ri.index + "  index:" + count );
                }

            ri.offset = in.readInt();
            ri.size = in.readInt();

            count++;
            }
    }

    public ResourceInputStream nextResource() throws IOException
    {
        if( currentResource >= resources.size() )
            return( null );

        ResourceIndex ri = (ResourceIndex)resources.get(currentResource++);

        return( new ResourceInputStream(ri) );
    }

    public static int dumpStream( File output, InputStream in ) throws IOException
    {
        FileOutputStream fOut = new FileOutputStream(output);
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

    /**
     *  Need to combine the various extractModule() methods. -FIXME
     */
    public static void extractModule( File module, File destination, ProgressReporter pr ) throws IOException
    {
        FileInputStream fIn = new FileInputStream( module );
        BufferedInputStream in = new BufferedInputStream( fIn, 65536 );
        int count = 0;
        long total = 0;

        try
            {
            File root = destination;

            ModReader m = new ModReader( in );
            pr.setMaximum( m.getResourceCount() );

            ResourceInputStream rIn = null;
            while( (rIn = m.nextResource()) != null )
                {
                String name = rIn.getResourceName();
                pr.setMessage( "Extracting: " + name );
                pr.setProgress( count );
                if( pr.isCanceled() )
                    {
                    System.out.println( "User aborted." );
                    return;
                    }

                if( name.length() == 0 )
                    {
                    pr.setMessage( "Skipping empty resource entry." );
                    continue;
                    }
                name += "." + ResourceUtils.getExtensionForType(rIn.getResourceType()).toLowerCase();

                File f = new File( root, name );
                pr.setMessage( "Writing:" + f.getName() + " size:" + rIn.getBytesLeft() );

                int size = dumpStream( f, rIn );

                total += size;
                count++;
                }
            }
        finally
            {
            in.close();
            }
    }

    public static void extractModule( File module, File destination, boolean verbose ) throws IOException
    {
        FileInputStream fIn = new FileInputStream( module );
        BufferedInputStream in = new BufferedInputStream( fIn, 65536 );
        int count = 0;
        long total = 0;

        try
            {
            File root = destination;

            ModReader m = new ModReader( in );

            // If it has a description and is a HAK file then write the description
            // to a special file since .haks don't normally have anything like that.
            String type = m.getType();
            String description = m.getDescription();
            if( "HAK".equals(type) && description != null && description.length() > 0 )
                {
                String name = type.toLowerCase() + ".description";
                System.out.println( "Including " + name + " file containing the description." );
                StringUtils.writeFile( description, new File( root, name ) );
                }

            ResourceInputStream rIn = null;
            while( (rIn = m.nextResource()) != null )
                {
                String name = rIn.getResourceName();
                if( name.length() == 0 )
                    {
                    if( verbose )
                        System.out.println( "Skipping empty resource entry.                         " );
                    continue;
                    }
                name += "." + ResourceUtils.getExtensionForType(rIn.getResourceType()).toLowerCase();

                File f = new File( root, name );
                if( verbose )
                    {
                    System.out.print( "Writing:" + f + "  size:" + rIn.getBytesLeft()
                                                + "                   \r" );
                    }
                int size = dumpStream( f, rIn );

                total += size;
                count++;
                }

            if( verbose )
                System.out.println( "Extracted " + count + " resources for a total of " + total + " bytes.     " );
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
            System.out.println( "Usage: ModUnpacker <mod file> <destination directory>" );
            System.out.println();
            System.out.println( "Where: <mod file> is the module file to unpack." );
            System.out.println( "    and <destination directory> is location where the" );
            System.out.println( "    files will be unpacked." );
            return;
            }

        System.out.println( "--- Module Unpacker version 0.6 ---" );

        long start = System.currentTimeMillis();

        extractModule( new File( args[0] ), new File( args[1] ), true );

        long totalTime = System.currentTimeMillis() - start;
        double secs = (double)totalTime / 1000.0;

        System.out.println( "\nTotal time:" + secs + " seconds." );
    }

    private class ResourceIndex implements Comparable
    {
        String name;
        int    index;
        int    type;
        int    offset;
        int    size;

        public int compareTo( Object obj )
        {
            ResourceIndex ri = (ResourceIndex)obj;
            return( index - ri.index );
        }

        public String toString()
        {
            return( "Resource:" + name + "  type:" + type + "  index:" + index + "  offset:" + offset
                        + "  size:" + size );
        }
    }

    public class ResourceInputStream extends InputStream
    {
        private ResourceIndex ri;
        private int start;
        private int bytesLeft;

        public ResourceInputStream( ResourceIndex ri ) throws IOException
        {
            this.ri = ri;
            start = ri.offset;
            bytesLeft = ri.size;
            if( start > 0 )
                in.gotoPosition( start );
        }

        public String getResourceName()
        {
            return( ri.name );
        }

        public int getResourceType()
        {
            return( ri.type );
        }

        public int getBytesLeft()
        {
            return( bytesLeft );
        }

        public int read() throws IOException
        {
            if( bytesLeft <= 0 )
                return( -1 );
            int result = (in.readByte() & 0xff);
            bytesLeft--;
            return( result );
        }

        public int read( byte[] b ) throws IOException
        {
            if( bytesLeft == 0 )
                return( -1 );
            int len = Math.min( bytesLeft, b.length );
            return( read( b, 0, len ) );
        }

        public int read( byte[] b, int off, int len ) throws IOException
        {
            if( bytesLeft == 0 )
                return( -1 );
            len = Math.min( bytesLeft, len );
            int result = in.read( b, off, len );
            bytesLeft -= result;
            return( result );
        }
    }
}

