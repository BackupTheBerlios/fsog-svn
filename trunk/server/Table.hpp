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

#include <map>
#include "Definitions.hpp"
#include "Game.hpp"
#include "TablePlayer.hpp"

class Table
{
private:
  static int64_t tableCounter;
  static TableId nextTableId() throw();

public:
  TurnGame* p_game;
  const TableId id;

  //Is table player id necessary?
  std::map<TablePlayerId,TablePlayer*> tablePlayerIdToTablePlayerPointer;

  std::vector<TablePlayer*> turnGamePlayerToTablePlayerPointer;

  /**
     If 0 is returned, no player can be added.
  */
  TablePlayerId nextTablePlayerId() throw()
  {
    if(tablePlayerIdToTablePlayerPointer.empty())
      return 1;

    TablePlayerId biggest = tablePlayerIdToTablePlayerPointer.rbegin()->first;

    //Maximum 20 players at table:
    if(biggest<19)
      return biggest+1;

    for(TablePlayerId id = 1; id<20; id++)
      if(tablePlayerIdToTablePlayerPointer.count(id)==0)
        return id;

    return 0;
  }

  Table()
    :p_game(0),
     id(Table::nextTableId())
  {
  }

  virtual ~Table()
  {
    if(this->p_game)
      {
        delete this->p_game;
        this->p_game=0;
      }
  }
};
