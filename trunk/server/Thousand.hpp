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


#pragma once

#include <stdint.h>
#include <vector>
#include "Game.hpp"
#include "ThousandProtocol.hpp"
#include "ThousandCardSet.hpp"

class Thousand : public TurnGame
{
private:

  enum Stage
    {
      BIDDING,
      SELECTING_FIRST,
      SELECTING_SECOND,
      CONTRACTING,
      PLAYING_FIRST,
      PLAYING_SECOND,
      PLAYING_THIRD,
      ENDED
    };

  static int_fast8_t deck[24];

  Stage stage;

  Player startsBidding;

  Player biddingWinner;

  ThousandCardSet must;
  std::vector<ThousandCardSet> sets;

  std::vector<int_fast8_t> bids10;
  int_fast8_t minimumNextBid10;

  int_fast8_t trumpShift;
  int_fast8_t firstShift;
  int_fast8_t secondShift;
  int_fast8_t thirdShift;

  std::vector<int_fast16_t> smallPoints;
  std::vector<int_fast32_t> bigPoints10;

  void deal() throw();
  int_fast8_t maxBid10() const throw();

public:

  Thousand(const Player numberOfPlayers) throw()
    :TurnGame(numberOfPlayers),
     stage(BIDDING),
     must(),
     sets(3)
  {}

  ~Thousand() throw() {}

  bool initialize(std::list<PlayerAddressedMessage>& initialMessages) throw();

  MoveResult move(const std::vector<char>& move,
                  std::list<PlayerAddressedMessage>& moveMessages,
                  std::list<std::set<Player> >& endResult) throw();

};
