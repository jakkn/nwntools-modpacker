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

package org.progeeks.nwn.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.nwn.resource.*;

/**
 *  A simple area viewer that uses the in-game maps to view the
 *  area.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class TestAreaViewer extends JFrame
{
    private static ResourceManager resMgr = new ResourceManager();

    private Struct areaStruct;
    private BufferedImage image;
    private List tileset;
    private List images = new ArrayList();

    public TestAreaViewer( File area ) throws IOException
    {
        super( "Area Viewer" );
        setDefaultCloseOperation( EXIT_ON_CLOSE );

        setSize( 600, 600 );

        long startTime = System.currentTimeMillis();
        readArea( area );
        drawArea();
        long endTime = System.currentTimeMillis();

        System.out.println( "Load and render time:" + (endTime - startTime) );

        getContentPane().add( new JLabel( new ImageIcon( image ) ) );
    }

    protected void readArea( File area ) throws IOException
    {
        FileInputStream fIn = new FileInputStream( area );
        GffReader reader = new GffReader( fIn );
        try
            {
            areaStruct = reader.readRootStruct();
            }
        finally
            {
            reader.close();
            }
    }

    protected List getTileset( String tileset ) throws IOException
    {
        List tiles = (List)resMgr.getResource( new ResourceKey( tileset, ResourceTypes.TYPE_SET ) );
        int size = tiles.size();
        for( int i = 0; i < size; i++ )
            images.add( null );
        return( tiles );
    }

    protected Image getTileImage( int index ) throws IOException
    {
        Image img = (Image)images.get( index );
        if( img != null )
            return( img );

        Map tile = (Map)tileset.get( index );
        String res = (String)tile.get( "ImageMap2D" );
        res = res.toLowerCase();
System.out.println( "Res:" + res );
        img = (Image)resMgr.getResource( new ResourceKey( res, ResourceTypes.TYPE_TGA ) );
        images.set( index, img );
        return( img );
    }

    protected void drawArea() throws IOException
    {
        int width = areaStruct.getInt( "Width" );
        int height = areaStruct.getInt( "Height" );

        System.out.println( "Size:" + width + " x " + height );

        image = new BufferedImage( (width * 16) * 2, (height * 16) * 2, BufferedImage.TYPE_INT_ARGB );
        //image = new BufferedImage( (width * 17) * 2, (height * 17) * 2, BufferedImage.TYPE_INT_ARGB );

        String tilesetName = areaStruct.getString( "Tileset" );
        System.out.println( "Tileset:" + tilesetName );

        tileset = getTileset( tilesetName );

        List tileList = areaStruct.getList( "Tile_List" );
        Iterator i = tileList.iterator();
        Graphics g = image.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.scale( 2, -2 );
        for( int y = 0; y < height; y++ )
            {
            for( int x = 0; x < width; x++ )
                {
                Struct tile = (Struct)i.next();

                int tileId = tile.getInt( "Tile_ID" );
                int orient = tile.getInt( "Tile_Orientation" );
                int tileX = x * 16 + 8;
                int tileY = y * 16 + 8 - (height * 16);
                //int tileX = x * 17 + 8;
                //int tileY = y * 17 + 8 - (height * 17);

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

                Image img = getTileImage( tileId );
                //g.drawString( String.valueOf(tileId), x * 16, y * 16 );
                g2.translate( tileX, tileY );
                g2.rotate( rads );
                g2.drawImage( img, -8, -8, null );
                g2.rotate( -rads );
                g2.translate( -tileX, -tileY );
                }
            }
        //System.out.println( "Tile:" + tiles );
    }

/*
            <struct id="1" >
                <element name="Tile_ID" type="5" value="38" />
                <element name="Tile_Orientation" type="5" value="2" />
                <element name="Tile_Height" type="5" value="0" />
                <element name="Tile_MainLight1" type="0" value="0" />
                <element name="Tile_MainLight2" type="0" value="0" />
                <element name="Tile_SrcLight1" type="0" value="2" />
                <element name="Tile_SrcLight2" type="0" value="2" />
                <element name="Tile_AnimLoop1" type="0" value="1" />
                <element name="Tile_AnimLoop2" type="0" value="1" />
                <element name="Tile_AnimLoop3" type="0" value="1" />
            </struct>
*/

    public static void main( String[] args ) throws IOException
    {
        org.progeeks.util.log.Log.initialize();

        resMgr.loadDefaultKeys();

        File f = new File( args[0] );
        TestAreaViewer viewer = new TestAreaViewer( f );
        viewer.setVisible( true );
    }
}
