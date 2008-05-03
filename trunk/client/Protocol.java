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


import java.util.*;

public class ThousandProtocol{

  //Can be used for switching to detect
  //which deserializer should be used.
  enum MessageType{
    UNKNOWN_MESSAGE_1,
    //Sent by client to log in. Loginng in associates the sending IP+port as belonging to the particular user until logged out. Can be used to create an account. Valid nick is between 5 and 20 letters, consisting of any printable non-whitespace ASCII characters. Password should be at least 6 characters. Easy passwords won't be admitted.
    LOG_IN_1,
    //Sent by server to let the user know that log in was OK.
    LOG_IN_CORRECT_1,
    //Sent by server to let the user know that log in wasn't OK.
    LOG_IN_INCORRECT_1,
    //Message sent by client to request 'room' statistics.
    GET_STATISTICS_1,
    //Sent by server when returning info about 'room' stats.
    RETURN_STATISTICS_1,
    //Sent by client when searching for some game.
    SEARCH_GAME_1,
    //Sent by server when no game with given search cryteria is currently available.
    AWAIT_GAME_1,
    //Sent by server when game is matched. Includes game settings. One of four usernames will be equal to the requesting player's if she should play the game, otherwise it's observing. Must be acknowledged by the client program immediately, using the secret provided here.
    PROPOSED_GAME_1,
    //Sent by client. After this, server just waits for GAME_START. If server doesn't get this ack for some reason, will send again PROPOSED_GAME and ack must be repeated.
    ACKNOWLEDGE_1,
    //Client clicks the 'start button' to accept a game. Immediately after all clients do this, server will deal cards and start counting time for some player. Server sends no acknowledgement for it. If cards are not dealt after, say, 5s, client can re-send GAME_START. But they might not be dealt because the opponent didn't click GAME_START.
    GAME_START_1,
    //Everybody pressed start, so let's deal the cards. The cards sent are not sorted in any way.
    GAME_DEAL_7_CARDS_1,
    //The message sent by client when placing a bid.
    GAME_BID_1,
    //Message sent by the server to let other players know what bid was placed.
    GAME_BID_MADE_1,
    //Message sent by the server to let players know that bidding is over. 'Must' is not shown.
    GAME_BID_END_HIDDEN_MUST_1,
    //Message sent by the server to let players know that bidding is over. 'Must' is shown.
    GAME_BID_END_SHOW_MUST_1
  }


  //Used in card games. You can encode a card on 
  //one byte by bitwise disjunction of value and
  //color. Use VALUE_MASK and COLOR_MASK to decode.
  final static byte ACE = (byte) 0x01;
  final static byte TWO = (byte) 0x02;
  final static byte THREE = (byte) 0x03;
  final static byte FOUR = (byte) 0x04;
  final static byte FIVE = (byte) 0x05;
  final static byte SIX = (byte) 0x06;
  final static byte SEVEN = (byte) 0x07;
  final static byte EIGHT = (byte) 0x08;
  final static byte NINE = (byte) 0x09;
  final static byte TEN = (byte) 0x0A;
  final static byte JACK = (byte) 0x0B;
  final static byte QUEEN = (byte) 0x0C;
  final static byte KING = (byte) 0x0D;
  final static byte JOKER = (byte) 0x0E;
  final static byte VALUE_MASK = (byte) 0x0F;
  final static byte HEARTS = (byte) 0x10;
  final static byte DIAMONDS = (byte) 0x20;
  final static byte CLUBS = (byte) 0x30;
  final static byte SPADES = (byte) 0x40;
  final static byte COLOR_MASK = (byte) 0xF0;

