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

structure Protocol :> Protocol =
struct
    datatype kind = int8
                  |int16
                  |int24
                  |int32
                  |int64
                  |cstring
                  |binary
                  |vector of kind

    type element = {kind:kind,
                    name:string,
                    comment:string}

    type message = {name:string,
                    comment:string,
                    elements:element list}

    (* Internal message, which includes identifiers *)
    type imessage = {identifier:int,message:message}

    type protocol = {name:string,
                     version:int,
                     comment:string,
                     license:string,
                     messages:message list}

    fun imessages messages
      = let
          fun h _ [] = []
            | h id (m::ms) = {identifier=id,message=m}::(h (id+1) ms)
      in
          h 1 messages
      end

    fun hpp_messageTypes (p:protocol)
      =
      let
          fun hpp_messageType (m:imessage)
            = "    //" ^ #comment (#message m) ^ "\n"
              ^ "    static const int8_t " ^ #name (#message m)
              ^ "_" ^ Int.toString (#version p)
              ^ " = " ^ Int.toString (#identifier m) ^ ";\n"
      in
          (* TODO: When only one message, don't waste one byte in serialized message for message type. *)
          "  //Can be used for switching to detect\n"
          ^ "  //which deserializer should be used.\n"
          ^ "    static const int8_t UNKNOWN_MESSAGE_"
          ^ Int.toString(#version p)
          ^ " = 0;\n"
          ^ foldl op^ "" (map hpp_messageType (imessages (#messages p)))
          ^ "\n\n"
      end

    fun hpp_header (protocol:protocol)
      = "/*\n"
        ^ #license protocol
        ^ "\n*/\n"
        ^ "#pragma once\n"
        ^ "\n"
        ^ "#include <vector>\n"
        ^ "#include <string>\n"
        ^ "#include <sstream>\n"
        ^ "#include <cctype>\n"
        ^ "#include <list>\n"
        ^ "#include \"Time.hpp\"\n"
        ^ "#include \"Message.hpp\"\n"
        ^ "#include \"SessionAddressedMessage.hpp\"\n"
        ^ "\n"
        ^ "class " ^ #name protocol ^ "Protocol\n"
        ^ "{\n"
        ^ "public:\n"
        ^ "\n"
        ^ hpp_messageTypes protocol
        ^ "\n"

    fun hpp protocol 
      = hpp_header protocol
        
        (*^ hpp_constantDefinitions protocol

        (*for(FlagSetDefinition flagSetDefinition
                : this.flagSetDefinitions){
            hpp_FlagSetDefinition(flagSetDefinition);
        }*)

        ^ hpp_messageTypeToString protocol
        ^ hpp_getMessageType protocol
        ^ hpp_messageToString protocol

        ^ foldl op^ "" (map hpp_messageDefinition ms)
        ^ hpp_footer protocol

        ^ hpp_handler protocol
*)
end
