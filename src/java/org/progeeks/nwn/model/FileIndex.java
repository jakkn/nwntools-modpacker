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

package org.progeeks.nwn.model;

import java.io.File;
import java.util.StringTokenizer;

/**
 *  Similar to a java.io.File except this one is always relative
 *  to the project root... even if the project root changes.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class FileIndex implements Comparable
{
    private FileIndex parent;
    private String file;
    private String fullPath;
    private long   lastModified;

    public FileIndex( FileIndex parent, String file )
    {
        this.parent = parent;
        setFullPath( file );
    }

    public FileIndex( String file )
    {
        this( null, file );
    }

    public FileIndex()
    {
    }

    /**
     *  Returns the name of this file, not including parent information.
     */
    public String getName()
    {
        return( file );
    }

    /**
     *  Sets the fullpath of this file index.  The path will be split up
     *  and the parent FileIndex updated accordingly.  If there is already a
     *  name set for this FileIndex then an exception will be thrown.
     */
    public void setFullPath( String path )
    {
        if( file != null )
            {
            throw new RuntimeException( "The path has already been set for this object." );
            }

        // Parse out multiple directories as needed.
        StringTokenizer st = new StringTokenizer( path, "/\\" );

        String current = st.nextToken();
        while( st.hasMoreTokens() )
            {
            this.parent = new FileIndex( this.parent, current );
            current = st.nextToken();
            }

        this.file = current;

        // Cache our full path
        if( parent == null )
            fullPath = this.file;
        else
            fullPath = parent.getFullPath() + "/" + this.file;
    }

    /**
     *  Returns the full path of this file index relative to the
     *  project directory.
     */
    public String getFullPath()
    {
        return( fullPath );
    }

    /**
     *  Sets the time this file was last modified.  This is cached
     *  in this object separately from the actual file so that
     *  changes can be detected that happened outside of the tool.
     */
    public void setLastModified( long lastModified )
    {
        this.lastModified = lastModified;
    }

    /**
     *  Returns the last modified time that has been cached in
     *  this object.
     */
    public long getLastModified()
    {
        return( lastModified );
    }

    /**
     *  Updates the cached last modified time by synching it with the
     *  physical file.
     */
    public boolean updateLastModified( Project project )
    {
        long old = lastModified;
        lastModified = getFile( project ).lastModified();
        return( lastModified > old );
    }

    /**
     *  Returns the physical file for this file index based
     *  on the specified project's directory settings.
     */
    public File getFile( Project project )
    {
        // If we've implemented FileIndex correctly, the the
        // project root + full path is all we should need.
        // File will do the rest
        return( new File( project.getProjectFile().getParentFile(), fullPath ) );
    }

    /**
     *  Returns the parent file index or null if this is a root-level
     *  file.
     */
    public FileIndex getParent()
    {
        return( parent );
    }

    public boolean equals( Object obj )
    {
        if( !(obj instanceof FileIndex) )
            return( false );
        return( fullPath.equals( ((FileIndex)obj).fullPath ) );
    }

    public int hashCode()
    {
        return( fullPath.hashCode() );
    }

    public int compareTo( Object obj )
    {
        if( !(obj instanceof FileIndex) )
            return( -1 );
        return( fullPath.compareTo( ((FileIndex)obj).fullPath ) );
    }

    public String toString()
    {
        return( "FileIndex[" + getFullPath() + "]" );
    }

    public static void main( String[] args )
    {
        for( int i = 0; i < args.length; i++ )
            {
            FileIndex f = new FileIndex( args[i] );
            System.out.println( "name:" + f.getName() + "  full path:" + f.getFullPath() );
            }
    }
}
