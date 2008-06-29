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

#include <string>
#include <stdint.h>
#include "Table.hpp"

class Table;

class TablePlayer
{
public:
  //What is my session id?
  const int32_t sessionId;
  //My screen name:
  const std::string screenName;
  //Table I'm playing at:
  const Table& table;
  //TablePlayerId at the table I'm playing at:
  const TablePlayerId tablePlayerId;
  //Game Player in the game I'm playing:
  //TODO: assign it somewhere.
  Player turnGamePlayer;

public:
  
  TablePlayer(const int32_t sessionId,
              const std::string& screenName,
              const Table&table,
              const TablePlayerId tablePlayerId)
    :sessionId(sessionId),
     screenName(screenName),
     table(table),
     tablePlayerId(tablePlayerId),
     turnGamePlayer(-10)
  {
  }

  virtual ~TablePlayer()
  {
  }
};
