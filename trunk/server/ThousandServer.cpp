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
      checkDeliveryTimeouts();

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
    {
      this->user = this->addressToUserIterator->second;
      this->user->lastActionTime=std::time(0);
    }

  this->game
    = (this->userKnown()
       ?this->user->game
       :this->games.end());

  this->gameKnown
    = (this->game!=this->games.end());

  switch(this->inputMessage.getMessageType())
    {
    case LOG_IN_1:
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

        this->user->myDeliveryTimeout = this->deliveryTimeouts.end();
        this->user->lastActionTime=std::time(0);
        
        Protocol::serialize_1_LOG_IN_CORRECT(this->outputMessage);
        
        //If undelivered, client has to ask again.
        this->reply(this->user);
      }
      break; //End of LOG_IN
    case GET_STATISTICS_1:
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
      this->reply(this->user);

      break;

    case SEARCH_GAME_1:
      this->handle_SEARCH_GAME();
      break;
    case ACKNOWLEDGE_1:
      //User must be logged in!
      //Drop the packet if user is unknown.
      if(!userKnown())
        break;

      //Try to deserialize the message:
      if(!Protocol::deserialize_1_ACKNOWLEDGE
         (this->inputMessage,
          this->temporary_ACKNOWLEDGE))
       {
          std::cerr
            <<"Couldn't deserialize message."<<std::endl
            <<this->inputMessage.toString()<<std::endl;
          break;
        }

      this->acknowledgementReceived();
      break;

    case GAME_START_1:
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

void ThousandServer::checkDeliveryTimeouts() throw()
{
  TimeMicro now = nowMicro();

  //std::cerr<<"INFO: Handling timeouts at:"<<now<<std::endl;

  std::multimap<TimeMicro,std::list<User>::iterator>::iterator it;
  std::multimap<TimeMicro,std::list<User>::iterator>::iterator end
    = this->deliveryTimeouts.end();

  while(true)
    {
      it=this->deliveryTimeouts.begin();
      if(it==end || it->first >= now)
        break;


      std::cerr<<"Delivery timeout expired at: "<<it->first<<std::endl;

      if(now-it->first>100000)
        std::cerr<<"WARNING: now-deliverytimeout: " <<now-it->first<<std::endl;

      this->user=it->second;

      this->deliveryTimeouts.erase(it);
      this->user->myDeliveryTimeout = this->deliveryTimeouts.end();

      if(this->user->deliveryQueue.empty())
        {
          std::cerr
            <<"ERROR! Delivery timeout, but deliveryQueue empty! Last inputMessage:"<<std::endl
            <<this->inputMessage.toString()<<std::endl;
        }
      else
        {
          this->resendMessage();
        }
    }
}

void ThousandServer::resendMessage() throw()
{
  uint8_t& seconds = this->user->nextTimeoutSeconds;

  //Increase waiting time exponentially:
  //TODO: Start with 100ms.
  if(seconds<=0)
    seconds=1;
  else
    seconds*=2;

  if(seconds<=16)
    {
      //Send the message first:
      //TODO: Just use the address in the socket.
      this->socket.sendMessage(this->user->deliveryQueue.front(),
                               this->user->myAddressToUserIterator->first);

      //Add new timeout:
      this->user->myDeliveryTimeout
        = this->deliveryTimeouts.insert(std::make_pair(futureMicro(seconds),
                                                       this->user));
    }
  else
    {
      //Enough waiting! Log the user out.
      this->killUser();
    }
}

void ThousandServer::killUser() throw()
{
  std::cerr<<"I'm killing nonresponsive user \""
           <<this->user->myNickToUserIterator->first
           <<"\"."
           <<std::endl;

  //TODO: end the game that the user played!
  //TODO: what's the difference between game->players.size() and game->numberOfPlayers?
  if(gameKnown)
    for(uint_fast8_t i=0;i<game->players.size();i++)
      if(game->players[i]==this->user)
        game->players[i]=this->users.end();

  this->addressToUser.erase(this->user->myAddressToUserIterator);
  this->nickToUser.erase(this->user->myNickToUserIterator);
  this->removeFromSearchers(this->user);
  //Timeout already erased in checkDeliveryTimeouts().
  
  this->users.erase(this->user);
  this->user=this->users.end();
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
  
  //Don't agree on show must:
  if(intersection & (Protocol::SHOW_MUST_100
                     |Protocol::SHOW_MUST_110) == 0)
    return false;
  
  //Don't agree on publicity:
  if(intersection & (Protocol::PUBLIC_GAME
                     |Protocol::PRIVATE_GAME) == 0)
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
            this->deliver(this->game->players[i]);
        }
      else
        this->deliver(this->user);

    }
  else
    {
      Protocol::serialize_1_AWAIT_GAME(this->outputMessage);
      this->reply(this->user);
    }
}

void ThousandServer::reply(std::list<User>::iterator destinationUser) throw()
{
  //Send the message first:
  this->socket.sendMessage(this->outputMessage,
                           destinationUser->myAddressToUserIterator->first);
}

void ThousandServer::deliver(std::list<User>::iterator destinationUser) throw()
{
  //Warning, the user's deliveryQueue might be nonempty, in which case
  //previous messages must be delivered first.
  if(destinationUser->deliveryQueue.empty())
    {
      //Queue is empty, so this is the first message that needs to be delivered.
      //Send the message first:
      this->socket.sendMessage(this->outputMessage,
                               destinationUser->myAddressToUserIterator->first);

      //Add new timeout:
      destinationUser->myDeliveryTimeout
        = this->deliveryTimeouts.insert(std::make_pair(futureMicro(1),
                                                       destinationUser));

      destinationUser->nextTimeoutSeconds=1;
    }

  //Whether queue is empty or not, we append the message to be delivered:
  destinationUser->deliveryQueue.push_back(this->outputMessage);
}

void ThousandServer::acknowledgementReceived() throw()
{
  //Warning: method handles acks for all message types.

  //Old or duplicate ack packet:
  if(this->user->deliveryQueue.empty())
    return;

  //If ack secret is wrong, drop this old packet:
  if(this->temporary_ACKNOWLEDGE.secret
     != this->user->nextAcknowledgeSecret)
    return;

  //So we have a good ack.
  this->user->deliveryQueue.pop_front();
  this->deliveryTimeouts.erase(this->user->myDeliveryTimeout);
      
  if(this->user->deliveryQueue.empty())
    {
      //This was the last message scheduled for delivery:
      this->user->myDeliveryTimeout = this->deliveryTimeouts.end();
    }
  else
    {
      //There's more messages scheduled for delivery:

      //Send the message first:
      this->socket.sendMessage(this->user->deliveryQueue.front(),
                               this->user->myAddressToUserIterator->first);

      //Add new timeout:
      this->user->myDeliveryTimeout
        = this->deliveryTimeouts.insert(std::make_pair(futureMicro(1),
                                                       this->user));

      this->user->nextTimeoutSeconds=1;
    }
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
      this->reply(this->game->players[i]);
    }

  this->game->state=Thousand::BIDDING;
}
