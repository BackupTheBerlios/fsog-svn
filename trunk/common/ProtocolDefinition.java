/* -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 * vim:expandtab:shiftwidth=4:tabstop=4: */


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


import java.io.*;
import java.util.*;

/** This class is used for defining a protocol. A protocol
 * consists of a set of message, flag, enum
 * definitions. Because maintaining both Java and C++ code for
 * serializing/deserializing messages is difficult, this class
 * generates such code automatically. To use this class,
 * simply create an instance of it:
 *
 * ProtocolDefinition pd = new ProtocolDefinition(...);
 *
 * Then add elements you need in your protocol, like:
 * 
 * pd.defineMessage("MY_MESSAGE",...);
 *
 * In the end to actually generate Java and C++ files, do:
 *
 * pd.write();
 *
 * The generated classes will contain methods of the form:
 *
 * ... serialize_13_MY_MESSAGE(...)
 * 
 * The method names contain the protocol number. Remember that
 * if you change something in your protocol, you should change
 * the protocol version. If you change the protocol version,
 * your old code which uses the (de)serializers will not work
 * anymore. The protocol version is added to all method names
 * on purpose. It's because if you change the protocol
 * version, you should at least inspect all sources using the
 * protocol to see if they still use the protocol correctly.
 */

public class ProtocolDefinition{

    //Members:
    private final FileWriter javaWriter;
    private final FileWriter cppWriter;
    private final int protocolVersion;
    private final int serverUDPPort;

    private final Vector<MessageDefinition> messageDefinitions;
    private final Vector<FlagSetDefinition> flagSetDefinitions;
    
    private boolean addCardsDefinition;

    //ctor:
    public ProtocolDefinition(final int protocolVersion,
                              final int serverUDPPort,
                              final String cppFileName,
                              final String javaFileName)
        throws Exception
    {
        if(protocolVersion<1 || protocolVersion>255)
            throw new Exception("Protocol version error.");

        this.protocolVersion = protocolVersion;

        this.cppWriter
            = new FileWriter(cppFileName);

        this.javaWriter
            = new FileWriter(javaFileName);

        this.serverUDPPort = serverUDPPort;

        this.messageDefinitions
            = new Vector<MessageDefinition>();

        this.flagSetDefinitions
            = new Vector<FlagSetDefinition>();

        this.addCardsDefinition = false;
    }

    public void defineMessage(final String name,
                              final String comment,
                              final Sender sentBy,
                              final PieceDefinition ... pieceDefinitions)
        throws Exception
    {
        this.messageDefinitions.add
            (new MessageDefinition(name,
                                   comment,
                                   sentBy,
                                   pieceDefinitions));
    }

    public void defineFlagSet(final String name,
                              final String comment,
                              final FlagDefinition ... flagDefinitions){
        this.flagSetDefinitions.add
            (new FlagSetDefinition(name,
                                   comment,
                                   flagDefinitions));
    }

    public void addCardsDefinition(){
        this.addCardsDefinition = true;
    }

    private void cppWrite(final String s)
    throws IOException
    {
        this.cppWriter.write(s);
    }

    private void javaWrite(final String s)
    throws IOException
    {
        this.javaWriter.write(s);
    }

    private void bothWrite(final String s)
    throws IOException
    {
        this.cppWriter.write(s);
        this.javaWriter.write(s);
    }

    private void writeHeader() throws IOException{
        bothWrite("/*\n"
                  +"    FSOG - Free Software Online Games\n"
                  +"    Copyright (C) 2007 Bartlomiej Antoni Szymczak\n"
                  +"\n"
                  +"    This file is part of FSOG.\n"
                  +"\n"
                  +"    FSOG is free software: you can redistribute it and/or modify\n"
                  +"    it under the terms of the GNU Affero General Public License as\n"
                  +"    published by the Free Software Foundation, either version 3 of the\n"
                  +"    License, or (at your option) any later version.\n"
                  +"\n"
                  +"    This program is distributed in the hope that it will be useful,\n"
                  +"    but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                  +"    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
                  +"    GNU Affero General Public License for more details.\n"
                  +"\n"
                  +"    You should have received a copy of the GNU Affero General Public License\n"
                  +"    along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
                  +"\n"
                  +"*/\n"
                  +"\n"
                  +"/*\n"
                  +"    You can contact the author, Bartlomiej Antoni Szymczak, by:\n"
                  +"    - electronic mail: rhywek@gmail.com\n"
                  +"    - paper mail:\n"
                  +"        Bartlomiej Antoni Szymczak\n"
                  +"        Boegesvinget 8, 1. sal\n"
                  +"        2740 Skovlunde\n"
                  +"        Denmark\n"
                  +"*/\n"
                  +"\n");

        cppWrite("#pragma once\n");
        cppWrite("\n");
        cppWrite("#include \"Message.hpp\"\n");
        cppWrite("\n");
        cppWrite("class Protocol\n");
        cppWrite("{\n");
        cppWrite("public:\n");
        cppWrite("\n");

        javaWrite("\n");
        javaWrite("import java.util.*;\n");
        javaWrite("\n");
        javaWrite("public class Protocol{\n");
        javaWrite("\n");
    }

