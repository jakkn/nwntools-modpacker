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
 *  Output stream that will write data in intel byte-ordered
 *  format and keep track of exactly how many bytes have been
 *  written.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class BinaryDataOutputStream extends OutputStream
                                    implements DataOutput
{
    private DataOutputStream out;
    private long bytesWritten = 0;

    public BinaryDataOutputStream( OutputStream out )
    {
        this.out = new DataOutputStream( out );
    }

    public long getFilePosition()
    {
        return( bytesWritten );
    }

    public void close() throws IOException
    {
        out.close();
    }

    public void write( int b ) throws IOException
    {
        out.write( b );
        bytesWritten++;
    }

    public void write( byte[] b ) throws IOException
    {
        out.write( b );
        bytesWritten += b.length;
    }

    public void write( byte[] b, int off, int len ) throws IOException
    {
        out.write( b, off, len );
        bytesWritten += len;
    }

    public void writeBoolean( boolean v ) throws IOException
    {
        out.writeBoolean( v );
        bytesWritten++;
    }

    public void writeByte( int v ) throws IOException
    {
        out.writeByte( v );
        bytesWritten++;
    }

    public void writeShort( int v ) throws IOException
    {
        out.write( (byte)(0xff & v) );
        out.write( (byte)(0xff & (v >> 8)) );
        bytesWritten += 2;
    }

    public void writeChar( int v ) throws IOException
    {
        writeShort( v );
    }

    public void writeInt( int v ) throws IOException
    {
        out.write( (byte)(0xff & v) );
        out.write( (byte)(0xff & (v >> 8)) );
        out.write( (byte)(0xff & (v >> 16)) );
        out.write( (byte)(0xff & (v >> 24)) );
        bytesWritten += 4;
    }

    public void writeLong( long v ) throws IOException
    {
        out.write( (byte)(0xff & v) );
        out.write( (byte)(0xff & (v >>  8)) );
        out.write( (byte)(0xff & (v >> 16)) );
        out.write( (byte)(0xff & (v >> 24)) );
        out.write( (byte)(0xff & (v >> 32)) );
        out.write( (byte)(0xff & (v >> 40)) );
        out.write( (byte)(0xff & (v >> 48)) );
        out.write( (byte)(0xff & (v >> 56)) );
        bytesWritten += 8;
    }

    public void writeFloat( float v ) throws IOException
    {
        int i = Float.floatToIntBits( v );
        writeInt( i );
    }

    public void writeDouble( double v ) throws IOException
    {
        long l = Double.doubleToLongBits(v);
        writeLong( l );
    }

    public void writeBytes( String s ) throws IOException
    {
        int len = s.length();
        for( int i = 0; i < len; i++ )
            {
            writeByte( (int)s.charAt(i) );
            }
    }

    public void writeChars( String s ) throws IOException
    {
        int len = s.length();
        for( int i = 0; i < len; i++ )
            {
            writeChar( s.charAt(i) );
            }
    }

    public void writeUTF( String str ) throws IOException
    {
        throw new UnsupportedOperationException( "readLine() not supported." );
    }
}
