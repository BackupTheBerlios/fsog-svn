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

#pragma once

#include <vector>
#include <string>
#include <sstream>
#include <cctype>
#include <list>
#include "Time.hpp"
#include "Message.hpp"
#include "SessionAddressedMessage.hpp"

class Card
{
public:


  //Constants:
  //Used in card games. You can encode a card on one byte by bitwise disjunction of value and color. Use VALUE_MASK and SUIT_MASK to decode. The guarantee is that 0<(value|suit)<=127, so it can be used for lookup in an array. Those values are also used in Set55.
  static const int8_t VALUE_MASK = 0x0F;
  //
  static const int8_t ACE = 0x00;
  //
  static const int8_t KING = 0x01;
  //
  static const int8_t QUEEN = 0x02;
  //
  static const int8_t JACK = 0x03;
  //
  static const int8_t TEN = 0x04;
  //
  static const int8_t NINE = 0x05;
  //
  static const int8_t EIGHT = 0x06;
  //
  static const int8_t SEVEN = 0x07;
  //
  static const int8_t SIX = 0x08;
  //
  static const int8_t FIVE = 0x09;
  //
  static const int8_t FOUR = 0x0A;
  //
  static const int8_t THREE = 0x0B;
  //
  static const int8_t TWO = 0x0C;
  //
  static const int8_t JOKER = 0x0D;
  //
  static const int8_t UNKNOWN_VALUE = 0x0E;
  //
  static const int8_t SUIT_MASK = 0x70;
  //
  static const int8_t HEART = 0x00;
  //
  static const int8_t DIAMOND = 0x10;
  //
  static const int8_t CLUB = 0x20;
  //
  static const int8_t SPADE = 0x30;
  //
  static const int8_t RED = 0x40;
  //
  static const int8_t BLACK = 0x50;
  //
  static const int8_t BLUE = 0x60;
  //
  static const int8_t UNKNOWN_SUIT = 0x70;
  //
  static const int8_t UNKNOWN = 0x7E;


};
