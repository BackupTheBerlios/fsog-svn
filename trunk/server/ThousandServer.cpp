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


#include <iostream>

#include "Time.hpp"
#include "ThousandServer.hpp"
#include "CommandLine.hpp"
#include "BinaryEqual.hpp"

ThousandServer::ThousandServer(const unsigned short port) throw()
  :socket(port)
{
  //Seed the random number generator with current time:
  std::srand(std::time(0));
}

ThousandServer::~ThousandServer() throw()
{
}

void ThousandServer::mainLoop() throw()
{
  while(true)
    {
      checkTimeouts();

      //This sleeps for specified time, but wakes up
      //if something appeared on socket:
      this->socket.await(50);

      readAndInterpretMessage();
    }
}

bool ThousandServer::userKnown() throw()
{
  return this->addressToUserIterator != this->addressToUser.end();
}

void ThousandServer::readAndInterpretMessage() throw()
{

  const bool somethingRead
    =this->socket.receiveMessage(this->inputMessage);

  //If nothing available:
  if(!somethingRead)
    return;

  this->address = this->socket.getClientAddress();

  //See which user sent the message:
  this->addressToUserIterator
    = this->addressToUser.find(this->address);

  if(this->userKnown())
    this->user = this->addressToUserIterator->second;

  this->game
    = (this->userKnown()
       ?this->user->game
       :this->games.end());

  this->gameKnown
    = (this->game!=this->games.end());

  const Protocol::MessageType messageType
    =Protocol::lookupMessageType(this->inputMessage);

  switch (messageType)
    {
    case Protocol::LOG_IN_1:
      {
        if(!Protocol::deserialize_1_LOG_IN(this->inputMessage,
                                           this->temporary_LOG_IN))
          {
            std::cerr
              <<"Couldn't deserialize message."<<std::endl
              <<this->inputMessage.toString()<<std::endl;
            break;
          }
        
        //TODO: if user re-connected after a crash, we should
        //do something good.
        
        //See whether the user was already logged in from some address:
        std::map<std::string,std::list<User>::iterator>::iterator nickToUserIterator
          = this->nickToUser.find(this->temporary_LOG_IN.nick);
        
        //Remove the previous address association:
        //if(nickToUserIterator!=nickToUser.end())
        //  nickToUser.remove(nickToUserIterator);
        
        //TODO: check password in database.
        
        //Create new user:
        //std::pair<std::map<Address,User>::iterator,bool> tempPair
        this->users.push_front(User(this->games.end()));
        this->user = this->users.begin();
        
        this->user->myAddressToUserIterator
          = this->addressToUser.insert(std::make_pair(this->address,
                                                      this->user)
                                       ).first;
        
        this->user->myNickToUserIterator
          = this->nickToUser.insert(std::make_pair(this->temporary_LOG_IN.nick,
                                                   this->user)
                                    ).first;
        
        Protocol::serialize_1_LOG_IN_CORRECT(this->outputMessage);
        
        //If undelivered, client has to ask again.
        this->sendMessage(this->user);
      }
      break; //End of LOG_IN
    case Protocol::GET_STATISTICS_1:
      if(!Protocol::deserialize_1_GET_STATISTICS(this->inputMessage))
        {
          std::cerr
            <<"Couldn't deserialize message."<<std::endl
            <<this->inputMessage.toString()<<std::endl;
          break;
        }

      Protocol::serialize_1_RETURN_STATISTICS(this->users.size(),
                                              this->games.size(),
                                              this->searchers.size(),
                                              this->outputMessage);

      //If undelivered, client has to ask again.
      this->sendMessage(this->user);

      break;

    case Protocol::SEARCH_GAME_1:
      this->handle_SEARCH_GAME();
      break;
    case Protocol::ACKNOWLEDGE_PROPOSED_GAME_1:
      //User must be logged in!
      //Drop the packet if user is unknown.
      if(!userKnown())
        break;

      //Try to deserialize the message:
      if(!Protocol::deserialize_1_ACKNOWLEDGE_PROPOSED_GAME
         (this->inputMessage,
          this->temporary_ACKNOWLEDGE_PROPOSED_GAME))
        {
          std::cerr
            <<"Couldn't deserialize message."<<std::endl
            <<this->inputMessage.toString()<<std::endl;
          break;
        }

      if(this->user->serverAwaits
         ==Protocol::ACKNOWLEDGE_PROPOSED_GAME_1)
        {
          //Server was waiting for this message from the user.
          //Check if ack secret is OK:
          if(this->temporary_ACKNOWLEDGE_PROPOSED_GAME.secret
             == this->user->nextAcknowledgeSecret)
            {
              //Now we know the user received PROPOSED_GAME.
              this->acknowledgementReceived();
            }
        }

      break;

    case Protocol::GAME_START_1:
      {
        //User must be logged in!
        //Drop the packet if user is unknown.
        if(!userKnown())
          break;
        
        //Try to deserialize the message:
        if(!Protocol::deserialize_1_GAME_START(this->inputMessage))
          {
            std::cerr
              <<"Couldn't deserialize message."<<std::endl
              <<this->inputMessage.toString()<<std::endl;
            break;
          }
        
        this->user->pressedStart=true;
        
        bool allStarted = true;
        for(char i=0;i<this->game->numberOfPlayers;i++)
          if(!this->game->players[i]->pressedStart)
            {
              allStarted = false;
              break;
            }
        
        if(allStarted)
          {
            //All players clicked start. Deal the cards now:
            this->dealCards();
          }
      }//END of message GAME_START
    default:
      std::cerr
        <<"Unknown message type."<<std::endl
        <<this->inputMessage.toString()<<std::endl;
      break;
    }
}

