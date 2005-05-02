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

package org.progeeks.nwn.io.image;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import java.io.*;

import org.progeeks.nwn.io.*;

/**
 *  A simple reader for type 2 targa files.
 *  NOT a standard Java reader.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class TgaReader
{
    private BinaryDataInputStream in;
    private BufferedImage image;

    public TgaReader( InputStream in ) throws IOException
    {
        this.in = new BinaryDataInputStream( in );
    }

    public Image readImage() throws IOException
    {
        readTargaData();
        return( image );
    }

    public void close() throws IOException
    {
        in.close();
    }

    private void readTargaData() throws IOException
    {
        int idLen = in.readUnsignedByte();
        int cmapType = in.readUnsignedByte();
        int type = in.readUnsignedByte();

        int cmapOffset = in.readUnsignedShort();
        int cmapLength = in.readUnsignedShort();
        int entrySize = in.readUnsignedByte();

        int xOrigin = in.readUnsignedShort();
        int yOrigin = in.readUnsignedShort();
        int width = in.readUnsignedShort();
        int height = in.readUnsignedShort();

        int pixelSize = in.readUnsignedByte();
        if( pixelSize != 24 && pixelSize != 32 )
            throw new RuntimeException( "Currently only support 24 and 32 bit images.  Read bit size:" + pixelSize );
        int descriptor = in.readUnsignedByte();

        byte[] idField = new byte[idLen];
        if( idLen > 0 )
            in.readFully( idField );

        int skip = (entrySize >> 3) * cmapLength;
        in.skip( skip );

        switch( pixelSize )
            {
            case 24:
                {
                image = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );

                byte[] pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();

                readImageData( width, height, type, pixels );
                }
                break;
            case 32:
                {
                image = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );

                byte[] pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();

                readImageData32( width, height, type, pixels );
                }
                break;
            }

        /*System.out.println( "Position:"+ in.getFilePosition() );

        System.out.println( "idLen:" + idLen );
        System.out.println( "cmapType:" + cmapType );
        System.out.println( "type:" + type );
        System.out.println( "cmapOffset:" + cmapOffset );
        System.out.println( "cmapLength:" + cmapLength );
        System.out.println( "entrySize:" + entrySize );

        System.out.println( "Image:" + xOrigin + ", " + yOrigin + "   " + width + " x " + height );
        System.out.println( "pixel size:" + pixelSize );
        System.out.println( "desciptor byte:" + Integer.toHexString( descriptor ) );*/

    }

    protected void readImageData( int width, int height, int type, byte[] pixels ) throws IOException
    {
        switch( type )
            {
            case 2:
                in.readFully( pixels );
                break;
            case 10:
                readCompressedImage( width, height, type, pixels );
                break;
            default:
                throw new UnsupportedOperationException( "Only type 2 and type 10 targa files"
                                                         + " are supported, read type:" + type );
            }
    }

    protected void readImageData32( int width, int height, int type, byte[] pixels ) throws IOException
    {
        switch( type )
            {
            case 2:
                in.readFully( pixels );
                break;
            case 10:
                readCompressedImage32( width, height, type, pixels );
                break;
            default:
                throw new UnsupportedOperationException( "Only type 2 and type 10 targa files"
                                                         + " are supported, read type:" + type );
            }
    }

    protected void readCompressedImage( int width, int height, int type, byte[] pixels ) throws IOException
    {
        // We only support 24 bit imagery at the moment
        byte[] color = new byte[3];

        int bPos = 0;
        while( bPos < pixels.length )
            {
            int b = in.readByte();
            int packetType = b & 0x80; // high order bit
            int count = (b & 0x7f) + 1;

            if( packetType > 0 )
                {
                // Run-length encoded packet
                in.readFully( color );
                for( int i = 0; i < count; i++ )
                    {
                    pixels[bPos++] = color[0];
                    pixels[bPos++] = color[1];
                    pixels[bPos++] = color[2];
                    }
                }
            else
                {
                // Raw unencoded packet
                for( int i = 0; i < count; i++ )
                    {
                    in.readFully( color );
                    pixels[bPos++] = color[0];
                    pixels[bPos++] = color[1];
                    pixels[bPos++] = color[2];
                    }
                }
            }
        /*
        Image Data Field.

        This field specifies (width) x (height) pixels.  The
        RGB color information for the pixels is stored in
        packets.  There are two types of packets:  Run-length
        encoded packets, and raw packets.  Both have a 1-byte
        header, identifying the type of packet and specifying a
        count, followed by a variable-length body.
        The high-order bit of the header is "1" for the
        run length packet, and "0" for the raw packet.

        For the run-length packet, the header consists of:
            __________________________________________________
            | 1 bit |   7 bit repetition count minus 1.      |
            |   ID  |   Since the maximum value of this      |
            |       |   field is 127, the largest possible   |
            |       |   run size would be 128.               |
            |-------|----------------------------------------|
            |   1   |  C     C     C     C     C     C    C  |
            --------------------------------------------------

        For the raw packet, the header consists of:
            __________________________________________________
            | 1 bit |   7 bit number of pixels minus 1.      |
            |   ID  |   Since the maximum value of this      |
            |       |   field is 127, there can never be     |
            |       |   more than 128 pixels per packet.     |
            |-------|----------------------------------------|
            |   0   |  N     N     N     N     N     N    N  |
            --------------------------------------------------


        For the run length packet, the header is followed by
        a single color value, which is assumed to be repeated
        the number of times specified in the header.  The
        packet may cross scan lines ( begin on one line and end
        on the next ).

        For the raw packet, the header is followed by
        the number of color values specified in the header.

        The color entries themselves are two bytes, three bytes,
        or four bytes ( for Targa 16, 24, and 32 ), and are
        broken down as follows:

        The 2 byte entry -
        ARRRRRGG GGGBBBBB, where each letter represents a bit.
        But, because of the lo-hi storage order, the first byte
        coming from the file will actually be GGGBBBBB, and the
        second will be ARRRRRGG. "A" represents an attribute bit.

        The 3 byte entry contains 1 byte each of blue, green,
        and red.

        The 4 byte entry contains 1 byte each of blue, green,
        red, and attribute.  For faster speed (because of the
        hardware of the Targa board itself), Targa 24 image are
        sometimes stored as Targa 32 images.
        */

    }

    protected void readCompressedImage32( int width, int height, int type, byte[] pixels ) throws IOException
    {
        // This is for 32 bit imagery
        byte[] color = new byte[4];

        int bPos = 0;
        while( bPos < pixels.length )
            {
            int b = in.readByte();
            int packetType = b & 0x80; // high order bit
            int count = (b & 0x7f) + 1;

            if( packetType > 0 )
                {
                // Run-length encoded packet
                in.readFully( color );
                for( int i = 0; i < count; i++ )
                    {
                    pixels[bPos++] = color[3];
                    pixels[bPos++] = color[0];
                    pixels[bPos++] = color[1];
                    pixels[bPos++] = color[2];
                    }
                }
            else
                {
                // Raw unencoded packet
                for( int i = 0; i < count; i++ )
                    {
                    in.readFully( color );
                    pixels[bPos++] = color[3];
                    pixels[bPos++] = color[0];
                    pixels[bPos++] = color[1];
                    pixels[bPos++] = color[2];
                    }
                }
            }
    }


    public static void main( String[] args ) throws IOException
    {
        FileInputStream fIn = new FileInputStream( args[0] );
        TgaReader reader = new TgaReader( fIn );


        //Toolkit tk = Toolkit.getDefaultToolkit();
        //Image img = tk.createImage( args[0] );

        JFrame frame = new JFrame( "Test" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setSize( 600, 600 );
        Container c = frame.getContentPane();
        c.add( new JLabel( new ImageIcon( reader.image ) ) );

        frame.setVisible( true );
    }
}

