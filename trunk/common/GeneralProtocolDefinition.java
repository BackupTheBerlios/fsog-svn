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

public class GeneralProtocolDefinition{

    public static void main(String[] args) throws Exception{

        final int protocolVersion = 1;
        
        final ProtocolDefinition protocol
            = new ProtocolDefinition("General",
                                     protocolVersion,
                                     "GeneralProtocol",
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
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER));

        protocol.defineMessage
            ("CREATE_THOUSAND_TABLE",
             "Sent by client when creating a new Thousand table.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER));

        protocol.defineMessage
            ("TABLE_CREATED",
             "Sent by server after table has been created.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.INT64,"id",
                                 "ID for newly created table."));

        protocol.defineMessage
            ("SAY",
             "Sent by client when saying something (chat message).",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER),
             new PieceDefinition(PieceType.BINARY,"text_UTF8",
                                 "Text of the chat message in UTF8 encoding."));

        //TODO: Don't send it back to the sayer.
        protocol.defineMessage
            ("SAID",
             "Sent by server when someone says something (chat message).",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.INT8,"tablePlayerId",
                                 "Who said it."),
             new PieceDefinition(PieceType.BINARY,"text_UTF8",
                                 "Text of the chat message in UTF8 encoding."));


        protocol.defineMessage
            ("JOIN_TABLE_TO_PLAY",
             "Sent by client when joining some table to play.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER),
             new PieceDefinition(PieceType.INT64,"tableId",
                                 "Table id."),
             new PieceDefinition(PieceType.CSTRING,"screenName",
                                 "Name of the player (not unique)."));
        
        protocol.defineMessage
            ("YOU_JOINED_TABLE",
             "Sent by server to new player who joined a table.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.INT8,"tablePlayerId",
                                 "New player's table player id."));
        protocol.defineMessage
            ("JOINING_TABLE_FAILED_INCORRECT_TABLE_ID",
             "Sent by server to new player who joined a table.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER));
                
        protocol.defineMessage
             ("NEW_PLAYER_JOINED_TABLE",
             "Sent by server after new player joined a table"
             +" to already present people.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.CSTRING,"screenName",
                                 "New player's name."),
             new PieceDefinition(PieceType.INT8,"tablePlayerId",
                                 "New player's table player id."));

        protocol.defineMessage
            ("PLAYER_LEFT_TABLE",
             "Sent by server when a player left.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.INT8,"tablePlayerId",
                                 "Leaving player's table player id."));

        //TODO: Definitions.hpp should be defined in protocol, so both
        //sides know what sizes are the integers, so they can use the
        //same.

        protocol.defineMessage
            ("GAME_STARTED_WITHOUT_INITIAL_MESSAGE",
             "Sent by server when game is started and no initial message is"
             +" designated for the receiver.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.VECTOR(PieceType.INT8),
                                 "turnGamePlayerToTablePlayerId",
                                 "Specifies how many players will play the"
                                 +" just-started game, which tablePlayerId"
                                 +"each of them has, and what's their order."));
        
        protocol.defineMessage
            ("GAME_STARTED_WITH_INITIAL_MESSAGE",
             "Sent by server when game is started and an initial message is"
             +" designated for the receiver.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.VECTOR(PieceType.INT8),
                                 "turnGamePlayerToTablePlayerId",
                                 "Specifies how many players will play the"
                                 +" just-started game, which tablePlayerId"
                                 +"each of them has, and what's their order."),
             new PieceDefinition(PieceType.BINARY,"initialMessage",
                                 "Game-specific initial information."));
        

        protocol.defineMessage
            ("MAKE_MOVE",
             "Sent by client when making a move.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER),
             new PieceDefinition(PieceType.BINARY,"gameMove",
                                 "Game-specific move information."));

        protocol.defineMessage
            ("MOVE_MADE",
             "Sent by server after client made a move.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.BINARY,"gameMove",
                                 "Game-specific move information."));


        protocol.write();
    }
}
