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
                          std::multimap<int32_t,std::vector<char> >& toBeSent,
                          TimeMicro& timeout) throw()
{
  if(CommandLine::printNetworkPackets())
    std::cout<<"Received message:"<<std::endl
             <<GeneralProtocol::messageToString(message)<<std::endl
             <<"on session: "<<sessionID<<std::endl;

  this->handle(message,sessionID,toBeSent,timeout);
}

bool GameServer::handle_1_CREATE_TICTACTOE_TABLE(const int32_t sessionID,
                                                 std::multimap<int32_t,std::vector<char> >& toBeSent,
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
  std::vector<char> response;
  GeneralProtocol::serialize_1_TABLE_CREATED(table.id,response);

  //Demand sending response to the sender (same sessionID):
  toBeSent.insert(std::pair<int32_t,std::vector<char> >(sessionID,response));
  return true;
}

bool GameServer::handle_1_JOIN_TABLE_TO_PLAY
(const int32_t sessionId,
 std::multimap<int32_t,std::vector<char> >& toBeSent,
 TimeMicro& /*timeout*/,
 const int64_t tableId,
 const std::string& screenName) throw()
{
  //Check whether table exists:
  std::map<Table::TableId,TableIterator>::iterator entryIterator = 
    tableIdToTableIterator.find(tableId);

  if(entryIterator == tableIdToTableIterator.end())
    {
      //TODO: Send a message back to client saying table doesn't
      //exist.
      return false;
    }

  //Table exists.

  TableIterator tableIterator = entryIterator->second;

  Table& table = *tableIterator;
  
  if(sessionIdToTableIterator.find(sessionId)!=sessionIdToTableIterator.end())
    {
      //TODO: What if session was at another table already?
      return false;
    }

  sessionIdToTableIterator[sessionId] = tableIterator;

  const Table::TablePlayerId tablePlayerId
    = table.nextTablePlayerId();

  if(!tablePlayerId)
    {
      //TODO: better handling of this problem. Perhaps a message.
      return false;
    }

  std::vector<char> toOthers;
  GeneralProtocol::serialize_1_NEW_PLAYER_JOINED_TABLE(screenName,
                                                       tablePlayerId,
                                                       toOthers);

  for(std::map<Table::TablePlayerId,int32_t>::const_iterator it
        = table.tablePlayerIdToSessionId.begin();
      it!=table.tablePlayerIdToSessionId.end();
      it++)
    toBeSent.insert(std::pair<int32_t,std::vector<char> >
                    (it->second,
                     toOthers));


  //TODO: remove this when session dies.
  table.tablePlayerIdToSessionId[tablePlayerId] = sessionId;

  std::vector<char> toJoiner;
  GeneralProtocol::serialize_1_YOU_JOINED_TABLE(tablePlayerId,
                                                toJoiner);

  toBeSent.insert(std::pair<int32_t,std::vector<char> >(sessionId,toJoiner));

  //TODO: Start game

  return true;
}

bool GameServer::handle_1_MAKE_MOVE
(const int32_t /*sessionID*/,
 std::multimap<int32_t,std::vector<char> >& /*toBeSent*/,
 TimeMicro& /*timeout*/,
 const std::vector<char>& /*move*/) throw()
{

  //TODO: finish.
  return false;
}