  //Flags 'ThousandFlags':
  //Various booleans used in search for saying what games user is interested in. true - interested, false - not interested. Also used for game settings.
    //2-players game.
  public static final int TWO_PLAYERS = 1;
    //3-players game.
  public static final int THREE_PLAYERS = 2;
    //4-players game.
  public static final int FOUR_PLAYERS = 4;
    //Game without bombs.
  public static final int NO_BOMBS = 8;
    //Game with bombs.
  public static final int BOMBS = 16;
    //Game with unlimited bombs.
  public static final int UNLIMITED_BOMBS = 32;
    //Game with 10 bid increment.
  public static final int BID_INCREMENT_10 = 64;
    //Game with any bid increment.
  public static final int BID_INCREMENT_ANY = 128;
    //Game where 'must' is always shown.
  public static final int SHOW_MUST_100 = 256;
    //Game where 'must' is shown from 110.
  public static final int SHOW_MUST_110 = 512;
    //Public game.
  public static final int PUBLIC_GAME = 1024;
    //Private game.
  public static final int PRIVATE_GAME = 2048;
    //Ranking game.
  public static final int RANKING_GAME = 4096;
    //Sparring (non-ranked) game.
  public static final int SPARRING_GAME = 8192;
    //7 minutes game.
  public static final int TIME_7 = 16384;
    //10 minutes game.
  public static final int TIME_10 = 32768;
    //15 minutes game.
  public static final int TIME_15 = 65536;
    //20 minutes game.
  public static final int TIME_20 = 131072;
    //30 minutes game.
  public static final int TIME_30 = 262144;


  //This method can be used for rapid message
  //type lookup, so you don't need to try
  //deserializing using all deserializers.
  //Remember that deserialization can still
  //always fail, even if this method returns
  //some known type. It doesn't read the whole
  //message, just the part where message type
  //is present.
  public static MessageType lookupMessageType(final Message message){
    try{
        final Iterator<Byte> iterator
         = message.iterator();
        
        Message.read1Byte(iterator);
        
        final byte byteMessageType
         = Message.read1Byte(iterator);
        
        switch(byteMessageType){
        case 1: return MessageType.LOG_IN_1;
        case 2: return MessageType.LOG_IN_CORRECT_1;
        case 3: return MessageType.LOG_IN_INCORRECT_1;
        case 4: return MessageType.GET_STATISTICS_1;
        case 5: return MessageType.RETURN_STATISTICS_1;
        case 6: return MessageType.SEARCH_GAME_1;
        case 7: return MessageType.AWAIT_GAME_1;
        case 8: return MessageType.PROPOSED_GAME_1;
        case 9: return MessageType.ACKNOWLEDGE_1;
        case 10: return MessageType.GAME_START_1;
        case 11: return MessageType.GAME_DEAL_7_CARDS_1;
        case 12: return MessageType.GAME_BID_1;
        case 13: return MessageType.GAME_BID_MADE_1;
        case 14: return MessageType.GAME_BID_END_HIDDEN_MUST_1;
        case 15: return MessageType.GAME_BID_END_SHOW_MUST_1;
        default: return MessageType.UNKNOWN_MESSAGE_1;
        }
    }catch(Exception e){
        return MessageType.UNKNOWN_MESSAGE_1;
    }
  }

  //Message LOG_IN:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 1.
  //Sent by client to log in. Loginng in associates the sending IP+port as belonging to the particular user until logged out. Can be used to create an account. Valid nick is between 5 and 20 letters, consisting of any printable non-whitespace ASCII characters. Password should be at least 6 characters. Easy passwords won't be admitted.

