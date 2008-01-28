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


#include "../server/Protocol.hpp"

  bool Server::handle(const Message&message) throw()
  {
    switch(message.getMessageType())
    {
    case LOG_IN_1:
      return Protocol::deserialize_1_LOG_IN(message,
                              this->deserialized_LOG_IN)
             && this->handle_1_LOG_IN();
    case GET_STATISTICS_1:
      return Protocol::deserialize_1_GET_STATISTICS(message)
             && this->handle_1_GET_STATISTICS();
    case SEARCH_GAME_1:
      return Protocol::deserialize_1_SEARCH_GAME(message,
                              this->deserialized_SEARCH_GAME)
             && this->handle_1_SEARCH_GAME();
    case ACKNOWLEDGE_1:
      return Protocol::deserialize_1_ACKNOWLEDGE(message,
                              this->deserialized_ACKNOWLEDGE)
             && this->handle_1_ACKNOWLEDGE();
    case GAME_START_1:
      return Protocol::deserialize_1_GAME_START(message)
             && this->handle_1_GAME_START();
    case GAME_BID_1:
      return Protocol::deserialize_1_GAME_BID(message,
                              this->deserialized_GAME_BID)
             && this->handle_1_GAME_BID();
    default:
      return false;
    }
  }
