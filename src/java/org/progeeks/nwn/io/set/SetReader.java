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

package org.progeeks.nwn.io.set;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.*;
import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.resource.*;

/**
 *  NWN Area tileset reader.
 *  NOT a standard Java reader implementation.
 *
 *  Note: currently this reader only reads the ImageMap2D settings
 *  for each of the tiles.  More thorough SetReader functionality should be
 *  created in the future.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class SetReader
{
    private BufferedReader in;
    private List tiles = new ArrayList();
    private int count = 0;

    public SetReader( InputStream in ) throws IOException
    {
        this.in = new BufferedReader( new InputStreamReader( in ) );
    }

    public List readTiles() throws IOException
    {
        skipTo( "[TILES]" );

        String line = findLine( "Count=" );
        count = Integer.parseInt( line.substring( "Count=".length() ) );

        // Quick and dirty reading for now
        for( int i = 0; i < count; i++ )
            {
            skipTo( "[TILE" + i + "]" );
            line = findLine( "ImageMap2D=" );
            String mapImage = line.substring( "ImageMap2D=".length() );

            // Because it's nice and typeless
            Map tile = new HashMap();
            tile.put( "ImageMap2D", mapImage );

            tiles.add( tile );
            }

        return( tiles );
    }

    public void close() throws IOException
    {
        in.close();
    }

    private String findLine( String prefix ) throws IOException
    {
        String line = null;
        while( (line = in.readLine()) != null )
            {
            if( line.startsWith( prefix ) )
                return( line );
            }
        return( null );
    }

    private void skipTo( String s ) throws IOException
    {
        String line = null;
        while( (line = in.readLine()) != null )
            {
            if( s.equals( line.trim() ) )
                break;
            }
    }

    public static void main( String[] args ) throws IOException
    {
        ResourceManager resMgr = new ResourceManager();
        resMgr.loadDefaultKeys();

        for( int i = 0; i < args.length; i++ )
            {
            InputStream in = resMgr.getResourceStream( new ResourceKey( args[0], ResourceTypes.TYPE_SET ) );
            SetReader setReader = new SetReader( in );
            try
                {
                List tiles = setReader.readTiles();
System.out.println( "Tiles:" + tiles );
                }
            finally
                {
                setReader.close();
                }
            }
    }


}
/*
line:[TILES]
line:Count=283
line:
line:[TILE0]
line:Model=ttr01_a01_01
line:WalkMesh=msb01
line:TopLeft=Grass
line:TopLeftHeight=1
line:TopRight=Grass
line:TopRightHeight=1
line:BottomLeft=Grass
line:BottomLeftHeight=0
line:BottomRight=Grass
line:BottomRightHeight=1
line:Top=
line:Right=
line:Bottom=
line:Left=
line:MainLight1=1
line:MainLight2=1
line:SourceLight1=1
line:SourceLight2=1
line:AnimLoop1=1
line:AnimLoop2=1
line:AnimLoop3=1
line:Doors=0
line:Sounds=0
line:PathNode=B
line:Orientation=0
line:VisibilityNode=A
line:VisibilityOrientation=0
line:ImageMap2D=mitr01_A01

*/
