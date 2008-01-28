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


#include <cstring>
#include <sys/socket.h>
#include <iostream>
#include "UDPSocket.hpp"
#include "CommandLine.hpp"

UDPSocket::UDPSocket(const unsigned short port)
  throw()
{
  //TODO handle errors.
  socketFileDescriptor=socket(AF_INET,SOCK_DGRAM,0);

  //Zero the servaddr structure:
  std::memset(&serverAddress.address, 0, sizeof(serverAddress.address));

  //Set the socket parameters:

  //sin_family is always set to AF_INET.  This is required; in
  //Linux 2.2 most networking functions return EINVAL when
  //this setting is missing.
  serverAddress.address.sin_family = AF_INET;

  //When INADDR_ANY is specified in the bind call the socket
  //will be bound to all local interfaces.

  //sin_addr is the IP host address.  The s_addr member of
  //struct in_addr contains the host interface address in
  //network byte order.  in_addr should be assigned one of the
  //INADDR_* values (e.g., INADDR_ANY) or set using the
  //inet_aton(3), inet_addr(3), inet_makeaddr(3) library
  //functions or directly with the name resolver (see
  //gethostbyname(3)).

  //Note that the address and the port are always stored in
  //network byt order.  In particular, this means that you
  //need to call htons(3) on the number that is assigned to a
  //port. All address/port manipulation functions in the
  //standard library work in network byte order.
  serverAddress.address.sin_addr.s_addr=htonl(INADDR_ANY);

  //sin_port contains the port in network byte order.  The
  //port numbers below 1024 are called reserved ports.
  serverAddress.address.sin_port=htons(port);

  //`man bind`:
  bind(socketFileDescriptor,
       (struct sockaddr *)&serverAddress.address,
       sizeof(struct sockaddr_in));

  //For continuous polling of the socket:
  pollFileDescriptor.fd=socketFileDescriptor;
  pollFileDescriptor.events=POLLIN;
  pollFileDescriptor.revents=0;
  
}

Address& UDPSocket::getClientAddress() throw()
{
  return this->clientAddress;
}

// void UDPSocket::setClientAddress(const Address& clientAddress)
//   throw()
// {
//   this->clientAddress = clientAddress;
// }

void UDPSocket::await(const uint_fast32_t milliseconds) throw()
{
  //`man poll`

  //The timeout argument specifies an upper limit on the time
  //for which poll() will block, in milliseconds.  Specifying
  //a negative value in timeout means an infinite timeout.

  //Usually the caller calculates the timeout as
  //timeToWakeUp-timeNow, so it becomes negative if it should
  //wake up before now or zero if now. Therefore we return
  //immediately:
  if(milliseconds<=0)
    return;

  //On success, a positive number is returned; this is the
  //number of structures which have non-zero revents fields
  //(in other words, those descriptors with events or errors
  //reported).  A value of 0 indicates that the call timed out
  //and no file descriptors were ready.  On error, -1 is
  //returned, and errno is set appropriately.
  
  poll(&(this->pollFileDescriptor),//We pass address of structure
       1,//It's not really an array, read only one entry
       milliseconds);
}

bool UDPSocket::receiveMessage(Message&message) throw()
{
  // All three routines return the length of the message on
  // successful com- pletion.  If a message is too long to fit
  // in the supplied buffer, excess bytes may be discarded
  // depending on the type of socket the mes- sage is received
  // from.

  
  //   If no messages are available at the socket, the receive
  //   calls wait for a message to arrive, unless the socket
  //   is nonblocking (see fcntl(2)), in which case the value
  //   -1 is returned and the external variable errno // set
  //   to EAGAIN.  The receive calls normally return any data
  //   available, // up to the requested amount, rather than
  //   waiting for receipt of the full amount requested.

  temporaryLength = sizeof(clientAddress.address);

  message.resize(100*1024);

  this->bytesRead
    = recvfrom(socketFileDescriptor,
               message.data(),
               message.size(),
               MSG_DONTWAIT,
               (struct sockaddr *)&clientAddress.address,
               &temporaryLength);

  if(this->bytesRead>0)
    {
      message.resize(bytesRead);
      if(CommandLine::printNetworkPackets())
        std::cout
          <<"Received: "<<std::endl
          <<message.toString()<<std::endl
          <<"From: "<<this->clientAddress.toString()<<std::endl;
      return true;
    }
  else
    {
      message.resize(0);
      return false;
    }
}

void UDPSocket::sendMessage(const Message&message,
                            const Address&address) throw()
{
  if(CommandLine::printNetworkPackets())
    std::cout<<"Sending message:"<<std::endl
             <<message.toString()<<std::endl
             <<"to:"<<address.toString()<<std::endl;
  sendto(this->socketFileDescriptor,
         message.data(),
         message.size(),
         0,
         (struct sockaddr *)&address.address,
         sizeof(address.address));
  if(CommandLine::printNetworkPackets())
    std::cout<<"Sent."<<std::endl;
}