/*
--------------------------------------------------------------------------------
DATA TYPE 2:  Unmapped RGB images.                                             |
_______________________________________________________________________________|
| Offset | Length |                     Description                            |
|--------|--------|------------------------------------------------------------|
|--------|--------|------------------------------------------------------------|
|    0   |     1  |  Number of Characters in Identification Field.             |
|        |        |                                                            |
|        |        |  This field is a one-byte unsigned integer, specifying     |
|        |        |  the length of the Image Identification Field.  Its value  |
|        |        |  is 0 to 255.  A value of 0 means that no Image            |
|        |        |  Identification Field is included.                         |
|        |        |                                                            |
|--------|--------|------------------------------------------------------------|
|    1   |     1  |  Color Map Type.                                           |
|        |        |                                                            |
|        |        |  This field contains either 0 or 1.  0 means no color map  |
|        |        |  is included.  1 means a color map is included, but since  |
|        |        |  this is an unmapped image it is usually ignored.  TIPS    |
|        |        |  ( a Targa paint system ) will set the border color        |
|        |        |  the first map color if it is present.                     |
|        |        |                                                            |
|--------|--------|------------------------------------------------------------|
|    2   |     1  |  Image Type Code.                                          |
|        |        |                                                            |
|        |        |  This field will always contain a binary 2.                |
|        |        |  ( That's what makes it Data Type 2 ).                     |
|        |        |                                                            |
|--------|--------|------------------------------------------------------------|
|    3   |     5  |  Color Map Specification.                                  |
|        |        |                                                            |
|        |        |  Ignored if Color Map Type is 0; otherwise, interpreted    |
|        |        |  as follows:                                               |
|        |        |                                                            |
|    3   |     2  |  Color Map Origin.                                         |
|        |        |  Integer ( lo-hi ) index of first color map entry.         |
|        |        |                                                            |
|    5   |     2  |  Color Map Length.                                         |
|        |        |  Integer ( lo-hi ) count of color map entries.             |
|        |        |                                                            |
|    7   |     1  |  Color Map Entry Size.                                     |
|        |        |  Number of bits in color map entry.  16 for the Targa 16,  |
|        |        |  24 for the Targa 24, 32 for the Targa 32.                 |
|        |        |                                                            |
|--------|--------|------------------------------------------------------------|
|    8   |    10  |  Image Specification.                                      |
|        |        |                                                            |
|    8   |     2  |  X Origin of Image.                                        |
|        |        |  Integer ( lo-hi ) X coordinate of the lower left corner   |
|        |        |  of the image.                                             |
|        |        |                                                            |
|   10   |     2  |  Y Origin of Image.                                        |
|        |        |  Integer ( lo-hi ) Y coordinate of the lower left corner   |
|        |        |  of the image.                                             |
|        |        |                                                            |
|   12   |     2  |  Width of Image.                                           |
|        |        |  Integer ( lo-hi ) width of the image in pixels.           |
|        |        |                                                            |
|   14   |     2  |  Height of Image.                                          |
|        |        |  Integer ( lo-hi ) height of the image in pixels.          |
|        |        |                                                            |
|   16   |     1  |  Image Pixel Size.                                         |
|        |        |  Number of bits in a pixel.  This is 16 for Targa 16,      |
|        |        |  24 for Targa 24, and .... well, you get the idea.         |
|        |        |                                                            |
|   17   |     1  |  Image Descriptor Byte.                                    |
|        |        |  Bits 3-0 - number of attribute bits associated with each  |
|        |        |             pixel.  For the Targa 16, this would be 0 or   |
|        |        |             1.  For the Targa 24, it should be 0.  For     |
|        |        |             Targa 32, it should be 8.                      |
|        |        |  Bit 4    - reserved.  Must be set to 0.                   |
|        |        |  Bit 5    - screen origin bit.                             |
|        |        |             0 = Origin in lower left-hand corner.          |
|        |        |             1 = Origin in upper left-hand corner.          |
|        |        |             Must be 0 for Truevision images.               |
|        |        |  Bits 7-6 - Data storage interleaving flag.                |
|        |        |             00 = non-interleaved.                          |
|        |        |             01 = two-way (even/odd) interleaving.          |
|        |        |             10 = four way interleaving.                    |
|        |        |             11 = reserved.                                 |
|        |        |                                                            |
|--------|--------|------------------------------------------------------------|
|   18   | varies |  Image Identification Field.                               |
|        |        |  Contains a free-form identification field of the length   |
|        |        |  specified in byte 1 of the image record.  It's usually    |
|        |        |  omitted ( length in byte 1 = 0 ), but can be up to 255    |
|        |        |  characters.  If more identification information is        |
|        |        |  required, it can be stored after the image data.          |
|        |        |                                                            |
|--------|--------|------------------------------------------------------------|
| varies | varies |  Color map data.                                           |
|        |        |                                                            |
|        |        |  If the Color Map Type is 0, this field doesn't exist.     |
|        |        |  Otherwise, just read past it to get to the image.         |
|        |        |  The Color Map Specification describes the size of each    |
|        |        |  entry, and the number of entries you'll have to skip.     |
|        |        |  Each color map entry is 2, 3, or 4 bytes.                 |
|        |        |                                                            |
|--------|--------|------------------------------------------------------------|
| varies | varies |  Image Data Field.                                         |
|        |        |                                                            |
|        |        |  This field specifies (width) x (height) pixels.  Each     |
|        |        |  pixel specifies an RGB color value, which is stored as    |
|        |        |  an integral number of bytes.                              |
|        |        |                                                            |
|        |        |  The 2 byte entry is broken down as follows:               |
|        |        |  ARRRRRGG GGGBBBBB, where each letter represents a bit.    |
|        |        |  But, because of the lo-hi storage order, the first byte   |
|        |        |  coming from the file will actually be GGGBBBBB, and the   |
|        |        |  second will be ARRRRRGG. "A" represents an attribute bit. |
|        |        |                                                            |
|        |        |  The 3 byte entry contains 1 byte each of blue, green,     |
|        |        |  and red.                                                  |
|        |        |                                                            |
|        |        |  The 4 byte entry contains 1 byte each of blue, green,     |
|        |        |  red, and attribute.  For faster speed (because of the     |
|        |        |  hardware of the Targa board itself), Targa 24 images are  |
|        |        |  sometimes stored as Targa 32 images.                      |
|        |        |                                                            |
--------------------------------------------------------------------------------
*/

