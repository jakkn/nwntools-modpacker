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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.nwn.io.image.*;
import org.progeeks.nwn.io.set.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.util.log.*;

/**
 *  Command-line utility for extracting the minimaps from a module
 *  as PNG images.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class MiniMapExporter
{
    public static final String OPTION_NWN = "-nwn";
    public static final String OPTION_DESTINATION = "-d";
    public static final String OPTION_SCALE = "-scale";
    public static final String OPTION_HAK = "-hak";

    public static final String[] usage = new String[] {
            "Usage: MiniMapExport <options> <files>",
            "",
            "Where:",
            "",
            "<files> are any combination of extracted .are area files or .mod",
            "        module files.  The .are are converted to PNG images",
            "        containing the area's minimap.  For .mod files, PNG images",
            "        will be generated for every area in the module.",
            "        For .mod files, the HAK list is picked up automatically",
            "        from the module file.",
            "",
            "<options> are one of the following:",
            "    -nwn <dir>     Specifies the main Neverwinter Nights install",
            "                   directory where the .key files can be found.",
            "                   Defaults to " + ResourceManager.DEFAULT_NWN_DIR,
            "                   This is also where .HAK files are located when",
            "                   extracting area minimaps from a .mod file.",
            "",
            "    -d <dir>       Specifies the directory to which images will be",
            "                   written.  Defaults to the current directory.",
            "",
            "    -scale <scale> Specifies a magnification factor for the generated",
            "                   images.  Mini-map images can be pretty small so",
            "                   it is often necessary to scale them up.",
            "                   Scaling example option:",
            "                       -scale 2.0",
            "                   The above scale would double the mini-map image",
            "                   sizes.",
            "",
            "   -hak <file>     Specifies a HAK file that should be used when",
            "                   resolving tileset resources."
        };

    private static ResourceManager resourceManager = new ResourceManager();

    private File nwnDir = ResourceManager.DEFAULT_NWN_DIR;
    private File destinationDir = new File( "." );
    private double scale = 1.0;
    private List hakFiles = new ArrayList();
    private List areaFiles = new ArrayList();
    private List modFiles = new ArrayList();
    private Map modInfos = new HashMap();

    // Cache the tileset minimap tiles as we go
    private static Map tilesets = new HashMap();

    public MiniMapExporter()
    {
    }

    protected TilesetImages getTilesetImages( String tileset, ResourceManager resMgr ) throws Exception
    {
        TilesetImages ti = (TilesetImages)tilesets.get( tileset );
        if( ti != null )
            return( ti );

        ti = new TilesetImages( tileset, resMgr );
        tilesets.put( tileset, ti );

        return( ti );
    }

    /*protected Struct readArea( File area ) throws IOException
    {
        FileInputStream in = new FileInputStream( area );
        try
            {
            return( readArea( in ) );
            }
        finally
            {
            in.close();
            }
    }*/

    protected Struct readArea( InputStream in ) throws IOException
    {
        GffReader reader = new GffReader( in );
        return( reader.readRootStruct() );
    }

    protected void exportMiniMap( File area, ResourceManager resMgr ) throws Exception
    {
        FileInputStream in = new FileInputStream( area );
        try
            {
            exportMiniMap( area.getName(), in, resMgr );
            }
        finally
            {
            in.close();
            }
    }

    protected void exportMiniMap( String areaName, InputStream in, ResourceManager resMgr ) throws Exception
    {
        // Read the area
        System.out.println( "Loading area:" + areaName );
        Struct areaStruct = readArea( in );

        String name = areaName.toLowerCase();
        name = name.replaceAll( "\\.are", "\\.png" );
        File export = new File( destinationDir, name );
        System.out.println( "    output image:" + export );

        int width = areaStruct.getInt( "Width" );
        int height = areaStruct.getInt( "Height" );

        System.out.println( "    area size:" + width + " x " + height );

        BufferedImage miniMap = new BufferedImage( (int)((width * 16) * scale),
                                                   (int)((height * 16) * scale),
                                                   BufferedImage.TYPE_INT_ARGB );

        String tilesetName = areaStruct.getString( "Tileset" );
        System.out.println( "    tileset:" + tilesetName );
        TilesetImages tileImages = getTilesetImages( tilesetName, resMgr );

        System.out.println( "Generating image." );
        List tileList = areaStruct.getList( "Tile_List" );
        Iterator i = tileList.iterator();
        Graphics g = miniMap.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.scale( scale, -scale );
        for( int y = 0; y < height; y++ )
            {
            for( int x = 0; x < width; x++ )
                {
                Struct tile = (Struct)i.next();

                int tileId = tile.getInt( "Tile_ID" );
                int orient = tile.getInt( "Tile_Orientation" );
                int tileX = x * 16 + 8;
                int tileY = y * 16 + 8 - (height * 16);

                double rads = 0;
                switch( orient )
                    {
                    case 0: // 0
                        break;
                    case 1:
                        rads = Math.PI * 0.5;
                        break;
                    case 2:
                        rads = Math.PI;
                        break;
                    case 3:
                        rads = Math.PI * 1.5;
                        break;
                    }

                Image img = tileImages.getTileImage( tileId );
                if( img == null )
                    {
                    continue;
                    }
                g2.translate( tileX, tileY );
                g2.rotate( rads );
                g2.drawImage( img, -8, -8, null );
                g2.rotate( -rads );
                g2.translate( -tileX, -tileY );
                }
            }

        // Write the image to a file
        System.out.println( "Writing image:" + export );
        if( !ImageIO.write( miniMap, "png", export ) )
            System.out.println( "Image not written successfully." );
    }

    protected int scanModules() throws IOException
    {
        int areaCount = 0;

        for( Iterator i = modFiles.iterator(); i.hasNext(); )
            {
            File mod = (File)i.next();
            System.out.println( "Prescanning:" + mod );
            ModuleInfo info = new ModuleInfo( mod );
            modInfos.put( mod, info );
            areaCount += info.areaCount;
            }

        return( areaCount );
    }

    public void export() throws Exception
    {
        int imageCount = areaFiles.size();

        // Quickly scan the module files to pick-up HAK information
        // and images counts.  This could be done as the files are being
        // processed but is a good pre-validation step.
        imageCount += scanModules();

        System.out.println( "Will export " + imageCount + " images." );
        System.out.println( "    using NWN in:" + ((nwnDir == null)?"/NeverwinterNights/NWN":nwnDir.toString()) );
        System.out.println( "    writing images to:" + destinationDir );
        System.out.println( "    scale: " + scale );
        if( hakFiles.size() > 0 )
            System.out.println( "    HAK files: " + hakFiles );

        System.out.println( "Loading NWN .key files..." );
        if( nwnDir == null )
            resourceManager.loadDefaultKeys();
        else
            resourceManager.loadDefaultKeys( nwnDir );

        for( Iterator i = hakFiles.iterator(); i.hasNext(); )
            {
            File f = (File)i.next();
            System.out.println( "Loading:" + f );
            resourceManager.addEncapsulatedResourceFile( f );
            }

        System.out.println( "Exporting areas..." );
        for( Iterator i = areaFiles.iterator(); i.hasNext(); )
            {
            exportMiniMap( (File)i.next(), resourceManager );
            }

        for( Iterator i = modFiles.iterator(); i.hasNext(); )
            {
            File mod = (File)i.next();
            ModuleInfo info = (ModuleInfo)modInfos.get(mod);
            info.exportMiniMaps();
            }
    }

    public File searchForNwnPath() throws Exception
    {
        String keyName = "chitin.key";

        // First try the current working directory
        File key = new File( ".", keyName );
        if( key.exists() )
            return( key );

        // Now check the paths provided.
        Set paths = new HashSet();
        for( Iterator i = areaFiles.iterator(); i.hasNext(); )
            {
            File f = (File)i.next();
            paths.add( f.getParentFile().getAbsoluteFile() );
            }
        for( Iterator i = modFiles.iterator(); i.hasNext(); )
            {
            File f = (File)i.next();
            paths.add( f.getParentFile().getAbsoluteFile() );
            }

        for( Iterator i = paths.iterator(); i.hasNext(); )
            {
            File f = (File)i.next();
            while( f != null )
                {
                key = new File( f, keyName );
                if( key.exists() )
                    return( key );
                f = f.getParentFile();
                }
            }

        return( null );
    }

    public static void main( String[] args ) throws Exception
    {
        Log.initialize();

        if( args.length == 0 )
            {
            for( int i = 0; i < usage.length; i++ )
                System.out.println( usage[i] );
            return;
            }
        System.out.println( "--- Mini-map Exporter version 0.5 ---" );

        MiniMapExporter exporter = new MiniMapExporter();

        for( int i = 0; i < args.length; i++ )
            {
            String arg = args[i];
            if( arg.startsWith( "-" ) )
                {
                if( OPTION_NWN.equals( arg ) && i + 1 < args.length )
                    {
                    exporter.nwnDir = new File( args[i + 1] );
                    i++;
                    }
                else if( OPTION_DESTINATION.equals( arg ) && i + 1 < args.length )
                    {
                    exporter.destinationDir = new File( args[i + 1] );
                    i++;
                    }
                else if( OPTION_SCALE.equals( arg ) && i + 1 < args.length )
                    {
                    exporter.scale = Double.parseDouble( args[i + 1] );
                    i++;
                    }
                else if( OPTION_HAK.equals( arg ) && i + 1 < args.length )
                    {
                    exporter.hakFiles.add( new File( args[i + 1] ) );
                    i++;
                    }
                else
                    {
                    System.out.println( "Error: Don't know how to handle arg:" + arg );
                    System.out.println( "...ignoring." );
                    }
                }
            else if( arg.toLowerCase().endsWith( ".are" ) )
                {
                exporter.areaFiles.add( new File( arg ) );
                }
            else if( arg.toLowerCase().endsWith( ".mod" ) )
                {
                exporter.modFiles.add( new File( arg ) );
                }
            else
                {
                System.out.println( "Error: Don't know how to handle arg:" + arg );
                System.out.println( "...ignoring." );
                }
            }

        if( exporter.areaFiles.size() == 0 && exporter.modFiles.size() == 0 )
            {
            System.out.println( "No files to export." );
            return;
            }

        // Verify the NWN path
        File key = new File( exporter.nwnDir, "chitin.key" );
        if( !key.exists() )
            {
            // Attempt a quick search in the current directory and any
            // paths we might have been given.
            key = exporter.searchForNwnPath();

            if( key == null )
                {
                System.out.println( "*** Warning: NWN path does not seem to be correct.\n"
                                + "***          Could not locate:" + key + "\n"
                                + "***          Please check the -nwn option or specify one if the\n"
                                + "***          default " + ResourceManager.DEFAULT_NWN_DIR
                                + " is not correct." );
                }
            else
                {
                exporter.nwnDir = key.getParentFile();
                }
            }

        exporter.export();
    }

    /**
     *  Caches the tileset images as they are required.
     */
    private static class TilesetImages
    {
        ResourceManager resMgr;
        List tiles;
        List images = new ArrayList();

        public TilesetImages( String tileset, ResourceManager resMgr ) throws IOException
        {
            this.resMgr = resMgr;
            tiles = (List)resMgr.getResource( new ResourceKey( tileset, ResourceTypes.TYPE_SET ) );
            if( tiles == null )
                throw new RuntimeException( "Tileset not found:" + tileset );
            int size = tiles.size();
            for( int i = 0; i < size; i++ )
                images.add( null );
        }

        public Image getTileImage( int index ) throws IOException
        {
            Image img = (Image)images.get( index );
            if( img != null )
                return( img );

            Map tile = (Map)tiles.get( index );
            String res = (String)tile.get( "ImageMap2D" );
            if( res == null )
                {
                System.out.println( "No image specified for tile index:" + index );
                return( null );
                }
            res = res.toLowerCase();
            if( res.trim().length() == 0 || "mi_temp01".equals( res ) )
                return( null );
            img = (Image)resMgr.getResource( new ResourceKey( res, ResourceTypes.TYPE_TGA ) );
            images.set( index, img );

            if( img == null )
                {
                System.out.println( "Failed to load image:" + res );
                }
            return( img );
        }

    }

    /**
     *  Contains the area count and HAK list for a module.
     */
    private class ModuleInfo
    {
        File mod;
        int areaCount;
        List haks = new ArrayList();
        Struct info;

        public ModuleInfo( File mod ) throws IOException
        {
            this.mod = mod;

            BufferedInputStream in = new BufferedInputStream( new FileInputStream(mod) );
            try
                {
                ModReader modReader = new ModReader( in );
                ModReader.ResourceInputStream res = null;
                while( (res = modReader.nextResource()) != null )
                    {
                    int type = res.getResourceType();
                    String name = res.getResourceName();

                    if( type == ResourceTypes.TYPE_IFO && name.equals( "module" ) )
                        {
                        this.info = readStruct( res );
                        extractHakList();
                        continue;
                        }
                    if( type == ResourceTypes.TYPE_ARE )
                        areaCount++;
                    }
                }
            finally
                {
                in.close();
                }
        }

        /**
         *  Converts the list of haks in the info struct into a usable form.
         */
        protected void extractHakList()
        {
            ListElement hakList = (ListElement)info.getValue( "Mod_HakList" );
            if( hakList == null )
                return;
            List entries = hakList.getValue();
            for( Iterator i = entries.iterator(); i.hasNext(); )
                {
                Struct s = (Struct)i.next();
                String hak = s.getValue( "Mod_Hak" ).getStringValue();
                File f = new File( nwnDir, "hak/" + hak + ".hak" );
                haks.add( f );
                }
        }

        protected Struct readStruct( InputStream in ) throws IOException
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

        /**
         *  Goes through each .are resource and calls up to the outer
         *  class to process the stream into a PNG.
         */
        public void exportMiniMaps() throws Exception
        {
            // If we have HAK files... then we need to create our
            // own resource manager to load them so as not to effect
            // subsequent modules.  We will inherit the base resources
            // though.
            ResourceManager resMgr = resourceManager;
            if( haks.size() > 0 )
                {
                resMgr = (ResourceManager)resMgr.clone();
                for( Iterator i = haks.iterator(); i.hasNext(); )
                    {
                    File f = (File)i.next();
                    resMgr.addEncapsulatedResourceFile( f );
                    }
                }

            BufferedInputStream in = new BufferedInputStream( new FileInputStream(mod) );
            try
                {
                ModReader modReader = new ModReader( in );
                ModReader.ResourceInputStream res = null;
                while( (res = modReader.nextResource()) != null )
                    {
                    int type = res.getResourceType();
                    String name = res.getResourceName();
                    if( type == ResourceTypes.TYPE_ARE )
                        {
                        System.out.println( "Exporting area:" + name + " from module:" + mod );
                        exportMiniMap( name + ".are", res, resMgr );
                        }
                    }
                }
            finally
                {
                in.close();
                }

        }
    }
}