void ThousandServer::handle_SEARCH_GAME() throw()
{
  //User must be logged in!
  //Drop the packet if user is unknown.
  if(!this->userKnown())
    return;

  //Try to deserialize the message:
  if(!Protocol::deserialize_1_SEARCH_GAME(this->inputMessage,
                                          this->temporary_SEARCH_GAME))
    {
      std::cerr
        <<"Couldn't deserialize message."<<std::endl
        <<this->inputMessage.toString()<<std::endl;
      return;
    }

  //If this search is exactly the same as previous one,
  //client probably didn't get our response. Send the
  //response and that's it.
  if(binary_equal(this->temporary_SEARCH_GAME,
                  this->user->last_SEARCH_GAME))
    {
      this->send_SEARCH_GAME_response(false);
      return;
    }

  //Remember the search in case of C->S duplicate packets
  //and S->C lost packets.
  user->last_SEARCH_GAME=this->temporary_SEARCH_GAME;

  //Remove player from all searches (in case this is second search).
  this->removeFromSearchers(this->user);

  //Search searchers and if matches, start game.
  if(this->searchAndStartGame())
    {
      //Send info to all players.
      this->send_SEARCH_GAME_response(true);
    }
  else
    {
      this->registerInSearchers();
      this->send_SEARCH_GAME_response(false);
    }
}

void ThousandServer::checkTimeouts() throw()
{
}

int main(const int argc,
         char** argv)
  throw()
{
  CommandLine::parse(argc,argv);
  
  ThousandServer gameServer(Protocol::getServerUDPPort_1());
  gameServer.mainLoop();
}

void ThousandServer::removeFromSearchers(const std::list<User>::iterator&userToBeRemoved)
  throw()
{
  //Remove from tripples:
  for(std::list<SearcherTripple>::iterator trippleIterator
        = this->searcherTripples.begin();
      trippleIterator!=this->searcherTripples.end();
      )
    {
      if(userToBeRemoved==trippleIterator->user0
         ||userToBeRemoved==trippleIterator->user1
         ||userToBeRemoved==trippleIterator->user2)
        {
          trippleIterator
            =this->searcherTripples.erase(trippleIterator);
          continue;
        }
      else
        trippleIterator++;
    }
  
  //Remove from pairs:
  for(std::list<SearcherPair>::iterator pairIterator
        = this->searcherPairs.begin();
      pairIterator!=this->searcherPairs.end();
      )
    {
      if(userToBeRemoved==pairIterator->user0
         ||userToBeRemoved==pairIterator->user1)
        {
          //Client searches for the second time.
          //Remove previous search and continue search.
          pairIterator
            =this->searcherPairs.erase(pairIterator);
          continue;
        }
      else
        pairIterator++;
    }
  
  //Remove from searchers:
  for(std::list<Searcher>::iterator searcherIterator
        = this->searchers.begin();
      searcherIterator!=this->searchers.end();
      )
    {
      if(userToBeRemoved==searcherIterator->user)
        {
          //Client searches for the second time.
          //Remove previous search and continue search.
          searcherIterator
            =this->searchers.erase(searcherIterator);
          continue;
        }
      else
        searcherIterator++;
    }
}      


