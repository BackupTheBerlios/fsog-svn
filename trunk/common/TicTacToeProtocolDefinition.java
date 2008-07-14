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

public class TicTacToeProtocolDefinition{

    public static void main(String[] args) throws Exception{

        final int protocolVersion = 1;
        
        final ProtocolDefinition protocol
            = new ProtocolDefinition("TicTacToe",
                                     protocolVersion,
                                     "TicTacToeProtocol",
                                     "../server/TicTacToeProtocol.hpp",
                                     "../server/TicTacToeProtocol.cpp",
                                     "../client/TicTacToeProtocol.java");
        
        //TODO: When only 1 message, don't save messageType in serialized vector.


        protocol.defineMessage
            ("TIC_TAC_TOE_MOVE",
             "Simple TicTacToe move. No need to say who made this move.",
             EnumSet.of(Create.CPP_SERIALIZER,
                        Create.JAVA_SERIALIZER,
                        Create.CPP_DESERIALIZER,
                        Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.INT8,
                                 "row",
                                 "In which row player puts her X or O."),
             new PieceDefinition(PieceType.INT8,
                                 "column",
                                 "In which column player puts her X or O."));
                
        protocol.write();
    }
}
