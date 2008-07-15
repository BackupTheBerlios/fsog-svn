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


#include "GeneralProtocol.hpp"

  bool GeneralHandler::handle(const std::vector<char>& message,
                  const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent
,                  TimeMicro& timeout) throw()
  {

    switch(GeneralProtocol::getMessageType(message))
    {
    case GeneralProtocol::CREATE_TICTACTOE_TABLE_1:
      return GeneralProtocol::deserialize_1_CREATE_TICTACTOE_TABLE(message)
             && this->handle_1_CREATE_TICTACTOE_TABLE(sessionID,toBeSent,timeout);
    case GeneralProtocol::CREATE_THOUSAND_TABLE_1:
      return GeneralProtocol::deserialize_1_CREATE_THOUSAND_TABLE(message)
             && this->handle_1_CREATE_THOUSAND_TABLE(sessionID,toBeSent,timeout);
    case GeneralProtocol::SAY_1:
      return GeneralProtocol::deserialize_1_SAY(message,
                              this->deserialized_SAY)
             && this->handle_1_SAY(sessionID,toBeSent,timeout,
                      this->deserialized_SAY.text_UTF8);
    case GeneralProtocol::JOIN_TABLE_TO_PLAY_1:
      return GeneralProtocol::deserialize_1_JOIN_TABLE_TO_PLAY(message,
                              this->deserialized_JOIN_TABLE_TO_PLAY)
             && this->handle_1_JOIN_TABLE_TO_PLAY(sessionID,toBeSent,timeout,
                      this->deserialized_JOIN_TABLE_TO_PLAY.tableId,
                      this->deserialized_JOIN_TABLE_TO_PLAY.screenName);
    case GeneralProtocol::MAKE_MOVE_1:
      return GeneralProtocol::deserialize_1_MAKE_MOVE(message,
                              this->deserialized_MAKE_MOVE)
             && this->handle_1_MAKE_MOVE(sessionID,toBeSent,timeout,
                      this->deserialized_MAKE_MOVE.gameMove);
    default:
      return false;
    }
  }
