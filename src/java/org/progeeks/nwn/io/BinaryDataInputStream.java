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

package org.progeeks.nwn.io;

import java.io.*;

/**
 *  Input stream that will read data in intel byte-ordered
 *  format and keep track of exactly how many bytes have been
 *  read.  WARNING: All DataInput methods return little-endian
 *  versions of the shorts/ints/floats that a normal data input
 *  would return.  This would be a reverse byte ordering when
 *  reading a normal file as compared to a regular DatatInput
 *  implementation.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class BinaryDataInputStream extends InputStream
                                   implements DataInput
{
    private DataInputStream in;
    private long bytesRead = 0;

    public BinaryDataInputStream( InputStream in )
    {
        this.in = new DataInputStream( in );
    }

    public long getFilePosition()
    {
        return( bytesRead );
    }

    /**
     *  Skips enough bytes to ensure that the specified position
     *  is obtained.
     */
    public void gotoPosition( long pos ) throws IOException
    {
        long skip = pos - bytesRead;
        if( skip < 0 )
            throw new IOException( "Cannot skip backwards.  At " + bytesRead + ", going to " + pos );
        if( skip == 0 )
            return;
        long result = skip( skip );
        if( result < skip )
            throw new IOException( "Only skipped ahead " + result + " bytes instead of " + skip );
    }

    public void close() throws IOException
    {
        in.close();
    }

    public void readFully( byte[] b ) throws IOException
    {
        in.readFully( b );
        bytesRead += b.length;
    }

    public void readFully( byte[] b, int off, int len) throws IOException
    {
        in.readFully( b, off, len );
        bytesRead += len;
    }

    public int skipBytes( int n ) throws IOException
    {
        return( (int)skip(n) );
    }

    public boolean readBoolean() throws IOException
    {
        boolean b = in.readBoolean();
        bytesRead++;
        return( b );
    }

    public byte readByte() throws IOException
    {
        byte b = in.readByte();
        bytesRead++;
        return( b );
    }

    public int readUnsignedByte() throws IOException
    {
        int i = in.readUnsignedByte();
        bytesRead++;
        return( i );
    }

    public short readShort() throws IOException
    {
        int low = in.readUnsignedByte();
        int hi = in.readByte();
        short s = (short)(hi << 8 | low);
        bytesRead += 2;
        return( s );
    }

    public int readUnsignedShort() throws IOException
    {
        int low = in.readUnsignedByte();
        int hi = in.readUnsignedByte();
        int s = hi << 8 | low;
        bytesRead += 2;
        return( s );
    }

    public char readChar() throws IOException
    {
        char c = in.readChar();
        bytesRead += 2;
        return( c );
    }

    public int readInt() throws IOException
    {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();

        int i = b4 << 24 | b3 << 16 | b2 << 8 | b1;
        bytesRead += 4;
        return( i );
    }

    public long readLong() throws IOException
    {
        int low = readInt();
        int hi = readInt();
        long l = hi << 32 | low;
        bytesRead += 8;
        return( l );
    }

    public float readFloat() throws IOException
    {
        int i = in.readInt();
        float f = Float.intBitsToFloat(i);
        bytesRead += 4;
        return( f );
    }

    public double readDouble() throws IOException
    {
        long l = in.readLong();
        double d = Double.longBitsToDouble( l );
        bytesRead += 8;
        return( d );
    }

    public String readLine() throws IOException
    {
        throw new UnsupportedOperationException( "readLine() not supported." );
    }

    public String readUTF() throws IOException
    {
        throw new UnsupportedOperationException( "readUTF() not supported." );
    }

    public long skip( long skip ) throws IOException
    {
        long total = 0;
        long result;
        while( (result = in.skip(skip - total)) > 0 )
            {
            total += result;
            if( total >= skip )
                break;
            }
        bytesRead += total;
        return( total );
    }

    public int read() throws IOException
    {
        int val = in.read();
        if( val >= 0 )
            bytesRead++;
        return( val );
    }

    public int read( byte[] b ) throws IOException
    {
        return( read( b, 0, b.length ) );
    }

    public int read( byte[] b, int off, int len ) throws IOException
    {
        int result = in.read( b, off, len );
        if( result > 0 )
            bytesRead += result;
        return( result );
    }

    private static int swapBytes( int i )
    {
        int result = (i & 0xff) << 24;
        result |= (i & 0xff00) << 8;
        result |= (i & 0xff0000) >> 8;
        result |= ((i & 0xff000000) >> 24) & 0xff;
        return( result );
    }
}
