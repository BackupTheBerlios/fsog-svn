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


#include "../server/TicTacToeProtocol.hpp"

  bool TicTacToeHandler::handle(const std::vector<char>& message,
                  const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent
,                  TimeMicro& timeout) throw()
  {

    switch(TicTacToeProtocol::getMessageType(message))
    {
    case TicTacToeProtocol::TIC_TAC_TOE_MOVE_1:
      return TicTacToeProtocol::deserialize_1_TIC_TAC_TOE_MOVE(message,
                              this->deserialized_TIC_TAC_TOE_MOVE)
             && this->handle_1_TIC_TAC_TOE_MOVE(sessionID,toBeSent,timeout,
                      this->deserialized_TIC_TAC_TOE_MOVE.row,
                      this->deserialized_TIC_TAC_TOE_MOVE.column);
    default:
      return false;
    }
  }
