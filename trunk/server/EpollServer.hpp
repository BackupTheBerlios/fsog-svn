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
#include <exception>
#include <string>
#include <map>
#include <vector>
#include <list>

#include "SessionAddressedMessage.hpp"
#include "GameServer.hpp"

/**
   Objects of this class will be thrown.
 */
class Problem : public std::exception
{
  const std::string message;
  virtual const char* what() const throw()
  {
    return message.c_str();
  }

  static std::string make_message(const char*const func,
                                  const char*const file,
                                  const int line,
                                  const char*const description,
                                  const int _errno)
  {
    std::ostringstream o;
    o<<"ERROR: In "<<func<<" "<<file<<" "<<line<<". "
     <<description;
    if(_errno!=0)
      o<<sys_errlist[_errno];
    return o.str();
  }

public:
  Problem(const char*const func,
          const char*const file,
          const int line,
          const char*const description,
          const int _errno) throw()
    :message(make_message(func,file,line,description,_errno))
  {}

  virtual ~Problem() throw(){}
};

class Session
{
public:
  std::vector<char> inputBuffer;
  std::vector<char> outputBuffer;
};

const short mainBufferSize = 1024;
const int maxevents = 100;

class EpollServer
{
  int listener;
  int epoll_fd;
  int client;
  char mainBuffer[mainBufferSize];
  epoll_event events[maxevents];

  std::map<int,Session> sessions;

  GameServer gameServer;

  void do_use_fd(const int fd) throw(std::exception);
  //TODO: what will it throw?
  void sendMessages(std::list<SessionAddressedMessage>& toBeSent);
  //TODO: what will it throw?
  void terminate(const int fd);
public:
  EpollServer() throw(std::exception);
  void loop() throw(std::exception);
  virtual ~EpollServer() throw();
};
