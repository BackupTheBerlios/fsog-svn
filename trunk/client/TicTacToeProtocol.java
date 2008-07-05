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

public class TicTacToeProtocol{

  //Can be used for switching to detect
  //which deserializer should be used.
  enum MessageType{
    UNKNOWN_MESSAGE_1,
    //Simple TicTacToe move.
    MAKE_MOVE_1,
    //Simple TicTacToe move. No need to say who made this move.
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
        case 1: return MessageType.MAKE_MOVE_1;
        case 2: return MessageType.MOVE_MADE_1;
        default: return MessageType.UNKNOWN_MESSAGE_1;
        }
    }catch(Exception e){
        return MessageType.UNKNOWN_MESSAGE_1;
    }
  }

  //Message MAKE_MOVE:

  //This message is sent by CLIENT.

  //In protocol version 1 this message has id 1.
  //Simple TicTacToe move.

  public static Message serialize_1_MAKE_MOVE(
        //In which row player puts her X or O.
        final byte row,
        //In which column player puts her X or O.
        final byte column){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(1);

    //Serialize row:
    outputMessage.append1Byte(row);
    //Serialize column:
    outputMessage.append1Byte(column);
    return outputMessage;
  }

  /* Message sent by CLIENT only,
     no need to serialize on other side (here).
  public static class Deserialized_1_MAKE_MOVE{
    //In which row player puts her X or O.
    public final byte row;
    //In which column player puts her X or O.
    public final byte column;
    public Deserialized_1_MAKE_MOVE(final Message inputMessage)
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

    //Deserialize row:
      this.row = Message.read1Byte(iterator);
    //Deserialize column:
      this.column = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }

  */

  //Message MOVE_MADE:

  //This message is sent by SERVER.

  //In protocol version 1 this message has id 2.
  //Simple TicTacToe move. No need to say who made this move.

  /* Message sent by SERVER only,
     no need to serialize on other side (here).
  public static Message serialize_1_MOVE_MADE(
        //In which row player puts her X or O.
        final byte row,
        //In which column player puts her X or O.
        final byte column){
    final Message outputMessage
     = new Message();

    //Let the receiver know which protocol version this is:
    outputMessage.append1Byte(1);
    //Let the receiver know what kind of message this is:
    outputMessage.append1Byte(2);

    //Serialize row:
    outputMessage.append1Byte(row);
    //Serialize column:
    outputMessage.append1Byte(column);
    return outputMessage;
  }

  */

  public static class Deserialized_1_MOVE_MADE{
    //In which row player puts her X or O.
    public final byte row;
    //In which column player puts her X or O.
    public final byte column;
    public Deserialized_1_MOVE_MADE(final Message inputMessage)
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

    //Deserialize row:
      this.row = Message.read1Byte(iterator);
    //Deserialize column:
      this.column = Message.read1Byte(iterator);
      }catch(NoSuchElementException e){
        throw new MessageDeserializationException(e);
      }
    }
  }


  public static boolean handle(final Message message,
                               final TicTacToeHandler handler){

    switch(TicTacToeProtocol.lookupMessageType(message))
    {
    case MOVE_MADE_1:
      {
        try{
          final TicTacToeProtocol.Deserialized_1_MOVE_MADE deserialized = 
              new TicTacToeProtocol.Deserialized_1_MOVE_MADE(message);
        return handler.handle_1_MOVE_MADE(deserialized.row,
                      deserialized.column);
        }catch(final MessageDeserializationException e){
          return false;
        }
    }
    default:
      return false;
    }
  }

public static interface TicTacToeHandler
{

  //Handlers for various message types:
  public abstract boolean handle_1_MOVE_MADE(final byte row,
                  final byte column);
}
}
