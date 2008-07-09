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
#include "TicTacToeProtocol.hpp"

typedef uint8_t Field;
const Field EMPTY = 0x00;
const Field X = 0x01;
const Field O = 0x02;

class TicTacToe : public TurnGame
{
private:
  //Our 3x3 board:
  std::vector<std::vector<Field> > board;
  //Used for counting how many fields are still empty:
  uint8_t empty;

  //Temporary object for deserializing move message:
  TicTacToeProtocol::Deserialized_1_TIC_TAC_TOE_MOVE deserialized_1_TIC_TAC_TOE_MOVE;

  Field currentPlayersField() const throw()
  {
    return (turn==0 ? X : O);
  }

public:

  TicTacToe(const Player numberOfPlayers) throw()
    :TurnGame(numberOfPlayers)
  {}

  ~TicTacToe() throw() {}

  bool initialize(std::list<PlayerAddressedMessage>& initialMessages) throw();

  MoveResult move(const std::vector<char>& move,
                  std::list<PlayerAddressedMessage>& moveMessages,
                  std::list<std::set<Player> >& endResult) throw();

};
