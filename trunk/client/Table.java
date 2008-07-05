/* -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 * vim:expandtab:shiftwidth=4:tabstop=4: */


/*
    FSOG - Free Software Online Games
    Copyright (C) 2007 Bartlomiej Antoni Szymczak

    This file is part of FSOG.

    FSOG is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

/*
    You can contact the author, Bartlomiej Antoni Szymczak, by:
    - electronic mail: rhywek@gmail.com
    - paper mail:
        Bartlomiej Antoni Szymczak
        Boegesvinget 8, 1. sal
        2740 Skovlunde
        Denmark
*/

/** Thread--safe. */

public class Table {

    private final java.util.TreeMap<Byte,TablePlayer> players;
    private Byte myTablePlayerId;
    
    //Constructor:
    public Table(){
        this.players = new java.util.TreeMap<Byte,TablePlayer>();
        this.myTablePlayerId = null;
    }

    public synchronized java.util.TreeMap<Byte,TablePlayer> getPlayersDeepCopy(){
        java.util.TreeMap<Byte,TablePlayer> deepCopy
            = new java.util.TreeMap<Byte,TablePlayer>();
        for(java.util.Map.Entry<Byte,TablePlayer> e : this.players.entrySet()){
            deepCopy.put(new Byte(e.getKey()),
                         e.getValue().clone());
        }
        return deepCopy;
    }

    public synchronized TablePlayer getTablePlayerCopy(final byte tablePlayerId){
        final TablePlayer tablePlayer =  this.players.get(tablePlayerId);
        if(tablePlayer == null)
            return null;
        return tablePlayer.clone();
    }

    /** After this method is called, no thread can use me. */
    public synchronized void addMe(final Byte tablePlayerId,
                                   final TablePlayer me){
        this.players.put(tablePlayerId,me);
        this.myTablePlayerId = tablePlayerId;
    }

    public synchronized void addPlayer(final Byte tablePlayerId,
                                       final TablePlayer player){
        this.players.put(tablePlayerId,player);
    }

    public synchronized void removePlayer(final Byte tablePlayerId){
        this.players.remove(tablePlayerId);
    }

}
