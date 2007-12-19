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
#include <netinet/in.h>
#include <poll.h>
#include "Message.hpp"
#include "Address.hpp"

class UDPSocket
{
private:
  //`man ip` is a good overview.

  int socketFileDescriptor;
  ssize_t bytesRead;
  Address serverAddress;

  socklen_t temporaryLength;

  //For continuous polling of the socket:
  struct pollfd pollFileDescriptor;

  Address clientAddress;
  
public:
  UDPSocket(const unsigned short port) throw();

  Address& getClientAddress() throw();
  //void setClientAddress(const Address& clientAddress) throw();

  void await(const uint_fast32_t milliseconds) throw();

  //Reads from the socket to this->inputMessage
  //true if something read
  //false if there was nothing to read or error occurred
  bool receiveMessage(Message&message) throw();

  //Sends from this->inputMessage to the socket
  void sendMessage(const Message&message,
                   const Address&address) throw();

};
