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

package org.progeeks.nwn.io.nss;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.resource.*;

/**
 *  Parses a script and returns a list of discovered dependencies
 *  as resource keys.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class DependencyReader
{
    private Reader in;
    private StreamTokenizer st;
    private List dependencies = new ArrayList();
    private String directive = null;

    public DependencyReader( InputStream in ) throws IOException
    {
        this( new InputStreamReader(in) );
    }

    public DependencyReader( Reader in )
    {
        this.in = in;
        st = new StreamTokenizer( this.in );
        st.slashStarComments( true );
        st.slashSlashComments( true );
    }

    public List readDependencies() throws IOException
    {
        int token = 0;
        while( (token = st.nextToken()) != StreamTokenizer.TT_EOF )
            {
            switch( token )
                {
                case StreamTokenizer.TT_WORD:
                    if( directive != null )
                        {
                        directive += st.sval;
                        continue;
                        }
                    break;
                case '#':
                    directive = "#";
                    continue;
                case '"':
                    if( "#include".equals( directive ) )
                        {
                        ResourceKey key = new ResourceKey( st.sval, ResourceTypes.TYPE_NSS );
                        dependencies.add( key );
                        }
                    break;
                case StreamTokenizer.TT_NUMBER:
                    break;
                default:
                    break;
                }

            directive = null;
            }

        return( dependencies );
    }

    public void close() throws IOException
    {
        in.close();
    }

    public static void main( String[] args ) throws Exception
    {
        for( int i = 0; i < args.length; i++ )
            {
            DependencyReader r = new DependencyReader( new FileReader( new File(args[i]) ) );
            try
                {
                List l = r.readDependencies();
                System.out.println( "list:" + l );
                }
            finally
                {
                r.close();
                }
            }
    }
}


