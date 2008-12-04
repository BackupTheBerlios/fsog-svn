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

class ThousandProtocol
{
public:

  //Can be used for switching to detect
  //which deserializer should be used.
    static const int8_t UNKNOWN_MESSAGE_1 = 0;
    //Everybody pressed start, so let's deal the cards. Seven cards are represented as 3--byte thousandCardSet.
    static const int8_t DEAL_1 = 1;
    //The message sent by client when placing a bid. Also sent by server when bid was made.
    static const int8_t BID_1 = 2;
    //Message sent by the server to let players know that bidding is over. 'Must' is not shown.
    static const int8_t BID_END_HIDDEN_MUST_1 = 3;
    //Message sent by the server to let players know that bidding is over. 'Must' is shown.
    static const int8_t BID_END_SHOW_MUST_1 = 4;
    //Message sent by the client when selecting a card., which is given to opponent. The same message is sent to opponent from the server.
    static const int8_t SELECT_1 = 5;
    //Message sent by the server when selecting a card., which is given to some opponent. This message is sent to player who should not see the card.
    static const int8_t SELECT_HIDDEN_1 = 6;
    //The message sent by client when decided on how much to play. Also sent by server when contract was made.
    static const int8_t CONTRACT_1 = 7;
    //Message sent by the client when playing a card. Also sometimes sent by server back to clients. There's 24 cards to be played. On cards 1-23 this message is sent by client to server and the server sends some message back to other people, but not the one who played it. On card 24 (that is when one round finishes) some message is sent to all people, including the one who played the last card.
    static const int8_t PLAY_1 = 8;
    //Message sent by server back to clients, when new trump is set.
    static const int8_t PLAY_NEW_TRUMP_1 = 9;
    //Message sent by server back to clients, when last card was played and new cards are dealt. Seven cards are represented as 3--byte thousandCardSet.
    static const int8_t PLAY_AND_DEAL_1 = 10;



  //Constants:
  static const int8_t ACE_SHIFT = 5;
  static const int8_t TEN_SHIFT = 4;
  static const int8_t KING_SHIFT = 3;
  static const int8_t QUEEN_SHIFT = 2;
  static const int8_t JACK_SHIFT = 1;
  static const int8_t NINE_SHIFT = 0;
  static const int8_t HEART_SHIFT = 18;
  static const int8_t DIAMOND_SHIFT = 12;
  static const int8_t CLUB_SHIFT = 6;
  static const int8_t SPADE_SHIFT = 0;
  static const int8_t NO_TRUMP_SHIFT = 24;


  //Can be used for printing message type
  //in human-readable form.
  static std::string messageTypeToString(int8_t messageType) throw()
  {
    switch(messageType)
    {
      case UNKNOWN_MESSAGE_1: return "UNKNOWN_MESSAGE";
      case DEAL_1: return "DEAL";
      case BID_1: return "BID";
      case BID_END_HIDDEN_MUST_1: return "BID_END_HIDDEN_MUST";
      case BID_END_SHOW_MUST_1: return "BID_END_SHOW_MUST";
      case SELECT_1: return "SELECT";
      case SELECT_HIDDEN_1: return "SELECT_HIDDEN";
      case CONTRACT_1: return "CONTRACT";
      case PLAY_1: return "PLAY";
      case PLAY_NEW_TRUMP_1: return "PLAY_NEW_TRUMP";
      case PLAY_AND_DEAL_1: return "PLAY_AND_DEAL";
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
      <<" (Thousand v1, "<<message.size()<<"B):"<<std::endl;

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
  //Message DEAL:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 1.
  //Everybody pressed start, so let's deal the cards. Seven cards are represented as 3--byte thousandCardSet.

  static void serialize_1_DEAL(
        //Set of dealt cards.
        const int32_t thousandCardSet,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(1,outputMessage);

    //Serialize thousandCardSet:
    Message::append3Bytes(thousandCardSet,outputMessage);
  }

  //Message BID:

  //This message will create: [CPP_SERIALIZER, CPP_DESERIALIZER, JAVA_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 2.
  //The message sent by client when placing a bid. Also sent by server when bid was made.

  static void serialize_1_BID(
        //This is actually 10% of the bid. Multiply by 10 to get the real value. A bid10 of 0 is 'pass'. On the last 'pass', this message is not sent. Instead, BID_END_... is sent.
        const int8_t bid10,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(2,outputMessage);

    //Serialize bid10:
    Message::append1Byte(bid10,outputMessage);
  }

  class Deserialized_1_BID
  {
  public:
    //This is actually 10% of the bid. Multiply by 10 to get the real value. A bid10 of 0 is 'pass'. On the last 'pass', this message is not sent. Instead, BID_END_... is sent.
    int8_t bid10;
  };

  static bool deserialize_1_BID(const std::vector<char>&inputMessage,
        Deserialized_1_BID&output)
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

    //Deserialize bid10:
    if(!Message::read1Byte(it,messageEnd,output.bid10))
      return false;
    return true;
  }

  static bool deserialize_1_BID(const std::vector<char>&inputMessage,
        int8_t& bid10)
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

    //Deserialize bid10:
    if(!Message::read1Byte(it,messageEnd,bid10))
      return false;
    return true;
  }

