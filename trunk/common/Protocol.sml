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
                  | int16
                  | int24
                  | int32
                  | int64
                  | cstring
                  | binary
                  | vector of kind

    type element = {kind:kind,
                    name:string,
                    comment:string}

    type message = {name:string,
                    comment:string,
                    elements:element list}

    (* Internal message, which includes identifiers *)
    type imessage = {(* Message identifier as string: *)
                     s_identifier:string,
                     name:string,
                     comment:string,
                     elements:element list}

    type constant = {name:string,
                     comment:string,
                     kind:kind,
                     value:string}

    type protocol_definition
      = {name:string,
         version:int,
         comment:string,
         license:string,
         messages:message list,
         constants:constant list}

    type protocol = {name:string,
                     (* Protocol version as string: *)
                     s_version:string,
                     comment:string,
                     license:string,
                     imessages:imessage list,
                     constants:constant list}

    exception problem of string

    fun define ({name,version,comment,license,messages,constants}
                :protocol_definition)
      = let
          fun imessages messages
            = let
                fun h _ [] = []
                  | h id ({name,comment,elements}::ms)
                    = {s_identifier=Int.toString id,
                       name=name,comment=comment,elements=elements}
                      ::(h (id+1) ms)
            in
                h 1 messages
            end
      in
          if version<1 orelse version>127
          then raise problem "Incorrect protocol version."
          else
              if length messages > 127
              then raise problem "Too many messages."
              else
                  {name=name,
                   (* Protocol version, as string: *)
                   s_version=Int.toString version,
                   comment=comment,
                   license=license,
                   imessages=imessages messages,
                   constants=constants}
      end

    fun kindToCppType int8 = "int8_t"
      | kindToCppType int16 = "int16_t"
      | kindToCppType int24 = "int32_t"
      | kindToCppType int32 = "int32_t"
      | kindToCppType int64 = "int64_t"
      | kindToCppType cstring = "std::string"
      | kindToCppType binary = "std::vector<char>"
      | kindToCppType (vector k) = "std::vector<"^kindToCppType k^" >"

    fun kindToCppConstType int8 = "const int8_t"
      | kindToCppConstType int16 = "const int16_t"
      | kindToCppConstType int24 = "const int32_t"
      | kindToCppConstType int32 = "const int32_t"
      | kindToCppConstType int64 = "const int64_t"
      | kindToCppConstType cstring = "const std::string&"
      | kindToCppConstType binary = "const std::vector<char>&"
      | kindToCppConstType (vector k) = "const std::vector<"^kindToCppType k^" >&"

    fun kindToAppender int8 = "append1Byte"
      | kindToAppender int16 = "append2Bytes"
      | kindToAppender int24 = "append3Bytes"
      | kindToAppender int32 = "append4Bytes"
      | kindToAppender int64 = "append8Bytes"
      | kindToAppender cstring = "appendCString"
      | kindToAppender binary = "appendBinary"
      | kindToAppender (vector k) = "appendVector"

    fun kindToReader int8 = "read1Byte"
      | kindToReader int16 = "read2Bytes"
      | kindToReader int24 = "read3Bytes"
      | kindToReader int32 = "read4Bytes"
      | kindToReader int64 = "read8Bytes"
      | kindToReader cstring = "readCString"
      | kindToReader binary = "readBinary"
      | kindToReader (vector k) = "readVector"

    fun member x ys = List.exists (fn y=>y=x) ys

    fun hpp_messageTypes (p:protocol) =
        let
            fun hpp_messageType (m:imessage)
              = "    //" ^ #comment m ^ "\n"
                ^ "    static const int8_t " ^ #name m
                ^ "_" ^ #s_version p
                ^ " = " ^ #s_identifier m ^ ";\n"
        in
            (* TODO: When only one message, don't waste one *)
            (* byte in serialized message for message type. *)
            "  //Can be used for switching to detect\n"
            ^ "  //which deserializer should be used.\n"
            ^ "    static const int8_t UNKNOWN_MESSAGE_"
            ^ #s_version p
            ^ " = 0;\n"
            ^ foldr op^ "" (map hpp_messageType (#imessages p))
            ^ "\n\n"
        end

    fun hpp_header (p:protocol)
      = "/*\n"
        ^ #license p
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
        ^ "class " ^ #name p ^ "Protocol\n"
        ^ "{\n"
        ^ "public:\n"
        ^ "\n"
        ^ hpp_messageTypes p
        ^ "\n"

    fun cpp_header (p:protocol)
      = "/*\n"
        ^ #license p
        ^ "\n*/\n"
        ^ "\n#include \"" ^ #name p ^ "Protocol.hpp\"\n";

    fun hpp_messageTypeToString (p:protocol)
      = "  //Can be used for printing message type\n"
        ^ "  //in human-readable form.\n"
        ^ "  static std::string messageTypeToString(int8_t messageType)"
        ^ " throw()"
        ^ "\n  {"
        ^ "\n    switch(messageType)"
        ^ "\n    {"
        ^ "\n      case UNKNOWN_MESSAGE_" ^ #s_version p
        ^ ": return \"UNKNOWN_MESSAGE\";"
        ^ (let
               fun h (m:imessage)
                 = "\n"
                   ^ "      case " ^ #name m
                   ^ "_" ^ #s_version p
                   ^ ": return \"" ^ #name m ^ "\";"
           in
               foldr op^ "" (map h (#imessages p))
           end)
        ^ "\n    }\n\n"
        ^ "\n    return \"ERROR. No such message in this protocol version!\";"
        ^ "\n  }\n\n"

    fun hpp_getMessageType _
      = "  //This method can be used for rapid message\n"
        ^ "  //type lookup, so you don't need to try\n"
        ^ "  //deserializing using all deserializers.\n"
        ^ "  //Remember that deserialization can still\n"
        ^ "  //fail, even if this method returns\n"
        ^ "  //some known type. It doesn't read the whole\n"
        ^ "  //message, just the part where message type\n"
        ^ "  //is present. If the message type cannot be\n"
        ^ "  //determined, 0 is returned. This could happen\n"
        ^ "  //e.g. if message is empty.\n"
        ^ "  static int8_t getMessageType(const std::vector<char>& message)"
        ^ " throw()\n"
        ^ "  {\n"
        ^ "    if(message.size()<2)\n"
        ^ "      return 0;\n"
        ^ "    \n"
        ^ "    return message[1];\n"
        ^ "  }\n\n"

    fun hpp_messageToString (p:protocol)
      = "  //Represent message as string\n"
        ^ "  static std::string messageToString(const std::vector<char>& message)\n"
        ^ "    throw()\n"
        ^ "  {\n"
        ^ "    std::ostringstream output;\n"
        ^ "\n"
        ^ "    output\n"
        ^ "      <<\"MSG \"<<"
        ^ "messageTypeToString(getMessageType(message))\n"
        ^ "      <<\" (" ^ #name p ^ " v" ^ #s_version p
        ^ ", \"<<message.size()<<\"B):\"<<std::endl;\n"
        ^ "\n"
        ^ "    const char*const hex = \"0123456789ABCDEF\";\n"
        ^ "\n"
        ^ "    for(uint_fast16_t line=0;20*line<message.size();line++)\n"
        ^ "    {\n"
        ^ "      //Print hexadecimal line:\n"
        ^ "      for(uint_fast16_t i=20*line;i<20*(line+1) && i<message.size();i++)\n"
        ^ "        {\n"
        ^ "          const char c = message[i];\n"
        ^ "          output<<hex[(c>>4) & 0x0F]<<hex[c & 0x0F]<<' ';\n"
        ^ "        }\n"
        ^ "      output<<std::endl;\n"
        ^ "      //Print human--readable line:\n"
        ^ "      for(uint_fast16_t i=20*line;i<20*(line+1) && i<message.size();i++)\n"
        ^ "        {\n"
        ^ "          const char c = message[i];\n"
        ^ "          if(std::isprint(c))\n"
        ^ "            output<<c<<\"  \";\n"
        ^ "          else\n"
        ^ "            output<<\"__ \";\n"
        ^ "        }\n"
        ^ "      output<<std::endl;\n"
        ^ "    }\n"
        ^ "    return output.str();\n"
        ^ "  }\n"

    fun hpp_messageDefinition {protocol:protocol,ser,des} (m:imessage) =
        let
            val pv = #s_version protocol
            val mn = #name m
            val mc = #comment m
            val mi = #s_identifier m
            val me = #elements m
            fun h1 (e:element) =
                "\n        //" ^ #comment e ^ "\n" ^ "        "
                ^ kindToCppConstType (#kind e) ^ " " ^ #name e ^ ",\n"
            fun h2 (e:element) =
                "    //Serialize " ^ #name e ^ ":\n"
                ^ "    Message::" ^ kindToAppender (#kind e)
                ^ "(" ^ #name e ^ ",outputMessage);\n"
            fun h3 (e:element) = 
                "    //" ^ #comment e ^ "\n"
                ^ "    " ^ kindToCppType (#kind e) ^ " " ^ #name e ^ ";\n"
            fun h4 (e:element) =
                "    //Deserialize " ^ #name e ^ ":\n"
                ^ "    if(!Message::" ^ kindToReader (#kind e)
                ^ "(it,messageEnd,output." ^ #name e ^ "))\n"
                ^ "      return false;\n"
            fun h5 (e:element) =
                ",\n        " ^ kindToCppType (#kind e) ^ "& " ^ #name e
            fun h6 (e:element) =
                "    //Deserialize " ^ #name e ^ ":\n"
                ^ "    if(!Message::" ^ kindToReader (#kind e)
                ^ "(it,messageEnd," ^ #name e ^ "))\n"
                ^ "      return false;\n"
        in
            "  //" ^ "Message " ^ mn ^ ":\n\n"
            ^ "  //In protocol version " ^ pv ^ " this message has id " ^ mi ^ ".\n"
            ^ "  //" ^ mc ^ "\n\n"
            ^ (if member m ser
               then
                   "  static void serialize_"^pv^"_"^mn^"("
                   ^ foldr op^ "" (map h1 me)
                   ^ "    std::vector<char>&outputMessage)\n"
                   ^ "    throw()\n"
                   ^ "  {\n"
                   ^ "    outputMessage.resize(0);\n"
                   ^ "    //Let the receiver know which "
                   ^ "protocol version this is:\n"
                   ^ "    Message::append1Byte("
                   ^ pv ^ ",outputMessage);\n"
                   ^ "    //Let the receiver know what kind "
                   ^ "of message this is:\n"
                   ^ "    Message::append1Byte(" ^ mi
                   ^ ",outputMessage);\n\n"
                   ^ foldr op^ "" (map h2 me)
                   ^ "  }\n\n"
               else "")
            ^ (if member m des
               then
                   "  class Deserialized_" ^ pv ^ "_" ^ mn ^ "\n"
                   ^ "  {\n"
                   ^ "  public:\n"
                   ^ foldr op^ "" (map h3 me)
                   ^ "  };\n\n"
                   (* Deserialize into one object: *)
                   ^ "  static bool deserialize_" ^ pv ^ "_" ^ mn
                   ^ "(const std::vector<char>&inputMessage"
                   ^ (if null me then ""
                      else ",\n        Deserialized_" ^ pv ^ "_" ^ mn ^ "&output")
                   ^ ")\n"
                   ^ "  throw()\n"
                   ^ "  {\n"
                   ^ "    std::vector<char>::const_iterator it\n"
                   ^ "     = inputMessage.begin();\n"
                   ^ "    const std::vector<char>::const_iterator messageEnd\n"
                   ^ "     = inputMessage.end();\n"
                   ^ "    \n"
                   ^ "    //Check protocol version:\n"
                   ^ "    char protocolVersion=0;\n"
                   ^ "    if(!Message::read1Byte(it,messageEnd,protocolVersion))\n"
                   ^ "      return false;\n"
                   ^ "    if(protocolVersion!=" ^ pv ^ ")\n"
                   ^ "      return false;\n"
                   ^ "    \n"
                   ^ "    //Check message kind:\n"
                   ^ "    char messageKind=0;\n"
                   ^ "    if(!Message::read1Byte(it,messageEnd,messageKind))\n"
                   ^ "      return false;\n"
                   ^ "    if(messageKind!=" ^ mi ^ ")\n"
                   ^ "      return false;\n\n"
                   ^ "    //Deserialize pieces:\n\n"
                   ^ foldr op^ "" (map h4 me)
                   ^ "    return true;\n"
                   ^ "  }\n\n"
                   (* End deserialize into single object. *)
                   (* Deserialize into multiple objects:*)
                   ^ (if null me then ""
                      else "  static bool deserialize_" ^ pv ^ "_" ^ mn
                           ^ "(const std::vector<char>&inputMessage"
                           ^ foldr op^ "" (map h5 me)
                           ^ ")\n"
                           ^ "  throw()\n"
                           ^ "  {\n"
                           ^ "    std::vector<char>::const_iterator it\n"
                           ^ "     = inputMessage.begin();\n"
                           ^ "    const std::vector<char>::const_iterator messageEnd\n"
                           ^ "     = inputMessage.end();\n"
                           ^ "    \n"
                           ^ "    //Check protocol version:\n"
                           ^ "    char protocolVersion=0;\n"
                           ^ "    if(!Message::read1Byte(it,messageEnd,protocolVersion))\n"
                           ^ "      return false;\n"
                           ^ "    if(protocolVersion!=" ^ pv ^ ")\n"
                           ^ "      return false;\n"
                           ^ "    \n"
                           ^ "    //Check message kind:\n"
                           ^ "    char messageKind=0;\n"
                           ^ "    if(!Message::read1Byte(it,messageEnd,messageKind))\n"
                           ^ "      return false;\n"
                           ^ "    if(messageKind!=" ^ mi ^ ")\n"
                           ^ "      return false;\n\n"
                           ^ "    //Deserialize pieces:\n\n"
                           ^ foldr op^ "" (map h6 me)
                           ^ "    return true;\n"
                           ^ "  }\n\n"
                     )
               (* End deserialize into multiple objects.*)

               else "")
        end

    fun hpp_footer protocol = "};\n"

    fun hpp_handler {protocol:protocol,ser,des} =
        "\n"
        ^ "class " ^ #name protocol ^ "Handler\n"
        ^ "{\n"
        ^ "  private:\n"
        ^ "  //Objects for temporary deserialization (to avoid creating\n"
        ^ "  //new ones all the time):\n"
        ^ (let
               fun h (m:imessage) =
                   "  " ^ #name protocol ^ "Protocol::Deserialized"
                   ^ "_" ^ #s_version protocol
                   ^ "_" ^ #name m
                   ^ " deserialized_" ^ #name m ^ ";\n"
           in
               foldr op^ "" (map h
                                 (List.filter (fn (m:imessage)=>member m des
                                                  andalso not (null (#elements m)))
                                              (#imessages protocol)))
           end)
        ^ "\n"
        ^ "public:\n"
        ^ "  bool handle(const std::vector<char>& message,\n"
        ^ "              const SessionId sessionID,\n"
        ^ "              std::list<SessionAddressedMessage>& toBeSent,\n"
        ^ "              TimeMicro& timeout) throw();\n"
        ^ "\n"
        ^ "  //Handlers for various message types:\n"
        ^ (let
               fun h (m:imessage) =
                   "  virtual bool handle" ^ "_" ^ #s_version protocol
                   ^ "_" ^ #name m ^ "(const SessionId sessionID,\n"
                   ^ "                  "
                   ^ "std::list<SessionAddressedMessage>& toBeSent,\n"
                   ^ "                  "
                   ^ "TimeMicro& timeout"
                   ^ foldr op^ "" (map (fn (e:element)=>
                                           ",\n" ^ "                  "
                                           ^ kindToCppConstType(#kind e)
                                           ^ " " ^ #name e)
                                       (#elements m))
                   ^ ") throw() =0;\n"
                   
           in
               foldr op^ "" (map h
                                 (List.filter (fn (m:imessage)=>member m des)
                                              (#imessages protocol)))
           end)
        ^ "  virtual ~" ^ #name protocol ^ "Handler() throw() {}\n"
        ^ "};\n"

    fun hpp_constants cs =
        "  //Constants:\n"
        ^ (let fun h {comment,kind,name,value} =
                   (if comment="" then "" else "  //" ^ comment ^ "\n")
                   ^ "  static " ^ kindToCppConstType kind ^ " " ^ name
                   ^ " = " ^ value ^ ";\n"
           in
               foldr op^ "" (map h cs)
           end)
        ^ "\n\n"

    fun hpp_all (info as {protocol,ser,des})
      = hpp_header protocol
        ^ hpp_constants (#constants protocol)
        ^ hpp_messageTypeToString protocol
        ^ hpp_getMessageType protocol
        ^ hpp_messageToString protocol
        ^ foldr op^ "" (map (hpp_messageDefinition info)
                            (#imessages protocol))
        ^ hpp_footer protocol
        ^ hpp_handler info

    fun cpp_handler {protocol:protocol,des}
      = "\n"
        ^ "  bool " ^ #name protocol
        ^ "Handler::handle(const std::vector<char>& message,\n"
        ^ "                  const SessionId sessionID,\n"
        ^ "                  std::list<SessionAddressedMessage>& toBeSent\n,"
        ^ "                  TimeMicro& timeout) throw()\n"
        ^ "  {\n"
        ^ "\n"
        ^ "    switch(" ^ #name protocol ^ "Protocol::getMessageType(message))\n"
        ^ "    {\n"
        ^ (let fun h (m:imessage)
                 = "    case " ^ #name protocol ^ "Protocol::"
                   ^ #name m ^ "_" ^ #s_version protocol ^ ":\n"
                   ^ "      return " ^ #name protocol ^ "Protocol::deserialize_"
                   ^ #s_version protocol ^ "_" ^ #name m ^ "(message"
                   ^(if null (#elements m) then ""
                     else ",\n"
                          ^ "                              "
                          ^ "this->deserialized_" ^ #name m)
                   ^ ")\n"
                   ^ "             && this->handle_" ^ #s_version protocol
                   ^ "_" ^ #name m ^ "(sessionID,toBeSent,timeout"
                   ^ foldr op^ "" (map (fn (e:element)
                                           =>",\n                      "
                                             ^ "this->deserialized_" ^ #name m
                                             ^ "." ^ #name e)
                                       (#elements m))
                   ^ ");\n"
           in
               foldr op^ "" (map h des)
           end)
        ^ "    default:\n"
        ^ "      return false;\n"
        ^ "    }\n"
        ^ "  }\n"

    fun cpp_all (info as {protocol,ser,des})
      = cpp_header protocol
        ^ cpp_handler {protocol=protocol,des=des}

    fun stringToFile string filename =
        let
            val out_stream = TextIO.openOut filename
        in
            TextIO.output(out_stream,string);
            TextIO.flushOut out_stream;
            TextIO.closeOut out_stream
        end

    fun name2imessage imessages n =
        case List.find (fn (m:imessage)=>(#name m)=n) imessages of
            SOME x => x
          | NONE => raise problem ("No message with name \"" ^ n ^"\".")

    fun names2imessages imessages names =
        map (name2imessage imessages) names

    fun cpp {protocol,ser,des,hppfile,cppfile} =
        let
            val s = names2imessages (#imessages protocol) ser
            val d = names2imessages (#imessages protocol) des
            val hppcontent = hpp_all {protocol=protocol,ser=s,des=d}
            val cppcontent = cpp_all {protocol=protocol,ser=s,des=d}
        in
            stringToFile hppcontent hppfile;
            stringToFile cppcontent cppfile
        end
end
