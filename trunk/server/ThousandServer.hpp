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

#include <stdint.h>
#include <map>
#include "Game.hpp"
#include "Protocol.hpp"
#include "UDPSocket.hpp"
#include "Searcher.hpp"
#include "SearcherPair.hpp"
#include "SearcherTripple.hpp"
#include "Thousand.hpp"

class ThousandServer : private Deserializer
{
private:
  //Socket stuff.

  UDPSocket socket;

  //TODO: merge.
  //Input messages are written here by the socket.
  Message inputMessage;
  //Output messages are written here and later sent to the socket.
  Message outputMessage;

  //Remainder: list and map don't invalidate iterators for no reason.

  //Games:
  //TODO: Actually we don't need the list, could be just objects.
  std::list<Thousand> games;

  //All users:
  //TODO: Actually we don't need the list, could be just objects.
  std::list<User> users;

  //When you get a packet, you can see which user it is.
  //TODO: iterator might be very big, like 8 bytes.
  std::map<Address,std::list<User>::iterator> addressToUser;
  //Map nicks to User iterator.
  std::map<std::string,std::list<User>::iterator> nickToUser;
  //
  Address address;

  //Temporary iterators to game and user:
  std::list<Thousand>::iterator game;
  bool gameKnown;
  std::list<User>::iterator user;
  std::map<Address,std::list<User>::iterator>::iterator addressToUserIterator;
  
  //People searching for games:
  std::list<Searcher> searchers;
  std::list<SearcherPair> searcherPairs;
  std::list<SearcherTripple> searcherTripples;

  //TODO: add lastAction map.

  //Functions:

  //Sends the message this->outputMessage to destinationUser.
  void reply(std::list<User>::iterator destinationUser) throw();

  //BEGIN OF DELIVERY METHODS

  //Delivery is a process where server sends a message to the user and
  //the user must acknowledge the message within some time. A timeout
  //is created by this method, so if no reply happens, delivery
  //timeout fires.
  void deliver(std::list<User>::iterator destinationUser) throw();

  //TODO: iterator might be too big. Use 3 or max 4 bytes.
  //When we send a message to the user and we require
  //confirmation, we insert a pair into this map, where first
  //is the timeout before which the user should reply and the
  //second is the iterator to the user herself.
  std::multimap<TimeMicro,std::list<User>::iterator> deliveryTimeouts;

  void checkDeliveryTimeouts() throw();

  void handleDeliveryTimeout() throw();

  //Resends last message to client, e.g. in case of timeout
  //expiry. Increases the timeout.
  void resendMessage() throw();

  //If the user doesn't send acknowledgement few times, this is
  //called. It logs out the user and removes it from all
  //datastructures.
  void killUser() throw();

  //Some acknowledgement was received. Check if it's OK and make sure
  //server thinks the message is delivered.
  void acknowledgementReceived() throw();

  //END OF DELIVERY METHODS

  void readAndInterpretMessage() throw();

  inline bool userKnown() throw();

  void handle_SEARCH_GAME() throw();
  
  void removeFromSearchers(const std::list<User>::iterator& userIterator)
    throw();

  //\todo Don't use protocol objects here.
  Protocol::Deserialized_1_SEARCH_GAME intersected_SEARCH_GAME;

  bool intersectCryteria(const Protocol::Deserialized_1_SEARCH_GAME& cryteria)
    throw();

  bool searchAndStartGame() throw();

  void startGame(const Protocol::Deserialized_1_SEARCH_GAME& searchCryteria,
                 const std::list<Searcher>::iterator single)
    throw();

  void startGame(const Protocol::Deserialized_1_SEARCH_GAME& searchCryteria,
                 const std::list<SearcherPair>::iterator pair)
    throw();

  void startGame(const Protocol::Deserialized_1_SEARCH_GAME& searchCryteria,
                 const std::list<SearcherTripple>::iterator tripple)
    throw();

  //If search was successful, and game was started, send info
  //about it. Otherwise send info that client should wait for
  //opponents. Parameter means if it should be sent to all
  //participants or just current user.
  void send_SEARCH_GAME_response(const bool toAll) throw();
  
  void registerInSearchers() throw();

  void dealCards() throw();

public:

  ThousandServer(const unsigned short port) throw();

  virtual ~ThousandServer() throw();

  void mainLoop() throw();

};
