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

package org.progeeks.nwn.resource;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.*;
import org.progeeks.nwn.io.key.*;
import org.progeeks.nwn.io.bif.*;
import org.progeeks.util.log.*;


/**
 *  Hooks up ResourceKeys to the streams that contain their data.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ResourceStreamer
{
    static Log log = Log.getLog( ResourceStreamer.class );

    /**
     *  Maps resource keys to specific stream implementations.
     */
    private Map resources = new HashMap();

    /**
     *  Returns the InputStream for the specified resource key.
     */
    public InputStream getResourceStream( ResourceKey key )
    {
        try
            {
            ResourceIndex index = (ResourceIndex)resources.get( key );
            return( index.getResourceStream() );
            }
        catch( IOException e )
            {
            e.printStackTrace();
            return( null );
            }
    }

    /**
     *  Adds the specified key file to this resource streamer's
     *  resource index.
     */
    public void addKeyFile( File keyFile ) throws IOException
    {
        FileInputStream fIn = new FileInputStream( keyFile );
        try
            {
            File parent = keyFile.getParentFile();
            KeyReader reader = new KeyReader( fIn );
            for( Iterator i = reader.getFiles().iterator(); i.hasNext(); )
                {
                KeyReader.FileEntry file = (KeyReader.FileEntry)i.next();
                for( Iterator j = file.getKeyEntries().iterator(); j.hasNext(); )
                    {
                    KeyReader.KeyEntry key = (KeyReader.KeyEntry)j.next();

                    resources.put( key.getKey(), new BifIndex( new File( parent, file.getFileName() ),
                                                               key.getResourceId() ) );
                    }
                }
            }
        finally
            {
            fIn.close();
            }
    }

    /**
     *  Adds the specified directory to this resource streamer's
     *  resource index.
     */
    public void addResourceDirectory( File directory )
    {
        File[] list = directory.listFiles();
        for( int i = 0; i < list.length; i++ )
            {
            if( list[i].isDirectory() )
                continue;

            int type = ResourceUtils.getTypeForFileName( list[i].getName() );
            if( type < 0 )
                continue;

            addResourceFile( list[i], type );
            }
    }

    /**
     *  Adds the specified resource file to this resource streamer's
     *  resource index determining the type from the file's extension.
     */
    public void addResourceFile( File resource )
    {
        int type = ResourceUtils.getTypeForFileName( resource.getName() );
        if( type < 0 )
            throw new RuntimeException( "Unknown resource type for file:" + resource );
        addResourceFile( resource, type );
    }

    /**
     *  Adds the specified resource file to this resource streamer's
     *  resource index using the specified resource type.
     */
    public void addResourceFile( File resource, int type )
    {
        String name = resource.getName();
        // Chop off the extension... it will always have one.
        int split = name.lastIndexOf( '.' );
        name = name.substring( 0, split ).toLowerCase();

        resources.put( new ResourceKey( name, type ), new DirectIndex( resource ) );
    }

    /**
     *  Adds the specified ERF file to this resource manager's
     *  resouce index.  I'd actually rather avoid implementing this
     *  method since I don't think it will be used.
     */
    public void addModule( File erfFile ) throws IOException
    {
    }

    public void loadDefaultKeys() throws IOException
    {
        long startTime = System.currentTimeMillis();

        // Check for the default location of standard NWN key files
        // FIXME - this check should be a bit more advanced, maybe check
        //         user preferences.
        File nwn = new File( "/NeverwinterNights/NWN" );
        if( nwn.exists() && nwn.isDirectory() )
            {
            System.out.println( "Indexing Neverwinter Nights key files..." );
            File[] keys = nwn.listFiles( new FileFilter()
                            {
                                public boolean accept( File f )
                                {
                                    if( f.getName().toLowerCase().endsWith( ".key" ) )
                                        return( true );
                                    return( false );
                                }
                            } );

            for( int i = 0; i < keys.length; i++ )
                {
                addKeyFile( keys[i] );
                }

            long endTime = System.currentTimeMillis();
            if( log.isInfoEnabled() )
                {
                log.info( "Indexed default keys files in: " + (endTime - startTime) + " ms" );
System.out.println( "Indexed default keys files in: " + (endTime - startTime) + " ms" );
                }
            }
    }

    public static void main( String[] args ) throws Exception
    {
        long startTime = System.currentTimeMillis();

        ResourceStreamer streamer = new ResourceStreamer();
        streamer.loadDefaultKeys();

        System.out.println( "Indexing user specified files..." );
        List resources = new ArrayList();
        for( int i = 0; i < args.length; i++ )
            {
            if( args[i].equals( "-r" ) && i < args.length - 1 )
                {
                String resource = args[i+1];
                int split = resource.lastIndexOf( '.' );
                String name = resource.substring( 0, split );
                String ext = resource.substring( split + 1 );
                resources.add( new ResourceKey( name, ResourceUtils.getTypeForExtension( ext ) ) );
                i++;
                }
            else
                {
                File f = new File( args[i] );
                if( args[i].toLowerCase().endsWith( ".key" ) )
                    streamer.addKeyFile( f );
                else if( args[i].toLowerCase().endsWith( ".mod" ) )
                    streamer.addModule( f );
                else if( f.isDirectory() )
                    streamer.addResourceDirectory( f );
                else
                    streamer.addResourceFile( f );
                }
            }

        long endTime = System.currentTimeMillis();
        System.out.println( "Index time:" + (endTime - startTime) + " ms" );

        // Now try to lookup and read any specified resources
        for( Iterator i = resources.iterator(); i.hasNext(); )
            {
            ResourceKey key = (ResourceKey)i.next();
            System.out.println( "Looking up:" + key );
            InputStream in = streamer.getResourceStream( key );
            try
                {
                System.out.println( "Stream:" + in );
                int b;
                while( (b = in.read()) != -1 )
                    {
                    System.out.print( (char)b );
                    }
                }
            finally
                {
                in.close();
                }
            }

    }

    /**
     *  Base class for objects that know how to locate the
     *  data for a given resource.
     */
    private static abstract class ResourceIndex implements Serializable
    {
        static final long serialVersionUID = 42L;

        /**
         *  Returns the input stream for this resouce's data.
         */
        public abstract InputStream getResourceStream() throws IOException;
    }

    /**
     *  Provides direct indexing to a resource file.
     */
    private static class DirectIndex extends ResourceIndex
    {
        private File file;

        protected DirectIndex( File file )
        {
            this.file = file;
        }

        /**
         *  Returns the input stream for this resouce's data.
         */
        public InputStream getResourceStream() throws IOException
        {
            return( new FileInputStream( file ) );
        }
    }

    /**
     *  Provides indexing into a BIF file.
     */
    private static class BifIndex extends ResourceIndex
    {
        private File file;
        private int  id;

        protected BifIndex( File file, int id )
        {
            this.file = file;
            this.id = id;
        }

        /**
         *  Returns the input stream for this resouce's data.
         */
        public InputStream getResourceStream() throws IOException
        {
System.out.println( "Opening BIF file:" + file + "   index:" + (id & 0x3fff) );
            FileInputStream fIn = new FileInputStream( file );
            BifReader reader = new BifReader( new BufferedInputStream( fIn, 32768 ) );
            InputStream resIn = reader.getResource( id );

            return( new ParentClosingStream( resIn, fIn ) );
        }
    }

    /**
     *  Filter stream implementation that can close a parent stream
     *  when the stream is closed.
     */
    private static class ParentClosingStream extends FilterInputStream
    {
        private InputStream parent;

        public ParentClosingStream( InputStream in, InputStream parent )
        {
            super( in );
            this.parent = parent;
        }

        public void close() throws IOException
        {
            super.close();
            parent.close();
        }
    }
}

