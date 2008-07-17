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

#include "Card.hpp"
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

  void addShift(const int8_t shift) throw()
  {
    value|=(1<<shift);
  }

  void addAll(const ThousandCardSet& other) throw()
  {
    value|=other.value;
  }

  bool containsShift(const int8_t shift) const throw()
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
  static int8_t shift(const char card) throw()
  {
    const int8_t value = card & Card::VALUE_MASK;
    const int8_t suit = card & Card::SUIT_MASK;
    int8_t shift = 0;
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

  void removeShift(const int8_t shift) throw()
  {
    value &= (~(1<<shift));
  }

  /** Remove a shift from this set. In the game of thousand, you have
      to put a higher card of the same color, if you have it.
  */
  bool removeFirstShift(const int8_t shift,
                        int8_t& trumpShift) throw()
  {
    //Did the player have the card?
    if(value & (1<<shift))
      {
        value &= (~(1<<shift));
        //Shall we set new trump?
        if(((shift%6)==ThousandProtocol::QUEEN_SHIFT
            && ((1<<(shift+1)) & value))
           ||((shift%6)==ThousandProtocol::KING_SHIFT
              && ((1<<(shift-1)) & value)))
          trumpShift = 6*(shift/6);
        return true;
      }
    else
      return false;
  }

  bool removeSecondShift(const int8_t firstShift,
                         const int8_t secondShift,
                         const int8_t trumpShift) throw()
  {
    //If there's no such card, return false.
    if(!(value & (1<<secondShift)))
      return false;

    //Remove the card:
    value &= (~(1<<secondShift));
    
    const int8_t firstValueShift = firstShift%6;
    const int8_t firstSuitShift = 6*(firstShift/6);
    //const int8_t secondSuitShift = 6*(shift/6);
    if((firstShift/6)==(secondShift/6))
      {//Same suit
        //If higher card was played, it's a valid move.
        if(secondShift>firstShift)
          return true;
        else
          {//Lower card was played. It's a valid move, if there was no
           //higher.
           // 000100 000000 - Queen of clubs played first (shift 6+2)
           // 000000 111111 - 0x3f
           // 000000 000111 - 0x3f >> 2+1
           // 111000 000000 - All higher in same suit
            return !(value & ((0x3F>>(firstValueShift+1))<<(firstShift+1)));
          }
      }
    else
      {//Different suit was played.
        //If the player has cards of first card suit, move is invalid.
        if(value & (0x3F<<firstSuitShift))
          return false;
        //Was trump played?
        if(secondShift & (0x3F<<trumpShift))
          {//Trump on non--trump was played, so it's OK.
            return true;
          }
        else
          {//Non--trump played. It's OK if we don't have a trump.
            return (value & (0x3F<<trumpShift))==0;
          }
      }
  }

};