bool ThousandServer::searchAndStartGame() throw()
{
  //Search tripples
  if(this->temporary_SEARCH_GAME.searchFlags&Protocol::FOUR_PLAYERS)
    {
      for(std::list<SearcherTripple>::iterator trippleIterator
            = this->searcherTripples.begin();
          trippleIterator!=this->searcherTripples.end();
          trippleIterator++)
        {
          //See if this tripple is OK:
          if(this->intersectCryteria(trippleIterator->cryteria))
            {
              //Nice! 3 matching opponents found.
              //Start game and remove players from other sets.
              
              this->startGame(this->intersected_SEARCH_GAME,
                              trippleIterator);
              return true;
            }
        }
    }//End of &FOUR_PLAYERS

  //Couldn't find 3 matching opponents.
  //Maybe we can find 2 matching opponents:
  if(this->temporary_SEARCH_GAME.searchFlags&Protocol::THREE_PLAYERS)
    {
      for(std::list<SearcherPair>::iterator pairIterator
            = this->searcherPairs.begin();
          pairIterator!=this->searcherPairs.end();
          pairIterator++)
        {
          //See if this pair is OK:
          if(this->intersectCryteria(pairIterator->cryteria))
            {
              //Nice! 2 matching opponents found.
              //Start game and remove players from other sets.
              
              this->startGame(this->intersected_SEARCH_GAME,
                              pairIterator);
              return true;
            }
        }
    }//End of &THREE_PLAYERS
  
  //Couldn't find 2 matching opponents.
  //Maybe we can find 1 matching opponent:
  if(this->temporary_SEARCH_GAME.searchFlags&Protocol::TWO_PLAYERS)
    {
      for(std::list<Searcher>::iterator searcherIterator
            = this->searchers.begin();
          searcherIterator!=this->searchers.end();
          searcherIterator++)
        {
          //See if this searcher is OK:
          if(this->intersectCryteria(searcherIterator->cryteria))
            {
              //Nice! matching opponent found.
              //Start game and remove players from other sets.
              
              this->startGame(this->intersected_SEARCH_GAME,
                              searcherIterator);
              return true;
            }
        }
    }//End of &THREE_PLAYERS

  //Buuuh... Nobody found to play with!
  return false;  
}

void ThousandServer::startGame(const Protocol::Deserialized_1_SEARCH_GAME& searchCryteria,
                               const std::list<SearcherTripple>::iterator tripple)
  throw()
{
  //Create new game:
  {
    Thousand newGame(4,searchCryteria.searchFlags);

    newGame.players[0]=tripple->user0;
    newGame.players[1]=tripple->user1;
    newGame.players[2]=tripple->user2;
    newGame.players[3]=this->user;

    this->games.push_front(newGame);
    this->game=this->games.begin();
    this->gameKnown = true;
  }

  //Let each user remember her game iterator:
  this->game->players[0]->game=this->game;
  this->game->players[1]->game=this->game;
  this->game->players[2]->game=this->game;
  this->game->players[3]->game=this->game;

  //Now remove all users from searcher sets. WARNING! The
  //iterator passed to this function will be now invalidated,
  //as the element can be removed by the below:
  this->removeFromSearchers(this->game->players[0]);
  this->removeFromSearchers(this->game->players[1]);
  this->removeFromSearchers(this->game->players[2]);
  this->removeFromSearchers(this->game->players[3]);
}

