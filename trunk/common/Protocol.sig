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

signature Protocol =
sig
    (*  *)
    type kind
    val int8 : kind
    val int16 : kind
    val int24 : kind
    val int32 : kind
    val int64 : kind
    val cstring : kind
    val binary : kind
    val vector : kind -> kind

    (*  *)
    type element = {kind:kind,
                    name:string,
                    comment:string}

    (*  *)
    type message = {name:string,
                    comment:string,
                    elements:element list}
    (* TODO: value should be kind-dependent, not string. *)
    type constant = {name:string,
                     comment:string,
                     kind:kind,
                     value:string}

    (* One can define the protocol using a definition. *)
    type protocol_definition = {name:string,
                                version:int,
                                comment:string,
                                license:string,
                                messages:message list,
                                constants:constant list}

    type protocol

    exception problem of string

    (* Throws problem. *)
    val define : protocol_definition -> protocol

    (* Generate C++ header file for a given protocol.
                
     [cpp(p,ser,des,hppfile,cppfile)] has the side effect of writing a
     header file hppfile and implementation file cppfile. They will
     contain definition of protocol p, with serializers for messages
     ser and deserializers for messages des.
     *)
    val cpp : {protocol : protocol,
               ser : string list,
               des : string list,
               hppfile : string,
               cppfile : string}
              -> unit
end
