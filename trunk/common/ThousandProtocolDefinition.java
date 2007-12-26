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


public class ThousandProtocolDefinition{

    public static void main(String[] args) throws Exception{

        final int protocolVersion = 1;
        final int serverUDPPort = 10137;
        
        final ProtocolDefinition protocol
            = new ProtocolDefinition(protocolVersion,
                                     serverUDPPort,
                                     "../server/Protocol.hpp",
                                     "../client/Protocol.java");

        protocol.addCardsDefinition();

        protocol.defineMessage
            ("LOG_IN",
             "Sent by client to log in. Loginng in associates the sending"
             +" IP+port as belonging to the particular user until logged"
             +" out. Can be used to create an account. Valid nick is"
             +" between 5 and 20 letters, consisting of any printable"
             +" non-whitespace ASCII characters. Password should be at"
             +" least 6 characters. Easy passwords won't be admitted.",
             Sender.CLIENT,
             new PieceDefinition(PieceType.CSTRING,"nick",
                                 "Unique nick."),
             new PieceDefinition(PieceType.CSTRING,"password",
                                 "User's password."));

        protocol.defineMessage
            ("LOG_IN_CORRECT",
             "Sent by server to let the user know that log in was OK.",
             Sender.SERVER);

        protocol.defineMessage
            ("LOG_IN_INCORRECT",
             "Sent by server to let the user know that log in wasn't OK.",
             Sender.SERVER,
             new PieceDefinition(PieceType.CSTRING,"reason",
                                 "Why login failed.")
             );

        protocol.defineMessage
            ("GET_STATISTICS",
             "Message sent by client to request 'room' statistics.",
             Sender.CLIENT);

        protocol.defineMessage
            ("RETURN_STATISTICS",
             "Sent by server when returning info about 'room' stats.",
             Sender.SERVER,
             new PieceDefinition(PieceType.INT32,"numberOfUsers",
                                 "Number of people logged in."),
             new PieceDefinition(PieceType.INT32,"numberOfGames",
                                 "Number of games being played in the 'room'."),
             new PieceDefinition(PieceType.INT32,"numberOfSearchers",
                                 "Number of people searching in the 'room'."));

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
        
        protocol.defineMessage
            ("SEARCH_GAME",
             "Sent by client when searching for some game.",
             Sender.CLIENT,
             new PieceDefinition(PieceType.INT8,"minimumOponentRanking50",
               "Minimum ranking of the oponent, measured in 50 points."
                                 +" E.g. 60 means 60*50=3000 points."),
             new PieceDefinition(PieceType.FLAGSET("ThousandFlags"),
                                 "searchFlags",
                                 "Have a look at ThousandFlags.")
             );

        protocol.defineMessage
            ("AWAIT_GAME",
             "Sent by server when no game with given search"
             +" cryteria is currently available.",
             Sender.SERVER);

        protocol.defineMessage
            ("PROPOSED_GAME",
             "Sent by server when game is matched."
             +" Includes game settings. One of four usernames"
             +" will be equal to the requesting player's if she should"
             +" play the game, otherwise it's observing. Must be"
             +" acknowledged by the client program immediately,"
             +" using the secret provided here.",
             Sender.SERVER,
             //new PieceDefinition(PieceType.INT16,"gameIdentifier",
             //  "Identifier of the game created by server."),
             new PieceDefinition(PieceType.CSTRING,"player0Username",
               "The username (nick) of player #0."
               +" Empty string means no player at"
               +" this side of the table."),
             new PieceDefinition(PieceType.CSTRING,"player1Username",
               "The username (nick) of player #1."
               +" Empty string means no player at"
               +" this side of the table."),
             new PieceDefinition(PieceType.CSTRING,"player2Username",
               "The username (nick) of player #2."
               +" Empty string means no player at"
               +" this side of the table."),
             new PieceDefinition(PieceType.CSTRING,"player3Username",
               "The username (nick) of player #3."
               +" Empty string means no player at"
               +" this side of the table."),
             new PieceDefinition(PieceType.FLAGSET("ThousandFlags"),
                                 "gameSettings",
                                 "Look at ThousandFlags."),
             new PieceDefinition(PieceType.INT8,
                                 "acknowledgeSecret",
                                 "The same value must be sent by client"
                                 +" when acknowledging.")
             );
        
        protocol.defineMessage
            ("ACKNOWLEDGE_PROPOSED_GAME",
             "Sent by client. After this, server just waits for"
             +" GAME_START. If server doesn't get this ack for some"
             +" reason, will send again PROPOSED_GAME and ack must"
             +" be repeated.",
             Sender.CLIENT,
             //new PieceDefinition(PieceType.INT16,"gameIdentifier",
             //  "Identifier of the game created by server."),
             new PieceDefinition(PieceType.INT8,
                                 "secret",
                                 "Protection against very old"
                                 +" duplicate packets.")
             );

        //TODO: time limit for pressing START for the client.
        protocol.defineMessage
            ("GAME_START",
             "Client clicks the 'start button' to accept a game."
             +" Immediately after all clients do this, server will"
             +" deal cards and start counting time for some player."
             +" Server sends no acknowledgement for it. If cards"
             +" are not dealt after, say, 5s, client can re-send"
             +" GAME_START. But they might not be dealt because"
             +" the opponent didn't click GAME_START.",
             Sender.CLIENT);

        protocol.defineMessage
            ("GAME_DEAL_7_CARDS",
             "Everybody pressed start, so let's deal the cards."
             +" The cards sent are not sorted in any way.",
             Sender.SERVER,
             new PieceDefinition(PieceType.INT8,"card0","First card."),
             new PieceDefinition(PieceType.INT8,"card1","Second card."),
             new PieceDefinition(PieceType.INT8,"card2","Third card."),
             new PieceDefinition(PieceType.INT8,"card3","Fourth card."),
             new PieceDefinition(PieceType.INT8,"card4","Fifth card."),
             new PieceDefinition(PieceType.INT8,"card5","Sixth card."),
             new PieceDefinition(PieceType.INT8,"card6","Seventh card."));

        protocol.defineMessage
            ("GAME_BID",
             "The message sent by client when placing a bid.",
             Sender.CLIENT,
             new PieceDefinition(PieceType.INT16,"gameIdentifier",
               "Which game this is about."),
             new PieceDefinition(PieceType.INT8,"bid",
               "This is actually 10% of the bid."
               +" Multiply by 10 to get the real value."));

        protocol.defineMessage
            ("GAME_BID_MADE",
             "Message sent by the server to let other"
             +" players know what bid was placed.",
             Sender.SERVER,
             new PieceDefinition(PieceType.INT16,"gameId","Which game this is about."),
             new PieceDefinition(PieceType.INT8,"bid",
               "This is actually 10% of the bid."
               +" Multiply by 10 to get the real value."));

        protocol.defineMessage
            ("GAME_BID_END_HIDDEN_MUST",
             "Message sent by the server to let players know that"
             +" bidding is over. 'Must' is not shown.",
             Sender.SERVER,
             new PieceDefinition(PieceType.INT16,"gameIdenitfier","Which game this is about."));

        protocol.defineMessage
            ("GAME_BID_END_SHOW_MUST",
             "Message sent by the server to let players know that"
             +" bidding is over. 'Must' is shown.",
             Sender.SERVER,
             new PieceDefinition(PieceType.INT16,"gameIdenitfier","Which game this is about."),
             new PieceDefinition(PieceType.INT8,"mustCard0","First must card."),
             new PieceDefinition(PieceType.INT8,"mustCard1","Second must card."),
             new PieceDefinition(PieceType.INT8,"mustCard2","Third must card."));

                
        protocol.write();
    }
}
