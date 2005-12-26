/*
 * $Id$
 *
 * Copyright (c) 2005, Paul Speed
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

package org.progeeks.nwn.status;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.util.ExtensionFileFilter;

/**
 *  Keeps track of NWN server state and writes it to HTML when
 *  committed.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class HtmlGeneratingProcessor implements EventProcessor
{
    private File outputFile;
    private File serverVault;

    /**
     *  Maps player names to a map of their character names and character files.
     */
    private Map players = new HashMap();

    public HtmlGeneratingProcessor()
    {
    }

    /**
     *  Sets the file to use for web page generation.
     */
    public void setOutputFile( File file )
    {
        this.outputFile = file;
    }

    /**
     *  Sets the location of the NWN server vault.
     */
    public void setServerVault( File file )
    {
        this.serverVault = file;

        loadPlayerData();
    }

    /**
     *  Processes the parsed object to update the server state.
     */
    public boolean processObject( Object obj )
    {
        System.out.println( "event[" + obj + "]" );

        return( true );
    }

    /**
     *  Writes the HTML status file from accumulated state changes.
     */
    public void commit()
    {
    }


    /**
     *  Reads the character file to determine name and other information.
     */
    protected void readCharacter( File character ) throws IOException
    {
        FileInputStream in = new FileInputStream( character );
        try
            {
            GffReader reader = new GffReader( in );
            Struct root = reader.readRootStruct();

            // For testing
            //GffReader.printElement( root, "    " );
            //System.out.println( root );
            System.out.println( "First name:" + root.getString( "FirstName" ) );
            System.out.println( "Last name:" + root.getString( "LastName" ) );
            System.out.println( "Is DM:" + root.getInt( "IsDM" ) );
            System.out.println( "Age:" + root.getInt( "Age" ) );
            System.out.println( "Gender:" + root.getInt( "Gender" ) );
            System.out.println( "Race:" + root.getInt( "Race" ) );
            System.out.println( "Subrace:" + root.getString( "Subrace" ) );
            System.out.println( "Deity:" + root.getString( "Deity" ) );
            System.out.println( "Gold:" + root.getInt( "Gold" ) );
            System.out.println( "AC:" + root.getInt( "ArmorClass" ) );
            System.out.println( "Natural AC:" + root.getInt( "NaturalAC" ) );
            System.out.println( "Str:" + root.getInt( "Str" ) );
            System.out.println( "Dex:" + root.getInt( "Dex" ) );
            System.out.println( "Int:" + root.getInt( "Int" ) );
            System.out.println( "Wis:" + root.getInt( "Wis" ) );
            System.out.println( "Con:" + root.getInt( "Con" ) );
            System.out.println( "Cha:" + root.getInt( "Cha" ) );
            System.out.println( "Hit points:" + root.getInt( "HitPoints" ) );
            System.out.println( "Max hit points" + root.getInt( "MaxHitPoints" ) );
            System.out.println( "XP:" + root.getInt( "Experience" ) );
            }
        finally
            {
            in.close();
            }
    }

    /**
     *  Adds a new player and loads (or reloads) their list of active characters.
     */
    protected void addPlayer( File player )
    {
        System.out.println( "Player:" + player );
        File[] chars = player.listFiles( new ExtensionFileFilter( "bic" ) );
        for( int i = 0; i < chars.length; i++ )
            {
            System.out.println( "    character:" + chars[i] );
            try
                {
                readCharacter( chars[i] );
                }
            catch( IOException e )
                {
                System.out.println( "Error reading character file:" + chars[i] );
                e.printStackTrace();
                }
            }
    }

    /**
     *  Loads player data from the configured server vault directory.
     */
    protected void loadPlayerData()
    {
        File[] players = serverVault.listFiles();
        for( int i = 0; i < players.length; i++ )
            {
            if( !players[i].isDirectory() )
                continue;
            addPlayer( players[i] );
            }
//System.exit(0);
    }
}
