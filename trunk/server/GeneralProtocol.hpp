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
#include <map>
#include "Time.hpp"
#include "Message.hpp"


class GeneralProtocol
{
public:

  //Can be used for switching to detect
  //which deserializer should be used.
    static const int8_t UNKNOWN_MESSAGE_1 = 0;
    //Sent by client when creating a new Tic Tac Toe table.
    static const int8_t CREATE_TICTACTOE_TABLE_1 = 1;
    //Sent by server after table has been created.
    static const int8_t TABLE_CREATED_1 = 2;
    //Sent by client when joining some table to play.
    static const int8_t JOIN_TABLE_TO_PLAY_1 = 3;
    //Sent by server to new player who joined a table.
    static const int8_t YOU_JOINED_TABLE_1 = 4;
    //Sent by server after new player joined a table to already present people.
    static const int8_t NEW_PLAYER_JOINED_TABLE_1 = 5;
    //Sent by server when game is started. Some initialization messages can be sent right after. Move from first player(s) is awaited after that.
    static const int8_t GAME_STARTED_1 = 6;
    //Sent by client when making a move.
    static const int8_t MAKE_MOVE_1 = 7;
    //Sent by server after client made a move.
    static const int8_t MOVE_MADE_1 = 8;



  //Can be used for printing message type
  //in human-readable form.
  static std::string messageTypeToString(int8_t messageType) throw()
  {
    switch(messageType)
    {
      case UNKNOWN_MESSAGE_1: return "UNKNOWN_MESSAGE";
      case CREATE_TICTACTOE_TABLE_1: return "CREATE_TICTACTOE_TABLE";
      case TABLE_CREATED_1: return "TABLE_CREATED";
      case JOIN_TABLE_TO_PLAY_1: return "JOIN_TABLE_TO_PLAY";
      case YOU_JOINED_TABLE_1: return "YOU_JOINED_TABLE";
      case NEW_PLAYER_JOINED_TABLE_1: return "NEW_PLAYER_JOINED_TABLE";
      case GAME_STARTED_1: return "GAME_STARTED";
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
  //Message CREATE_TICTACTOE_TABLE:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 1.
  //Sent by client when creating a new Tic Tac Toe table.

  static void serialize_1_CREATE_TICTACTOE_TABLE(    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(1,outputMessage);

  }

  class Deserialized_1_CREATE_TICTACTOE_TABLE
  {
  public:
  };

  static bool deserialize_1_CREATE_TICTACTOE_TABLE(const std::vector<char>&inputMessage)
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

    return true;
  }

  //Message TABLE_CREATED:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 2.
  //Sent by server after table has been created.

  static void serialize_1_TABLE_CREATED(
        //ID for newly created table.
        const int64_t id,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(2,outputMessage);

    //Serialize id:
    Message::append8Bytes(id,outputMessage);
  }

  class Deserialized_1_TABLE_CREATED
  {
  public:
    //ID for newly created table.
    int64_t id;
  };

  static bool deserialize_1_TABLE_CREATED(const std::vector<char>&inputMessage,
        Deserialized_1_TABLE_CREATED&output)
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

    //Deserialize id:
    if(!Message::read8Bytes(it,messageEnd,output.id))
      return false;
    return true;
  }

  //Message JOIN_TABLE_TO_PLAY:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 3.
  //Sent by client when joining some table to play.

  static void serialize_1_JOIN_TABLE_TO_PLAY(
        //Table id.
        const int64_t tableId,

        //Name of the player (not unique).
        const std::string& screenName,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(3,outputMessage);

    //Serialize tableId:
    Message::append8Bytes(tableId,outputMessage);
    //Serialize screenName:
    Message::appendCString(screenName,outputMessage);
  }

  class Deserialized_1_JOIN_TABLE_TO_PLAY
  {
  public:
    //Table id.
    int64_t tableId;
    //Name of the player (not unique).
    std::string screenName;
  };

  static bool deserialize_1_JOIN_TABLE_TO_PLAY(const std::vector<char>&inputMessage,
        Deserialized_1_JOIN_TABLE_TO_PLAY&output)
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
    if(messageKind!=3)
      return false;

    //Deserialize pieces:

    //Deserialize tableId:
    if(!Message::read8Bytes(it,messageEnd,output.tableId))
      return false;
    //Deserialize screenName:
    if(!Message::readCString(it,messageEnd,output.screenName))
      return false;
    return true;
  }

  //Message YOU_JOINED_TABLE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 4.
  //Sent by server to new player who joined a table.

  static void serialize_1_YOU_JOINED_TABLE(
        //New player's table player id.
        const int8_t tablePlayerId,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(4,outputMessage);

    //Serialize tablePlayerId:
    Message::append1Byte(tablePlayerId,outputMessage);
  }

  class Deserialized_1_YOU_JOINED_TABLE
  {
  public:
    //New player's table player id.
    int8_t tablePlayerId;
  };

  static bool deserialize_1_YOU_JOINED_TABLE(const std::vector<char>&inputMessage,
        Deserialized_1_YOU_JOINED_TABLE&output)
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
    if(messageKind!=4)
      return false;

    //Deserialize pieces:

    //Deserialize tablePlayerId:
    if(!Message::read1Byte(it,messageEnd,output.tablePlayerId))
      return false;
    return true;
  }

  //Message NEW_PLAYER_JOINED_TABLE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 5.
  //Sent by server after new player joined a table to already present people.

  static void serialize_1_NEW_PLAYER_JOINED_TABLE(
        //New player's name.
        const std::string& screenName,

        //New player's table player id.
        const int8_t tablePlayerId,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(5,outputMessage);

    //Serialize screenName:
    Message::appendCString(screenName,outputMessage);
    //Serialize tablePlayerId:
    Message::append1Byte(tablePlayerId,outputMessage);
  }

  class Deserialized_1_NEW_PLAYER_JOINED_TABLE
  {
  public:
    //New player's name.
    std::string screenName;
    //New player's table player id.
    int8_t tablePlayerId;
  };

  static bool deserialize_1_NEW_PLAYER_JOINED_TABLE(const std::vector<char>&inputMessage,
        Deserialized_1_NEW_PLAYER_JOINED_TABLE&output)
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
    if(messageKind!=5)
      return false;

    //Deserialize pieces:

    //Deserialize screenName:
    if(!Message::readCString(it,messageEnd,output.screenName))
      return false;
    //Deserialize tablePlayerId:
    if(!Message::read1Byte(it,messageEnd,output.tablePlayerId))
      return false;
    return true;
  }

  //Message GAME_STARTED:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 6.
  //Sent by server when game is started. Some initialization messages can be sent right after. Move from first player(s) is awaited after that.

  static void serialize_1_GAME_STARTED(    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(6,outputMessage);

  }

  class Deserialized_1_GAME_STARTED
  {
  public:
  };

  static bool deserialize_1_GAME_STARTED(const std::vector<char>&inputMessage)
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
    if(messageKind!=6)
      return false;

    //Deserialize pieces:

    return true;
  }

  //Message MAKE_MOVE:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 7.
  //Sent by client when making a move.

  static void serialize_1_MAKE_MOVE(
        //Game-specific move information.
        const std::vector<char>& move,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(7,outputMessage);

    //Serialize move:
    Message::appendBinary(move,outputMessage);
  }

  class Deserialized_1_MAKE_MOVE
  {
  public:
    //Game-specific move information.
    std::vector<char> move;
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
    if(messageKind!=7)
      return false;

    //Deserialize pieces:

    //Deserialize move:
    if(!Message::readBinary(it,messageEnd,output.move))
      return false;
    return true;
  }

  //Message MOVE_MADE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 8.
  //Sent by server after client made a move.

  static void serialize_1_MOVE_MADE(
        //Game-specific move information.
        const std::vector<char>& move,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(8,outputMessage);

    //Serialize move:
    Message::appendBinary(move,outputMessage);
  }

  class Deserialized_1_MOVE_MADE
  {
  public:
    //Game-specific move information.
    std::vector<char> move;
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
    if(messageKind!=8)
      return false;

    //Deserialize pieces:

    //Deserialize move:
    if(!Message::readBinary(it,messageEnd,output.move))
      return false;
    return true;
  }

};

class GeneralHandler
{
  private:
  //Objects for temporary deserialization (to avoid creating
  //new ones all the time):
  GeneralProtocol::Deserialized_1_JOIN_TABLE_TO_PLAY deserialized_JOIN_TABLE_TO_PLAY;
  GeneralProtocol::Deserialized_1_MAKE_MOVE deserialized_MAKE_MOVE;

public:
  bool handle(const std::vector<char>& message,
              const int32_t sessionID,
              std::multimap<int32_t,std::vector<char> >& toBeSent,
              TimeMicro& timeout) throw();

  //Handlers for various message types:
  virtual bool handle_1_CREATE_TICTACTOE_TABLE(const int32_t sessionID,
                  std::multimap<int32_t,std::vector<char> >& toBeSent,
                  TimeMicro& timeout) throw() =0;
  virtual bool handle_1_JOIN_TABLE_TO_PLAY(const int32_t sessionID,
                  std::multimap<int32_t,std::vector<char> >& toBeSent,
                  TimeMicro& timeout,
                  const int64_t tableId,
                  const std::string& screenName) throw() =0;
  virtual bool handle_1_MAKE_MOVE(const int32_t sessionID,
                  std::multimap<int32_t,std::vector<char> >& toBeSent,
                  TimeMicro& timeout,
                  const std::vector<char>& move) throw() =0;
  virtual ~GeneralHandler() throw() {}
};
