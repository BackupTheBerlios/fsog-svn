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

public class Card123{


  //Constants:
  //Used in card games. You can encode a card on one byte by bitwise disjunction of value and color. Use VALUE_MASK and SUIT_MASK to decode. The guarantee is that 0<(value|suit)<=127, so it can be used for lookup in an array. Those values are also used in Set55.
  public static final byte VALUE_MASK = 0x0F;
  public static final byte ACE = 0x00;
  public static final byte KING = 0x01;
  public static final byte QUEEN = 0x02;
  public static final byte JACK = 0x03;
  public static final byte TEN = 0x04;
  public static final byte NINE = 0x05;
  public static final byte EIGHT = 0x06;
  public static final byte SEVEN = 0x07;
  public static final byte SIX = 0x08;
  public static final byte FIVE = 0x09;
  public static final byte FOUR = 0x0A;
  public static final byte THREE = 0x0B;
  public static final byte TWO = 0x0C;
  public static final byte JOKER = 0x0D;
  public static final byte UNKNOWN_VALUE = 0x0E;
  public static final byte SUIT_MASK = 0x70;
  public static final byte HEART = 0x00;
  public static final byte DIAMOND = 0x10;
  public static final byte CLUB = 0x20;
  public static final byte SPADE = 0x30;
  public static final byte RED = 0x40;
  public static final byte BLACK = 0x50;
  public static final byte BLUE = 0x60;
  public static final byte UNKNOWN_SUIT = 0x70;
  public static final byte UNKNOWN = 0x7E;


}
