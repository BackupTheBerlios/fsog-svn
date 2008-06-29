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

public class GeneralHandler extends GeneralProtocol.AbstractGeneralHandler{
    private final UserInterface userInterface;
    private final Sender sender;
    private final Table table;

    //Constructor:
    public GeneralHandler(final UserInterface userInterface,
                          final Sender sender,
                          final Table table){
        this.userInterface = userInterface;
        this.sender = sender;
        this.table = table;
    }

    public boolean handle_1_TABLE_CREATED(final long id){
        //TODO: Something wrong with protocol definitions, we should
        //not need to handle this message here.
        return false;
    }

    public boolean handle_1_YOU_JOINED_TABLE(final byte tablePlayerId){
        final TablePlayer player = new TablePlayer("Me");
        this.table.addMe(tablePlayerId,player);
        this.userInterface.newPlayer(tablePlayerId,player);
        return true;
    }

    public boolean handle_1_NEW_PLAYER_JOINED_TABLE(final String screenName,
                                                    final byte tablePlayerId){
        final TablePlayer player = new TablePlayer(screenName);
        this.table.addPlayer(tablePlayerId,player);
        this.userInterface.newPlayer(tablePlayerId,player);
        return true;
    }

    public boolean handle_1_GAME_STARTED(){
        return false;
    }

    public boolean handle_1_MOVE_MADE(final java.util.Vector<Byte> move){
        return false;
    }
}
