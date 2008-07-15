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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

//TODO: Change name to JTurnGame

public abstract class JBoard extends JPanel{

    protected final byte numberOfPlayers;
    protected byte turn;

    protected final Table table;
    protected final MoveListener moveListener;
    protected final JTabbedPane jTabbedPane;

    JBoard(final byte numberOfPlayers,
           final Table table,
           final MoveListener moveListener,
           final JTabbedPane jTabbedPane){
        this.numberOfPlayers = numberOfPlayers;
        this.table = table;
        this.moveListener = moveListener;
        this.jTabbedPane = jTabbedPane;
    }

    /** 
     @return Returns number of players playing.
    */
    public byte getNumberOfPlayers(){
        return this.numberOfPlayers;
    }

    /** Who's turn is it now?
     @return Returns the turn game player number.
    */
    public byte getTurn(){
        return this.turn;
    }

    public boolean myTurn(){
        return this.turn == this.table.myTurnGamePlayer;
    }

    //@param which 0--my turn, 1--the one after me moves now, etc...
    protected boolean opponentsTurn(final int which){
        return this.turn == ((this.table.myTurnGamePlayer+which)
                             %this.table.turnGamePlayerToTablePlayerId.size());
    }

    protected byte getOpponent(){
        return (byte)(1-this.turn);
    }

    protected void setFirstPlayer(){
        this.turn = 0;
    }

    protected void setNextPlayer(){
        this.turn++;
        this.turn %= this.numberOfPlayers;
    }
    
    //TODO: change boolean return to exception
    /** @param  initialMessage Can  be null  if no  initialMessage was
        sent by the server.*/
    public abstract boolean initialize(final java.util.Vector<Byte> initialMessage);

    //Bit mask for specifying whether the game shall continue or finish.
    public static final int CONTINUITY_MASK = 0x01;
    public static final int CONTINUE = 0x00;
    public static final int END = 0x01;

    //Bit mask for specifying whether the move sent was valid.
    public static final int VALIDITY_MASK = 0x02;
    public static final int VALID = 0x00;
    public static final int INVALID = 0x02;

    public abstract int moveMade(final Vector<Byte> move,
                                 final int[] endResult);
}
