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
import org.apache.tools.ant.util.*;

import org.progeeks.util.DefaultProgressReporter;

import org.progeeks.nwn.*;
import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;

/**
 *  ANT task for generating an ERF-based file.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ErfPackerTask extends MatchingTask
{
    private File target;
    private File baseDir;
    private String description;
    private List lines = new ArrayList();

    public void setBasedir( File base )
    {
        this.baseDir = base;
    }

    public void setErffile( File target )
    {
        this.target = target;
    }

    /**
     *  Creates a nested description element that can be used for
     *  multi-line descriptions.
     */
    public Description createDescription()
    {
        Description d = new Description();
        lines.add( d );
        return( d );
    }

    /**
     *  Sets a one-line description that will be used for HAK files.  This
     *  will also override the description for MOD files.
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    protected String composeDescription( String type )
    {
        if( lines.size() == 0 )
            return( description );

        // HAK files have some peculiar conditions for their line-endings.
        // The want just LF for the first two lines but CR/LF for any additional
        // lines.
        boolean isHak = "HAK".equals( type );

        StringBuffer sb = new StringBuffer();
        int index = 0;
        for( Iterator i = lines.iterator(); i.hasNext(); index++ )
            {
            if( index > 2 && isHak )
                sb.append( "\r\n" );
            else if( index > 0 )
                sb.append( "\n" );

            Description d = (Description)i.next();
            sb.append( d.getLine() );
            }
        return( sb.toString() );
    }

    protected String getType()
    {
        String type = null;

        // Get the file extension for the type
        String fileName = target.getName();
        int split = fileName.lastIndexOf( '.' );
        if( split > 0 )
            {
            type = fileName.substring( split + 1 ).toUpperCase();
            // Check to make sure it's a valid type
            if( !"MOD".equals( type ) && !"ERF".equals( type ) && !"HAK".equals(type)
                && !"SAV".equals( type ) )
                {
                // Not a valid type
                log( "Unknown type:" + type + "  Treating it as a MOD file." );
                type = null;
                }
            }

        if( type == null || type.length() < 3 )
            {
            type = "MOD";
            }

        return( type );
    }

    protected String getModuleDescription( File ifo ) throws BuildException
    {
        try
            {
            return( ModPacker.getModuleDescription(ifo) );
            }
        catch( IOException e )
            {
            throw new BuildException( "Error reading IFO file:" + ifo, e );
            }
    }

    /**
     *  Write a GFF structure to the specified file.
     */
    protected void writeGff( Struct struct, String type, File target ) throws IOException
    {
        FileOutputStream out = new FileOutputStream( target );
        try
            {
            GffWriter writer = new GffWriter( type, out );
            writer.writeStruct( struct );
            }
        finally
            {
            out.close();
            }
    }

    /**
     *  Creates an export info GFF structure for an ERF file.  HAKs and
     *  MODs don't need this... just .erfs.
     */
    protected Struct createExportInfo( String description )
    {
        // Should be:
        //  name: ExportInfo.gff
        //  type: GFF
        //  version: V3.2
        //

        // Really should standardize this stuff into a different
        // separate set of classes for GFF types.
        Struct root = new Struct( -1 );

        // These should be configurable
        root.addValue( new StringElement( "Mod_MinGameVer", Element.TYPE_STRING, "1.64" ) );
        root.addValue( new IntElement( "Expansion_Pack", Element.TYPE_UINT16, 3 ) );
        root.addValue( new StringElement( "Comments", Element.TYPE_STRING, description ) );

        // Turns out we don't really need anything else.  The toolset seems
        // to cope just fine with the above.

        return( root );
    }

    public void execute() throws BuildException
    {
        if( target == null )
            throw new BuildException( "No erffile specified." );

        // We'll keep track of any temporary ExportInfo.gff file
        // that we create so we can remove it after packing.
        File exportInfoFile = null;

        // Do some deducing
        String type = getType();
        String desc = composeDescription(type);

        // We only allow empty HAK Files... otherwise, you need sources
        if( baseDir == null && !"HAK".equals( type ) )
            throw new BuildException( "Non-HAK files cannot be empty.  No basedir specified." );

        // Depending on the type, we'll look for different descriptions
        if( "HAK".equals( type ) && desc == null )
            {
            throw new BuildException( "HAK files must have a description attribute." );
            }
        else if( "MOD".equals( type ) )
            {
            // See if the .ifo file exists
            File ifo = new File( baseDir, "module.ifo" );
            if( !ifo.exists() )
                throw new BuildException( "MOD files require a .ifo file. " );

            if( desc == null )
                desc = getModuleDescription( ifo );
            }
        else if( "ERF".equals( type ) )
            {
            // Need to generate an ExportInfo file containing a list of
            // the included resources and the description.
            Struct exportInfo = createExportInfo( desc );
            try
                {
                // Note: the space at the end of GFF is necessary right now.
                exportInfoFile = new File( baseDir, "ExportInfo.gff" );
                if( !exportInfoFile.exists() )
                    {
                    writeGff( exportInfo, "GFF ", new File( baseDir, "ExportInfo.gff" ) );
                    }
                else
                    {
                    // We'll use the existing one
                    exportInfoFile = null;
                    }
                }
            catch( IOException e )
                {
                throw new BuildException( "Error generating ExportInfo.gff", e );
                }
            }

        ModPacker packer = new ModPacker( target, desc, new NullMonitor() );

        if( baseDir != null )
            {
            DirectoryScanner ds = getDirectoryScanner( baseDir );
            String[] sources = ds.getIncludedFiles();

            log( "Packing " + sources.length + " files into:" + target );

            for( int i = 0; i < sources.length; i++ )
                {
                File f = new File( baseDir, sources[i] );

                // Just in case
                if( f.isDirectory() )
                    continue;
                if( "hak.description".equals( f.getName() ) )
                    continue;

                packer.addFile( f );
                }
            }
        else
            {
            log( "No sources specified.  Generating empty:" + target );
            }

        try
            {
            // And go
            long size = packer.writeModule();
            System.out.println( size + " bytes written from " + packer.getResourceCount()
                                + " resources.                                 " );
            }
        catch( IOException e )
            {
            throw new BuildException( "Error writing erf:" + target, e );
            }

        // Clean up the temporary ExportInfo if we created one
        if( exportInfoFile != null )
            {
            exportInfoFile.delete();
            }
    }

    private class NullMonitor extends DefaultProgressReporter
    {
        public void setMessage( String message )
        {
            log( message, Project.MSG_VERBOSE );
        }
    }

    public class Description
    {
        private String line;

        public Description()
        {
        }

        public void setLine( String line )
        {
            this.line = line;
        }

        public String getLine()
        {
            return( line );
        }
    }
}