    private void writeFooter() throws IOException{
        cppWrite("};\n");
        javaWrite("}\n");
    }

    private void writeServerUDPPort() throws IOException{
        bothWrite("  //Known UDP port number of the server:\n");
        cppWrite("  static uint_fast16_t getServerUDPPort_"
                 +this.protocolVersion+"()\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    return "+this.serverUDPPort+";\n"
                 +"  }\n\n");
        javaWrite("  static int getServerUDPPort_"
                  +this.protocolVersion+"(){\n"
                  +"    return "+this.serverUDPPort+";\n"
                  +"  }\n\n");
    }

    private void writeFlagSetDefinition(final FlagSetDefinition flagSetDefinition)
    throws Exception
    {
        bothWrite("  //Flags '"+flagSetDefinition.name+"':\n"
                   +"  //"+flagSetDefinition.comment+"\n");
        cppWrite("  enum "+flagSetDefinition.name+"\n"
                 +"  {\n");

        int flagValue = 1;
        for(int i=0;i<flagSetDefinition.flagDefinitions.length;i++){
            
            final FlagDefinition flagDefinition
                = flagSetDefinition.flagDefinitions[i];

            bothWrite("    //"+flagDefinition.comment+"\n");
            cppWrite("    "+flagDefinition.name+" = "+flagValue
                     +(i<flagSetDefinition.flagDefinitions.length-1?",":"")
                     +"\n");
            javaWrite("  public static final int "
                      +flagDefinition.name+" = "+flagValue+";\n");
            flagValue*=2;
        }

        cppWrite("  };\n\n");
        javaWrite("\n\n");
    }

    private void writeMessageEnum()
        throws IOException
    {

        this.bothWrite("  //Can be used for switching to detect\n"
                       +"  //which deserializer should be used.\n"
                       +"  enum MessageType");

        this.cppWrite("\n  {");
        this.javaWrite("{");
        this.cppWrite("\n    UNKNOWN_MESSAGE_"+this.protocolVersion
                      +" = 0");
        this.javaWrite("\n    UNKNOWN_MESSAGE_"+this.protocolVersion);
        
        for(int i=0;i<this.messageDefinitions.size();i++){
            
            final MessageDefinition messageDefinition
                = messageDefinitions.get(i);
            
            bothWrite(",\n    //"+messageDefinition.comment+"\n"
                      +"    "+messageDefinition.name
                      +"_"+this.protocolVersion);
            cppWrite(" = "+messageDefinition.identifier);
        }

        this.cppWrite("\n  };\n\n");
        this.javaWrite("\n  }\n\n");
    }

