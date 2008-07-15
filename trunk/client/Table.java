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

import java.util.*;

public class Table {

    //Data valid always after joining a table:
    public final TreeMap<Byte,TablePlayer> tablePlayerIdToTablePlayer;
    public byte myTablePlayerId;
    public boolean gameOn;

    //Data valid only when game is started:
    public Vector<Byte> turnGamePlayerToTablePlayerId;
    public byte myTurnGamePlayer;

    //Constructor:
    public Table(){
        this.tablePlayerIdToTablePlayer = new TreeMap<Byte,TablePlayer>();
        this.myTablePlayerId = 0;
        this.gameOn = false;
        this.turnGamePlayerToTablePlayerId = new Vector<Byte>();
        this.myTurnGamePlayer = -100;
    }

    /*
    public synchronized TreeMap<Byte,TablePlayer> getTablePlayerIdToTablePlayerDeepCopy(){
        TreeMap<Byte,TablePlayer> deepCopy
            = new TreeMap<Byte,TablePlayer>();
        for(Map.Entry<Byte,TablePlayer> e : this.tablePlayerIdToTablePlayer.entrySet()){
            deepCopy.put(new Byte(e.getKey()),
                         e.getValue().clone());
        }
        return deepCopy;
    }
    */

    /**
       @param which 0 -- myself, 1 -- first opponent, etc...
    */
    public TablePlayer getOpponent(final int which){
        final byte opponentTurnGamePlayer
            = (byte)((myTurnGamePlayer+which)
                     %turnGamePlayerToTablePlayerId.size());
        
        return this.tablePlayerIdToTablePlayer.get
            (turnGamePlayerToTablePlayerId.get(opponentTurnGamePlayer));
    }

    public TablePlayer getFirstOpponent(){
        return this.getOpponent(1);
    }

    public TablePlayer getSecondOpponent(){
        return this.getOpponent(2);
    }

    public void addMe(final Byte tablePlayerId,
                      final TablePlayer me){
        this.tablePlayerIdToTablePlayer.put(tablePlayerId,me);
        this.myTablePlayerId = tablePlayerId;
    }

    public void addPlayer(final Byte tablePlayerId,
                          final TablePlayer player){
        this.tablePlayerIdToTablePlayer.put(tablePlayerId,player);
    }

    /*
    public synchronized void setTurnGamePlayerToTablePlayerId
        (final Vector<Byte> original){
        this.turnGamePlayerToTablePlayerId.setSize(0);
        for(int i=0;i<original.size();i++){
            final Byte b = original.get(i);
            this.turnGamePlayerToTablePlayerId.add(new Byte(b));
            if(b.equals(this.myTablePlayerId))
                this.myTurnGamePlayer = (byte)i;
        }
    }
    */

    /*
    public synchronized Vector<Byte> getTurnGamePlayerToTablePlayerIdDeepCopy(){
        final Vector<Byte> result = new Vector<Byte>();
        for(Byte b : this.turnGamePlayerToTablePlayerId)
            result.add(new Byte(b));
        return result;
    }
    */

    /*
    public synchronized Byte getMyTurnGamePlayer(){
        return new Byte(this.myTurnGamePlayer);
    }
    */
}
