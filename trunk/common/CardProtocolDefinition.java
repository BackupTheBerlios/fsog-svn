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

public class CardProtocolDefinition{

    public static void main(String[] args) throws Exception{

        final int protocolVersion = 1;
        
        final ProtocolDefinition protocol
            = new ProtocolDefinition("Card",
                                     protocolVersion,
                                     "Card",
                                     "../server/Card.hpp",
                                     null,
                                     "../client/Card.java");

        protocol.defineConstants
            (new ConstantDefinition
             ("Used in card games. You can encode a"
              +" card on one byte by bitwise disjunction of value and"
              +" color. Use VALUE_MASK and SUIT_MASK to decode."
              +" The guarantee is that 0<(value|suit)<=127, so it"
              +" can be used for lookup in an array. Those values"
              +" are also used in Set55.",
              PieceType.INT8,"VALUE_MASK","0x0F"),
             new ConstantDefinition("",PieceType.INT8,"ACE","0x00"),
             new ConstantDefinition("",PieceType.INT8,"KING","0x01"),
             new ConstantDefinition("",PieceType.INT8,"QUEEN","0x02"),
             new ConstantDefinition("",PieceType.INT8,"JACK","0x03"),
             new ConstantDefinition("",PieceType.INT8,"TEN","0x04"),
             new ConstantDefinition("",PieceType.INT8,"NINE","0x05"),
             new ConstantDefinition("",PieceType.INT8,"EIGHT","0x06"),
             new ConstantDefinition("",PieceType.INT8,"SEVEN","0x07"),
             new ConstantDefinition("",PieceType.INT8,"SIX","0x08"),
             new ConstantDefinition("",PieceType.INT8,"FIVE","0x09"),
             new ConstantDefinition("",PieceType.INT8,"FOUR","0x0A"),
             new ConstantDefinition("",PieceType.INT8,"THREE","0x0B"),
             new ConstantDefinition("",PieceType.INT8,"TWO","0x0C"),
             new ConstantDefinition("",PieceType.INT8,"JOKER","0x0D"),
             new ConstantDefinition("",PieceType.INT8,"UNKNOWN_VALUE","0x0E"),
             new ConstantDefinition("",PieceType.INT8,"SUIT_MASK","0x70"),
             new ConstantDefinition("",PieceType.INT8,"HEART","0x00"),
             new ConstantDefinition("",PieceType.INT8,"DIAMOND","0x10"),
             new ConstantDefinition("",PieceType.INT8,"CLUB","0x20"),
             new ConstantDefinition("",PieceType.INT8,"SPADE","0x30"),
             new ConstantDefinition("",PieceType.INT8,"RED","0x40"),
             new ConstantDefinition("",PieceType.INT8,"BLACK","0x50"),
             new ConstantDefinition("",PieceType.INT8,"BLUE","0x60"),
             new ConstantDefinition("",PieceType.INT8,"UNKNOWN_SUIT","0x70"),
             new ConstantDefinition("",PieceType.INT8,"UNKNOWN","0x7E")
             );

        protocol.write();
    }
}
