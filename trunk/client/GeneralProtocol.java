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

public class GeneralProtocol{

  //Can be used for switching to detect
  //which deserializer should be used.
  enum MessageType{
    UNKNOWN_MESSAGE_1,
    //Sent by client when creating a new Tic Tac Toe table.
    CREATE_TICTACTOE_TABLE_1,
    //Sent by client when creating a new Thousand table.
    CREATE_THOUSAND_TABLE_1,
    //Sent by server after table has been created.
    TABLE_CREATED_1,
    //Sent by client when saying something (chat message).
    SAY_1,
    //Sent by server when someone says something (chat message).
    SAID_1,
    //Sent by client when joining some table to play.
    JOIN_TABLE_TO_PLAY_1,
    //Sent by server to new player who joined a table.
    YOU_JOINED_TABLE_1,
    //Sent by server to new player who joined a table.
    JOINING_TABLE_FAILED_INCORRECT_TABLE_ID_1,
    //Sent by server after new player joined a table to already present people.
    NEW_PLAYER_JOINED_TABLE_1,
    //Sent by server when a player left.
    PLAYER_LEFT_TABLE_1,
    //Sent by server when game is started and no initial message is designated for the receiver.
    GAME_STARTED_WITHOUT_INITIAL_MESSAGE_1,
    //Sent by server when game is started and an initial message is designated for the receiver.
    GAME_STARTED_WITH_INITIAL_MESSAGE_1,
    //Sent by client when making a move.
    MAKE_MOVE_1,
    //Sent by server after client made a move.
    MOVE_MADE_1
  }


  //Constants:


  //This method can be used for rapid message
  //type lookup, so you don't need to try
  //deserializing using all deserializers.
  //Remember that deserialization can still
  //always fail, even if this method returns
  //some known type. It doesn't read the whole
  //message, just the part where message type
  //is present.
  public static MessageType lookupMessageType(final Vector<Byte> message){
    try{
        final Iterator<Byte> iterator
         = message.iterator();
        
        Message.read1Byte(iterator);
        
        final byte byteMessageType
         = Message.read1Byte(iterator);
        
        switch(byteMessageType){
        case 1: return MessageType.CREATE_TICTACTOE_TABLE_1;
        case 2: return MessageType.CREATE_THOUSAND_TABLE_1;
        case 3: return MessageType.TABLE_CREATED_1;
        case 4: return MessageType.SAY_1;
        case 5: return MessageType.SAID_1;
        case 6: return MessageType.JOIN_TABLE_TO_PLAY_1;
        case 7: return MessageType.YOU_JOINED_TABLE_1;
        case 8: return MessageType.JOINING_TABLE_FAILED_INCORRECT_TABLE_ID_1;
        case 9: return MessageType.NEW_PLAYER_JOINED_TABLE_1;
        case 10: return MessageType.PLAYER_LEFT_TABLE_1;
        case 11: return MessageType.GAME_STARTED_WITHOUT_INITIAL_MESSAGE_1;
        case 12: return MessageType.GAME_STARTED_WITH_INITIAL_MESSAGE_1;
        case 13: return MessageType.MAKE_MOVE_1;
        case 14: return MessageType.MOVE_MADE_1;
        default: return MessageType.UNKNOWN_MESSAGE_1;
        }
    }catch(Exception e){
        return MessageType.UNKNOWN_MESSAGE_1;
    }
  }

  //Message CREATE_TICTACTOE_TABLE:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 1.
  //Sent by client when creating a new Tic Tac Toe table.

  public static Vector<Byte> serialize_1_CREATE_TICTACTOE_TABLE(){
    final Vector<Byte> outputMessage
     = new Vector<Byte>();

    //Let the receiver know which protocol version this is:
    Message.append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message.append1Byte(1,outputMessage);

    return outputMessage;
  }

  //Message CREATE_THOUSAND_TABLE:

  //This message will create: [CPP_SERIALIZER, CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 2.
  //Sent by client when creating a new Thousand table.

  public static Vector<Byte> serialize_1_CREATE_THOUSAND_TABLE(){
    final Vector<Byte> outputMessage
     = new Vector<Byte>();

    //Let the receiver know which protocol version this is:
    Message.append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message.append1Byte(2,outputMessage);

    return outputMessage;
  }

  //Message TABLE_CREATED:

  //This message will create: [CPP_SERIALIZER, CPP_DESERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 3.
  //Sent by server after table has been created.

  public static class Deserialized_1_TABLE_CREATED{
    //ID for newly created table.
    public final long id;
    public Deserialized_1_TABLE_CREATED(final Vector<Byte> inputMessage)
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

