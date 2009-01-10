(* -*- Mode: sml; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 * vim:expandtab:shiftwidth=4:tabstop=4: *)

(*
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

*)

(*
    You can contact the author, Bartlomiej Antoni Szymczak, by:
    - electronic mail: rhywek@gmail.com
    - paper mail:
        Bartlomiej Antoni Szymczak
        Boegesvinget 8, 1. sal
        2740 Skovlunde
        Denmark
*)

load "Protocol";

open Protocol

(* Define messages: *)

(*
val GET_STATISTICS : message
  = {name="GET_STATISTICS",
     comment="Client requests statistics.",
     elements=[]}

val RETURN_STATISTICS : message
  = {name="RETURN_STATISTICS",
     comment="Sent by server when returning info about stats.",
     elements=[{kind=int32,name="numberOfUsers",
                comment="Number of people logged in."},
               {kind=int32,name="numberOfGames",
                comment="Number of games being played in the 'room'."},
               {kind=int32,name="numberOfSearchers",
                comment="Number of people searching in the 'room'."}]}
*)

val CREATE_TICTACTOE_TABLE : message
  = {name="CREATE_TICTACTOE_TABLE",
     comment="Sent by client when creating a new Tic Tac Toe table.",
     elements=[]}

val CREATE_THOUSAND_TABLE : message
  = {name="CREATE_THOUSAND_TABLE",
     comment="Sent by client when creating a new Thousand table.",
     elements=[]}

val TABLE_CREATED : message
  = {name="TABLE_CREATED",
     comment="Sent by server after table has been created.",
     elements=[{kind=int64,name="id",comment="ID for newly created table."}]}

val SAY : message
  = {name="SAY",
     comment="Sent by client when saying something (chat message).",
     elements=[{kind=binary,name="text_UTF8",
                comment="Text of the chat message in UTF8 encoding."}]}

