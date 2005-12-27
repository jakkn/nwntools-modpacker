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

package org.progeeks.nwn.status;

import java.io.*;
import java.util.*;

import org.progeeks.nwn.gff.*;
import org.progeeks.nwn.io.gff.*;
import org.progeeks.util.ExtensionFileFilter;


/**
 *  Tracks the characters and state information for a player.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class Player
{
    private String name;
    private File playerDirectory;

    public Player( File playerDirectory )
    {
        this.name = playerDirectory.getName();
        this.playerDirectory = playerDirectory;
        loadCharacters();
    }

    public String getName()
    {
        return( name );
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
     *  Loads (or reloads) the player's list of active characters.
     */
    protected void loadCharacters()
    {
        File[] chars = playerDirectory.listFiles( new ExtensionFileFilter( "bic" ) );
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

}
