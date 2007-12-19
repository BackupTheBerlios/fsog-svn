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
#include "Game.hpp"
#include "Address.hpp"
#include "User.hpp"
#include <vector>
#include <map>

class User;

class Thousand : public Game
{
public:
  //Settings of this game:
  const int32_t flags;

  enum State
    {
      WAITING_FOR_START = 0x01,
      BIDDING = 0x02,
      PLAY = 0x03
    };

  //What's this game's state:
  char state;

  //How many people play:
  const char numberOfPlayers;

  //Set of players:
  std::vector<std::map<Address,User>::iterator> players;

private:  
  //Who's dealing the cards:
  char dealer;
  
  //3 cards on must:
  std::vector<char> must;
  //2 cards on first small must (for 2 players game):
  std::vector<char> smallMust1;
  //2 cards on second small must (for 2 players game):
  std::vector<char> smallMust2;

  static std::vector<char> createDeck() throw();
  static std::vector<char> deck;
  
  //People can agree on few possible game types. This function
  //choses one game type. E.g. if they both wanted to play 10
  //or 15 minutes, this function will leave only one
  //setting. Also removes the setting about number of players.
  static int32_t purifyFlags(const int32_t flags)
    throw();
public:
  //ctor:
  Thousand(const char numberOfPlayers,
           const int32_t flags) throw();

  void deal() throw();

};
