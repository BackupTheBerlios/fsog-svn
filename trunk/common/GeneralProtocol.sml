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

val general_protocol : protocol
  = {name="General",
     version=1,
     comment="TODO: display this comment in hpp file, etc. and change it!",
     license=agpl3,
     messages=[GET_STATISTICS,
               RETURN_STATISTICS,
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
               MOVE_MADE]}

val hpp_string = hpp general_protocol
