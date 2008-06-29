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


public class GeneralProtocolDefinition{

    public static void main(String[] args) throws Exception{

        final int protocolVersion = 1;
        
        final ProtocolDefinition protocol
            = new ProtocolDefinition("General",
                                     protocolVersion,
                                     "../server/GeneralProtocol.hpp",
                                     "../server/GeneralProtocol.cpp",
                                     "../client/GeneralProtocol.java");

//         protocol.defineMessage
//             ("GET_STATISTICS",
//              "Message sent by client to request 'room' statistics.",
//              Sender.CLIENT);

//         protocol.defineMessage
//             ("RETURN_STATISTICS",
//              "Sent by server when returning info about 'room' stats.",
//              Sender.SERVER,
//              new PieceDefinition(PieceType.INT32,"numberOfUsers",
//                                  "Number of people logged in."),
//              new PieceDefinition(PieceType.INT32,"numberOfGames",
//                                  "Number of games being played in the 'room'."),
//              new PieceDefinition(PieceType.INT32,"numberOfSearchers",
//                                  "Number of people searching in the 'room'."));


        protocol.defineMessage
            ("CREATE_TICTACTOE_TABLE",
             "Sent by client when creating a new Tic Tac Toe table.",
             Sender.CLIENT);

        protocol.defineMessage
            ("TABLE_CREATED",
             "Sent by server after table has been created.",
             Sender.SERVER,
             new PieceDefinition(PieceType.INT64,"id",
                                 "ID for newly created table."));

        protocol.defineMessage
            ("JOIN_TABLE_TO_PLAY",
             "Sent by client when joining some table to play.",
             Sender.CLIENT,
             new PieceDefinition(PieceType.INT64,"tableId",
                                 "Table id."),
             new PieceDefinition(PieceType.CSTRING,"screenName",
                                 "Name of the player (not unique)."));
        
        protocol.defineMessage
            ("YOU_JOINED_TABLE",
             "Sent by server to new player who joined a table.",
             Sender.SERVER,
             new PieceDefinition(PieceType.INT8,"tablePlayerId",
                                 "New player's table player id."));
                
        protocol.defineMessage
             ("NEW_PLAYER_JOINED_TABLE",
             "Sent by server after new player joined a table"
             +" to already present people.",
             Sender.SERVER,
             new PieceDefinition(PieceType.CSTRING,"screenName",
                                 "New player's name."),
             new PieceDefinition(PieceType.INT8,"tablePlayerId",
                                 "New player's table player id."));

        protocol.defineMessage
            ("GAME_STARTED_AND_INITIAL_MESSAGE",
             "Sent by server when game is started."
             +" Some initialization message can be sent within."
             +" Move from first player(s) is awaited after that.",
             Sender.SERVER,
             new PieceDefinition(PieceType.BINARY,"initialMessage",
                                 "Initial game--specific message."));

        protocol.defineMessage
            ("MAKE_MOVE",
             "Sent by client when making a move.",
             Sender.CLIENT,
             new PieceDefinition(PieceType.BINARY,"move",
                                 "Game-specific move information."));

        protocol.defineMessage
            ("MOVE_MADE",
             "Sent by server after client made a move.",
             Sender.SERVER,
             new PieceDefinition(PieceType.BINARY,"move",
                                 "Game-specific move information."));

        protocol.write();
    }
}
