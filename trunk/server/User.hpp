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
#include "Thousand.hpp"
#include <vector>
#include <map>
#include <list>
#include "Protocol.hpp"
#include "Time.hpp"

class Thousand;

class User
{
public:
  std::map<Address,std::list<User>::iterator>::iterator myAddressToUserIterator;
  std::map<std::string,std::list<User>::iterator>::iterator myNickToUserIterator;
  //Iterator pointing to the game in the list of games of
  //which this user is a member. If not member of any game, it
  //will point to the end of the list of games.
  std::list<Thousand>::iterator game;
  //List of cards. See Protocol for how they are represented
  //on 1 byte.
  std::vector<char> cards;
  //TODO: use platform-independent type names here.
  //Points from all rounds, measured 1:1.
  short totalPoints;
  //Points gathered so far in current round, measured 1:1.
  short roundPoints;

  //Last SEARCH_GAME of this user:
  Protocol::Deserialized_1_SEARCH_GAME last_SEARCH_GAME;
  //Next acknowledge secret:
  char nextAcknowledgeSecret;
  //Next timeout index. Have a look at 
  uint8_t nextTimeoutSeconds;
  //Did this user already press start?
  bool pressedStart;

  //What message did the server send most recently?
  Message serverSent;
  //Iterator to entry in timeouts. This iterator is only valid if
  //this->serverSent!=UNKNOWN_MESSAGE
  std::multimap<TimeMicro,std::list<User>::iterator>::iterator myTimeout;

  //ctor
  User(std::list<Thousand>::iterator game) throw()
    :game(game),
     nextAcknowledgeSecret(1),
     pressedStart(false),
     serverSent()
  {
    //Some versions of the game might need so much.
    //TODO: better reservation management.
    this->cards.reserve(12);
  }
};
