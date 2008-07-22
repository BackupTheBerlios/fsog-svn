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

#include "ThousandCardSet.hpp"

int_fast8_t ThousandCardSet::points[24] = {11,10,4,3,2,0,
                                           11,10,4,3,2,0,
                                           11,10,4,3,2,0,
                                           11,10,4,3,2,0};

int_fast8_t ThousandCardSet::marriagePoints[24] = {0,0,100,100,0,0,
                                                   0,0,80,80,0,0,
                                                   0,0,60,60,0,0,
                                                   0,0,40,40,0,0};

int_fast8_t ThousandCardSet::valueShift(const int_fast8_t shift) throw()
{
  return shift%6;
}

int_fast8_t ThousandCardSet::suitShift(const int_fast8_t shift) throw()
{
  return 6*(shift/6);
}

bool ThousandCardSet::hasAnyOf(const int_fast32_t set) const throw()
{
  return (value&set)!=0;
}

ThousandCardSet::ThousandCardSet() throw()
  :value(0)
{
}

void ThousandCardSet::setEmpty() throw()
{
  value = 0;
}

bool ThousandCardSet::isEmpty() const throw()
{
  return value == 0;
}

void ThousandCardSet::addShift(const int_fast8_t shift) throw()
{
  value|=(1<<shift);
}

void ThousandCardSet::addAll(const ThousandCardSet& other) throw()
{
  value|=other.value;
}

bool ThousandCardSet::containsShift(const int_fast8_t shift) const throw()
{
  return (value & (1<<shift)) != 0;
}

void ThousandCardSet::removeShift(const int_fast8_t shift) throw()
{
  value &= (~(1<<shift));
}

bool ThousandCardSet::removeFirstShift(const int_fast8_t shift,
                                       int_fast8_t& trumpShift,
                                       int_fast16_t& firstSmallPoints) throw()
{
  //Did the player have the card?
  if(!containsShift(shift))
    return false;

  //Remove the card:
  removeShift(shift);

  //Shall we set new trump?
  if((valueShift(shift)==ThousandProtocol::QUEEN_SHIFT && containsShift(shift+1))
     ||(valueShift(shift)==ThousandProtocol::KING_SHIFT && containsShift(shift-1)))
    {
      firstSmallPoints+=marriagePoints[shift];
      trumpShift = suitShift(shift);
    }
  return true;
}

bool ThousandCardSet::removeSecondShift(const int_fast8_t firstShift,
                                        const int_fast8_t secondShift,
                                        const int_fast8_t trumpShift) throw()
{
  //If there's no such card, return false.
  if(!containsShift(secondShift))
    return false;
  
  //Remove the card:
  removeShift(secondShift);
  
  const int_fast8_t firstValueShift = valueShift(firstShift);
  const int_fast8_t firstSuitShift = suitShift(firstShift);
  const int_fast8_t secondSuitShift = suitShift(secondShift);
  
  //Is it the same suit?
  if(firstSuitShift==secondSuitShift)
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
          return !hasAnyOf((0x3F>>(firstValueShift+1))
                           <<(firstShift+1));
        }
    }
  else
    {//Different suit was played.
      //If the player has cards of first card suit, move is invalid.
      if(hasAnyOf(0x3F<<firstSuitShift))
        return false;
      //Was trump played?
      if(secondSuitShift==trumpShift)
        {//Trump on non--trump was played, so it's OK.
          return true;
        }
      else
        {//Non--trump played. It's OK if we don't have a trump.
          return !hasAnyOf(0x3F<<trumpShift);
        }
    }
}

bool ThousandCardSet::removeThirdShift(const int_fast8_t firstShift,
                                       const int_fast8_t secondShift,
                                       const int_fast8_t thirdShift,
                                       const int_fast8_t trumpShift,
                                       int_fast16_t&firstSmallPoints,
                                       int_fast16_t&secondSmallPoints,
                                       int_fast16_t&thirdSmallPoints,
                                       int_fast8_t& turnIncrement) throw()
{
  //If there's no such card, return false.
  if(!containsShift(thirdShift))
    return false;

  //Remove the card:
  removeShift(thirdShift);

  const int_fast16_t sum
    = points[firstShift]
    + points[secondShift]
    + points[thirdShift];
    
  //const int_fast8_t firstValueShift = valueShift(firstShift);
  const int_fast8_t firstSuitShift = suitShift(firstShift);
  const int_fast8_t secondValueShift = valueShift(secondShift);
  const int_fast8_t secondSuitShift = suitShift(secondShift);
  const int_fast8_t thirdSuitShift = suitShift(thirdShift);

  //Is it the same suit as the first card?
  if(firstSuitShift==thirdSuitShift)
    {//Same suit
      //If second was trump (and first not), we don't need to play higher:
      if(secondSuitShift==trumpShift && secondSuitShift!=firstSuitShift)
        {
          secondSmallPoints+=sum;
          turnIncrement=2;
          return true;
        }
      //Second was not trump (or all are trumps). Which card should be beaten?
      const bool secondWins
        = secondSuitShift==firstSuitShift && secondShift>firstShift;
      const int_fast8_t maxShift
        = secondWins ? secondShift:firstShift;
      const int_fast8_t maxValueShift = valueShift(maxShift);
      //If higher card was played, it's a valid move.
      if(thirdShift>maxShift)
        {
          thirdSmallPoints+=sum;
          turnIncrement=0;
          return true;
        }
      else
        {//Lower card was played. It's a valid move, if there was no
          //higher.
          // 000100 000000 - Queen of clubs played first (shift 6+2)
          // 000000 111111 - 0x3f
          // 000000 000111 - 0x3f >> 2+1
          // 111000 000000 - All higher in same suit
          if(secondWins)
            {
              secondSmallPoints+=sum;
              turnIncrement=2;
            }
          else
            {
              firstSmallPoints+=sum;
              turnIncrement=1;
            }
          return !hasAnyOf((0x3F>>(maxValueShift+1))
                           <<(maxShift+1));
        }
    }
  else
    {//Different suit was played.
      //If the player has cards of first card suit, move is invalid.
      if(hasAnyOf(0x3F<<firstSuitShift))
        return false;
      //Was trump played?
      if(thirdSuitShift==trumpShift)
        {//1:non--trump 3:trump
          //Was the second card also a trump?
          if(secondSuitShift==trumpShift)
            {//1:non--trump 2:trump 3:trump
              //If we can, we have to beat the second card.
              if(thirdShift>secondShift)
                {
                  thirdSmallPoints+=sum;
                  turnIncrement=0;
                  return true;
                }
              else
                {//Lower card was played. It's a valid move, if there was no
                  //higher.
                  secondSmallPoints+=sum;
                  turnIncrement=2;
                  //For how this is calculated, read above.
                  return !hasAnyOf((0x3F>>(secondValueShift+1))
                                   <<(secondShift+1));
                }                
            }
          else
            {//1:non--trump 2:non--trump 3:trump
              thirdSmallPoints+=sum;
              turnIncrement=0;
              return true;
            }
        }
      else
        {//1:non--trump 3:non--trump
          if(secondSuitShift==trumpShift
             ||(secondSuitShift==firstSuitShift && secondShift>firstShift))
            {
              secondSmallPoints+=sum;
              turnIncrement=2;
            }
          else
            {
              firstSmallPoints+=sum;
              turnIncrement=1;
            }
          //Non--trump played. It's OK if we don't have a trump.
          return !(hasAnyOf(0x3F<<trumpShift));
        }
    }
}
