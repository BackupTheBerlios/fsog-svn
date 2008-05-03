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

#include "TicTacToe.hpp"

bool TicTacToe::initialize(std::vector<Message>& initialMessages) throw()
{
  if(this->numberOfPlayers!=2 || this->nicks.size()!=2)
    return false;
  //Randomize order of players:
  this->shuffleNicks();
  
  //Initialize board to be 3x3 with all empty fields:
  this->board
    = std::vector<std::vector<Field> >(3,std::vector<Field>(3,EMPTY));
  this->covered = 0;
  
  //Ready for playing! After this function returns server will send
  //initial information to clients and will await move from the
  //first player (which is nick[0]).
  return true;
}

Game::MoveResult TicTacToe::move(const Message& move,
                                 std::vector<Message>& moveMessages,
                                 std::list< std::list<std::string> >& endResult)
  throw()
{
  //First we deserialize the move. If deserialization fails, current
  //player sent invalid move.
  if(!TicTacToeProtocol::deserialize_1_MAKE_MOVE(move,
                                                 this->deserialized_1_MAKE_MOVE))
    return currentPlayerNumber;
  
  int8_t& row = this->deserialized_1_MAKE_MOVE.row;
  int8_t& column = this->deserialized_1_MAKE_MOVE.column;
  
  //Let's see whether the move is valid (both coordinates are within
  //<0,2> and the pointed field is still empty):
  if(row<0 || row>2 || column<0 || column>2 || board[row][column]!=EMPTY)
    return currentPlayerNumber;
  
  const Field c = currentPlayersField();
  
  board[row][column] = c;
    
  covered++;
  
  //Let's see whether we have 3--in--a--row after this move:
  if( (board[row][0]==c && board[row][1]==c && board[row][2]==c)
      ||(board[0][column]==c && board[1][column]==c && board[2][column]==c)
      ||(board[0][0]==c && board[1][1]==c && board[2][2]==c)
      ||(board[0][2]==c && board[1][1]==c && board[2][0]==c))
    {
      //Yes. The current player just won. We need to set "endResult"
      //correctly and return "END":
      endResult.push_back(std::list<std::string>(1,nicks[currentPlayerNumber]));
      endResult.push_back(std::list<std::string>(1,nicks[1-currentPlayerNumber]));
      return END;
    }
  
  //If all fields are filled, it's draw.
  if(covered==9)
    {
      endResult.push_back(std::list<std::string>());
      (*(endResult.rbegin())).push_back(nicks[0]);
      (*(endResult.rbegin())).push_back(nicks[1]);
      return END;
    }
  
  return CONTINUE;
}
