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

package org.progeeks.nwn.ant;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.*;

import org.progeeks.nwn.*;

/**
 *  ANT task for executing the XmlToGff utility to "compile"
 *  XML source into GFF binaries.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class XmlToGffTask extends MatchingTask
{
    private int threadCount = 10;
    private File srcdir;
    private File destdir;

    public void setSrcdir( File src )
    {
        this.srcdir = src;
    }

    public void setDestdir( File destdir )
    {
        this.destdir = destdir;
    }

    public void execute() throws BuildException
    {
        if( srcdir == null )
            throw new BuildException( "No srcdir attribute specified." );
        if( destdir == null )
            throw new BuildException( "No destdir attribute specified." );

        DirectoryScanner ds = getDirectoryScanner( srcdir );
        String[] sources = ds.getIncludedFiles();

        File[] files = getNewFiles( srcdir, destdir, sources );
        if( files.length == 0 )
            return;

        log( "Converting " + files.length + " XML files GFF in:" + destdir );

        File outDir = null;
        XmlToGff xmlConverter = new XmlToGff();
        xmlConverter.setVerbose( false );

        for( int i = 0; i < files.length; i++ )
            {
            // Need to figure out the destination path from the
            // source path.
            File targetFile = getTargetFile( files[i] );
            File targetPath = targetFile.getParentFile();

            // Make sure the directory exists
            if( !targetPath.exists() && !targetPath.mkdirs() )
                throw new BuildException( "Error creating target directory:" + targetPath );

            log( files[i].toString(), Project.MSG_VERBOSE );

            if( targetPath != outDir )
                {
                outDir = targetPath;
                xmlConverter.setOutputDirectory( outDir );
                }

            try
                {
                // Now run the conversion
                xmlConverter.processFile( files[i] );
                }
            catch( IOException e )
                {
                throw new BuildException( "Error processing file:" + files[i], e );
                }
            }
    }

    /**
     *  Returns the full destination path for the specified source path.
     */
    protected File getTargetFile( File source )
    {
        // First we need to find the part of the path
        // that is not our srcdir.
        String root = srcdir.toString();
        String path = source.toString();
        if( path.startsWith( root ) )
            path = path.substring( root.length() + 1 );

        return( new File( destdir, getGffName( path ) ) );
    }

    /**
     *  Returns the list of source files that are newer than their
     *  binary counterparts.
     */
    protected File[] getNewFiles( File srcDir, File destDir, String[] files )
    {
        FileNameMapper mapper = new XmlGffMapper();

        SourceFileScanner sourceScanner = new SourceFileScanner(this);
        File[] newFiles = sourceScanner.restrictAsFiles( files, srcDir, destDir, mapper );
        return( newFiles );
    }

    protected String getGffName( String source )
    {
        int length = source.length();
        String extension = source.substring( length - 4 );
        if( !".xml".equals( extension ) )
            return( null );

        String file = source.substring( 0, length - 4 );
        return( file );
    }

    private class XmlGffMapper implements FileNameMapper
    {
        public void setFrom( String from )
        {
        }

        public void setTo( String to )
        {
        }

        public String[] mapFileName( String sourceFileName )
        {
            int length = sourceFileName.length();
            String extension = sourceFileName.substring( length - 4 );
            if( !".xml".equals( extension ) )
                return( null );

            String file = sourceFileName.substring( 0, length - 4 );
            return( new String[] { file } );
        }
    }
}
