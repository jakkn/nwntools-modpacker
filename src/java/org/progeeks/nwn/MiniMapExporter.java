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

    public static final String[] usage = new String[] {
            "Usage: MiniMapExport <options> <area files>",
            "",
            "Where:",
            "",
            "<area files> are the extracted .are files that should be converted",
            "             to PNG images.",
            "",
            "<options> are one of the following:",
            "    -nwn <dir>     Specifies the main Neverwinter Nights install",
            "                   directory where the .key files can be found.",
            "                   Defaults to \\NeverwinterNights\\NWN",
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
            "                   sizes."
        };

    private static ResourceManager resMgr = new ResourceManager();

    private File nwnDir;
    private File destinationDir = new File( "." );
    private double scale = 1.0;
    private List areaFiles = new ArrayList();

    // Cache the tileset minimap tiles as we go
    private static Map tilesets = new HashMap();

    public MiniMapExporter()
    {
    }

    protected TilesetImages getTilesetImages( String tileset ) throws Exception
    {
        TilesetImages ti = (TilesetImages)tilesets.get( tileset );
        if( ti != null )
            return( ti );

        ti = new TilesetImages( tileset );
        tilesets.put( tileset, ti );

        return( ti );
    }

    protected Struct readArea( File area ) throws IOException
    {
        FileInputStream fIn = new FileInputStream( area );
        GffReader reader = new GffReader( fIn );
        try
            {
            return( reader.readRootStruct() );
            }
        finally
            {
            reader.close();
            }
    }

    protected void exportMiniMap( File area ) throws Exception
    {
        // Read the area
        System.out.println( "Loading area:" + area );
        Struct areaStruct = readArea( area );

        String name = area.getName().toLowerCase();
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
        TilesetImages tileImages = getTilesetImages( tilesetName );

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
                    continue;
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

    public void export() throws Exception
    {
        System.out.println( "Will export " + areaFiles.size() + " images." );
        System.out.println( "    using NWN in:" + ((nwnDir == null)?"/NeverwinterNights/NWN":nwnDir.toString()) );
        System.out.println( "    writing images to:" + destinationDir );
        System.out.println( "    scale: " + scale );

        System.out.println( "Loading NWN .key files..." );
        if( nwnDir == null )
            resMgr.loadDefaultKeys();
        else
            resMgr.loadDefaultKeys( nwnDir );

        System.out.println( "Exporting areas..." );
        for( Iterator i = areaFiles.iterator(); i.hasNext(); )
            {
            exportMiniMap( (File)i.next() );
            }
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
        System.out.println( "--- Mini-map Exporter version 0.1 ---" );

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
                }
            else if( arg.toLowerCase().endsWith( ".are" ) )
                {
                exporter.areaFiles.add( new File( arg ) );
                }
            else
                {
                System.out.println( "Error: Don't know how to handle arg:" + arg );
                System.out.println( "...ignoring." );
                }
            }

        exporter.export();
    }

    /**
     *  Caches the tileset images as they are required.
     */
    private static class TilesetImages
    {
        List tiles;
        List images = new ArrayList();

        public TilesetImages( String tileset ) throws IOException
        {
            tiles = (List)resMgr.getResource( new ResourceKey( tileset, ResourceTypes.TYPE_SET ) );
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
            res = res.toLowerCase();
            img = (Image)resMgr.getResource( new ResourceKey( res, ResourceTypes.TYPE_TGA ) );
            images.set( index, img );

            return( img );
        }

    }
}
