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
#include <vector>
#include "Time.hpp"
#include "Table.hpp"
#include "GeneralProtocol.hpp"
#include "SessionAddressedMessage.hpp"

class GameServer : GeneralHandler
{
public:
  /** message received from client on old or new session. */
  void received(const std::vector<char>& message,
                const int32_t sessionID,
                std::list<SessionAddressedMessage>& messagesToBeSent,
                TimeMicro& timeout) throw();
  
  /** Session terminated. */
  void terminated(const int32_t& sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout) throw();

  /** Timeout happened. */
  void timeout(std::list<SessionAddressedMessage>& toBeSent,
               TimeMicro& timeout) throw();
                  
private:

  std::list<Table> tables;
  typedef std::list<Table>::iterator TableIterator;

  std::map<int32_t,TablePlayer*> sessionIdToTablePlayerPointer;
  std::map<TableId,TableIterator> tableIdToTableIterator;

  bool handle_1_CREATE_TICTACTOE_TABLE(const int32_t sessionID,
                                       std::list<SessionAddressedMessage>& toBeSent,
                                       TimeMicro& timeout) throw();

  bool handle_1_SAY(const int32_t sessionID,
                    std::list<SessionAddressedMessage>& toBeSent,
                    TimeMicro& timeout,
                    const std::string& text) throw();

  bool handle_1_JOIN_TABLE_TO_PLAY(const int32_t sessionID,
                                   std::list<SessionAddressedMessage>& toBeSent,
                                   TimeMicro& timeout,
                                   const int64_t tableId,
                                   const std::string& screenName) throw();
  
  bool handle_1_MAKE_MOVE(const int32_t sessionID,
                          std::list<SessionAddressedMessage>& toBeSent,
                          TimeMicro& timeout,
                          const std::vector<char>& move) throw();
};
