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

/**
 *  The main object containing all of the relevant project
 *  configuration information.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class Project
{
    private String name;
    private String projectDescription;
    private String moduleName;
    private File   projectFile;
    private FileIndex buildDirectory;
    private FileIndex workDirectory;
    private FileIndex sourceDirectory;
    private ProjectGraph graph;
    private ProjectRoot root;

    public Project()
    {
        root = new ProjectRoot();
    }

    /**
     *  Sets the name of this project.  This can be different
     *  than the project file name.
     */
    public void setName( String name )
    {
        this.name = name;
        root.setName( name );
    }

    /**
     *  Returns the name of this project.
     */
    public String getName()
    {
        return( name );
    }

    /**
     *  Sets this project's project graph.  This will throw
     *  an exception if a graph has already been set.
     */
    public void setProjectGraph( ProjectGraph graph )
    {
        if( this.graph != null )
            throw new RuntimeException( "The project graph can only be set once." );
        this.graph = graph;
    }

    /**
     *  Returns the resource graph for this project.
     */
    public ProjectGraph getProjectGraph()
    {
        if( graph == null )
            graph = new ProjectGraph( root );
        return( graph );
    }

    /**
     *  Sets the project File in case it's somehow different than
     *  the project name.  This file will also be used to resolve
     *  relative paths because it is always set both when the project
     *  file is read (from the project's XML file) and after it is
     *  read.  This lets the Project convert any paths.  We do this
     *  because we can't really control the files that are passed
     *  in and returned... besides it's more convenient for them to
     *  be real Files while the program is running.
     */
    public void setProjectFile( File file )
    {
        if( file.equals( this.projectFile ) )
            return;

        File oldFile = this.projectFile;
        this.projectFile = file;

        // Repath the sub-directories as necessary.
    }

    public File getProjectFile()
    {
        return( projectFile );
    }

    /**
     *  Sets the name of the target module.
     */
    public void setTargetModuleName( String moduleName )
    {
        this.moduleName = moduleName;
    }

    /**
     *  Returns the name of the target module.
     */
    public String getTargetModuleName()
    {
        return( moduleName );
    }

    /**
     *  Sets the location of converted module binaries that will
     *  be combined into the .mod file.  This is the target of the
     *  XML -> GFF conversion process and the script compilation
     *  process.
     */
    public void setBuildDirectory( FileIndex buildDirectory )
    {
        this.buildDirectory = buildDirectory;
    }

    /**
     *  Returns the location of the converted module binaries.
     */
    public FileIndex getBuildDirectory()
    {
        return( buildDirectory );
    }

    /**
     *  Sets the location for project work files.  These are files
     *  that Pandora uses to keep track of the project such as dependency
     *  information and cached thumbnails, etc..
     */
    public void setWorkDirectory( FileIndex workDirectory )
    {
        this.workDirectory = workDirectory;
    }

    /**
     *  Returns the location of the project work files.
     */
    public FileIndex getWorkDirectory()
    {
        return( workDirectory );
    }

    /**
     *  Sets the location for the project source files.  These are the
     *  files that will be edited by the user and are used to build
     *  the GFF and NCS binaries before bundling them into a module.
     *  If you're going to put a directory in CVS, this is the one.
     */
    public void setSourceDirectory( FileIndex sourceDirectory )
    {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     *  Returns the location of the project source files.
     */
    public FileIndex getSourceDirectory()
    {
        return( sourceDirectory );
    }

///*
public void setTest1( Project p )
{
    System.out.println( "Test1:" + p );
}

public Project getTest1()
{
    return( null );
}

public void setTest2( java.util.List l )
{
    System.out.println( "Test2:" + l );
}

public void addTest2( Project p )
{
}

public java.util.List getTest2()
{
    return( null );
} //*/

    /**
     *  Sets the project description which may be different than the
     *  module description.
     */
    public void setProjectDescription( String description )
    {
System.out.println( "description:" + description );
        this.projectDescription = description;
    }

    /**
     *  Returns the project description.
     */
    public String getProjectDescription()
    {
        return( projectDescription );
    }

    public String toString()
    {
        return( "Project[" + name + ", file:" + projectFile + ", work:" + workDirectory
                            + ", source:" + sourceDirectory + ", build:" + buildDirectory + "]" );
    }
}


