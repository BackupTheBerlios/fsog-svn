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
#include <arpa/inet.h>
#include <sys/epoll.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <iostream>
#include <vector>
#include <string>
#include "Problem.hpp"
#include "GeneralProtocol.hpp"
#include "CGI.hpp"

void print_cgi_header() throw(std::exception)
{
  std::cout<<"Content-type: text/html"<<std::endl<<std::endl;
}

void print_html(const int64_t table_id,
                const Parameters& parameters) throw(std::exception)
{
  std::ostringstream link;
  link<<"http://fsog.org/cgi-bin/play?g="<<parameters.get("g")
      <<"&p=55555&t="<<table_id<<"&n=Guest";

  std::cout
    <<"<html>"<<std::endl
    <<"  <head>"<<std::endl
    <<"    <title>FSOG - Thousand</title>"<<std::endl
    <<"  </head>"<<std::endl
    <<"  <body>"<<std::endl
    <<"<p>A table was reserved for you and your friends."
    <<" Send this link to your friends: "
    <<" <a href=\""<<link.str()<<"\">"<<link.str()<<"</a>"
    <<" </p>"<<std::endl
    <<"  </body>"<<std::endl
    <<"</html>"<<std::endl;
}

int64_t createTable(const std::string& game) throw(std::exception)
{
  const int socket_fd = socket(AF_INET, SOCK_STREAM, 0);
  if(socket_fd<0)
    throw Problem(__func__,__FILE__,__LINE__,"ERROR opening socket",errno);

  //TODO: Is this necessary?
  {
    int temp = 1;
    if(setsockopt(socket_fd, SOL_SOCKET, SO_REUSEADDR, &temp,sizeof(temp))<0)
      throw Problem(__func__,__FILE__,__LINE__,"setsockopt",errno);
  }
      
  struct sockaddr_in serv_addr;
  
  //TODO: Which man page documents it needs to be zeroed?
  memset(&serv_addr,0,sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  //TODO: better way to find localhost.
  serv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
  serv_addr.sin_port = htons(55555);//CommandLine::port());
  if(connect(socket_fd, (struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) 
    throw Problem(__func__,__FILE__,__LINE__,"Can't connect.",errno);

  //Create message:
  std::vector<char> outputMessage;
  if(game=="k")
    GeneralProtocol::serialize_1_CREATE_THOUSAND_TABLE(outputMessage);
  else
    throw Problem(__func__,__FILE__,__LINE__,std::string("Wrong game: ")+game,errno);

  std::vector<char> outputBuffer;
  //Insert two bytes representing message length:
  outputBuffer.push_back(0x7F & (outputMessage.size()>>8));
  outputBuffer.push_back(0xFF & outputMessage.size());
  //Insert the message into the buffer:
  //TODO: avoid copying here.
  outputBuffer.insert(outputBuffer.end(),outputMessage.begin(),outputMessage.end());
  
  //Try writing as much as possible:
  while(!outputBuffer.empty())
    {
      //TODO: Is this the same as .data()? Is memory contiguous?
      const char * const outputBuffer_data = &outputBuffer[0];
      const ssize_t n
        = write(socket_fd,outputBuffer_data,outputBuffer.size());
      if(n<0)
        throw Problem(__func__,__FILE__,__LINE__,"write",errno);
      outputBuffer.erase(outputBuffer.begin(),outputBuffer.begin()+n);
    }
      
  //Message sent!

  //We need to receive a reply.
  const size_t bufferSize = 128;
  char buffer[bufferSize];
  std::vector<char> inputBuffer;
  //Try reading one message:
  while(inputBuffer.size()<2 || inputBuffer.size()<2+((inputBuffer[0]<<8) | inputBuffer[1]))
    {
      const ssize_t n = read(socket_fd,buffer,bufferSize);
      if(n<0)
        throw Problem(__func__,__FILE__,__LINE__,"read<0",errno);
      if(n==0)
        throw Problem(__func__,__FILE__,__LINE__,"read==0 EOF",errno);
      inputBuffer.insert(inputBuffer.end(),buffer,buffer+n);
    }

  //At least one message is ready!

  if(close(socket_fd)<0)
    throw Problem(__func__,__FILE__,__LINE__,"close",errno);

  const int16_t inputMessageLength
    =(inputBuffer[0]<<8) | inputBuffer[1];

  //TODO: avoid copying here:
  const std::vector<char> inputMessage
    (inputBuffer.begin()+2,
     inputBuffer.begin()+2+inputMessageLength);

  GeneralProtocol::Deserialized_1_TABLE_CREATED result;

  GeneralProtocol::deserialize_1_TABLE_CREATED(inputMessage,result);

  return result.id;
}

int main()
{
  try
    {
      const Parameters parameters;
      const int64_t table_id = createTable(parameters.get("g"));

      print_cgi_header();
      print_html(table_id,parameters);
    }
  catch(std::exception& e)
    {
      std::cerr<< "Exception: " << e.what() << std::endl
               <<"In "<<__func__<<", "<<__FILE__<<":"<<__LINE__<<std::endl;
      return -1;
    }
  
  return 0;
}
