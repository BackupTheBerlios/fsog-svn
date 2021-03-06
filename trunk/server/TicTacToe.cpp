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
#include <set>
#include <iostream>
#include "CommandLine.hpp"

/** This game always starts in the same way, so it won't send any
    initial messages. */
bool TicTacToe::initialize(std::list<PlayerAddressedMessage>& /*messages*/) throw()
{
  //Initialization fails if table was created not for 2 players:
  if(this->numberOfPlayers!=2)
    return false;

  //We expect move only from the first player:
  setFirstPlayer();

  //Initialize board to be 3x3 with all empty fields:
  //TODO: not efficient. Have static empty board.
  this->board
    = std::vector<std::vector<Field> >(3,std::vector<Field>(3,EMPTY));
  this->empty = 9;
  
  //Ready for playing! After this function returns server will send
  //initial information to clients and will await move from the
  //players specified.
  return true;
}

MoveResult TicTacToe::move(const std::vector<char>& move,
                           std::list<PlayerAddressedMessage>& moveMessages,
                           std::list< std::set<Player> >& endResult)
  throw()
{
  if(CommandLine::printNetworkPackets())
    std::cout<<"TTT::MV "
             <<TicTacToeProtocol::messageToString(move);

  //First we deserialize the move. If deserialization fails, current
  //player sent invalid move.
  //TODO: It would be easier: deserialize(move,this->row,this->column)
  if(!TicTacToeProtocol::deserialize_1_TIC_TAC_TOE_MOVE
     (move,
      this->deserialized_1_TIC_TAC_TOE_MOVE))
    {
      std::cout<<"deserialize_1_MAKE_MOVE failed."
               <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
      return INVALID|END;
    }
  
  int8_t& row = this->deserialized_1_TIC_TAC_TOE_MOVE.row;
  int8_t& column = this->deserialized_1_TIC_TAC_TOE_MOVE.column;
  
  //Let's see whether the move is valid (both coordinates are within
  //<0,2> and the pointed field is still empty):
  if(row<0 || row>2 || column<0 || column>2 || board[row][column]!=EMPTY)
    {
      std::cout<<"Incorrect move: row=="<<static_cast<int>(row)
               <<", column=="<<static_cast<int>(column)
               <<", board[row][column]=="<<static_cast<int>(board[row][column])
               <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
      std::cout<<"Board: "<<std::endl;
      for(std::vector<std::vector<Field> >::const_iterator it = board.begin();
          it!=board.end();
          it++)
        {
          for(std::vector<Field>::const_iterator jt = it->begin();
              jt!=it->end();
              jt++)
            std::cout<<static_cast<int>(*jt)<<" ";
          std::cout<<std::endl;
        }
      return INVALID|END;
    }

  //OK, move is valid. Let's introduce game state change.
  
  const Field c = currentPlayersField();
  
  board[row][column] = c;
  empty--;

  //Send a message to opponent with what move was made:
  moveMessages.push_back(PlayerAddressedMessage(getOpponent()));
  TicTacToeProtocol::serialize_1_TIC_TAC_TOE_MOVE(row,column,
                                                  moveMessages.back().message);
  
  //Let's see whether we have 3--in--a--row after this move:
  if( (board[row][0]==c && board[row][1]==c && board[row][2]==c)
      ||(board[0][column]==c && board[1][column]==c && board[2][column]==c)
      ||(board[0][0]==c && board[1][1]==c && board[2][2]==c)
      ||(board[0][2]==c && board[1][1]==c && board[2][0]==c))
    {
      //Yes. The current player just won. We need to set "endResult"
      //correctly and return "END":
      endResult.push_back(std::set<Player>());
      endResult.back().insert(turn);
      endResult.push_back(std::set<Player>());
      endResult.back().insert(1-turn);
      return VALID|END;
    }
  
  //If all fields are filled, but three--in--a--row was not detected,
  //it's a draw.
  if(empty==0)
    {
      endResult.push_back(std::set<Player>());
      endResult.back().insert(0);
      endResult.back().insert(1);
      return VALID|END;
    }

  //Game shall continue.
  setNextPlayer();
  return VALID|CONTINUE;
}
