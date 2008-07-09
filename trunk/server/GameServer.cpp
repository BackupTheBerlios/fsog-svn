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
                          const SessionId sessionID,
                          std::list<SessionAddressedMessage>& toBeSent,
                          TimeMicro& timeout) throw()
{
  if(CommandLine::printNetworkPackets())
    std::cout<<"RCV("<<sessionID<<") "
             <<GeneralProtocol::messageToString(message);

  const bool ok = this->handle(message,sessionID,toBeSent,timeout);
  
  if(!ok)
    std::cout<<"GameServer: handling message failed!"<<std::endl;
}

bool GameServer::handle_1_CREATE_TICTACTOE_TABLE(const SessionId sessionID,
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
      std::cout<<"Game allocation failed."<<std::endl
               <<"In "<<__func__<<", file "<<__FILE__
               <<", line"<<__LINE__<<std::endl;
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

void GameServer::terminated(const SessionId& sessionId,
                            std::list<SessionAddressedMessage>& toBeSent,
                            TimeMicro& /*timeout*/) throw()
{
  std::map<SessionId,TablePlayer*>::iterator entry
    = sessionIdToTablePlayerPointer.find(sessionId);

  if(entry == sessionIdToTablePlayerPointer.end())
    {
      //Strange, session doesn't exist!
      return;
    }

  const TablePlayer* tablePlayerPointer = entry->second;

  //At which table was the leaving player playing?
  Table& table = tablePlayerPointer->table;
  
  std::vector<char> toOthers;
  GeneralProtocol::serialize_1_PLAYER_LEFT_TABLE(tablePlayerPointer->tablePlayerId,
                                                 toOthers);

  sessionIdToTablePlayerPointer.erase(entry);

  table.tablePlayerIdToTablePlayerPointer.erase(tablePlayerPointer->tablePlayerId);

  delete tablePlayerPointer;
  tablePlayerPointer = 0;

  for(std::map<TablePlayerId,TablePlayer*>::const_iterator it
        = table.tablePlayerIdToTablePlayerPointer.begin();
      it!=table.tablePlayerIdToTablePlayerPointer.end();
      it++)
    {
      //Message to each old player that joiner joined:
      toBeSent.push_back(SessionAddressedMessage(it->second->sessionId));
      toBeSent.back().message = toOthers;
    }
  //TODO: Finish! What with started game? Delete table?
}

bool GameServer::handle_1_JOIN_TABLE_TO_PLAY
(const SessionId sessionId,
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
      //Send a message back to client saying table doesn't
      //exist.
      toBeSent.push_back(SessionAddressedMessage(sessionId));
      GeneralProtocol::serialize_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID
        (toBeSent.back().message);
      return true;
    }

  //Table exists.

  TableIterator tableIterator = entryIterator->second;

  Table& table = *tableIterator;
  
  if(sessionIdToTablePlayerPointer.find(sessionId)
     != sessionIdToTablePlayerPointer.end())
    {
      //TODO: What if session was at another table already?
      std::cout<<"Session with id: "<<sessionId<<"at another table."<<std::endl
               <<"In "<<__func__<<", file "<<__FILE__
               <<", line"<<__LINE__<<std::endl;
      return false;
    }

  const TablePlayerId tablePlayerId
    = table.nextTablePlayerId();

  if(!tablePlayerId)
    {
      //TODO: better handling of this problem. Perhaps a message.
      std::cout<<"No more tablePlayerId available."<<std::endl
               <<"In "<<__func__<<", file "<<__FILE__
               <<", line"<<__LINE__<<std::endl;
      return false;
    }

  //TODO: delete at some time!
  TablePlayer* tablePlayerPointer
    = new TablePlayer(sessionId,screenName,table,tablePlayerId);

  sessionIdToTablePlayerPointer.insert
    (std::pair<SessionId,TablePlayer*>(sessionId,
                                     tablePlayerPointer));

  //Let old players know that new player is here:
  //Also let the new player know what old players are here:
  std::vector<char> toOldPlayers;
  GeneralProtocol::serialize_1_NEW_PLAYER_JOINED_TABLE(screenName,
                                                       tablePlayerId,
                                                       toOldPlayers);

  for(std::map<TablePlayerId,TablePlayer*>::const_iterator it
        = table.tablePlayerIdToTablePlayerPointer.begin();
      it!=table.tablePlayerIdToTablePlayerPointer.end();
      it++)
    {
      //Message to each old player that joiner joined:
      toBeSent.push_back(SessionAddressedMessage(it->second->sessionId));
      toBeSent.back().message = toOldPlayers;

      //Message to joiner that each old player is here:
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
  //TODO: When to start game should be handled by table.expectedNumberOfPlayers
  if(table.p_game &&
     table.p_game->numberOfPlayers==table.tablePlayerIdToTablePlayerPointer.size())
    {
      //We need to calculate which players are not given initial
      //message. First we say it's all of the players, and we subtract
      //later in a loop those that get initial message.
      std::set<SessionId> whoDoesNotGetInitialMessage;

      //This information is sent to client in "GAME_START_..." message.
      std::vector<TablePlayerId> turnGamePlayerToTablePlayerId;
  
      //Assign turnGamePlayer to each TablePlayer:
      Player turnGamePlayer = 0;
      table.turnGamePlayerToTablePlayerPointer.resize(table.p_game->numberOfPlayers);
      turnGamePlayerToTablePlayerId.resize(table.p_game->numberOfPlayers);
      for(std::map<TablePlayerId,TablePlayer*>::iterator it
            = table.tablePlayerIdToTablePlayerPointer.begin();
          it != table.tablePlayerIdToTablePlayerPointer.end();
          it++)
        {
          whoDoesNotGetInitialMessage.insert(it->second->sessionId);
          table.turnGamePlayerToTablePlayerPointer[turnGamePlayer] = it->second;
          turnGamePlayerToTablePlayerId[turnGamePlayer] = it->first;
          it->second->turnGamePlayer = turnGamePlayer;
          turnGamePlayer++;
        }

      std::list<PlayerAddressedMessage> initialMessages;

      if(!table.p_game->initialize(initialMessages))
        {
          delete table.p_game;
          table.p_game = 0;
          //TODO: When returning false, calling session should be removed.
          return false;
        }

      table.gameStarted = true;

      //Send "GAME_STARTED_WITH_INITIAL_MESSAGE" to those who need it:
      for(std::list<PlayerAddressedMessage>::const_iterator it
            = initialMessages.begin();
          it != initialMessages.end();
          it++)
        {
          if(it->player >= table.turnGamePlayerToTablePlayerPointer.size()
             || it->player < 0)
            {
              std::cout<<"TurnGame wanted to send message to player"
                       <<it->player<<", while there are only "
                       <<table.turnGamePlayerToTablePlayerPointer.size()
                       <<" players in the game."
                       <<std::endl
                       <<"In "<<__func__<<", file "<<__FILE__
                       <<", line"<<__LINE__<<std::endl;
              return false;
            }
          const SessionId adresseeSessionId
            = table.turnGamePlayerToTablePlayerPointer[it->player]->sessionId;

          toBeSent.push_back(SessionAddressedMessage(adresseeSessionId));
          GeneralProtocol::serialize_1_GAME_STARTED_WITH_INITIAL_MESSAGE
            (turnGamePlayerToTablePlayerId,
             it->message,
             toBeSent.back().message);

          whoDoesNotGetInitialMessage.erase(adresseeSessionId);
        }

      //Send START_GAME_NO_INITIAL_MESSAGE to those who didn't get
      //initial message:
      for(std::set<SessionId>::const_iterator it
            = whoDoesNotGetInitialMessage.begin();
          it != whoDoesNotGetInitialMessage.end();
          it++)
        {
          toBeSent.push_back(SessionAddressedMessage(*it));
          GeneralProtocol::serialize_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE
            (turnGamePlayerToTablePlayerId,
             toBeSent.back().message);
        }
    }

  return true;
}

bool GameServer::handle_1_SAY(const SessionId sessionId,
                              std::list<SessionAddressedMessage>& toBeSent,
                              TimeMicro& /*timeout*/,
                              const std::vector<char>& text_UTF8) throw()
{
  std::map<SessionId,TablePlayer*>::const_iterator entry
    = sessionIdToTablePlayerPointer.find(sessionId);

  if(entry == sessionIdToTablePlayerPointer.end())
    {
      std::cout<<"Unknown sessionId "<<sessionId<<"."<<std::endl
               <<"In "<<__func__<<", file "<<__FILE__
               <<", line"<<__LINE__<<std::endl;
      return false;
    }

  TablePlayer& tablePlayer = *(entry->second);

  std::vector<char> toAll;
  GeneralProtocol::serialize_1_SAID(tablePlayer.tablePlayerId,
                                    text_UTF8,
                                    toAll);

  for(std::map<TablePlayerId,TablePlayer*>::const_iterator it
        = tablePlayer.table.tablePlayerIdToTablePlayerPointer.begin();
      it!=tablePlayer.table.tablePlayerIdToTablePlayerPointer.end();
      it++)
    {
      //Message to each old player that joiner joined:
      toBeSent.push_back(SessionAddressedMessage(it->second->sessionId));
      toBeSent.back().message = toAll;
    }
  return true;
}

bool GameServer::handle_1_MAKE_MOVE
(const SessionId sessionId,
 std::list<SessionAddressedMessage>& toBeSent,
 TimeMicro& /*timeout*/,
 const std::vector<char>& gameMove) throw()
{

  std::map<SessionId,TablePlayer*>::const_iterator entry
    = sessionIdToTablePlayerPointer.find(sessionId);

  if(entry == sessionIdToTablePlayerPointer.end())
    {
      std::cout<<"Unknown sessionId "<<sessionId<<". "
               <<"@"<<__func__<<"@"<<__FILE__
               <<":"<<__LINE__<<std::endl;
      return false;
    }

  TablePlayer& tablePlayer = *(entry->second);
  
  Table& table = tablePlayer.table;

  if(!table.p_game)
    {
      std::cout<<"!table.p_game for sessionId "<<sessionId<<". "
               <<"@"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
      return false;
    }
  
  if(!table.gameStarted)
    {
      std::cout<<"!table.gameStarted for sessionId "<<sessionId<<". "
               <<"@"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
      return false;
    }

  //Check whether it's the expected player making the move:
  if(table.p_game->turn!=tablePlayer.turnGamePlayer)
    {
      std::cout<<"table.p_game.turn!=tablePlayer.turnGamePlayer for sessionId "
               <<sessionId<<". "
               <<"@"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
      return false;
    }

  std::list<PlayerAddressedMessage> moveMessages;
  std::list<std::set<Player> > endResult;

  const MoveResult moveResult
    = table.p_game->move(gameMove,moveMessages,endResult);

  if((moveResult&VALIDITY_MASK)==INVALID)
    {
      std::cout<<"(moveResult&VALIDITY_MASK)==INVALID for sessionId "<<sessionId
               <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
      return false;
    }

  //Move is valid, so game state updated.

  //Send moveMessages:
  for(std::list<PlayerAddressedMessage>::const_iterator it
        = moveMessages.begin();
      it != moveMessages.end();
      it++)
    {
      if(it->player >= table.turnGamePlayerToTablePlayerPointer.size()
         || it->player < 0)
        {
          std::cout<<"TurnGame wanted to send message to player"
                   <<it->player<<", while there are only "
                   <<table.turnGamePlayerToTablePlayerPointer.size()
                   <<" players in the game."
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return false;
        }

      const SessionId adresseeSessionId
        = table.turnGamePlayerToTablePlayerPointer[it->player]->sessionId;

      toBeSent.push_back(SessionAddressedMessage(adresseeSessionId));
      GeneralProtocol::serialize_1_MOVE_MADE(it->message,toBeSent.back().message);
    }

  //If the game shall continue, there's nothing more we need to do:
  if((moveResult&CONTINUITY_MASK)==CONTINUE)
    return true;

  //Game shall finish.
  delete table.p_game;
  table.p_game = 0;
  return true;
  //TODO: Save ranking, etc.
}
