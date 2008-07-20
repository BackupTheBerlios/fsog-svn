/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- 
 * vim:expandtab:shiftwidth=2:tabstop=2: */


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

#include "ThousandProtocol.hpp"

/**
   Space-efficient representation of card set for 24-card deck in
   game of Thousand. Each card's presence is specified by one bit in
   a 32-bit int value.
   
   The bits are used as follows:
   [31,24]:unused [23,18]:HEARTS [17,12]:DIAMONDS [11,6]:CLUBS [5,0]:SPADES

   5:ACE 4:TEN 3:KING 2:QUEEN 1:JACK 0:NINE

   Only 24 bits are used, so the value can be sent over network using
   only 3 bytes.
 */
class ThousandCardSet
{
  int32_t value;
public:

  ThousandCardSet()
    :value(0)
  {
  }

  const int32_t& getValue() const throw()
  {
    return value;
  }

  void setEmpty() throw()
  {
    value = 0;
  }

  bool isEmpty() const throw()
  {
    return value == 0;
  }

  void addShift(const int_fast8_t shift) throw()
  {
    value|=(1<<shift);
  }

  void addAll(const ThousandCardSet& other) throw()
  {
    value|=other.value;
  }

  bool containsShift(const int_fast8_t shift) const throw()
  {
    return (value & (1<<shift));
  }

  /*
  bool containsCard(const char card) const throw()
  {
    return (value & (1<<shift(card)))!=0;
  }
  */

  //For each card assigns what shift apply to 1 to represents presence
  //of that card. The shift is between 0 and 23.
  /*
  static int_fast8_t shift(const char card) throw()
  {
    const int_fast8_t value = card & Card::VALUE_MASK;
    const int_fast8_t suit = card & Card::SUIT_MASK;
    int_fast8_t shift = 0;
    switch(suit)
      {
      case Card::HEART: shift+=ThousandProtocol::HEART_SHIFT; break;
      case Card::DIAMOND: shift+=ThousandProtocol::DIAMOND_SHIFT; break;
      case Card::CLUB: shift+=ThousandProtocol::CLUB_SHIFT; break;
      case Card::SPADE: shift+=ThousandProtocol::SPADE_SHIFT; break;
      }
    switch(value)
      {
      case Card::ACE: shift+=ThousandProtocol::ACE_SHIFT; break;
      case Card::TEN: shift+=ThousandProtocol::TEN_SHIFT; break;
      case Card::KING: shift+=ThousandProtocol::KING_SHIFT; break;
      case Card::QUEEN: shift+=ThousandProtocol::QUEEN_SHIFT; break;
      case Card::JACK: shift+=ThousandProtocol::JACK_SHIFT; break;
      case Card::NINE: shift+=ThousandProtocol::NINE_SHIFT; break;
      }
    return shift;
  }
  */

  void removeShift(const int_fast8_t shift) throw()
  {
    value &= (~(1<<shift));
  }

  /** Remove a shift from this set. In the game of thousand, you have
      to put a higher card of the same color, if you have it.
  */
  bool removeFirstShift(const int_fast8_t shift,
                        int_fast8_t& trumpShift,
                        int_fast16_t& firstSmallPoints) throw();

  /**
     Remove a card from this set. Checks validity of the move.

     @param firstShift -- shift of the card that was played as the first card.

     @param secondShift shift of the card played now. This card is
     removed from this set by this function.

     @param trumpShift specifies what's the current trump suit.
  */
  bool removeSecondShift(const int_fast8_t firstShift,
                         const int_fast8_t secondShift,
                         const int_fast8_t trumpShift) throw();

  bool removeThirdShift(const int_fast8_t firstShift,
                        const int_fast8_t secondShift,
                        const int_fast8_t thirdShift,
                        const int_fast8_t trumpShift,
                        int_fast16_t&firstSmallPoints,
                        int_fast16_t&secondSmallPoints,
                        int_fast16_t&thirdSmallPoints) throw();
};
