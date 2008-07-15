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

class GeneralProtocol
{
public:

  //Can be used for switching to detect
  //which deserializer should be used.
    static const int8_t UNKNOWN_MESSAGE_1 = 0;
    //Sent by client when creating a new Tic Tac Toe table.
    static const int8_t CREATE_TICTACTOE_TABLE_1 = 1;
    //Sent by client when creating a new Thousand table.
    static const int8_t CREATE_THOUSAND_TABLE_1 = 2;
    //Sent by server after table has been created.
    static const int8_t TABLE_CREATED_1 = 3;
    //Sent by client when saying something (chat message).
    static const int8_t SAY_1 = 4;
    //Sent by server when someone says something (chat message).
    static const int8_t SAID_1 = 5;
    //Sent by client when joining some table to play.
    static const int8_t JOIN_TABLE_TO_PLAY_1 = 6;
    //Sent by server to new player who joined a table.
    static const int8_t YOU_JOINED_TABLE_1 = 7;
    //Sent by server to new player who joined a table.
    static const int8_t JOINING_TABLE_FAILED_INCORRECT_TABLE_ID_1 = 8;
    //Sent by server after new player joined a table to already present people.
    static const int8_t NEW_PLAYER_JOINED_TABLE_1 = 9;
    //Sent by server when a player left.
    static const int8_t PLAYER_LEFT_TABLE_1 = 10;
    //Sent by server when game is started and no initial message is designated for the receiver.
    static const int8_t GAME_STARTED_WITHOUT_INITIAL_MESSAGE_1 = 11;
    //Sent by server when game is started and an initial message is designated for the receiver.
    static const int8_t GAME_STARTED_WITH_INITIAL_MESSAGE_1 = 12;
    //Sent by client when making a move.
    static const int8_t MAKE_MOVE_1 = 13;
    //Sent by server after client made a move.
    static const int8_t MOVE_MADE_1 = 14;



  //Constants:


  //Can be used for printing message type
  //in human-readable form.
  static std::string messageTypeToString(int8_t messageType) throw()
  {
    switch(messageType)
    {
      case UNKNOWN_MESSAGE_1: return "UNKNOWN_MESSAGE";
      case CREATE_TICTACTOE_TABLE_1: return "CREATE_TICTACTOE_TABLE";
      case CREATE_THOUSAND_TABLE_1: return "CREATE_THOUSAND_TABLE";
      case TABLE_CREATED_1: return "TABLE_CREATED";
      case SAY_1: return "SAY";
      case SAID_1: return "SAID";
      case JOIN_TABLE_TO_PLAY_1: return "JOIN_TABLE_TO_PLAY";
      case YOU_JOINED_TABLE_1: return "YOU_JOINED_TABLE";
      case JOINING_TABLE_FAILED_INCORRECT_TABLE_ID_1: return "JOINING_TABLE_FAILED_INCORRECT_TABLE_ID";
      case NEW_PLAYER_JOINED_TABLE_1: return "NEW_PLAYER_JOINED_TABLE";
      case PLAYER_LEFT_TABLE_1: return "PLAYER_LEFT_TABLE";
      case GAME_STARTED_WITHOUT_INITIAL_MESSAGE_1: return "GAME_STARTED_WITHOUT_INITIAL_MESSAGE";
      case GAME_STARTED_WITH_INITIAL_MESSAGE_1: return "GAME_STARTED_WITH_INITIAL_MESSAGE";
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
      <<"MSG "<<messageTypeToString(getMessageType(message))
      <<" (General v1, "<<message.size()<<"B):"<<std::endl;

    const char*const hex = "0123456789ABCDEF";

    for(uint_fast16_t line=0;20*line<message.size();line++)
    {
      //Print hexadecimal line:
      for(uint_fast16_t i=20*line;i<20*(line+1) && i<message.size();i++)
        {
          const char c = message[i];
          output<<hex[(c>>4) & 0x0F]<<hex[c & 0x0F]<<' ';
        }
      output<<std::endl;
      //Print human--readable line:
      for(uint_fast16_t i=20*line;i<20*(line+1) && i<message.size();i++)
        {
          const char c = message[i];
          if(std::isprint(c))
            output<<c<<"  ";
          else
            output<<"__ ";
        }
      output<<std::endl;
    }
    return output.str();
  }
  //Message CREATE_TICTACTOE_TABLE:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 1.
  //Sent by client when creating a new Tic Tac Toe table.

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

  //Message CREATE_THOUSAND_TABLE:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 2.
  //Sent by client when creating a new Thousand table.

  class Deserialized_1_CREATE_THOUSAND_TABLE
  {
  public:
  };

  static bool deserialize_1_CREATE_THOUSAND_TABLE(const std::vector<char>&inputMessage)
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

    return true;
  }

  //Message TABLE_CREATED:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 3.
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
    Message::append1Byte(3,outputMessage);

