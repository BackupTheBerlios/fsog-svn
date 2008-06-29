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
    //Sent by server after table has been created.
    TABLE_CREATED_1,
    //Sent by client when joining some table to play.
    JOIN_TABLE_TO_PLAY_1,
    //Sent by server to new player who joined a table.
    YOU_JOINED_TABLE_1,
    //Sent by server after new player joined a table to already present people.
    NEW_PLAYER_JOINED_TABLE_1,
    //Sent by server when game is started. Some initialization message can be sent within. Move from first player(s) is awaited after that.
    GAME_STARTED_AND_INITIAL_MESSAGE_1,
    //Sent by client when making a move.
    MAKE_MOVE_1,
    //Sent by server after client made a move.
    MOVE_MADE_1
  }


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
        case 1: return MessageType.CREATE_TICTACTOE_TABLE_1;
        case 2: return MessageType.TABLE_CREATED_1;
        case 3: return MessageType.JOIN_TABLE_TO_PLAY_1;
        case 4: return MessageType.YOU_JOINED_TABLE_1;
        case 5: return MessageType.NEW_PLAYER_JOINED_TABLE_1;
        case 6: return MessageType.GAME_STARTED_AND_INITIAL_MESSAGE_1;
        case 7: return MessageType.MAKE_MOVE_1;
        case 8: return MessageType.MOVE_MADE_1;
        default: return MessageType.UNKNOWN_MESSAGE_1;
        }
    }catch(Exception e){
        return MessageType.UNKNOWN_MESSAGE_1;
    }
  }

  //Message CREATE_TICTACTOE_TABLE:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 1.
  //Sent by client when creating a new Tic Tac Toe table.

  public static Message serialize_1_CREATE_TICTACTOE_TABLE(){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(1);

    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_CREATE_TICTACTOE_TABLE{
    public Deserialized_1_CREATE_TICTACTOE_TABLE(final Message inputMessage)
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

      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message TABLE_CREATED:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 2.
  //Sent by server after table has been created.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_TABLE_CREATED(
        //ID for newly created table.
        final long id){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(2);

    //Serialize id:
    outputMessage.append8Bytes(id);
    return outputMessage;
  }

  */

  public static class Deserialized_1_TABLE_CREATED{
    //ID for newly created table.
    public final long id;
    public Deserialized_1_TABLE_CREATED(final Message inputMessage)
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

    //Deserialize id:
      this.id = Message.read8Bytes(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message JOIN_TABLE_TO_PLAY:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 3.
  //Sent by client when joining some table to play.

  public static Message serialize_1_JOIN_TABLE_TO_PLAY(
        //Table id.
        final long tableId,
        //Name of the player (not unique).
        final String screenName){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(3);

    //Serialize tableId:
    outputMessage.append8Bytes(tableId);
    //Serialize screenName:
    outputMessage.appendCString(screenName);
    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_JOIN_TABLE_TO_PLAY{
    //Table id.
    public final long tableId;
    //Name of the player (not unique).
    public final String screenName;
    public Deserialized_1_JOIN_TABLE_TO_PLAY(final Message inputMessage)
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

    //Deserialize tableId:
      this.tableId = Message.read8Bytes(iterator);
    //Deserialize screenName:
      this.screenName = Message.readCString(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message YOU_JOINED_TABLE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 4.
  //Sent by server to new player who joined a table.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_YOU_JOINED_TABLE(
        //New player's table player id.
        final byte tablePlayerId){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(4);

    //Serialize tablePlayerId:
    outputMessage.append1Byte(tablePlayerId);
    return outputMessage;
  }

  */

  public static class Deserialized_1_YOU_JOINED_TABLE{
    //New player's table player id.
    public final byte tablePlayerId;
    public Deserialized_1_YOU_JOINED_TABLE(final Message inputMessage)
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

    //Deserialize tablePlayerId:
      this.tablePlayerId = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message NEW_PLAYER_JOINED_TABLE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 5.
  //Sent by server after new player joined a table to already present people.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_NEW_PLAYER_JOINED_TABLE(
        //New player's name.
        final String screenName,
        //New player's table player id.
        final byte tablePlayerId){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(5);

    //Serialize screenName:
    outputMessage.appendCString(screenName);
    //Serialize tablePlayerId:
    outputMessage.append1Byte(tablePlayerId);
    return outputMessage;
  }

  */

  public static class Deserialized_1_NEW_PLAYER_JOINED_TABLE{
    //New player's name.
    public final String screenName;
    //New player's table player id.
    public final byte tablePlayerId;
    public Deserialized_1_NEW_PLAYER_JOINED_TABLE(final Message inputMessage)
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

    //Deserialize screenName:
      this.screenName = Message.readCString(iterator);
    //Deserialize tablePlayerId:
      this.tablePlayerId = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message GAME_STARTED_AND_INITIAL_MESSAGE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 6.
  //Sent by server when game is started. Some initialization message can be sent within. Move from first player(s) is awaited after that.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_GAME_STARTED_AND_INITIAL_MESSAGE(
        //Initial game--specific message.
        final java.util.Vector<Byte> initialMessage){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(6);

    //Serialize initialMessage:
    outputMessage.appendBinary(initialMessage);
    return outputMessage;
  }

  */

  public static class Deserialized_1_GAME_STARTED_AND_INITIAL_MESSAGE{
    //Initial game--specific message.
    public final java.util.Vector<Byte> initialMessage;
    public Deserialized_1_GAME_STARTED_AND_INITIAL_MESSAGE(final Message inputMessage)
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

    //Deserialize initialMessage:
      this.initialMessage = Message.readBinary(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  //Message MAKE_MOVE:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 7.
  //Sent by client when making a move.

  public static Message serialize_1_MAKE_MOVE(
        //Game-specific move information.
        final java.util.Vector<Byte> move){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(7);

    //Serialize move:
    outputMessage.appendBinary(move);
    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_MAKE_MOVE{
    //Game-specific move information.
    public final java.util.Vector<Byte> move;
    public Deserialized_1_MAKE_MOVE(final Message inputMessage)
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

    //Deserialize move:
      this.move = Message.readBinary(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message MOVE_MADE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 8.
  //Sent by server after client made a move.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_MOVE_MADE(
        //Game-specific move information.
        final java.util.Vector<Byte> move){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(8);

    //Serialize move:
    outputMessage.appendBinary(move);
    return outputMessage;
  }

  */

  public static class Deserialized_1_MOVE_MADE{
    //Game-specific move information.
    public final java.util.Vector<Byte> move;
    public Deserialized_1_MOVE_MADE(final Message inputMessage)
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

    //Deserialize move:
      this.move = Message.readBinary(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }


static abstract class AbstractGeneralHandler
{


  public boolean handle(final Message message){
    switch(GeneralProtocol.lookupMessageType(message))
    {
    case TABLE_CREATED_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_TABLE_CREATED deserialized = 
              new GeneralProtocol.Deserialized_1_TABLE_CREATED(message);
        return this.handle_1_TABLE_CREATED(deserialized.id);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case YOU_JOINED_TABLE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_YOU_JOINED_TABLE deserialized = 
              new GeneralProtocol.Deserialized_1_YOU_JOINED_TABLE(message);
        return this.handle_1_YOU_JOINED_TABLE(deserialized.tablePlayerId);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case NEW_PLAYER_JOINED_TABLE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_NEW_PLAYER_JOINED_TABLE deserialized = 
              new GeneralProtocol.Deserialized_1_NEW_PLAYER_JOINED_TABLE(message);
        return this.handle_1_NEW_PLAYER_JOINED_TABLE(deserialized.screenName,
                      deserialized.tablePlayerId);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case GAME_STARTED_AND_INITIAL_MESSAGE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_GAME_STARTED_AND_INITIAL_MESSAGE deserialized = 
              new GeneralProtocol.Deserialized_1_GAME_STARTED_AND_INITIAL_MESSAGE(message);
        return this.handle_1_GAME_STARTED_AND_INITIAL_MESSAGE(deserialized.initialMessage);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    case MOVE_MADE_1:
      {
        try{
          final GeneralProtocol.Deserialized_1_MOVE_MADE deserialized = 
              new GeneralProtocol.Deserialized_1_MOVE_MADE(message);
        return this.handle_1_MOVE_MADE(deserialized.move);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    default:
      return false;
    }
  }
  //Handlers for various message types:
  public abstract boolean handle_1_TABLE_CREATED(final long id);
  public abstract boolean handle_1_YOU_JOINED_TABLE(final byte tablePlayerId);
  public abstract boolean handle_1_NEW_PLAYER_JOINED_TABLE(final String screenName,
                  final byte tablePlayerId);
  public abstract boolean handle_1_GAME_STARTED_AND_INITIAL_MESSAGE(final java.util.Vector<Byte> initialMessage);
  public abstract boolean handle_1_MOVE_MADE(final java.util.Vector<Byte> move);
}
}
