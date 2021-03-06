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

public class ThousandProtocolDefinition{

    public static void main(String[] args) throws Exception{

        final int protocolVersion = 1;
        
        final ProtocolDefinition protocol
            = new ProtocolDefinition("Thousand",
                                     protocolVersion,
                                     "ThousandProtocol",
                                     "../server/ThousandProtocol.hpp",
                                     "../server/ThousandProtocol.cpp",
                                     "../client/ThousandProtocol.java");

        protocol.defineConstants
            (new ConstantDefinition(null,PieceType.INT8,"ACE_SHIFT","5"),
             new ConstantDefinition(null,PieceType.INT8,"TEN_SHIFT","4"),
             new ConstantDefinition(null,PieceType.INT8,"KING_SHIFT","3"),
             new ConstantDefinition(null,PieceType.INT8,"QUEEN_SHIFT","2"),
             new ConstantDefinition(null,PieceType.INT8,"JACK_SHIFT","1"),
             new ConstantDefinition(null,PieceType.INT8,"NINE_SHIFT","0"),
             new ConstantDefinition(null,PieceType.INT8,"HEART_SHIFT","18"),
             new ConstantDefinition(null,PieceType.INT8,"DIAMOND_SHIFT","12"),
             new ConstantDefinition(null,PieceType.INT8,"CLUB_SHIFT","6"),
             new ConstantDefinition(null,PieceType.INT8,"SPADE_SHIFT","0"),
             new ConstantDefinition(null,PieceType.INT8,"NO_TRUMP_SHIFT","24")
             );

        protocol.defineFlagSet
            ("ThousandFlags",
             "Various booleans used in search for saying"
             +" what games user is"
             +" interested in. true - interested, false - not interested."
             +" Also used for game settings.",
             new FlagDefinition("TWO_PLAYERS","2-players game."),
             new FlagDefinition("THREE_PLAYERS","3-players game."),
             new FlagDefinition("FOUR_PLAYERS","4-players game."),
             new FlagDefinition("NO_BOMBS","Game without bombs."),
             new FlagDefinition("BOMBS","Game with bombs."),
             new FlagDefinition("UNLIMITED_BOMBS",
               "Game with unlimited bombs."),
             new FlagDefinition("BID_INCREMENT_10",
               "Game with 10 bid increment."),
             new FlagDefinition("BID_INCREMENT_ANY",
               "Game with any bid increment."),
             new FlagDefinition("SHOW_MUST_100",
               "Game where 'must' is always shown."),
             new FlagDefinition("SHOW_MUST_110",
               "Game where 'must' is shown from 110."),
             new FlagDefinition("PUBLIC_GAME","Public game."),
             new FlagDefinition("PRIVATE_GAME","Private game."),
             new FlagDefinition("RANKING_GAME","Ranking game."),
             new FlagDefinition("SPARRING_GAME","Sparring (non-ranked) game."),
             new FlagDefinition("TIME_7","7 minutes game."),
             new FlagDefinition("TIME_10","10 minutes game."),
             new FlagDefinition("TIME_15","15 minutes game."),
             new FlagDefinition("TIME_20","20 minutes game."),
             new FlagDefinition("TIME_30","30 minutes game.")
             );
        //*/

        //One card is encoded as 8-bit integer:
        final PieceType SHIFT = PieceType.INT8;

        //We'll send card sets in 24-card deck using 3 bytes:
        final PieceType CARD_SET_24 = PieceType.INT24;

        protocol.defineMessage
            ("DEAL",
             "Everybody pressed start, so let's deal the cards."
             +" Seven cards are represented as 3--byte thousandCardSet.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(CARD_SET_24,"thousandCardSet",
                                 "Set of dealt cards."));

        protocol.defineMessage
            ("BID",
             "The message sent by client when placing a bid."
             +" Also sent by server when bid was made.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER,
                        Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.INT8,"bid10",
                                 "This is actually 10% of the bid."
                                 +" Multiply by 10 to get the real value."
                                 +" A bid10 of 0 is 'pass'."
                                 +" On the last 'pass', this message is not sent."
                                 +" Instead, BID_END_... is sent."));

        protocol.defineMessage
            ("BID_END_HIDDEN_MUST",
             "Message sent by the server to let players know that"
             +" bidding is over. 'Must' is not shown.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER));

        protocol.defineMessage
            ("BID_END_SHOW_MUST",
             "Message sent by the server to let players know that"
             +" bidding is over. 'Must' is shown.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(CARD_SET_24,"must",
                                 "Set of cards represented as ThousandCardSet."));
                
        protocol.defineMessage
            ("SELECT",
             "Message sent by the client when selecting a card."
             +", which is given to opponent. The same message is"
             +" sent to opponent from the server.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER,
                        Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(SHIFT,"shift","Shift of selected card."));

        protocol.defineMessage
            ("SELECT_HIDDEN",
             "Message sent by the server when selecting a card."
             +", which is given to some opponent. This message is"
             +" sent to player who should not see the card.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER,
                        Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER));

        protocol.defineMessage
            ("CONTRACT",
             "The message sent by client when decided on how much to play."
             +" Also sent by server when contract was made.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER,
                        Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(PieceType.INT8,"contract10",
                                 "This is actually 10% of the contract."
                                 +" Multiply by 10 to get the real value."));

        protocol.defineMessage
            ("PLAY",
             "Message sent by the client when playing a card."
             +" Also sometimes sent by server back to clients. There's"
             +" 24 cards to be played."
             +" On cards 1-23 this message is sent by client to server"
             +" and the server sends some message back to other people,"
             +" but not the one who played it."
             +" On card 24 (that is when one round finishes)"
             +" some message is sent to all people, including"
             +" the one who played the last card.",
             EnumSet.of(Create.JAVA_SERIALIZER,Create.CPP_DESERIALIZER,
                        Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(SHIFT,"shift","Shift of played card."));

        protocol.defineMessage
            ("PLAY_NEW_TRUMP",
             "Message sent by server back to clients, when new"
             +" trump is set.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(SHIFT,"shift","Shift of played card."));

        protocol.defineMessage
            ("PLAY_AND_DEAL",
             "Message sent by server back to clients, when last"
             +" card was played and new cards are dealt."
             +" Seven cards are represented as 3--byte thousandCardSet.",
             EnumSet.of(Create.CPP_SERIALIZER,Create.JAVA_DESERIALIZER),
             new PieceDefinition(SHIFT,"shift","Shift of played card."),
             new PieceDefinition(CARD_SET_24,"thousandCardSet",
                                 "Set of dealt cards."));

        protocol.write();
    }
}
