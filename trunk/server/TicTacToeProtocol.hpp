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

#include <vector>
#include <string>
#include <sstream>
#include <cctype>
#include <list>
#include "Time.hpp"
#include "Message.hpp"
#include "SessionAddressedMessage.hpp"

class TicTacToeProtocol
{
public:

  //Can be used for switching to detect
  //which deserializer should be used.
    static const int8_t UNKNOWN_MESSAGE_1 = 0;
    //Simple TicTacToe move.
    static const int8_t MAKE_MOVE_1 = 1;
    //Simple TicTacToe move. No need to say who made this move.
    static const int8_t MOVE_MADE_1 = 2;



  //Can be used for printing message type
  //in human-readable form.
  static std::string messageTypeToString(int8_t messageType) throw()
  {
    switch(messageType)
    {
      case UNKNOWN_MESSAGE_1: return "UNKNOWN_MESSAGE";
      case MAKE_MOVE_1: return "MAKE_MOVE";
      case MOVE_MADE_1: return "MOVE_MADE";
    }


    return "ERROR. No such message in this protocol version!";
  }

  //This method can be used for rapid message
  //type lookup, so you don't need to try
  //deserializing using all deserializers.
  //Remember that deserialization can still
  //fail, even if this method returns
  //some known type. It doesn't read the whole
  //message, just the part where message type
  //is present. If the message type cannot be
  //determined, 0 is returned. This could happen
  //e.g. if message is empty.
  static int8_t getMessageType(const std::vector<char>& message) throw()
  {
    if(message.size()<2)
      return 0;
    
    return message[1];
  }

  //Represent message as string
  static std::string messageToString(const std::vector<char>& message)
    throw()
  {
    std::ostringstream output;

    output
      <<"Message type: "<<messageTypeToString(getMessageType(message))
      <<", number of bytes: "<<message.size()<<std::endl;

    const char*const hex = "0123456789ABCDEF";

    int_fast16_t i=0;
    for(std::vector<char>::const_iterator it=message.begin();
        it!=message.end() && i<1024;
        it++)
      {
        output
          <<hex[((*it)>>4) & 0x0F]
          <<hex[(*it) & 0x0F]
          <<" ("<<(std::isprint(*it)?(*it):'_')<<") ";
        i++;
        if(i%10==0)
          output<<std::endl;
      }

    return output.str();
  }
  //Message MAKE_MOVE:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 1.
  //Simple TicTacToe move.

  static void serialize_1_MAKE_MOVE(
        //In which row player puts her X or O.
        const int8_t row,

        //In which column player puts her X or O.
        const int8_t column,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(1,outputMessage);

    //Serialize row:
    Message::append1Byte(row,outputMessage);
    //Serialize column:
    Message::append1Byte(column,outputMessage);
  }

  class Deserialized_1_MAKE_MOVE
  {
  public:
    //In which row player puts her X or O.
    int8_t row;
    //In which column player puts her X or O.
    int8_t column;
  };

  static bool deserialize_1_MAKE_MOVE(const std::vector<char>&inputMessage,
        Deserialized_1_MAKE_MOVE&output)
  throw()
  {
    std::vector<char>::const_iterator it
     = inputMessage.begin();
    const std::vector<char>::const_iterator messageEnd
     = inputMessage.end();
    
    //Check protocol version:
    char protocolVersion=0;
    if(!Message::read1Byte(it,messageEnd,protocolVersion))
      return false;
    if(protocolVersion!=1)
      return false;
    
    //Check message kind:
    char messageKind=0;
    if(!Message::read1Byte(it,messageEnd,messageKind))
      return false;
    if(messageKind!=1)
      return false;

    //Deserialize pieces:

    //Deserialize row:
    if(!Message::read1Byte(it,messageEnd,output.row))
      return false;
    //Deserialize column:
    if(!Message::read1Byte(it,messageEnd,output.column))
      return false;
    return true;
  }

  //Message MOVE_MADE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 2.
  //Simple TicTacToe move. No need to say who made this move.

  static void serialize_1_MOVE_MADE(
        //In which row player puts her X or O.
        const int8_t row,

        //In which column player puts her X or O.
        const int8_t column,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(2,outputMessage);

    //Serialize row:
    Message::append1Byte(row,outputMessage);
    //Serialize column:
    Message::append1Byte(column,outputMessage);
  }

  class Deserialized_1_MOVE_MADE
  {
  public:
    //In which row player puts her X or O.
    int8_t row;
    //In which column player puts her X or O.
    int8_t column;
  };

  static bool deserialize_1_MOVE_MADE(const std::vector<char>&inputMessage,
        Deserialized_1_MOVE_MADE&output)
  throw()
  {
    std::vector<char>::const_iterator it
     = inputMessage.begin();
    const std::vector<char>::const_iterator messageEnd
     = inputMessage.end();
    
    //Check protocol version:
    char protocolVersion=0;
    if(!Message::read1Byte(it,messageEnd,protocolVersion))
      return false;
    if(protocolVersion!=1)
      return false;
    
    //Check message kind:
    char messageKind=0;
    if(!Message::read1Byte(it,messageEnd,messageKind))
      return false;
    if(messageKind!=2)
      return false;

    //Deserialize pieces:

    //Deserialize row:
    if(!Message::read1Byte(it,messageEnd,output.row))
      return false;
    //Deserialize column:
    if(!Message::read1Byte(it,messageEnd,output.column))
      return false;
    return true;
  }

};

class TicTacToeHandler
{
  private:
  //Objects for temporary deserialization (to avoid creating
  //new ones all the time):
  TicTacToeProtocol::Deserialized_1_MAKE_MOVE deserialized_MAKE_MOVE;

public:
  bool handle(const std::vector<char>& message,
              const SessionId sessionID,
              std::list<SessionAddressedMessage>& toBeSent,
              TimeMicro& timeout) throw();

  //Handlers for various message types:
  virtual bool handle_1_MAKE_MOVE(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const int8_t row,
                  const int8_t column) throw() =0;
  virtual ~TicTacToeHandler() throw() {}
};
