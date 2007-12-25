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

#include "Message.hpp"

class Protocol
{
public:

  //Known UDP port number of the server:
  static uint_fast16_t getServerUDPPort_1()
    throw()
  {
    return 10137;
  }

  //Can be used for switching to detect
  //which deserializer should be used.
  enum MessageType
  {
    UNKNOWN_MESSAGE_1 = 0,
    //Sent by client to log in. Loginng in associates the sending IP+port as belonging to the particular user until logged out. Can be used to create an account. Valid nick is between 5 and 20 letters, consisting of any printable non-whitespace ASCII characters. Password should be at least 6 characters. Easy passwords won't be admitted.
    LOG_IN_1 = 1,
    //Sent by server to let the user know that log in was OK.
    LOG_IN_CORRECT_1 = 2,
    //Sent by server to let the user know that log in wasn't OK.
    LOG_IN_INCORRECT_1 = 3,
    //Message sent by client to request 'room' statistics.
    GET_STATISTICS_1 = 4,
    //Sent by server when returning info about 'room' stats.
    RETURN_STATISTICS_1 = 5,
    //Sent by client when searching for some game.
    SEARCH_GAME_1 = 6,
    //Sent by server when no game with given search cryteria is currently available.
    AWAIT_GAME_1 = 7,
    //Sent by server when game is matched. Includes game settings. One of four usernames will be equal to the requesting player's if she should play the game, otherwise it's observing. Must be acknowledge by the client program immediately, using the secret provided here.
    PROPOSED_GAME_1 = 8,
    //Sent by client. After this, server just waits for GAME_START. If server doesn't get this ack for some reason, will send again PROPOSED_GAME and ack must be repeated.
    ACKNOWLEDGE_PROPOSED_GAME_1 = 9,
    //Client clicks the 'start button' to accept a game. Immediately after all clients do this, server will deal cards and start counting time for some player. Server sends no acknowledgement for it. If cards are not dealt after, say, 5s, client can re-send GAME_START. But they might not be dealt because the opponent didn't click GAME_START.
    GAME_START_1 = 10,
    //Everybody pressed start, so let's deal the cards. The cards sent are not sorted in any way.
    GAME_DEAL_7_CARDS_1 = 11,
    //The message sent by client when placing a bid.
    GAME_BID_1 = 12,
    //Message sent by the server to let other players know what bid was placed.
    GAME_BID_MADE_1 = 13,
    //Message sent by the server to let players know that bidding is over. 'Must' is not shown.
    GAME_BID_END_HIDDEN_MUST_1 = 14,
    //Message sent by the server to let players know that bidding is over. 'Must' is shown.
    GAME_BID_END_SHOW_MUST_1 = 15
  };

  //Used in card games. You can encode a card on 
  //one byte by bitwise disjunction of value and
  //color. Use VALUE_MASK and COLOR_MASK to decode.
  enum Card
  {
    ACE = 0x01,
    TWO = 0x02,
    THREE = 0x03,
    FOUR = 0x04,
    FIVE = 0x05,
    SIX = 0x06,
    SEVEN = 0x07,
    EIGHT = 0x08,
    NINE = 0x09,
    TEN = 0x0A,
    JACK = 0x0B,
    QUEEN = 0x0C,
    KING = 0x0D,
    JOKER = 0x0E,
    VALUE_MASK = 0x0F,
    HEARTS = 0x10,
    DIAMONDS = 0x20,
    CLUBS = 0x30,
    SPADES = 0x40,
    COLOR_MASK = 0xF0
  };