void ThousandServer::startGame(const Protocol::Deserialized_1_SEARCH_GAME& searchCryteria,
                               const std::list<SearcherPair>::iterator pair)
  throw()
{
  //Create new game:
  {
    Thousand newGame(3,searchCryteria.searchFlags);

    newGame.players[0]=pair->user0;
    newGame.players[1]=pair->user1;
    newGame.players[2]=this->user;

    this->games.push_front(newGame);
    this->game=this->games.begin();
    this->gameKnown = true;
  }

  //Let each user remember her game iterator:
  this->game->players[0]->game=this->game;
  this->game->players[1]->game=this->game;
  this->game->players[2]->game=this->game;

  //Now remove all users from searcher sets. WARNING! The
  //iterator passed to this function will be now invalidated,
  //as the element can be removed by the below:
  this->removeFromSearchers(this->game->players[0]);
  this->removeFromSearchers(this->game->players[1]);
  this->removeFromSearchers(this->game->players[2]);
}

void ThousandServer::startGame(const Protocol::Deserialized_1_SEARCH_GAME& searchCryteria,
                               const std::list<Searcher>::iterator single)
  throw()
{
  //Create new game:
  {
    Thousand newGame(2,searchCryteria.searchFlags);

    newGame.players[0]=single->user;
    newGame.players[1]=this->user;

    this->games.push_front(newGame);
    this->game=this->games.begin();
    this->gameKnown = true;
  }

  //Let each user remember her game iterator:
  this->game->players[0]->game=this->game;
  this->game->players[1]->game=this->game;

  //Now remove all users from searcher sets. WARNING! The
  //iterator passed to this function will be now invalidated,
  //as the element can be removed by the below:
  this->removeFromSearchers(this->game->players[0]);
  this->removeFromSearchers(this->game->players[1]);
}

void ThousandServer::registerInSearchers() throw()
{
  //Create tripple if interested in playing in 4:
  if(this->temporary_SEARCH_GAME.searchFlags&Protocol::FOUR_PLAYERS)
    {
      for(std::list<SearcherPair>::const_iterator pairIterator
            = this->searcherPairs.begin();
          pairIterator!=this->searcherPairs.end();
          pairIterator++)
        {
          //See if this pair is OK:
          if(this->intersectCryteria(pairIterator->cryteria))
            this->searcherTripples.push_back
              (SearcherTripple(*pairIterator,
                               this->user,
                               this->intersected_SEARCH_GAME));
        }
    }
  
  //Create pair if interested in playing in 3 or 4:
  if(this->temporary_SEARCH_GAME.searchFlags
     &(Protocol::THREE_PLAYERS|Protocol::FOUR_PLAYERS))
    {
      for(std::list<Searcher>::const_iterator searcherIterator
            = this->searchers.begin();
          searcherIterator!=this->searchers.end();
          searcherIterator++)
        {
          //See if this pair would make sense:
          if(this->intersectCryteria(searcherIterator->cryteria))
            this->searcherPairs.push_back
              (SearcherPair(*searcherIterator,
                            this->user,
                            this->intersected_SEARCH_GAME));
        }
    }
  
  //Create single:
  //See if this guy would make sense with anyone
  //by intersecting with itself.
  if(this->intersectCryteria(this->temporary_SEARCH_GAME))
    this->searchers.push_back
      (Searcher(this->user,
                this->intersected_SEARCH_GAME));
}

bool ThousandServer::intersectCryteria(const Protocol::Deserialized_1_SEARCH_GAME& cryteria)
  throw()
{
  //TODO ranking minimum.
  int32_t& intersection
    = this->intersected_SEARCH_GAME.searchFlags;

  intersection
    = this->temporary_SEARCH_GAME.searchFlags
    & cryteria.searchFlags;
  
  //Don't agree on number of players:
  if(intersection & (Protocol::TWO_PLAYERS
                     |Protocol::THREE_PLAYERS
                     |Protocol::FOUR_PLAYERS) == 0)
    return false;
  
  //Don't agree on any bombs:
  if(intersection & (Protocol::NO_BOMBS
                     |Protocol::BOMBS
                     |Protocol::UNLIMITED_BOMBS) == 0)
    return false;

  //Don't agree on bid increment:
  if(intersection & (Protocol::BID_INCREMENT_10
                     |Protocol::BID_INCREMENT_ANY) == 0)
    return false;
  
  //Don't agree on 800/900:
  if(intersection & (Protocol::MUST_PLAY_FROM_800
                     |Protocol::MUST_PLAY_FROM_900) == 0)
    return false;
  
  //Don't agree on show must:
  if(intersection & (Protocol::SHOW_MUST_100
                     |Protocol::SHOW_MUST_110) == 0)
    return false;
  
  //Don't agree on publicity:
  if(intersection & (Protocol::PUBLIC
                     |Protocol::PRIVATE) == 0)
    return false;
  
  //Don't agree on ranking/sparring:
  if(intersection & (Protocol::RANKING_GAME
                     |Protocol::SPARRING_GAME) == 0)
    return false;
  
  //Don't agree on time:
  if(intersection & (Protocol::TIME_7
                     |Protocol::TIME_10
                     |Protocol::TIME_15
                     |Protocol::TIME_20
                     |Protocol::TIME_30) == 0)
    return false;
  
  //TODO: ranking.
  return true;
}