(* TODO: Don't send it back to the sayer. *)
val SAID : message
  = {name="SAID",
     comment="Sent by server when someone says something (chat message).",
     elements=[{kind=int8,name="tablePlayerId",
                comment="Who said it."},
               {kind=binary,name="text_UTF8",
                comment="Text of the chat message in UTF8 encoding."}]}

val JOIN_TABLE_TO_PLAY : message
  = {name="JOIN_TABLE_TO_PLAY",
     comment="Sent by client when joining some table to play.",
     elements=[{kind=int64,name="tableId",
                comment="Table id."},
               {kind=cstring,name="screenName",
                comment="Name of the player (not unique)."}]}
        
val YOU_JOINED_TABLE : message
  = {name="YOU_JOINED_TABLE",
     comment="Sent by server to new player who joined a table.",
     elements=[{kind=int8,name="tablePlayerId",
                comment="New player's table player id."}]}

val JOINING_TABLE_FAILED_INCORRECT_TABLE_ID : message
  = {name="JOINING_TABLE_FAILED_INCORRECT_TABLE_ID",
     comment="Sent by server to new player who joined a table.",
     elements=[]}
                
val NEW_PLAYER_JOINED_TABLE : message
  = {name="NEW_PLAYER_JOINED_TABLE",
     comment=("Sent by server after new player joined a table"
              ^ " to already present people."),
     elements=[{kind=cstring,name="screenName",
                comment="New player's name."},
               {kind=int8,name="tablePlayerId",
                comment="New player's table player id."}]}

val PLAYER_LEFT_TABLE : message
  = {name="PLAYER_LEFT_TABLE",
     comment="Sent by server when a player left.",
     elements=[{kind=int8,name="tablePlayerId",
                comment="Leaving player's table player id."}]}

(* TODO: Definitions.hpp should be defined in protocol, so both sides
 * know what sizes are the integers, so they can use the same.
 *)

val GAME_STARTED_WITHOUT_INITIAL_MESSAGE : message
  = {name="GAME_STARTED_WITHOUT_INITIAL_MESSAGE",
     comment=("Sent by server when game is started and no initial message is"
              ^ " designated for the receiver."),
     elements=[{kind=vector int8,name="turnGamePlayerToTablePlayerId",
                comment=("Specifies how many players will play the"
                         ^" just-started game, which tablePlayerId"
                         ^"each of them has, and what's their order.")}]}
    
val GAME_STARTED_WITH_INITIAL_MESSAGE : message
  = {name="GAME_STARTED_WITH_INITIAL_MESSAGE",
     comment=("Sent by server when game is started and an initial message is"
              ^" designated for the receiver."),
     elements=[{kind=vector int8,name="turnGamePlayerToTablePlayerId",
                comment=("Specifies how many players will play the"
                         ^" just-started game, which tablePlayerId"
                         ^"each of them has, and what's their order.")},
               {kind=binary,name="initialMessage",
                comment="Game-specific initial information."}]}
        
val MAKE_MOVE : message
  = {name="MAKE_MOVE",
     comment="Sent by client when making a move.",
     elements=[{kind=binary,name="gameMove",
                comment="Game-specific move information."}]}

val MOVE_MADE : message
  = {name="MOVE_MADE",
     comment="Sent by server after client made a move.",
     elements=[{kind=binary,name="gameMove",
                comment="Game-specific move information."}]}

val agpl3 : string
  = "    FSOG - Free Software Online Games\n"
    ^ "    Copyright (C) 2007 Bartlomiej Antoni Szymczak\n"
    ^ "\n"
    ^ "    This file is part of FSOG.\n"
    ^ "\n"
    ^ "    FSOG is free software: you can redistribute it and/or modify\n"
    ^ "    it under the terms of the GNU Affero General Public License as\n"
    ^ "    published by the Free Software Foundation, either version 3 of the\n"
    ^ "    License, or (at your option) any later version.\n"
    ^ "\n"
    ^ "    This program is distributed in the hope that it will be useful,\n"
    ^ "    but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
    ^ "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
    ^ "    GNU Affero General Public License for more details.\n"
    ^ "\n"
    ^ "    You should have received a copy of the GNU Affero General Public License\n"
    ^ "    along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
    ^ "\n"
    ^ "\n"
    ^ "    You can contact the author, Bartlomiej Antoni Szymczak, by:\n"
    ^ "    - electronic mail: rhywek@gmail.com\n"
    ^ "    - paper mail:\n"
    ^ "        Bartlomiej Antoni Szymczak\n"
    ^ "        Boegesvinget 8, 1. sal\n"
    ^ "        2740 Skovlunde\n"
    ^ "        Denmark\n"

val general_messages
  = [(*GET_STATISTICS,
     RETURN_STATISTICS,
     *)
     CREATE_TICTACTOE_TABLE,
     CREATE_THOUSAND_TABLE,
     TABLE_CREATED,
     SAY,
     SAID,
     JOIN_TABLE_TO_PLAY,
     YOU_JOINED_TABLE,
     JOINING_TABLE_FAILED_INCORRECT_TABLE_ID,
     NEW_PLAYER_JOINED_TABLE,
     PLAYER_LEFT_TABLE,
     GAME_STARTED_WITHOUT_INITIAL_MESSAGE,
     GAME_STARTED_WITH_INITIAL_MESSAGE,
     MAKE_MOVE,
     MOVE_MADE]

val thousand_constants:constant list
  = [{kind=int8,comment="",name="ACE_SHIFT",value="5"},
     {kind=int8,comment="",name="TEN_SHIFT",value="4"},
     {kind=int8,comment="",name="KING_SHIFT",value="3"},
     {kind=int8,comment="",name="QUEEN_SHIFT",value="2"},
     {kind=int8,comment="",name="JACK_SHIFT",value="1"},
     {kind=int8,comment="",name="NINE_SHIFT",value="0"},
     {kind=int8,comment="",name="HEART_SHIFT",value="18"},
     {kind=int8,comment="",name="DIAMOND_SHIFT",value="12"},
     {kind=int8,comment="",name="CLUB_SHIFT",value="6"},
     {kind=int8,comment="",name="SPADE_SHIFT",value="0"},
     {kind=int8,comment="",name="NO_TRUMP_SHIFT",value="24"}]

val DEAL : message
  = {name="DEAL",
     comment="Everybody pressed start, so let's deal the cards."
             ^ " Seven cards are represented as 3--byte thousandCardSet.",
     elements=[{kind=int24,name="thousandCardSet",
                comment="Set of dealt cards."}]}

val BID : message
  = {name="BID",
     comment="The message sent by client when placing a bid."
             ^ " Also sent by server when bid was made.",
     elements=[{kind=int8,name="bid10",
                comment = "This is actually 10% of the bid."
                          ^ " Multiply by 10 to get the real value."
                          ^ " A bid10 of 0 is 'pass'."
                          ^ " On the last 'pass', this message is not sent."
                          ^ " Instead, BID_END_... is sent."}]}

val BID_END_HIDDEN_MUST : message
  = {name="BID_END_HIDDEN_MUST",
     comment = "Message sent by the server to let players know that"
               ^ " bidding is over. 'Must' is not shown.",
     elements=[]}

val BID_END_SHOW_MUST : message
  = {name="BID_END_SHOW_MUST",
     comment = "Message sent by the server to let players know that"
               ^ " bidding is over. 'Must' is shown.",
     elements=[{kind=int24,name="must",
                comment="Set of cards represented as ThousandCardSet."}]}

val SELECT : message
  = {name="SELECT",
     comment = "Message sent by the client when selecting a card."
               ^ ", which is given to opponent. The same message is"
               ^ " sent to opponent from the server.",
     elements=[{kind=int8,name="shift",
                comment="Shift of selected card."}]}

val SELECT_HIDDEN : message
  = {name="SELECT_HIDDEN",
     comment = "Message sent by the server when selecting a card."
               ^ ", which is given to some opponent. This message is"
               ^ " sent to player who should not see the card.",
     elements=[]}

val CONTRACT : message
  = {name="CONTRACT",
     comment = "The message sent by client when decided on how much to play."
               ^ " Also sent by server when contract was made.",
     elements=[{kind=int8,name="contract10",
                comment = "This is actually 10% of the contract."
                          ^ " Multiply by 10 to get the real value."}]}

val PLAY : message
  = {name="PLAY",
     comment = "Message sent by the client when playing a card."
               ^ " Also sometimes sent by server back to clients. There's"
               ^ " 24 cards to be played."
               ^ " On cards 1-23 this message is sent by client to server"
               ^ " and the server sends some message back to other people,"
               ^ " but not the one who played it."
               ^ " On card 24 (that is when one round finishes)"
               ^ " some message is sent to all people, including"
               ^ " the one who played the last card.",
     elements=[{kind=int8,name="shift",
                comment="Shift of played card."}]}

val PLAY_NEW_TRUMP : message
  = {name="PLAY_NEW_TRUMP",
     comment="Message sent by server back to clients, when new trump is set.",
     elements=[{kind=int8,name="shift",
                comment="Shift of played card."}]}

val PLAY_AND_DEAL : message
  = {name="PLAY_AND_DEAL",
     comment = "Message sent by server back to clients, when last"
               ^ " card was played and new cards are dealt."
               ^ " Seven cards are represented as 3--byte thousandCardSet.",
     elements=[{kind=int8,name="shift",
                comment="Shift of played card."},
               {kind=int24,name="thousandCardSet",
                comment="Set of dealt cards."}]}

val thousand_messages
    = [DEAL,
       BID,
       BID_END_HIDDEN_MUST,
       BID_END_SHOW_MUST,
       SELECT,
       SELECT_HIDDEN,
       CONTRACT,
       PLAY,
       PLAY_NEW_TRUMP,
       PLAY_AND_DEAL]

val thousand_protocol : protocol
  = define {name="Thousand",
            version=1,
            comment="TODO: display this comment in hpp file, etc. and change it!",
            license=agpl3,
            messages=thousand_messages,
            constants=thousand_constants
           }

val general_protocol : protocol
  = define {name="General",
            version=1,
            comment="TODO: display this comment in hpp file, etc. and change it!",
            license=agpl3,
            messages=general_messages,
            constants=[]
           }

val () = cpp {protocol=general_protocol,
              ser=["TABLE_CREATED",
                   "SAID",
                   "YOU_JOINED_TABLE",
                   "JOINING_TABLE_FAILED_INCORRECT_TABLE_ID",
                   "NEW_PLAYER_JOINED_TABLE",
                   "PLAYER_LEFT_TABLE",
                   "GAME_STARTED_WITHOUT_INITIAL_MESSAGE",
                   "GAME_STARTED_WITH_INITIAL_MESSAGE",
                   "MOVE_MADE"],
              des=["CREATE_TICTACTOE_TABLE",
                   "CREATE_THOUSAND_TABLE",
                   "SAY",
                   "JOIN_TABLE_TO_PLAY",
                   "MAKE_MOVE"],
              hppfile="../server/GeneralProtocol.sml.hpp",
              cppfile="../server/GeneralProtocol.sml.cpp"}

val () = cpp {protocol=thousand_protocol,
              ser=["DEAL","BID","BID_END_HIDDEN_MUST","BID_END_SHOW_MUST",
                   "SELECT","SELECT_HIDDEN","CONTRACT",
                   "PLAY","PLAY_NEW_TRUMP","PLAY_AND_DEAL"],
              des=["BID",
                   "SELECT","SELECT_HIDDEN","CONTRACT",
                   "PLAY"],
              hppfile="../server/ThousandProtocol.sml.hpp",
              cppfile="../server/ThousandProtocol.sml.cpp"}