  //Flags 'ThousandFlags':
  //Various booleans used in search for saying what games user is interested in. true - interested, false - not interested. Also used for game settings.
  enum ThousandFlags
  {
    //2-players game.
    TWO_PLAYERS = 1,
    //3-players game.
    THREE_PLAYERS = 2,
    //4-players game.
    FOUR_PLAYERS = 4,
    //Game without bombs.
    NO_BOMBS = 8,
    //Game with bombs.
    BOMBS = 16,
    //Game with unlimited bombs.
    UNLIMITED_BOMBS = 32,
    //Game with 10 bid increment.
    BID_INCREMENT_10 = 64,
    //Game with any bid increment.
    BID_INCREMENT_ANY = 128,
    //Game where to get points you must make a game if you have >= 800 points.
    MUST_PLAY_FROM_800 = 256,
    //Game where to get points you must make a game if you have >= 900 points.
    MUST_PLAY_FROM_900 = 512,
    //Game where 'must' is always shown.
    SHOW_MUST_100 = 1024,
    //Game where 'must' is shown from 110.
    SHOW_MUST_110 = 2048,
    //Public game.
    PUBLIC = 4096,
    //Private game.
    PRIVATE = 8192,
    //Ranking game.
    RANKING_GAME = 16384,
    //Sparring (non-ranked) game.
    SPARRING_GAME = 32768,
    //7 minutes game.
    TIME_7 = 65536,
    //10 minutes game.
    TIME_10 = 131072,
    //15 minutes game.
    TIME_15 = 262144,
    //20 minutes game.
    TIME_20 = 524288,
    //30 minutes game.
    TIME_30 = 1048576
  };

  //This method can be used for rapid message
  //type lookup, so you don't need to try
  //deserializing using all deserializers.
  //Remember that deserialization can still
  //always fail, even if this method returns
  //some known type. It doesn't read the whole
  //message, just the part where message type
  //is present.
  static MessageType lookupMessageType(const Message&message)
  {
    if(message.size()<2)
      return Protocol::MessageType(UNKNOWN_MESSAGE_1);
    
    const char messageType
     = message[1];
    
    if(messageType<=Protocol::MessageType(UNKNOWN_MESSAGE_1)
       || messageType>GAME_BID_END_SHOW_MUST_1)
      return Protocol::MessageType(UNKNOWN_MESSAGE_1);
    
    return static_cast<MessageType>(messageType);
  }

  //Message LOG_IN:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 1.
  //Sent by client to log in. Loginng in associates the sending IP+port as belonging to the particular user until logged out. Can be used to create an account. Valid nick is between 5 and 20 letters, consisting of any printable non-whitespace ASCII characters. Password should be at least 6 characters. Easy passwords won't be admitted.

  static void serialize_1_LOG_IN(
        //Unique nick.
        const std::string& nick,

        //User's password.
        const std::string& password,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(1);

    //Serialize nick:
    outputMessage.appendCString(nick);
    //Serialize password:
    outputMessage.appendCString(password);
  }

  class Deserialized_1_LOG_IN
  {
  public:
    //Unique nick.
    std::string nick;
    //User's password.
    std::string password;
  };

  static bool deserialize_1_LOG_IN(const Message&inputMessage,
        Deserialized_1_LOG_IN&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    //Deserialize nick:
    if(!Message::readCString(it,messageEnd,output.nick))
      return false;
    //Deserialize password:
    if(!Message::readCString(it,messageEnd,output.password))
      return false;
    return true;
  }

  //Message LOG_IN_CORRECT:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 2.
  //Sent by server to let the user know that log in was OK.

  static void serialize_1_LOG_IN_CORRECT(    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(2);

  }

  class Deserialized_1_LOG_IN_CORRECT
  {
  public:
  };

  static bool deserialize_1_LOG_IN_CORRECT(const Message&inputMessage)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

  //Message LOG_IN_INCORRECT:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 3.
  //Sent by server to let the user know that log in wasn't OK.

  static void serialize_1_LOG_IN_INCORRECT(
        //Why login failed.
        const std::string& reason,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(3);

    //Serialize reason:
    outputMessage.appendCString(reason);
  }

  class Deserialized_1_LOG_IN_INCORRECT
  {
  public:
    //Why login failed.
    std::string reason;
  };

  static bool deserialize_1_LOG_IN_INCORRECT(const Message&inputMessage,
        Deserialized_1_LOG_IN_INCORRECT&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    //Deserialize reason:
    if(!Message::readCString(it,messageEnd,output.reason))
      return false;
    return true;
  }

