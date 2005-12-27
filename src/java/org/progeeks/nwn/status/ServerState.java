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
 *  Keeps the server information and processes new events
 *  to adjust the state.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ServerState
{
    public static final String EVENT_STARTUP = "startup";
    public static final String EVENT_SHUTDOWN = "shutdown";
    public static final String EVENT_LOAD_MODULE = "moduleLoaded";

    private File serverVault;

    /**
     *  Maps player names to a map of their character names and character files.
     */
    private Map playerMap = new HashMap();

    /**
     *  The current module being run.
     */
    private String module;

    /**
     *  The current server options.
     */
    private Map options;

    private boolean online = false;

    public ServerState()
    {
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
    public boolean processObject( ServerEvent event )
    {
        String name = event.getName();

        if( EVENT_STARTUP.equals( name ) )
            {
            online = true;
            options = new HashMap();
            options.putAll( event );
            return( true );
            }
        else if( EVENT_SHUTDOWN.equals( name ) )
            {
            online = false;
            module = null;
            options = null;
            return( true );
            }
        else if( EVENT_LOAD_MODULE.equals( name ) )
            {
            module = String.valueOf(event.get( "module" ));
            return( true );
            }

        System.out.println( "event[" + event + "]" );

        return( false );
    }

    /**
     *  Returns the collection of all players known to this server.
     */
    public Collection getPlayers()
    {
        return( playerMap.values() );
    }

    /**
     *  Returns the name of the module being run currently or null
     *  if the server is offline or does not have a module loaded.
     */
    public String getModuleName()
    {
        return( module );
    }

    /**
     *  Returns a map containing all of the server options or null
     *  if the server is currently offline.
     */
    public Map getServerOptions()
    {
        return( options );
    }

    /**
     *  Returns true if the server is currently online according
     *  to the latest log events.
     */
    public boolean isOnline()
    {
        return( online );
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
            System.out.println( "Player:" + players[i] );

            Player p = new Player( players[i] );
            playerMap.put( p.getName(), p );
            }
//System.exit(0);
    }
}