    //Deserialize id:
      this.id = Message.read8Bytes(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message SAY:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 4.
  //Sent by client when saying something (chat message).

  public static Vector<Byte> serialize_1_SAY(
        //Text of the chat message in UTF8 encoding.
        final java.util.Vector<Byte> text_UTF8){
    final Vector<Byte> outputMessage
     = new Vector<Byte>();

    //Let the receiver know which protocol version this is:
    Message.append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message.append1Byte(4,outputMessage);

    //Serialize text_UTF8:
    Message.appendBinary(text_UTF8,outputMessage);
    return outputMessage;
  }

  //Message SAID:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 5.
  //Sent by server when someone says something (chat message).

  public static class Deserialized_1_SAID{
    //Who said it.
    public final byte tablePlayerId;
    //Text of the chat message in UTF8 encoding.
    public final java.util.Vector<Byte> text_UTF8;
    public Deserialized_1_SAID(final Vector<Byte> inputMessage)
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

    //Deserialize tablePlayerId:
      this.tablePlayerId = Message.read1Byte(iterator);
    //Deserialize text_UTF8:
      this.text_UTF8 = Message.readBinary(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message JOIN_TABLE_TO_PLAY:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 6.
  //Sent by client when joining some table to play.

  public static Vector<Byte> serialize_1_JOIN_TABLE_TO_PLAY(
        //Table id.
        final long tableId,
        //Name of the player (not unique).
        final String screenName){
    final Vector<Byte> outputMessage
     = new Vector<Byte>();

    //Let the receiver know which protocol version this is:
    Message.append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message.append1Byte(6,outputMessage);

    //Serialize tableId:
    Message.append8Bytes(tableId,outputMessage);
    //Serialize screenName:
    Message.appendCString(screenName,outputMessage);
    return outputMessage;
  }

  //Message YOU_JOINED_TABLE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 7.
  //Sent by server to new player who joined a table.

  public static class Deserialized_1_YOU_JOINED_TABLE{
    //New player's table player id.
    public final byte tablePlayerId;
    public Deserialized_1_YOU_JOINED_TABLE(final Vector<Byte> inputMessage)
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

    //Deserialize tablePlayerId:
      this.tablePlayerId = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message JOINING_TABLE_FAILED_INCORRECT_TABLE_ID:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 8.
  //Sent by server to new player who joined a table.

  public static class Deserialized_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID{
    public Deserialized_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID(final Vector<Byte> inputMessage)
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

      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message NEW_PLAYER_JOINED_TABLE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 9.
  //Sent by server after new player joined a table to already present people.

  public static class Deserialized_1_NEW_PLAYER_JOINED_TABLE{
    //New player's name.
    public final String screenName;
    //New player's table player id.
    public final byte tablePlayerId;
    public Deserialized_1_NEW_PLAYER_JOINED_TABLE(final Vector<Byte> inputMessage)
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

    //Deserialize screenName:
      this.screenName = Message.readCString(iterator);
    //Deserialize tablePlayerId:
      this.tablePlayerId = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message PLAYER_LEFT_TABLE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 10.
  //Sent by server when a player left.

  public static class Deserialized_1_PLAYER_LEFT_TABLE{
    //Leaving player's table player id.
    public final byte tablePlayerId;
    public Deserialized_1_PLAYER_LEFT_TABLE(final Vector<Byte> inputMessage)
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

    //Deserialize tablePlayerId:
      this.tablePlayerId = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message GAME_STARTED_WITHOUT_INITIAL_MESSAGE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 11.
  //Sent by server when game is started and no initial message is designated for the receiver.

  public static class Deserialized_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE{
    //Specifies how many players will play the just-started game, which tablePlayerIdeach of them has, and what's their order.
    public final java.util.Vector<Byte> turnGamePlayerToTablePlayerId;
    public Deserialized_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE(final Vector<Byte> inputMessage)
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

    //Deserialize turnGamePlayerToTablePlayerId:
      this.turnGamePlayerToTablePlayerId = Message.readVector(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message GAME_STARTED_WITH_INITIAL_MESSAGE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 12.
  //Sent by server when game is started and an initial message is designated for the receiver.

  public static class Deserialized_1_GAME_STARTED_WITH_INITIAL_MESSAGE{
    //Specifies how many players will play the just-started game, which tablePlayerIdeach of them has, and what's their order.
    public final java.util.Vector<Byte> turnGamePlayerToTablePlayerId;
    //Game-specific initial information.
    public final java.util.Vector<Byte> initialMessage;
    public Deserialized_1_GAME_STARTED_WITH_INITIAL_MESSAGE(final Vector<Byte> inputMessage)
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

    //Deserialize turnGamePlayerToTablePlayerId:
      this.turnGamePlayerToTablePlayerId = Message.readVector(iterator);
    //Deserialize initialMessage:
      this.initialMessage = Message.readBinary(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message MAKE_MOVE:

  //This message will create: [CPP_DESERIALIZER, JAVA_SERIALIZER].

  //In protocol version 1 this message has id 13.
  //Sent by client when making a move.

  public static Vector<Byte> serialize_1_MAKE_MOVE(
        //Game-specific move information.
        final java.util.Vector<Byte> gameMove){
    final Vector<Byte> outputMessage
     = new Vector<Byte>();

    //Let the receiver know which protocol version this is:
    Message.append1Byte(1,outputMessage);
    //Let the receiver know what kind of message this is:
    Message.append1Byte(13,outputMessage);

    //Serialize gameMove:
    Message.appendBinary(gameMove,outputMessage);
    return outputMessage;
  }

  //Message MOVE_MADE:

  //This message will create: [CPP_SERIALIZER, JAVA_DESERIALIZER].

  //In protocol version 1 this message has id 14.
  //Sent by server after client made a move.

  public static class Deserialized_1_MOVE_MADE{
    //Game-specific move information.
    public final java.util.Vector<Byte> gameMove;
    public Deserialized_1_MOVE_MADE(final Vector<Byte> inputMessage)
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

    //Deserialize gameMove:
      this.gameMove = Message.readBinary(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }


  public static boolean handle(final Vector<Byte> message,
                               final GeneralHandler handler){

    switch(GeneralProtocol.lookupMessageType(message))
    {
    case TABLE_CREATED_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_TABLE_CREATED deserialized = 
              new GeneralProtocol.Deserialized_1_TABLE_CREATED(message);
        return handler.handle_1_TABLE_CREATED(deserialized.id);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case SAID_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_SAID deserialized = 
              new GeneralProtocol.Deserialized_1_SAID(message);
        return handler.handle_1_SAID(deserialized.tablePlayerId,
                      deserialized.text_UTF8);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case YOU_JOINED_TABLE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_YOU_JOINED_TABLE deserialized = 
              new GeneralProtocol.Deserialized_1_YOU_JOINED_TABLE(message);
        return handler.handle_1_YOU_JOINED_TABLE(deserialized.tablePlayerId);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case JOINING_TABLE_FAILED_INCORRECT_TABLE_ID_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID deserialized = 
              new GeneralProtocol.Deserialized_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID(message);
        return handler.handle_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID();
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case NEW_PLAYER_JOINED_TABLE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_NEW_PLAYER_JOINED_TABLE deserialized = 
              new GeneralProtocol.Deserialized_1_NEW_PLAYER_JOINED_TABLE(message);
        return handler.handle_1_NEW_PLAYER_JOINED_TABLE(deserialized.screenName,
                      deserialized.tablePlayerId);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case PLAYER_LEFT_TABLE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_PLAYER_LEFT_TABLE deserialized = 
              new GeneralProtocol.Deserialized_1_PLAYER_LEFT_TABLE(message);
        return handler.handle_1_PLAYER_LEFT_TABLE(deserialized.tablePlayerId);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case GAME_STARTED_WITHOUT_INITIAL_MESSAGE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE deserialized = 
              new GeneralProtocol.Deserialized_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE(message);
        return handler.handle_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE(deserialized.turnGamePlayerToTablePlayerId);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case GAME_STARTED_WITH_INITIAL_MESSAGE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_GAME_STARTED_WITH_INITIAL_MESSAGE deserialized = 
              new GeneralProtocol.Deserialized_1_GAME_STARTED_WITH_INITIAL_MESSAGE(message);
        return handler.handle_1_GAME_STARTED_WITH_INITIAL_MESSAGE(deserialized.turnGamePlayerToTablePlayerId,
                      deserialized.initialMessage);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case MOVE_MADE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_MOVE_MADE deserialized = 
              new GeneralProtocol.Deserialized_1_MOVE_MADE(message);
        return handler.handle_1_MOVE_MADE(deserialized.gameMove);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    default:
      return false;
    }
  }

public static interface GeneralHandler
{

  //Handlers for various message types:
  public abstract boolean handle_1_TABLE_CREATED(final long id);
  public abstract boolean handle_1_SAID(final byte tablePlayerId,
                  final java.util.Vector<Byte> text_UTF8);
  public abstract boolean handle_1_YOU_JOINED_TABLE(final byte tablePlayerId);
  public abstract boolean handle_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID();
  public abstract boolean handle_1_NEW_PLAYER_JOINED_TABLE(final String screenName,
                  final byte tablePlayerId);
  public abstract boolean handle_1_PLAYER_LEFT_TABLE(final byte tablePlayerId);
  public abstract boolean handle_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE(final java.util.Vector<Byte> turnGamePlayerToTablePlayerId);
  public abstract boolean handle_1_GAME_STARTED_WITH_INITIAL_MESSAGE(final java.util.Vector<Byte> turnGamePlayerToTablePlayerId,
                  final java.util.Vector<Byte> initialMessage);
  public abstract boolean handle_1_MOVE_MADE(final java.util.Vector<Byte> gameMove);
}
}