    private void writeLookupMessageType()
        throws IOException
    {
        this.bothWrite("  //This method can be used for rapid message\n"
                       +"  //type lookup, so you don't need to try\n"
                       +"  //deserializing using all deserializers.\n"
                       +"  //Remember that deserialization can still\n"
                       +"  //always fail, even if this method returns\n"
                       +"  //some known type. It doesn't read the whole\n"
                       +"  //message, just the part where message type\n"
                       +"  //is present.\n");

        final String highestMessageName
            = this.messageDefinitions.lastElement().name;

        this.cppWrite("  static MessageType lookupMessageType("
                      +"const Message&message)\n"
                      +"  {\n"
                      +"    if(message.size()<2)\n"
                      +"      return Protocol::MessageType("
                      +"UNKNOWN_MESSAGE_"+this.protocolVersion+");\n"
                      +"    \n"
                      +"    const char messageType\n"
                      +"     = message[1];\n"
                      +"    \n"
                      +"    if(messageType<=Protocol::MessageType("
                      +"UNKNOWN_MESSAGE_"+this.protocolVersion+")\n"
                      +"       || messageType>"+highestMessageName
                      +"_"+this.protocolVersion+")\n"
                      +"      return Protocol::MessageType("
                      +"UNKNOWN_MESSAGE_"+this.protocolVersion+");\n"
                      +"    \n"
                      +"    return static_cast<MessageType>(messageType);\n"
                      +"  }\n\n");

        this.javaWrite("  public static MessageType lookupMessageType("
                       +"final Message message){\n"
                       +"    try{\n"
                       +"        final Iterator<Byte> iterator\n"
                       +"         = message.iterator();\n"
                       +"        \n"
                       +"        Message.read1Byte(iterator);\n"
                       +"        \n"
                       +"        final byte byteMessageType\n"
                       +"         = Message.read1Byte(iterator);\n"
                       +"        \n"
                       +"        switch(byteMessageType){\n");

        for(MessageDefinition messageDefinition: this.messageDefinitions){
            this.javaWrite("        case "+messageDefinition.identifier
                           +": return MessageType."
                           +messageDefinition.name+"_"
                           +this.protocolVersion+";\n");
        }

        this.javaWrite("        default: return MessageType."
                       +"UNKNOWN_MESSAGE_"+this.protocolVersion+";\n"
                       +"        }\n"
                       +"    }catch(Exception e){\n"
                       +"        return MessageType.UNKNOWN_MESSAGE_"
                       +this.protocolVersion+";\n"
                       +"    }\n"
                       +"  }\n\n");
    }
    
    private void writeMessageDefinition(final MessageDefinition messageDefinition)
        throws Exception
    {
        if(protocolVersion<1||protocolVersion>255)
            throw new Exception("Incorrect protocol version.");

        bothWrite("  //"+"Message "+messageDefinition.name+":\n\n");
        bothWrite("  //This message is sent by "+messageDefinition.sentBy
                  +".\n\n");
        bothWrite("  //In protocol version "+protocolVersion
                  +" this message has id "+messageDefinition.identifier
                  +".\n");
        bothWrite("  //"+messageDefinition.comment+"\n\n");

        //Generate serializer:
        if(messageDefinition.sentBy.equals(Sender.CLIENT)){
            //cppWrite("  /* Message sent by "+messageDefinition.sentBy
            //+" only,\n"
            //+"     no need to serialize on other side (here).\n");
        }else{
            javaWrite("  /* Message sent by "+messageDefinition.sentBy
                      +" only,\n"
                      +"     no need to serialize on other side (here).\n");
        }

        cppWrite("  static void serialize_"
                 +protocolVersion+"_"
                 +messageDefinition.name+"(");
        javaWrite("  public static Message serialize_"
                  +protocolVersion+"_"
                  +messageDefinition.name+"(");

        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){

            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];

            bothWrite("\n        //"+pieceDefinition.comment+"\n"
                      +"        ");