    //Serialize id:
    Message::append8Bytes(id,outputMessage);
  }

  //Message SAY:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 4.
  //Sent by client when saying something (chat message).

  class Deserialized_1_SAY
  {
  public:
    //Text of the chat message in UTF8 encoding.
    std::vector<char> text_UTF8;
  };

  static bool deserialize_1_SAY(const std::vector<char>&inputMessage,
        Deserialized_1_SAY&output)
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

    //Deserialize text_UTF8:
    if(!Message::readBinary(it,messageEnd,output.text_UTF8))
      return false;
    return true;
  }

  //Message SAID:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 5.
  //Sent by server when someone says something (chat message).

  static void serialize_1_SAID(
        //Who said it.
        const int8_t tablePlayerId,

        //Text of the chat message in UTF8 encoding.
        const std::vector<char>& text_UTF8,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(5,outputMessage);

    //Serialize tablePlayerId:
    Message::append1Byte(tablePlayerId,outputMessage);
    //Serialize text_UTF8:
    Message::appendBinary(text_UTF8,outputMessage);
  }

  //Message JOIN_TABLE_TO_PLAY:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 6.
  //Sent by client when joining some table to play.

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
    if(messageKind!=6)
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

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 7.
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
    Message::append1Byte(7,outputMessage);

    //Serialize tablePlayerId:
    Message::append1Byte(tablePlayerId,outputMessage);
  }

  //Message JOINING_TABLE_FAILED_INCORRECT_TABLE_ID:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 8.
  //Sent by server to new player who joined a table.

  static void serialize_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID(    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(8,outputMessage);

  }

  //Message NEW_PLAYER_JOINED_TABLE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 9.
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
    Message::append1Byte(9,outputMessage);

    //Serialize screenName:
    Message::appendCString(screenName,outputMessage);
    //Serialize tablePlayerId:
    Message::append1Byte(tablePlayerId,outputMessage);
  }

  //Message PLAYER_LEFT_TABLE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 10.
  //Sent by server when a player left.

  static void serialize_1_PLAYER_LEFT_TABLE(
        //Leaving player's table player id.
        const int8_t tablePlayerId,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(10,outputMessage);

    //Serialize tablePlayerId:
    Message::append1Byte(tablePlayerId,outputMessage);
  }

  //Message GAME_STARTED_WITHOUT_INITIAL_MESSAGE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 11.
  //Sent by server when game is started and no initial message is designated for the receiver.

  static void serialize_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE(
        //Specifies how many players will play the just-started game, which tablePlayerIdeach of them has, and what's their order.
        const std::vector<int8_t>& turnGamePlayerToTablePlayerId,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(11,outputMessage);

    //Serialize turnGamePlayerToTablePlayerId:
    Message::appendVector(turnGamePlayerToTablePlayerId,outputMessage);
  }

  //Message GAME_STARTED_WITH_INITIAL_MESSAGE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 12.
  //Sent by server when game is started and an initial message is designated for the receiver.

  static void serialize_1_GAME_STARTED_WITH_INITIAL_MESSAGE(
        //Specifies how many players will play the just-started game, which tablePlayerIdeach of them has, and what's their order.
        const std::vector<int8_t>& turnGamePlayerToTablePlayerId,

        //Game-specific initial information.
        const std::vector<char>& initialMessage,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(12,outputMessage);

    //Serialize turnGamePlayerToTablePlayerId:
    Message::appendVector(turnGamePlayerToTablePlayerId,outputMessage);
    //Serialize initialMessage:
    Message::appendBinary(initialMessage,outputMessage);
  }

  //Message MAKE_MOVE:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 13.
  //Sent by client when making a move.

  class Deserialized_1_MAKE_MOVE
  {
  public:
    //Game-specific move information.
    std::vector<char> gameMove;
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
    if(messageKind!=13)
      return false;

    //Deserialize pieces:

    //Deserialize gameMove:
    if(!Message::readBinary(it,messageEnd,output.gameMove))
      return false;
    return true;
  }

  //Message MOVE_MADE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 14.
  //Sent by server after client made a move.

  static void serialize_1_MOVE_MADE(
        //Game-specific move information.
        const std::vector<char>& gameMove,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(14,outputMessage);

    //Serialize gameMove:
    Message::appendBinary(gameMove,outputMessage);
  }

};

class GeneralHandler
{
  private:
  //Objects for temporary deserialization (to avoid creating
  //new ones all the time):
  GeneralProtocol::Deserialized_1_SAY deserialized_SAY;
  GeneralProtocol::Deserialized_1_JOIN_TABLE_TO_PLAY deserialized_JOIN_TABLE_TO_PLAY;
  GeneralProtocol::Deserialized_1_MAKE_MOVE deserialized_MAKE_MOVE;

public:
  bool handle(const std::vector<char>& message,
              const SessionId sessionID,
              std::list<SessionAddressedMessage>& toBeSent,
              TimeMicro& timeout) throw();

  //Handlers for various message types:
  virtual bool handle_1_CREATE_TICTACTOE_TABLE(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout) throw() =0;
  virtual bool handle_1_CREATE_THOUSAND_TABLE(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout) throw() =0;
  virtual bool handle_1_SAY(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const std::vector<char>& text_UTF8) throw() =0;
  virtual bool handle_1_JOIN_TABLE_TO_PLAY(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const int64_t tableId,
                  const std::string& screenName) throw() =0;
  virtual bool handle_1_MAKE_MOVE(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const std::vector<char>& gameMove) throw() =0;
  virtual ~GeneralHandler() throw() {}
};