  public static Message serialize_1_LOG_IN(
        //Unique nick.
        final String nick,
        //User's password.
        final String password){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(1);

    //Serialize nick:
    outputMessage.appendCString(nick);
    //Serialize password:
    outputMessage.appendCString(password);
    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_LOG_IN{
    //Unique nick.
    public final String nick;
    //User's password.
    public final String password;
    public Deserialized_1_LOG_IN(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

    //Deserialize nick:
      this.nick = Message.readCString(iterator);
    //Deserialize password:
      this.password = Message.readCString(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message LOG_IN_CORRECT:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 2.
  //Sent by server to let the user know that log in was OK.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_LOG_IN_CORRECT(){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(2);

    return outputMessage;
  }

  */

  public static class Deserialized_1_LOG_IN_CORRECT{
    public Deserialized_1_LOG_IN_CORRECT(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=2)
          throw new MessageDeserializationException();

      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message LOG_IN_INCORRECT:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 3.
  //Sent by server to let the user know that log in wasn't OK.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_LOG_IN_INCORRECT(
        //Why login failed.
        final String reason){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(3);

    //Serialize reason:
    outputMessage.appendCString(reason);
    return outputMessage;
  }

  */

  public static class Deserialized_1_LOG_IN_INCORRECT{
    //Why login failed.
    public final String reason;
    public Deserialized_1_LOG_IN_INCORRECT(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=3)
          throw new MessageDeserializationException();

    //Deserialize reason:
      this.reason = Message.readCString(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message GET_STATISTICS:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 4.
  //Message sent by client to request 'room' statistics.

  public static Message serialize_1_GET_STATISTICS(){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(4);

    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_GET_STATISTICS{
    public Deserialized_1_GET_STATISTICS(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=4)
          throw new MessageDeserializationException();

      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message RETURN_STATISTICS:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 5.
  //Sent by server when returning info about 'room' stats.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_RETURN_STATISTICS(
        //Number of people logged in.
        final int numberOfUsers,
        //Number of games being played in the 'room'.
        final int numberOfGames,
        //Number of people searching in the 'room'.
        final int numberOfSearchers){
    final Message outputMessage
     = new Message();

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
    return outputMessage;
  }

  */

  public static class Deserialized_1_RETURN_STATISTICS{
    //Number of people logged in.
    public final int numberOfUsers;
    //Number of games being played in the 'room'.
    public final int numberOfGames;
    //Number of people searching in the 'room'.
    public final int numberOfSearchers;
    public Deserialized_1_RETURN_STATISTICS(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=5)
          throw new MessageDeserializationException();

    //Deserialize numberOfUsers:
      this.numberOfUsers = Message.read4Bytes(iterator);
    //Deserialize numberOfGames:
      this.numberOfGames = Message.read4Bytes(iterator);
    //Deserialize numberOfSearchers:
      this.numberOfSearchers = Message.read4Bytes(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message SEARCH_GAME:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 6.
  //Sent by client when searching for some game.

  public static Message serialize_1_SEARCH_GAME(
        //Minimum ranking of the oponent, measured in 50 points. E.g. 60 means 60*50=3000 points.
        final byte minimumOponentRanking50,
        //Have a look at ThousandFlags.
        final int searchFlags){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(6);

    //Serialize minimumOponentRanking50:
    outputMessage.append1Byte(minimumOponentRanking50);
    //Serialize searchFlags:
    outputMessage.append3Bytes(searchFlags);
    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_SEARCH_GAME{
    //Minimum ranking of the oponent, measured in 50 points. E.g. 60 means 60*50=3000 points.
    public final byte minimumOponentRanking50;
    //Have a look at ThousandFlags.
    public final int searchFlags;
    public Deserialized_1_SEARCH_GAME(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=6)
          throw new MessageDeserializationException();

    //Deserialize minimumOponentRanking50:
      this.minimumOponentRanking50 = Message.read1Byte(iterator);
    //Deserialize searchFlags:
      this.searchFlags = Message.read3Bytes(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message AWAIT_GAME:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 7.
  //Sent by server when no game with given search cryteria is currently available.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_AWAIT_GAME(){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(7);

    return outputMessage;
  }

  */

  public static class Deserialized_1_AWAIT_GAME{
    public Deserialized_1_AWAIT_GAME(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=7)
          throw new MessageDeserializationException();

      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message PROPOSED_GAME:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 8.
  //Sent by server when game is matched. Includes game settings. One of four usernames will be equal to the requesting player's if she should play the game, otherwise it's observing. Must be acknowledged by the client program immediately, using the secret provided here.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_PROPOSED_GAME(
        //The username (nick) of player #0. Empty string means no player at this side of the table.
        final String player0Username,
        //The username (nick) of player #1. Empty string means no player at this side of the table.
        final String player1Username,
        //The username (nick) of player #2. Empty string means no player at this side of the table.
        final String player2Username,
        //The username (nick) of player #3. Empty string means no player at this side of the table.
        final String player3Username,
        //Look at ThousandFlags.
        final int gameSettings,
        //The same value must be sent by client when acknowledging.
        final byte acknowledgeSecret){
    final Message outputMessage
     = new Message();

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
    return outputMessage;
  }

  */

  public static class Deserialized_1_PROPOSED_GAME{
    //The username (nick) of player #0. Empty string means no player at this side of the table.
    public final String player0Username;
    //The username (nick) of player #1. Empty string means no player at this side of the table.
    public final String player1Username;
    //The username (nick) of player #2. Empty string means no player at this side of the table.
    public final String player2Username;
    //The username (nick) of player #3. Empty string means no player at this side of the table.
    public final String player3Username;
    //Look at ThousandFlags.
    public final int gameSettings;
    //The same value must be sent by client when acknowledging.
    public final byte acknowledgeSecret;
    public Deserialized_1_PROPOSED_GAME(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=8)
          throw new MessageDeserializationException();

    //Deserialize player0Username:
      this.player0Username = Message.readCString(iterator);
    //Deserialize player1Username:
      this.player1Username = Message.readCString(iterator);
    //Deserialize player2Username:
      this.player2Username = Message.readCString(iterator);
    //Deserialize player3Username:
      this.player3Username = Message.readCString(iterator);
    //Deserialize gameSettings:
      this.gameSettings = Message.read3Bytes(iterator);
    //Deserialize acknowledgeSecret:
      this.acknowledgeSecret = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message ACKNOWLEDGE:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 9.
  //Sent by client. After this, server just waits for GAME_START. If server doesn't get this ack for some reason, will send again PROPOSED_GAME and ack must be repeated.

  public static Message serialize_1_ACKNOWLEDGE(
        //Protection against very old duplicate packets.
        final byte secret){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(9);

    //Serialize secret:
    outputMessage.append1Byte(secret);
    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_ACKNOWLEDGE{
    //Protection against very old duplicate packets.
    public final byte secret;
    public Deserialized_1_ACKNOWLEDGE(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=9)
          throw new MessageDeserializationException();

    //Deserialize secret:
      this.secret = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message GAME_START:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 10.
  //Client clicks the 'start button' to accept a game. Immediately after all clients do this, server will deal cards and start counting time for some player. Server sends no acknowledgement for it. If cards are not dealt after, say, 5s, client can re-send GAME_START. But they might not be dealt because the opponent didn't click GAME_START.

  public static Message serialize_1_GAME_START(){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(10);

    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_GAME_START{
    public Deserialized_1_GAME_START(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=10)
          throw new MessageDeserializationException();

      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message GAME_DEAL_7_CARDS:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 11.
  //Everybody pressed start, so let's deal the cards. The cards sent are not sorted in any way.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_GAME_DEAL_7_CARDS(
        //First card.
        final byte card0,
        //Second card.
        final byte card1,
        //Third card.
        final byte card2,
        //Fourth card.
        final byte card3,
        //Fifth card.
        final byte card4,
        //Sixth card.
        final byte card5,
        //Seventh card.
        final byte card6){
    final Message outputMessage
     = new Message();

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
    return outputMessage;
  }

  */

  public static class Deserialized_1_GAME_DEAL_7_CARDS{
    //First card.
    public final byte card0;
    //Second card.
    public final byte card1;
    //Third card.
    public final byte card2;
    //Fourth card.
    public final byte card3;
    //Fifth card.
    public final byte card4;
    //Sixth card.
    public final byte card5;
    //Seventh card.
    public final byte card6;
    public Deserialized_1_GAME_DEAL_7_CARDS(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=11)
          throw new MessageDeserializationException();

    //Deserialize card0:
      this.card0 = Message.read1Byte(iterator);
    //Deserialize card1:
      this.card1 = Message.read1Byte(iterator);
    //Deserialize card2:
      this.card2 = Message.read1Byte(iterator);
    //Deserialize card3:
      this.card3 = Message.read1Byte(iterator);
    //Deserialize card4:
      this.card4 = Message.read1Byte(iterator);
    //Deserialize card5:
      this.card5 = Message.read1Byte(iterator);
    //Deserialize card6:
      this.card6 = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message GAME_BID:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 12.
  //The message sent by client when placing a bid.

  public static Message serialize_1_GAME_BID(
        //Which game this is about.
        final short gameIdentifier,
        //This is actually 10% of the bid. Multiply by 10 to get the real value.
        final byte bid){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(12);

    //Serialize gameIdentifier:
    outputMessage.append2Bytes(gameIdentifier);
    //Serialize bid:
    outputMessage.append1Byte(bid);
    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_GAME_BID{
    //Which game this is about.
    public final short gameIdentifier;
    //This is actually 10% of the bid. Multiply by 10 to get the real value.
    public final byte bid;
    public Deserialized_1_GAME_BID(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=12)
          throw new MessageDeserializationException();

    //Deserialize gameIdentifier:
      this.gameIdentifier = Message.read2Bytes(iterator);
    //Deserialize bid:
      this.bid = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message GAME_BID_MADE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 13.
  //Message sent by the server to let other players know what bid was placed.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_GAME_BID_MADE(
        //Which game this is about.
        final short gameId,
        //This is actually 10% of the bid. Multiply by 10 to get the real value.
        final byte bid){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(13);

    //Serialize gameId:
    outputMessage.append2Bytes(gameId);
    //Serialize bid:
    outputMessage.append1Byte(bid);
    return outputMessage;
  }

  */

  public static class Deserialized_1_GAME_BID_MADE{
    //Which game this is about.
    public final short gameId;
    //This is actually 10% of the bid. Multiply by 10 to get the real value.
    public final byte bid;
    public Deserialized_1_GAME_BID_MADE(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=13)
          throw new MessageDeserializationException();

    //Deserialize gameId:
      this.gameId = Message.read2Bytes(iterator);
    //Deserialize bid:
      this.bid = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message GAME_BID_END_HIDDEN_MUST:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 14.
  //Message sent by the server to let players know that bidding is over. 'Must' is not shown.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_GAME_BID_END_HIDDEN_MUST(
        //Which game this is about.
        final short gameIdenitfier){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(14);

    //Serialize gameIdenitfier:
    outputMessage.append2Bytes(gameIdenitfier);
    return outputMessage;
  }

  */

  public static class Deserialized_1_GAME_BID_END_HIDDEN_MUST{
    //Which game this is about.
    public final short gameIdenitfier;
    public Deserialized_1_GAME_BID_END_HIDDEN_MUST(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=14)
          throw new MessageDeserializationException();

    //Deserialize gameIdenitfier:
      this.gameIdenitfier = Message.read2Bytes(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message GAME_BID_END_SHOW_MUST:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 15.
  //Message sent by the server to let players know that bidding is over. 'Must' is shown.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_GAME_BID_END_SHOW_MUST(
        //Which game this is about.
        final short gameIdenitfier,
        //First must card.
        final byte mustCard0,
        //Second must card.
        final byte mustCard1,
        //Third must card.
        final byte mustCard2){
    final Message outputMessage
     = new Message();

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
    return outputMessage;
  }

  */

  public static class Deserialized_1_GAME_BID_END_SHOW_MUST{
    //Which game this is about.
    public final short gameIdenitfier;
    //First must card.
    public final byte mustCard0;
    //Second must card.
    public final byte mustCard1;
    //Third must card.
    public final byte mustCard2;
    public Deserialized_1_GAME_BID_END_SHOW_MUST(final Message inputMessage)
      throws MessageDeserializationException{
      try{
        final Iterator<Byte> iterator
         = inputMessage.iterator();

        //Check protocol version:
        if(iterator.next()!=1)
          throw new MessageDeserializationException();

        //Check kind of message:
        if(iterator.next()!=15)
          throw new MessageDeserializationException();

    //Deserialize gameIdenitfier:
      this.gameIdenitfier = Message.read2Bytes(iterator);
    //Deserialize mustCard0:
      this.mustCard0 = Message.read1Byte(iterator);
    //Deserialize mustCard1:
      this.mustCard1 = Message.read1Byte(iterator);
    //Deserialize mustCard2:
      this.mustCard2 = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

}