            cppWrite(pieceDefinition.type.toCppConstType(this.flagSetDefinitions)
                     +" "+pieceDefinition.name+",\n");
            javaWrite(pieceDefinition.type.toJavaFinalType(this.flagSetDefinitions)
                      +" "+pieceDefinition.name
                      +(i<messageDefinition.pieceDefinitions.length-1
                        ?","
                        :""));
        }

        cppWrite("    Message&outputMessage)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    outputMessage.resize(0);\n");
        
        javaWrite("){\n"
                  +"    final Message outputMessage\n"
                  +"     = new Message();\n\n");

        bothWrite("    //Let the receiver know which "
                  +"protocol version this is:\n"
                  +"    outputMessage.append1Byte("
                  +protocolVersion+");\n"
                  +"    //Let the receiver know what kind "
                  +"of message this is:\n"
                  +"    outputMessage.append1Byte("
                  +messageDefinition.identifier+");\n\n");
        
        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){

            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];
            
            bothWrite("    //Serialize "+pieceDefinition.name+":\n");
            bothWrite("    outputMessage."
                      +pieceDefinition.type.getAppender
                      (this.flagSetDefinitions)
                      +"("+pieceDefinition.name+");\n");
        }
            
        javaWrite("    return outputMessage;\n");
        bothWrite("  }\n\n");

        if(messageDefinition.sentBy.equals(Sender.CLIENT)){
            //cppWrite("  */\n\n");
        }else{
            javaWrite("  */\n\n");
        }
        //End of serialization.

        //Deserialization.

        if(messageDefinition.sentBy.equals(Sender.CLIENT)){
            javaWrite("  /* Message sent by "+messageDefinition.sentBy
                      +" only,\n"
                      +"     no need to serialize on other side (here).\n");
        }else{
            //cppWrite("  /* Message sent by "+messageDefinition.sentBy
            //+" only,\n"
            //+"     no need to serialize on other side (here).\n");
        }

        //Generate class for deserialized object:
        cppWrite("  class Deserialized_"+this.protocolVersion+"_"
                 +messageDefinition.name+"\n"
                 +"  {\n"
                 +"  public:\n");
        javaWrite("  public static class Deserialized_"+this.protocolVersion+"_"
                  +messageDefinition.name+"{\n");
                
        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){
                    
            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];

            bothWrite("    //"+pieceDefinition.comment+"\n");
            bothWrite("    ");
            javaWrite("public final ");

            cppWrite(pieceDefinition.type.toCppType(this.flagSetDefinitions));
            javaWrite(pieceDefinition.type.toJavaType(this.flagSetDefinitions));

            bothWrite(" "+pieceDefinition.name+";\n");
        }

        //C++ class ends here, but Java later.
        cppWrite("  };\n\n");

        //Generate deserializer: C++ deserializer takes form
        //of static method, which writes result into
        //pre-existing object and returns false if
        //deserialization failed for performance reasons.
        //Java deserializer takes form of constructor and
        //throws exception if deserialization failed for
        //clarity reasons.
        cppWrite("  static bool deserialize_"
                 +this.protocolVersion+"_"
                 +messageDefinition.name+"(const Message&inputMessage"
                 +(messageDefinition.pieceDefinitions.length>0
                   ?",\n        Deserialized_"+this.protocolVersion+"_"
                   +messageDefinition.name
                   +"&output"
                   :"")
                 +")\n"
                 +"  throw()\n"
                 +"  {\n"
                 +"    Message::const_iterator it\n"
                 +"     = inputMessage.begin();\n"
                 +"    const Message::const_iterator messageEnd\n"
                 +"     = inputMessage.end();\n"
                 +"    \n"
                 +"    //Check protocol version:\n"
                 +"    char protocolVersion=0;\n"
                 +"    if(!Message::read1Byte(it,messageEnd,protocolVersion))\n"
                 +"      return false;\n"
                 +"    if(protocolVersion!="+protocolVersion+")\n"
                 +"      return false;\n"
                 +"    \n"
                 +"    //Check message kind:\n"
                 +"    char messageKind=0;\n"
                 +"    if(!Message::read1Byte(it,messageEnd,messageKind))\n"
                 +"      return false;\n"
                 +"    if(messageKind!="+messageDefinition.identifier+")\n"
                 +"      return false;\n\n"
                 +"    //Deserialize pieces:\n\n"
                 );

        javaWrite("    public Deserialized_"
                  +this.protocolVersion+"_"
                  +messageDefinition.name+"(final Message inputMessage)\n"
                  +"      throws MessageDeserializationException{\n");
        
        javaWrite("      try{\n"
                  +"        final Iterator<Byte> iterator\n"
                  +"         = inputMessage.iterator();\n\n"
                  +"        //Check protocol version:\n"
                  +"        if(iterator.next()!="+this.protocolVersion+")\n"
                  +"          throw new MessageDeserializationException();\n\n"
                  +"        //Check kind of message:\n"
                  +"        if(iterator.next()!="+messageDefinition.identifier+")\n"
                  +"          throw new MessageDeserializationException();\n\n");
        
        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){
            
            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];

            bothWrite("    //Deserialize "+pieceDefinition.name+":\n");
            cppWrite("    if(!Message::"
                     +pieceDefinition.type.getReader(this.flagSetDefinitions)
                     +"(it,messageEnd,output."
                     +pieceDefinition.name+"))\n"
                     +"      return false;\n");
            javaWrite("      this."+pieceDefinition.name
                      +" = Message."
                      +pieceDefinition.type.getReader(this.flagSetDefinitions)
                      +"(iterator);\n");
        }
        
        cppWrite("    return true;\n"
                 +"  }\n\n");

        javaWrite("      }catch(NoSuchElementException e){\n"
                  +"        throw new MessageDeserializationException(e);\n"
                  +"      }\n"
                  +"    }\n"
                  +"  }\n\n");

        if(messageDefinition.sentBy.equals(Sender.CLIENT)){
            javaWrite("  */\n\n");
        }else{
            //cppWrite("  */\n\n");
        }
        //End of deserialization.
    }

    /** Writes necessary enums, etc. for use in card games. */
    private void writeCards()
        throws IOException
    {
        bothWrite("  //Used in card games. You can encode a card on \n"
                  +"  //one byte by bitwise disjunction of value and\n"
                  +"  //color. Use VALUE_MASK and COLOR_MASK to decode.\n");

        cppWrite("  enum Card\n"
                 +"  {\n"
                 +"    ACE = 0x01,\n"
                 +"    TWO = 0x02,\n"
                 +"    THREE = 0x03,\n"
                 +"    FOUR = 0x04,\n"
                 +"    FIVE = 0x05,\n"
                 +"    SIX = 0x06,\n"
                 +"    SEVEN = 0x07,\n"
                 +"    EIGHT = 0x08,\n"
                 +"    NINE = 0x09,\n"
                 +"    TEN = 0x0A,\n"
                 +"    JACK = 0x0B,\n"
                 +"    QUEEN = 0x0C,\n"
                 +"    KING = 0x0D,\n"
                 +"    JOKER = 0x0E,\n"
                 +"    VALUE_MASK = 0x0F,\n"
                 +"    HEARTS = 0x10,\n"
                 +"    DIAMONDS = 0x20,\n"
                 +"    CLUBS = 0x30,\n"
                 +"    SPADES = 0x40,\n"
                 +"    COLOR_MASK = 0xF0\n"
                 +"  };\n\n");

        javaWrite("  final static byte ACE = (byte) 0x01;\n"
                  +"  final static byte TWO = (byte) 0x02;\n"
                  +"  final static byte THREE = (byte) 0x03;\n"
                  +"  final static byte FOUR = (byte) 0x04;\n"
                  +"  final static byte FIVE = (byte) 0x05;\n"
                  +"  final static byte SIX = (byte) 0x06;\n"
                  +"  final static byte SEVEN = (byte) 0x07;\n"
                  +"  final static byte EIGHT = (byte) 0x08;\n"
                  +"  final static byte NINE = (byte) 0x09;\n"
                  +"  final static byte TEN = (byte) 0x0A;\n"
                  +"  final static byte JACK = (byte) 0x0B;\n"
                  +"  final static byte QUEEN = (byte) 0x0C;\n"
                  +"  final static byte KING = (byte) 0x0D;\n"
                  +"  final static byte JOKER = (byte) 0x0E;\n"
                  +"  final static byte VALUE_MASK = (byte) 0x0F;\n"
                  +"  final static byte HEARTS = (byte) 0x10;\n"
                  +"  final static byte DIAMONDS = (byte) 0x20;\n"
                  +"  final static byte CLUBS = (byte) 0x30;\n"
                  +"  final static byte SPADES = (byte) 0x40;\n"
                  +"  final static byte COLOR_MASK = (byte) 0xF0;\n\n");
    }

    public void write()
        throws Exception
    {
        this.writeHeader();

        this.writeServerUDPPort();

        this.writeMessageEnum();

        if(this.addCardsDefinition){
            this.writeCards();
        }

        for(FlagSetDefinition flagSetDefinition
                : this.flagSetDefinitions){
            this.writeFlagSetDefinition(flagSetDefinition);
        }

        this.writeLookupMessageType();

        for(MessageDefinition messageDefinition : this.messageDefinitions){
            this.writeMessageDefinition(messageDefinition);
        }

        this.writeFooter();

        this.javaWriter.flush();
        this.javaWriter.close();
        this.cppWriter.flush();
        this.cppWriter.close();
    }
}
