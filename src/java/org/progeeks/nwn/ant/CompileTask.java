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

import org.progeeks.util.ErrorInfo;
import org.progeeks.util.ErrorListener;

import org.progeeks.nwn.model.ScriptCompiler;

/**
 *  ANT task for executing a commandline NWscript compiler.
 *  Right now I plan to support Bioware's but it should be easy
 *  to support PRC/Torlack's as well.  Actually, both tools are
 *  a little kludgy on the command line options but at least the
 *  PRC compiler lets us not generate debug info and we can specify
 *  an include path.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class CompileTask extends MatchingTask
{
    private int threadCount = 10;
    private File srcdir;
    private File destdir;
    private File nwncompiler = new File( "/NeverwinterNights/NWN/tools/nwnnsscomp.exe" );

    private ScriptCompiler scriptCompiler = new ScriptCompiler();
    private ErrorObserver errorObserver = new ErrorObserver();

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

        if( scriptCompiler.hasCompiler( nwncompiler ) )
            {
            scriptCompiler.setCompilerPath( nwncompiler );
            }
        else if( !scriptCompiler.hasCompiler() )
            {
            throw new BuildException( "Can't locate NWN compiler on path or at:" + nwncompiler );
            }

        scriptCompiler.setMaxCompilerCount( threadCount );

        log( "Using compiler:" + scriptCompiler.getCompilerPath(), Project.MSG_VERBOSE );
        log( "Executing commandline compilter", Project.MSG_VERBOSE );

        DirectoryScanner ds = getDirectoryScanner( srcdir );
        String[] files = ds.getIncludedFiles();

        String includes = getIncludes( files );
        log( "Includes:" + includes, Project.MSG_VERBOSE );

        File[] scripts = getNewFiles( srcdir, destdir, files );
        if( scripts.length == 0 )
            return;

        log( "Compiling " + scripts.length + " scripts to:" + destdir );

        for( int i = 0; i < scripts.length; i++ )
            {
            // Need to figure out the destination path from the
            // source path.
            File targetFile = getTargetFile( scripts[i] );
            File targetPath = targetFile.getParentFile();

            // Make sure the directory exists
            if( !targetPath.exists() && !targetPath.mkdirs() )
                throw new BuildException( "Error creating target directory:" + targetPath );

            log( scripts[i].toString(), Project.MSG_VERBOSE );

            // Now run the compile
            compileSource( targetPath, scripts[i], targetFile, includes );
            }

        try
            {
            scriptCompiler.waitForAll();

            int count = scriptCompiler.getIncludeCount();
            if( count > 0 )
                log( "Skipped " + count + " include files." );
            count = scriptCompiler.getCompiledCount();
            if( count > 0 )
                log( count + " scripts compiled successfully." );
            }
        catch( InterruptedException e )
            {
            e.printStackTrace();
            }
    }

    protected void compileSource( File work, File source, File target, String includes ) throws BuildException
    {
        String[] args = new String[] { "-i", includes,
                                       "-g" };
        scriptCompiler.compileScript( source.toString(), args, work, errorObserver );
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

        return( new File( destdir, path ) );
    }

    /**
     *  Returns an encoded list of implied include paths based on the
     *  specified script list.
     */
    protected String getIncludes( String[] files )
    {
        // Build up a list of directories that we need to use as potential
        // include paths.  The idea being that the compile task was called
        // with an entire "set" of scripts.  Proper partitioning should
        // be done at the task definition level.
        Set includes = new TreeSet();
        for( int i = 0; i < files.length; i++ )
            {
            //File f = new File( srcdir, files[i] );
            //System.out.println( "File:" + files[i] );
            File f = new File( srcdir, files[i] );
            //System.out.println( "File:" + f );
            includes.add( f.getParentFile() );
            }

        StringBuffer sb = new StringBuffer();
        for( Iterator i = includes.iterator(); i.hasNext(); )
            {
            File f = (File)i.next();
            if( sb.length() != 0 )
                sb.append( ";" );
            sb.append( f );
            }

        return( sb.toString() );
    }

    /**
     *  Returns the list of script files that are newer than their
     *  compiled counterparts.
     */
    protected File[] getNewFiles( File srcDir, File destDir, String[] files )
    {
        GlobPatternMapper mapper = new GlobPatternMapper();
        mapper.setFrom( "*.nss" );
        mapper.setTo( "*.ncs" );

        SourceFileScanner sourceScanner = new SourceFileScanner(this);
        File[] newFiles = sourceScanner.restrictAsFiles( files, srcDir, destDir, mapper );
        return( newFiles );
    }

    private class ErrorObserver implements ErrorListener
    {
        public void error( Object source, ErrorInfo error )
        {
            String s = String.valueOf( source );
            String root = srcdir.toString();
            if( s.startsWith( root ) )
                s = s.substring( root.length() + 1 );
            System.out.println( s + "  " + error );
        }
    }
}
