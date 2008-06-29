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

#include "GameServer.hpp"
#include "CommandLine.hpp"
#include "TicTacToe.hpp"

void GameServer::received(const std::vector<char>& message,
                          const int32_t sessionID,
                          std::list<SessionAddressedMessage>& toBeSent,
                          TimeMicro& timeout) throw()
{
  if(CommandLine::printNetworkPackets())
    std::cout<<"Received message:"<<std::endl
             <<GeneralProtocol::messageToString(message)<<std::endl
             <<"on session: "<<sessionID<<std::endl;

  const bool ok = this->handle(message,sessionID,toBeSent,timeout);
  
  if(!ok)
    std::cout<<"GameServer: handling message failed!"<<std::endl;
}

bool GameServer::handle_1_CREATE_TICTACTOE_TABLE(const int32_t sessionID,
                                                 std::list<SessionAddressedMessage>& toBeSent,
                                                 TimeMicro& /*timeout*/) throw()
{
  //Create new table:
  //TODO: remove this table at some point!
  this->tables.push_front(Table());

  Table& table = tables.front();
  tableIdToTableIterator[table.id]=tables.begin();

  table.p_game = 0;
  table.p_game = new TicTacToe(2);
  if(!table.p_game)
    {
      tables.pop_front();
      return false;
    }

  std::cout<<"New table's id: "<<table.id<<std::endl;

  //Prepare response:
  //Demand sending response to the sender (same sessionID):
  toBeSent.push_back(SessionAddressedMessage(sessionID));
  GeneralProtocol::serialize_1_TABLE_CREATED(table.id,toBeSent.back().message);

  return true;
}

bool GameServer::handle_1_JOIN_TABLE_TO_PLAY
(const int32_t sessionId,
 std::list<SessionAddressedMessage>& toBeSent,
 TimeMicro& /*timeout*/,
 const int64_t tableId,
 const std::string& screenName) throw()
{
  //Check whether table exists:
  std::map<TableId,TableIterator>::iterator entryIterator = 
    tableIdToTableIterator.find(tableId);

  if(entryIterator == tableIdToTableIterator.end())
    {
      //TODO: Send a message back to client saying table doesn't
      //exist.
      std::cout<<"No table with id: "<<tableId<<std::endl;
      return false;
    }

  //Table exists.

  TableIterator tableIterator = entryIterator->second;

  Table& table = *tableIterator;
  
  if(sessionIdToTablePlayerPointer.find(sessionId)!=sessionIdToTablePlayerPointer.end())
    {
      //TODO: What if session was at another table already?
      std::cout<<"Session at another table!"<<std::endl;
      return false;
    }

  const TablePlayerId tablePlayerId
    = table.nextTablePlayerId();

  if(!tablePlayerId)
    {
      //TODO: better handling of this problem. Perhaps a message.
      std::cout<<"Invalid tablePlayerId!"<<std::endl;
      return false;
    }

  //TODO: delete at some time!
  TablePlayer* tablePlayerPointer
    = new TablePlayer(sessionId,screenName,table,tablePlayerId);

  sessionIdToTablePlayerPointer.insert(std::pair<int32_t,TablePlayer*>(sessionId,
                                                                tablePlayerPointer));

  //Let old players know that new player is here:
  //Also let the new player know what old players are here:
  std::vector<char> toOthers;
  GeneralProtocol::serialize_1_NEW_PLAYER_JOINED_TABLE(screenName,
                                                       tablePlayerId,
                                                       toOthers);

  for(std::map<TablePlayerId,TablePlayer*>::const_iterator it
        = table.tablePlayerIdToTablePlayerPointer.begin();
      it!=table.tablePlayerIdToTablePlayerPointer.end();
      it++)
    {
      toBeSent.push_back(SessionAddressedMessage(it->second->sessionId));
      toBeSent.back().message = toOthers;

      toBeSent.push_back(SessionAddressedMessage(sessionId));
      GeneralProtocol::serialize_1_NEW_PLAYER_JOINED_TABLE
        (it->second->screenName,
         it->first,
         toBeSent.back().message);
    }


  //TODO: remove this when session dies.
  table.tablePlayerIdToTablePlayerPointer.insert
    (std::pair<TablePlayerId,TablePlayer*>(tablePlayerId,tablePlayerPointer));

  //Let new player know what's her tablePlayerId:
  toBeSent.push_back(SessionAddressedMessage(sessionId));
  GeneralProtocol::serialize_1_YOU_JOINED_TABLE(tablePlayerId,
                                                toBeSent.back().message);


  //If enough players, start the game!
  if(table.p_game->numberOfPlayers==table.tablePlayerIdToTablePlayerPointer.size())
    {
      //Assign turnGamePlayer to each TablePlayer:
      Player turnGamePlayer = 0;
      for(std::map<TablePlayerId,TablePlayer*>::iterator it
            = table.tablePlayerIdToTablePlayerPointer.begin();
          it!= table.tablePlayerIdToTablePlayerPointer.end();
          it++)
        it->second->turnGamePlayer = (turnGamePlayer++);

      std::list<PlayerAddressedMessage> initialMessages;

      if(!table.p_game->initialize(initialMessages))
        {
          delete table.p_game;
          table.p_game = 0;
          return false;
        }

      for(std::list<PlayerAddressedMessage>::const_iterator it
            = initialMessages.begin();
          it!=initialMessages.end();
          it++)
        {
          const int32_t adresseeSessionId
            = table.turnGamePlayerToTablePlayerPointer[it->player]->sessionId;

          toBeSent.push_back(SessionAddressedMessage(adresseeSessionId));
          GeneralProtocol::serialize_1_GAME_STARTED_AND_INITIAL_MESSAGE
            (it->message,
             toBeSent.back().message);
        }
    }

  return true;
}

bool GameServer::handle_1_MAKE_MOVE
(const int32_t /*sessionID*/,
 std::list<SessionAddressedMessage>& /*toBeSent*/,
 TimeMicro& /*timeout*/,
 const std::vector<char>& /*move*/) throw()
{

  //TODO: finish.
  return false;
}
