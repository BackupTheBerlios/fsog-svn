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


#include "ThousandProtocol.hpp"

  bool ThousandHandler::handle(const std::vector<char>& message,
                  const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent
,                  TimeMicro& timeout) throw()
  {

    switch(ThousandProtocol::getMessageType(message))
    {
    case ThousandProtocol::BID_1:
      return ThousandProtocol::deserialize_1_BID(message,
                              this->deserialized_BID)
             && this->handle_1_BID(sessionID,toBeSent,timeout,
                      this->deserialized_BID.bid10);
    case ThousandProtocol::SELECT_1:
      return ThousandProtocol::deserialize_1_SELECT(message,
                              this->deserialized_SELECT)
             && this->handle_1_SELECT(sessionID,toBeSent,timeout,
                      this->deserialized_SELECT.shift);
    case ThousandProtocol::SELECT_HIDDEN_1:
      return ThousandProtocol::deserialize_1_SELECT_HIDDEN(message)
             && this->handle_1_SELECT_HIDDEN(sessionID,toBeSent,timeout);
    case ThousandProtocol::CONTRACT_1:
      return ThousandProtocol::deserialize_1_CONTRACT(message,
                              this->deserialized_CONTRACT)
             && this->handle_1_CONTRACT(sessionID,toBeSent,timeout,
                      this->deserialized_CONTRACT.contract10);
    case ThousandProtocol::PLAY_1:
      return ThousandProtocol::deserialize_1_PLAY(message,
                              this->deserialized_PLAY)
             && this->handle_1_PLAY(sessionID,toBeSent,timeout,
                      this->deserialized_PLAY.shift);
    default:
      return false;
    }
  }