  //Message GET_STATISTICS:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 4.
  //Message sent by client to request 'room' statistics.

  static void serialize_1_GET_STATISTICS(    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(4);

  }

  class Deserialized_1_GET_STATISTICS
  {
  public:
  };

  static bool deserialize_1_GET_STATISTICS(const Message&inputMessage)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    return true;
  }

  //Message RETURN_STATISTICS:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 5.
  //Sent by server when returning info about 'room' stats.

  static void serialize_1_RETURN_STATISTICS(
        //Number of people logged in.
        const int32_t numberOfUsers,

        //Number of games being played in the 'room'.
        const int32_t numberOfGames,

        //Number of people searching in the 'room'.
        const int32_t numberOfSearchers,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(5);

    //Serialize numberOfUsers:
    outputMessage.append4Bytes(numberOfUsers);
    //Serialize numberOfGames:
    outputMessage.append4Bytes(numberOfGames);
    //Serialize numberOfSearchers:
    outputMessage.append4Bytes(numberOfSearchers);
  }

  class Deserialized_1_RETURN_STATISTICS
  {
  public:
    //Number of people logged in.
    int32_t numberOfUsers;
    //Number of games being played in the 'room'.
    int32_t numberOfGames;
    //Number of people searching in the 'room'.
    int32_t numberOfSearchers;
  };

  static bool deserialize_1_RETURN_STATISTICS(const Message&inputMessage,
        Deserialized_1_RETURN_STATISTICS&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    //Deserialize numberOfUsers:
    if(!Message::read4Bytes(it,messageEnd,output.numberOfUsers))
      return false;
    //Deserialize numberOfGames:
    if(!Message::read4Bytes(it,messageEnd,output.numberOfGames))
      return false;
    //Deserialize numberOfSearchers:
    if(!Message::read4Bytes(it,messageEnd,output.numberOfSearchers))
      return false;
    return true;
  }

  //Message SEARCH_GAME:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 6.
  //Sent by client when searching for some game.

  static void serialize_1_SEARCH_GAME(
        //Minimum ranking of the oponent, measured in 50 points. E.g. 60 means 60*50=3000 points.
        const int8_t minimumOponentRanking50,

        //Have a look at ThousandFlags.
        const int32_t searchFlags,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(6);

    //Serialize minimumOponentRanking50:
    outputMessage.append1Byte(minimumOponentRanking50);
    //Serialize searchFlags:
    outputMessage.append3Bytes(searchFlags);
  }

  class Deserialized_1_SEARCH_GAME
  {
  public:
    //Minimum ranking of the oponent, measured in 50 points. E.g. 60 means 60*50=3000 points.
    int8_t minimumOponentRanking50;
    //Have a look at ThousandFlags.
    int32_t searchFlags;
  };

  static bool deserialize_1_SEARCH_GAME(const Message&inputMessage,
        Deserialized_1_SEARCH_GAME&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    //Deserialize minimumOponentRanking50:
    if(!Message::read1Byte(it,messageEnd,output.minimumOponentRanking50))
      return false;
    //Deserialize searchFlags:
    if(!Message::read3Bytes(it,messageEnd,output.searchFlags))
      return false;
    return true;
  }

  //Message AWAIT_GAME:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 7.
  //Sent by server when no game with given search cryteria is currently available.

  static void serialize_1_AWAIT_GAME(    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(7);

  }

  class Deserialized_1_AWAIT_GAME
  {
  public:
  };

  static bool deserialize_1_AWAIT_GAME(const Message&inputMessage)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    return true;
  }

  //Message PROPOSED_GAME:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 8.
  //Sent by server when game is matched. Includes game settings. One of four usernames will be equal to the requesting player's if she should play the game, otherwise it's observing. Must be acknowledge by the client program immediately, using the secret provided here.

  static void serialize_1_PROPOSED_GAME(
        //The username (nick) of player #0. Empty string means no player at this side of the table.
        const std::string& player0Username,

        //The username (nick) of player #1. Empty string means no player at this side of the table.
        const std::string& player1Username,

        //The username (nick) of player #2. Empty string means no player at this side of the table.
        const std::string& player2Username,

        //The username (nick) of player #3. Empty string means no player at this side of the table.
        const std::string& player3Username,

        //Look at ThousandFlags.
        const int32_t gameSettings,

        //The same value must be sent by client when acknowledging.
        const int8_t acknowledgeSecret,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(8);

    //Serialize player0Username:
    outputMessage.appendCString(player0Username);
    //Serialize player1Username:
    outputMessage.appendCString(player1Username);
    //Serialize player2Username:
    outputMessage.appendCString(player2Username);
    //Serialize player3Username:
    outputMessage.appendCString(player3Username);
    //Serialize gameSettings:
    outputMessage.append3Bytes(gameSettings);
    //Serialize acknowledgeSecret:
    outputMessage.append1Byte(acknowledgeSecret);
  }

  class Deserialized_1_PROPOSED_GAME
  {
  public:
    //The username (nick) of player #0. Empty string means no player at this side of the table.
    std::string player0Username;
    //The username (nick) of player #1. Empty string means no player at this side of the table.
    std::string player1Username;
    //The username (nick) of player #2. Empty string means no player at this side of the table.
    std::string player2Username;
    //The username (nick) of player #3. Empty string means no player at this side of the table.
    std::string player3Username;
    //Look at ThousandFlags.
    int32_t gameSettings;
    //The same value must be sent by client when acknowledging.
    int8_t acknowledgeSecret;
  };

  static bool deserialize_1_PROPOSED_GAME(const Message&inputMessage,
        Deserialized_1_PROPOSED_GAME&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    //Deserialize player0Username:
    if(!Message::readCString(it,messageEnd,output.player0Username))
      return false;
    //Deserialize player1Username:
    if(!Message::readCString(it,messageEnd,output.player1Username))
      return false;
    //Deserialize player2Username:
    if(!Message::readCString(it,messageEnd,output.player2Username))
      return false;
    //Deserialize player3Username:
    if(!Message::readCString(it,messageEnd,output.player3Username))
      return false;
    //Deserialize gameSettings:
    if(!Message::read3Bytes(it,messageEnd,output.gameSettings))
      return false;
    //Deserialize acknowledgeSecret:
    if(!Message::read1Byte(it,messageEnd,output.acknowledgeSecret))
      return false;
    return true;
  }

  //Message ACKNOWLEDGE_PROPOSED_GAME:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 9.
  //Sent by client. After this, server just waits for GAME_START. If server doesn't get this ack for some reason, will send again PROPOSED_GAME and ack must be repeated.

  static void serialize_1_ACKNOWLEDGE_PROPOSED_GAME(
        //Protection against very old duplicate packets.
        const int8_t secret,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(9);

    //Serialize secret:
    outputMessage.append1Byte(secret);
  }

  class Deserialized_1_ACKNOWLEDGE_PROPOSED_GAME
  {
  public:
    //Protection against very old duplicate packets.
    int8_t secret;
  };

  static bool deserialize_1_ACKNOWLEDGE_PROPOSED_GAME(const Message&inputMessage,
        Deserialized_1_ACKNOWLEDGE_PROPOSED_GAME&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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
    if(messageKind!=9)
      return false;

    //Deserialize pieces:

    //Deserialize secret:
    if(!Message::read1Byte(it,messageEnd,output.secret))
      return false;
    return true;
  }

  //Message GAME_START:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 10.
  //Client clicks the 'start button' to accept a game. Immediately after all clients do this, server will deal cards and start counting time for some player. Server sends no acknowledgement for it. If cards are not dealt after, say, 5s, client can re-send GAME_START. But they might not be dealt because the opponent didn't click GAME_START.

  static void serialize_1_GAME_START(    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(10);

  }

  class Deserialized_1_GAME_START
  {
  public:
  };

  static bool deserialize_1_GAME_START(const Message&inputMessage)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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
    if(messageKind!=10)
      return false;

    //Deserialize pieces:

    return true;
  }

  //Message GAME_DEAL_7_CARDS:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 11.
  //Everybody pressed start, so let's deal the cards. The cards sent are not sorted in any way.

  static void serialize_1_GAME_DEAL_7_CARDS(
        //First card.
        const int8_t card0,

        //Second card.
        const int8_t card1,

        //Third card.
        const int8_t card2,

        //Fourth card.
        const int8_t card3,

        //Fifth card.
        const int8_t card4,

        //Sixth card.
        const int8_t card5,

        //Seventh card.
        const int8_t card6,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(11);

    //Serialize card0:
    outputMessage.append1Byte(card0);
    //Serialize card1:
    outputMessage.append1Byte(card1);
    //Serialize card2:
    outputMessage.append1Byte(card2);
    //Serialize card3:
    outputMessage.append1Byte(card3);
    //Serialize card4:
    outputMessage.append1Byte(card4);
    //Serialize card5:
    outputMessage.append1Byte(card5);
    //Serialize card6:
    outputMessage.append1Byte(card6);
  }

  class Deserialized_1_GAME_DEAL_7_CARDS
  {
  public:
    //First card.
    int8_t card0;
    //Second card.
    int8_t card1;
    //Third card.
    int8_t card2;
    //Fourth card.
    int8_t card3;
    //Fifth card.
    int8_t card4;
    //Sixth card.
    int8_t card5;
    //Seventh card.
    int8_t card6;
  };

  static bool deserialize_1_GAME_DEAL_7_CARDS(const Message&inputMessage,
        Deserialized_1_GAME_DEAL_7_CARDS&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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
    if(messageKind!=11)
      return false;

    //Deserialize pieces:

    //Deserialize card0:
    if(!Message::read1Byte(it,messageEnd,output.card0))
      return false;
    //Deserialize card1:
    if(!Message::read1Byte(it,messageEnd,output.card1))
      return false;
    //Deserialize card2:
    if(!Message::read1Byte(it,messageEnd,output.card2))
      return false;
    //Deserialize card3:
    if(!Message::read1Byte(it,messageEnd,output.card3))
      return false;
    //Deserialize card4:
    if(!Message::read1Byte(it,messageEnd,output.card4))
      return false;
    //Deserialize card5:
    if(!Message::read1Byte(it,messageEnd,output.card5))
      return false;
    //Deserialize card6:
    if(!Message::read1Byte(it,messageEnd,output.card6))
      return false;
    return true;
  }

  //Message GAME_BID:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 12.
  //The message sent by client when placing a bid.

  static void serialize_1_GAME_BID(
        //Which game this is about.
        const int16_t gameIdentifier,

        //This is actually 10% of the bid. Multiply by 10 to get the real value.
        const int8_t bid,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(12);

    //Serialize gameIdentifier:
    outputMessage.append2Bytes(gameIdentifier);
    //Serialize bid:
    outputMessage.append1Byte(bid);
  }

  class Deserialized_1_GAME_BID
  {
  public:
    //Which game this is about.
    int16_t gameIdentifier;
    //This is actually 10% of the bid. Multiply by 10 to get the real value.
    int8_t bid;
  };

  static bool deserialize_1_GAME_BID(const Message&inputMessage,
        Deserialized_1_GAME_BID&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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
    if(messageKind!=12)
      return false;

    //Deserialize pieces:

    //Deserialize gameIdentifier:
    if(!Message::read2Bytes(it,messageEnd,output.gameIdentifier))
      return false;
    //Deserialize bid:
    if(!Message::read1Byte(it,messageEnd,output.bid))
      return false;
    return true;
  }

  //Message GAME_BID_MADE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 13.
  //Message sent by the server to let other players know what bid was placed.

  static void serialize_1_GAME_BID_MADE(
        //Which game this is about.
        const int16_t gameId,

        //This is actually 10% of the bid. Multiply by 10 to get the real value.
        const int8_t bid,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(13);

    //Serialize gameId:
    outputMessage.append2Bytes(gameId);
    //Serialize bid:
    outputMessage.append1Byte(bid);
  }

  class Deserialized_1_GAME_BID_MADE
  {
  public:
    //Which game this is about.
    int16_t gameId;
    //This is actually 10% of the bid. Multiply by 10 to get the real value.
    int8_t bid;
  };

  static bool deserialize_1_GAME_BID_MADE(const Message&inputMessage,
        Deserialized_1_GAME_BID_MADE&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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

    //Deserialize gameId:
    if(!Message::read2Bytes(it,messageEnd,output.gameId))
      return false;
    //Deserialize bid:
    if(!Message::read1Byte(it,messageEnd,output.bid))
      return false;
    return true;
  }

  //Message GAME_BID_END_HIDDEN_MUST:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 14.
  //Message sent by the server to let players know that bidding is over. 'Must' is not shown.

  static void serialize_1_GAME_BID_END_HIDDEN_MUST(
        //Which game this is about.
        const int16_t gameIdenitfier,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(14);

    //Serialize gameIdenitfier:
    outputMessage.append2Bytes(gameIdenitfier);
  }

  class Deserialized_1_GAME_BID_END_HIDDEN_MUST
  {
  public:
    //Which game this is about.
    int16_t gameIdenitfier;
  };

  static bool deserialize_1_GAME_BID_END_HIDDEN_MUST(const Message&inputMessage,
        Deserialized_1_GAME_BID_END_HIDDEN_MUST&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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
    if(messageKind!=14)
      return false;

    //Deserialize pieces:

    //Deserialize gameIdenitfier:
    if(!Message::read2Bytes(it,messageEnd,output.gameIdenitfier))
      return false;
    return true;
  }

  //Message GAME_BID_END_SHOW_MUST:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 15.
  //Message sent by the server to let players know that bidding is over. 'Must' is shown.

  static void serialize_1_GAME_BID_END_SHOW_MUST(
        //Which game this is about.
        const int16_t gameIdenitfier,

        //First must card.
        const int8_t mustCard0,

        //Second must card.
        const int8_t mustCard1,

        //Third must card.
        const int8_t mustCard2,
    Message&outputMessage)
    throw()
  {
    outputMessage.resize(0);
    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(15);

    //Serialize gameIdenitfier:
    outputMessage.append2Bytes(gameIdenitfier);
    //Serialize mustCard0:
    outputMessage.append1Byte(mustCard0);
    //Serialize mustCard1:
    outputMessage.append1Byte(mustCard1);
    //Serialize mustCard2:
    outputMessage.append1Byte(mustCard2);
  }

  class Deserialized_1_GAME_BID_END_SHOW_MUST
  {
  public:
    //Which game this is about.
    int16_t gameIdenitfier;
    //First must card.
    int8_t mustCard0;
    //Second must card.
    int8_t mustCard1;
    //Third must card.
    int8_t mustCard2;
  };

  static bool deserialize_1_GAME_BID_END_SHOW_MUST(const Message&inputMessage,
        Deserialized_1_GAME_BID_END_SHOW_MUST&output)
  throw()
  {
    Message::const_iterator it
     = inputMessage.begin();
    const Message::const_iterator messageEnd
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
    if(messageKind!=15)
      return false;

    //Deserialize pieces:

    //Deserialize gameIdenitfier:
    if(!Message::read2Bytes(it,messageEnd,output.gameIdenitfier))
      return false;
    //Deserialize mustCard0:
    if(!Message::read1Byte(it,messageEnd,output.mustCard0))
      return false;
    //Deserialize mustCard1:
    if(!Message::read1Byte(it,messageEnd,output.mustCard1))
      return false;
    //Deserialize mustCard2:
    if(!Message::read1Byte(it,messageEnd,output.mustCard2))
      return false;
    return true;
  }

};