void ThousandServer::send_SEARCH_GAME_response(const bool toAll) throw()
{
  //If user takes part in some game:
  if(this->gameKnown)
    {
      switch(game->numberOfPlayers)
        {
        case 4:
          Protocol::serialize_1_PROPOSED_GAME
            (game->players[0]->myNickToUserIterator->first,
             game->players[1]->myNickToUserIterator->first,
             game->players[2]->myNickToUserIterator->first,
             game->players[3]->myNickToUserIterator->first,
             game->flags,
             user->nextAcknowledgeSecret++,
             this->outputMessage);
          break;
        case 3:
          Protocol::serialize_1_PROPOSED_GAME
            (game->players[0]->myNickToUserIterator->first,
             game->players[1]->myNickToUserIterator->first,
             game->players[2]->myNickToUserIterator->first,
             "",
             game->flags,
             user->nextAcknowledgeSecret++,
             this->outputMessage);
          break;
        case 2:
          Protocol::serialize_1_PROPOSED_GAME
            (game->players[0]->myNickToUserIterator->first,
             game->players[1]->myNickToUserIterator->first,
             "",
             "",
             game->flags,
             user->nextAcknowledgeSecret++,
             this->outputMessage);
          break;
        }
      //Client was proposed a game. She should quickly acknowledge that:
      if(toAll)
        {
          for(char i=0;i<this->game->numberOfPlayers;i++)
            this->sendMessage(this->game->players[i],
                              Protocol::ACKNOWLEDGE_PROPOSED_GAME_1,
                              1);
        }
      else
        this->sendMessage(this->user,
                          Protocol::ACKNOWLEDGE_PROPOSED_GAME_1,
                          1);

    }
  else
    {
      Protocol::serialize_1_AWAIT_GAME(this->outputMessage);
      this->sendMessage(this->user);
    }
}

void ThousandServer::sendMessage(std::list<User>::iterator destinationUser,
                                 const Protocol::MessageType messageType,
                                 const uint_fast16_t seconds) throw()
{
  //Send the message first:
  this->socket.sendMessage(this->outputMessage,
                           destinationUser->myAddressToUserIterator->first);

  //Add timeout:
  destinationUser->myTimeout
    = this->timeouts.insert(std::make_pair(timeMicro(seconds),
                                           destinationUser));
      
  destinationUser->serverAwaits = messageType;
}

void ThousandServer::acknowledgementReceived() throw()
{
  //Now the user is not awaiting anything:
  this->user->serverAwaits = Protocol::UNKNOWN_MESSAGE_1;
  //Remove timeout:
  this->timeouts.erase(this->user->myTimeout);
}

void ThousandServer::dealCards() throw()
{
  this->game->deal();

  //TODO: if 2 players, send 10 cards.

  //Send cards to all players:
  for(char i=0;i<this->game->numberOfPlayers;i++)
    {
      Protocol::serialize_1_GAME_DEAL_7_CARDS
        (this->game->players[i]->cards[0],
         this->game->players[i]->cards[1],
         this->game->players[i]->cards[2],
         this->game->players[i]->cards[3],
         this->game->players[i]->cards[4],
         this->game->players[i]->cards[5],
         this->game->players[i]->cards[6],
         this->outputMessage);
      this->sendMessage(this->game->players[i]);
    }

  this->game->state=Thousand::BIDDING;
}
