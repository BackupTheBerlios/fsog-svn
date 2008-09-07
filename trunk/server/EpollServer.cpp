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

#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/epoll.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>

#include "EpollServer.hpp"
#include "CommandLine.hpp"

void setnonblocking(const int fd) throw(std::exception)
{
  if(fcntl(fd, F_SETFL, fcntl(fd, F_GETFL, 0) | O_NONBLOCK)<0)
    throw Problem(__func__,__FILE__,__LINE__,"fcntl",errno);
}

EpollServer::EpollServer() throw(std::exception)
{
  listener = socket(AF_INET, SOCK_STREAM, 0);
  if(listener<0)
    throw Problem(__func__,__FILE__,__LINE__,"ERROR opening socket",errno);

  epoll_fd = epoll_create(20000);

  if(epoll_fd<0)
    throw Problem(__func__,__FILE__,__LINE__,"Can't create epoll.",errno);

  //TODO: Is this necessary?
  {
    int temp = 1;
    if(setsockopt(listener, SOL_SOCKET, SO_REUSEADDR, &temp,sizeof(temp))<0)
      throw Problem(__func__,__FILE__,__LINE__,"setsockopt",errno);
  }

  //listener must be non--blocking:
  setnonblocking(listener);

  struct sockaddr_in serv_addr;

  //TODO: Which man page documents it needs to be zeroed?
  memset(&serv_addr,0,sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons(CommandLine::port());
  if(bind(listener, (struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) 
    throw Problem(__func__,__FILE__,__LINE__,"ERROR on binding",errno);
  //TODO: maybe change 5 to SOMAXCONN, but which man page?
  if(listen(listener,5)<0)
    throw Problem(__func__,__FILE__,__LINE__,"Can't listen",errno);

  //Add listener to epoll:
  epoll_event event;
  event.events = EPOLLIN;
  event.data.fd = listener;
  if(epoll_ctl(epoll_fd, EPOLL_CTL_ADD, listener, &event)<0)
    throw Problem(__func__,__FILE__,__LINE__,"epoll_ctl",errno);
}

EpollServer::~EpollServer() throw()
{
  std::cout<<"Closing listener..."<<std::endl;
  if(close(listener)<0)
    perror("close");

  for(std::map<int,Session>::const_iterator it = sessions.begin();
      it!=sessions.end();
      it++)
    {
      std::cout<<"Closing fd "<<it->first<<"..."<<std::endl;
      if(close(it->first)<0)
        perror("close");
    }

  std::cout<<"Closing epoll..."<<std::endl;
  if(close(epoll_fd)<0)
    perror("close");
}

void EpollServer::loop() throw(std::exception)
{
  //BEGIN Code based on `man epoll`
  struct epoll_event ev;

  for(;;)
    {
      const int n = epoll_wait(epoll_fd, events, maxevents, -1);

      std::cout<<"epoll got "<<n<<" events."<<std::endl;

      for(int i = 0; i < n; ++i)
        {
          if(events[i].data.fd == listener)
            {
              client = accept(listener, 0, 0);
              if(client<0)
                {
                  perror("accept");
                  continue;
                }
              try
                {
                  setnonblocking(client);
                }
              catch(const std::exception& e)
                {
                  if(close(client)<0)
                    perror("close");
                  continue;
                }
              ev.events = EPOLLIN;
              ev.data.fd = client;
              if(epoll_ctl(epoll_fd, EPOLL_CTL_ADD, client, &ev)<0)
                {
                  perror("epoll_ctl");
                  std::cerr<<"epoll set insertion error: fd="
                           <<client<<std::endl;
                  if(close(client)<0)
                    perror("close");
                }
              std::cout<<"New client: "<<client<<std::endl;
            }
          else
            {//It's not the listener, but one of client sockets.
              if(events[i].events & (EPOLLHUP | EPOLLERR))
                {
                  std::cerr<<"EPOLLHUP | EPOLLERR on "
                           <<events[i].data.fd<<std::endl;
                  terminate(events[i].data.fd);
                }
              else
                {
                  try
                    {
                      do_use_fd(events[i].data.fd);
                    }
                  catch(const std::exception& e)
                    {
                      terminate(events[i].data.fd);
                    }
                }
            }
        }
    }
  //END Code based on `man epoll`
}

void EpollServer::do_use_fd(const int fd) throw(std::exception)
{
  //TODO: Maybe find(), not to create a new session if one does not
  //exist?
  const std::map<int,Session>::iterator entry
    = sessions.find(fd);
  if(entry == sessions.end())
    {
      std::cout<<"No such session: "<<fd<<std::endl;
      throw Problem(__func__,__FILE__,__LINE__,"No such session.",errno);
    }

  std::vector<char>& inputBuffer = entry->second.inputBuffer;
  std::vector<char>& outputBuffer = entry->second.outputBuffer;

  if(!outputBuffer.empty())
    {
      //Try writing as much as possible:
      while(true)
        {
          const ssize_t n
            = write(fd,outputBuffer.data(),outputBuffer.size());
          const int _errno = errno;
          outputBuffer.erase(outputBuffer.begin(),outputBuffer.begin()+n);
          //TODO: don't use EPOLLET. What if we write everything from
          //our buffer and we don't get EAGAIN?
          if(outputBuffer.empty())
            break;
          if(n<0 && errno==EAGAIN)
            break;
          else if(n<0 && errno==EINTR)
            continue;
          else if(n<0)
            throw Problem(__func__,__FILE__,__LINE__,"write",_errno);
        }
    }

  if(outputBuffer.empty())
    {
      //Tell epoll that we don't want to write anymore:
      epoll_event event;
      event.events = EPOLLIN;
      event.data.fd = fd;
      if(epoll_ctl(epoll_fd, EPOLL_CTL_MOD, fd, &event)<0)
        {
          //TODO: handle error!
        }
    }
  
  //Try reading as much as possible:
  while(true)
    {
      const ssize_t n = read(fd,mainBuffer,mainBufferSize);
      //TODO: error handling.
      if(n==0)
        throw Problem(__func__,__FILE__,__LINE__,"EOF",errno);
      else if(n<0)
        {
          if(errno==EAGAIN || errno==EINTR)
            break;
          else
            throw Problem(__func__,__FILE__,__LINE__,"read",errno);
        }
      inputBuffer.insert(inputBuffer.end(),
                         mainBuffer,mainBuffer+n);

      while(true)
        {
          //Can we already decode message length?
          if(inputBuffer.size()<2)
            break;
          //TODO: Maybe higher lengths too?
          const int16_t incomingMessageLength
            =(inputBuffer[0]<<8) | inputBuffer[1];
          //Can we already decode message?
          if(inputBuffer.size()<2+incomingMessageLength)
            break;

          //At least one message is ready!
          //TODO: avoid copying here:
          const std::vector<char> message
            (inputBuffer.begin()+2,
             inputBuffer.begin()+2+incomingMessageLength);

          inputBuffer.erase
            (inputBuffer.begin(),
             inputBuffer.begin()+2+incomingMessageLength);

          //Handle message.
          std::list<SessionAddressedMessage> toBeSent;
          TimeMicro timeout=0;
          this->gameServer.received(message,fd,toBeSent,timeout);
          sendMessages(toBeSent);

          //TODO: Handle timeout!
        }
    }
}

void EpollServer::terminate(const int fd)
{
  std::cout<<"Terminating session "<<fd<<std::endl;
  std::list<SessionAddressedMessage> toBeSent;
  TimeMicro timeout=0;
  this->gameServer.terminated(fd,
                              toBeSent,
                              timeout);

  sendMessages(toBeSent);

  //TODO: handle timeout.

  if(close(fd)<0)
    perror("close");
  sessions.erase(fd);
}

void EpollServer::sendMessages(std::list<SessionAddressedMessage>& toBeSent)
{
  for(std::list<SessionAddressedMessage>::const_iterator it
        =toBeSent.begin();
      it!=toBeSent.end();
      it++)
    {
      const std::map<int,Session>::iterator entry = 
        sessions.find(it->sessionId);
      if(entry == sessions.end())
        {
          //TODO: No such session!
          std::cout<<"gameServer mistakenly wants"
                   <<" to send a message to session with id: "
                   <<it->sessionId
                   <<std::endl;
        }
      else
        {
          //Insert two bytes representing message length:
          entry->second.outputBuffer.push_back(0x7F & (it->message.size()>>8));
          entry->second.outputBuffer.push_back(0xFF & it->message.size());
          //Insert the message into the buffer:
          entry->second.outputBuffer.insert
            (entry->second.outputBuffer.end(),
             it->message.begin(),
             it->message.end());

          //Tell epoll that we want to write to the socket:
          epoll_event event;
          event.events = EPOLLIN | EPOLLOUT;
          event.data.fd = it->sessionId;
          if(epoll_ctl(epoll_fd, EPOLL_CTL_MOD, it->sessionId, &event)<0)
            {
              //TODO: handle error!
            }
        }
    }
}

int main(const int argc, const char*const*const argv)
{
  if(!CommandLine::parse(argc,argv))
    return 1;

  std::srand(std::time(0));

  try
    {
      EpollServer epollServer;
      epollServer.loop();
    }
  catch(std::exception& e)
    {
      std::cout<< "Exception: " << e.what() << std::endl
               <<"In "<<__func__<<", "<<__FILE__<<":"<<__LINE__<<std::endl;
    }
  std::cout<<"Exiting main."<<std::endl;
  return 0;
}
