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
    private File   buildDirectory;
    private File   workDirectory;
    private File   sourceDirectory;

    public Project()
    {
    }

    /**
     *  Sets the name of this project.  This can be different
     *  than the project file name.
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     *  Returns the name of this project.
     */
    public String getName()
    {
        return( name );
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
    public void setBuildDirectory( File buildDirectory )
    {
        this.buildDirectory = buildDirectory;
    }

    /**
     *  Returns the location of the converted module binaries.
     */
    public File getBuildDirectory()
    {
        return( buildDirectory );
    }

    /**
     *  Sets the location for project work files.  These are files
     *  that Pandora uses to keep track of the project such as dependency
     *  information and cached thumbnails, etc..
     */
    public void setWorkDirectory( File workDirectory )
    {
        this.workDirectory = workDirectory;
    }

    /**
     *  Returns the location of the project work files.
     */
    public File getWorkDirectory()
    {
        return( workDirectory );
    }

    /**
     *  Sets the location for the project source files.  These are the
     *  files that will be edited by the user and are used to build
     *  the GFF and NCS binaries before bundling them into a module.
     *  If you're going to put a directory in CVS, this is the one.
     */
    public void setSourceDirectory( File sourceDirectory )
    {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     *  Returns the location of the project source files.
     */
    public File getSourceDirectory()
    {
        return( sourceDirectory );
    }

    /**
     *  Sets the project description which may be different than the
     *  module description.
     */
    public void setProjectDescription( String description )
    {
        this.projectDescription = description;
    }

    /**
     *  Returns the project description.
     */
    public String getProjectDescription()
    {
        return( projectDescription );
    }

}