  //Message BID_END_HIDDEN_MUST:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 3.
  //Message sent by the server to let players know that bidding is over. 'Must' is not shown.

  static void serialize_1_BID_END_HIDDEN_MUST(    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(3,outputMessage);

  }

  //Message BID_END_SHOW_MUST:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 4.
  //Message sent by the server to let players know that bidding is over. 'Must' is shown.

  static void serialize_1_BID_END_SHOW_MUST(
        //Set of cards represented as ThousandCardSet.
        const int32_t must,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(4,outputMessage);

    //Serialize must:
    Message::append3Bytes(must,outputMessage);
  }

  //Message SELECT:

  //This message will create: [CPP_SERIALIZER, CPP_DESERIALIZER, JAVA_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 5.
  //Message sent by the client when selecting a card., which is given to opponent. The same message is sent to opponent from the server.

  static void serialize_1_SELECT(
        //Shift of selected card.
        const int8_t shift,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(5,outputMessage);

    //Serialize shift:
    Message::append1Byte(shift,outputMessage);
  }

  class Deserialized_1_SELECT
  {
  public:
    //Shift of selected card.
    int8_t shift;
  };

  static bool deserialize_1_SELECT(const std::vector<char>&inputMessage,
        Deserialized_1_SELECT&output)
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

    //Deserialize shift:
    if(!Message::read1Byte(it,messageEnd,output.shift))
      return false;
    return true;
  }

  static bool deserialize_1_SELECT(const std::vector<char>&inputMessage,
        int8_t& shift)
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

    //Deserialize shift:
    if(!Message::read1Byte(it,messageEnd,shift))
      return false;
    return true;
  }

  //Message SELECT_HIDDEN:

  //This message will create: [CPP_SERIALIZER, CPP_DESERIALIZER, JAVA_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 6.
  //Message sent by the server when selecting a card., which is given to some opponent. This message is sent to player who should not see the card.

  static void serialize_1_SELECT_HIDDEN(    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(6,outputMessage);

  }

  class Deserialized_1_SELECT_HIDDEN
  {
  public:
  };

  static bool deserialize_1_SELECT_HIDDEN(const std::vector<char>&inputMessage)
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

  //Message CONTRACT:

  //This message will create: [CPP_SERIALIZER, CPP_DESERIALIZER, JAVA_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 7.
  //The message sent by client when decided on how much to play. Also sent by server when contract was made.

  static void serialize_1_CONTRACT(
        //This is actually 10% of the contract. Multiply by 10 to get the real value.
        const int8_t contract10,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(7,outputMessage);

    //Serialize contract10:
    Message::append1Byte(contract10,outputMessage);
  }

  class Deserialized_1_CONTRACT
  {
  public:
    //This is actually 10% of the contract. Multiply by 10 to get the real value.
    int8_t contract10;
  };

  static bool deserialize_1_CONTRACT(const std::vector<char>&inputMessage,
        Deserialized_1_CONTRACT&output)
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

    //Deserialize contract10:
    if(!Message::read1Byte(it,messageEnd,output.contract10))
      return false;
    return true;
  }

  static bool deserialize_1_CONTRACT(const std::vector<char>&inputMessage,
        int8_t& contract10)
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

    //Deserialize contract10:
    if(!Message::read1Byte(it,messageEnd,contract10))
      return false;
    return true;
  }

  //Message PLAY:

  //This message will create: [CPP_SERIALIZER, CPP_DESERIALIZER, JAVA_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 8.
  //Message sent by the client when playing a card. Also sometimes sent by server back to clients. There's 24 cards to be played. On cards 1-23 this message is sent by client to server and the server sends some message back to other people, but not the one who played it. On card 24 (that is when one round finishes) some message is sent to all people, including the one who played the last card.

  static void serialize_1_PLAY(
        //Shift of played card.
        const int8_t shift,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(8,outputMessage);

    //Serialize shift:
    Message::append1Byte(shift,outputMessage);
  }

  class Deserialized_1_PLAY
  {
  public:
    //Shift of played card.
    int8_t shift;
  };

  static bool deserialize_1_PLAY(const std::vector<char>&inputMessage,
        Deserialized_1_PLAY&output)
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

    //Deserialize shift:
    if(!Message::read1Byte(it,messageEnd,output.shift))
      return false;
    return true;
  }

  static bool deserialize_1_PLAY(const std::vector<char>&inputMessage,
        int8_t& shift)
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

    //Deserialize shift:
    if(!Message::read1Byte(it,messageEnd,shift))
      return false;
    return true;
  }

  //Message PLAY_NEW_TRUMP:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 9.
  //Message sent by server back to clients, when new trump is set.

  static void serialize_1_PLAY_NEW_TRUMP(
        //Shift of played card.
        const int8_t shift,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(9,outputMessage);

    //Serialize shift:
    Message::append1Byte(shift,outputMessage);
  }

  //Message PLAY_AND_DEAL:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 10.
  //Message sent by server back to clients, when last card was played and new cards are dealt. Seven cards are represented as 3--byte thousandCardSet.

  static void serialize_1_PLAY_AND_DEAL(
        //Shift of played card.
        const int8_t shift,

        //Set of dealt cards.
        const int32_t thousandCardSet,
    std::vector<char>&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    Message::append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message::append1Byte(10,outputMessage);

    //Serialize shift:
    Message::append1Byte(shift,outputMessage);
    //Serialize thousandCardSet:
    Message::append3Bytes(thousandCardSet,outputMessage);
  }

};

class ThousandHandler
{
  private:
  //Objects for temporary deserialization (to avoid creating
  //new ones all the time):
  ThousandProtocol::Deserialized_1_BID deserialized_BID;
  ThousandProtocol::Deserialized_1_SELECT deserialized_SELECT;
  ThousandProtocol::Deserialized_1_CONTRACT deserialized_CONTRACT;
  ThousandProtocol::Deserialized_1_PLAY deserialized_PLAY;

public:
  bool handle(const std::vector<char>& message,
              const SessionId sessionID,
              std::list<SessionAddressedMessage>& toBeSent,
              TimeMicro& timeout) throw();

  //Handlers for various message types:
  virtual bool handle_1_BID(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const int8_t bid10) throw() =0;
  virtual bool handle_1_SELECT(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const int8_t shift) throw() =0;
  virtual bool handle_1_SELECT_HIDDEN(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout) throw() =0;
  virtual bool handle_1_CONTRACT(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const int8_t contract10) throw() =0;
  virtual bool handle_1_PLAY(const SessionId sessionID,
                  std::list<SessionAddressedMessage>& toBeSent,
                  TimeMicro& timeout,
                  const int8_t shift) throw() =0;
  virtual ~ThousandHandler() throw() {}
};
