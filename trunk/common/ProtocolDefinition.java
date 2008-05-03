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
    private final FileWriter hppWriter;
    private final String hppFileName;
    private final FileWriter cppWriter;
    private final int protocolVersion;
    private final String protocolName;

    private final Vector<MessageDefinition> messageDefinitions;
    private final Vector<FlagSetDefinition> flagSetDefinitions;
    
    private boolean addCardsDefinition;

    //ctor:
    public ProtocolDefinition(final String protocolName,
                              final int protocolVersion,
                              final String hppFileName,
                              final String cppFileName,
                              final String javaFileName)
        throws Exception
    {
        if(protocolVersion<1 || protocolVersion>126)
            throw new Exception("Protocol version error.");

        this.protocolName = protocolName;

        this.protocolVersion = protocolVersion;

        this.hppFileName = hppFileName;

        this.hppWriter
            = new FileWriter(hppFileName);

        this.cppWriter
            = new FileWriter(cppFileName);

        this.javaWriter
            = new FileWriter(javaFileName);

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

    private void hppWrite(final String s)
    throws IOException
    {
        this.hppWriter.write(s);
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

    private static final String license
        ="/*\n"
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
        +"\n";

    private void writeHeader() throws IOException{
        hppWrite(license);

        hppWrite("#pragma once\n");
        hppWrite("\n");
        hppWrite("#include <vector>\n"
                 +"#include <string>\n"
                 +"#include <sstream>\n"
                 +"#include <cctype>\n"
                 +"#include \"Message.hpp\"\n"
                 +"\n"
                 +"\n");

        //Class Message is now in fixed file, not generated one.
        //hppWriteMessageClass();
        
        hppWrite("class "+protocolName+"Protocol\n");
        hppWrite("{\n");
        hppWrite("public:\n");
        hppWrite("\n");
        hppWriteMessageTypes();
        hppWrite("\n");

        cppWrite(license);
        cppWrite("\n"
                 +"#include \""+hppFileName+"\"\n");

        javaWrite(license);
        javaWrite("\n");
        javaWrite("import java.util.*;\n");
        javaWrite("\n");
        javaWrite("public class "+protocolName+"Protocol{\n");
        javaWrite("\n");
        javaWriteMessageEnum();
        javaWrite("\n");
    }

    private void hppWriteMessageClass() throws IOException{
        hppWrite("class Message : public std::vector<char>\n"
                 +"{\n"
                 +"private:\n"
                 +"  //Appends value to message\n"
                 +"  template<class T>\n"
                 +"  void appendInteger(const T value,\n"
                 +"                     const unsigned numberOfBytes)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    for(unsigned i=0;i<numberOfBytes;i++)\n"
                 +"      {\n"
                 +"        this->push_back(static_cast<char>(0xFF & (value>>(8*(numberOfBytes-1-i)))));\n"
                 +"      }\n"
                 +"  }\n"
                 +"\n"
                 +"public:\n"
                 +"  template<class T>\n"
                 +"  void append1Byte(const T value)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    this->appendInteger(value,1);\n"
                 +"  }\n"
                 +"\n"
                 +"  template<class T>\n"
                 +"  void append2Bytes(const T value)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    this->appendInteger(value,2);\n"
                 +"  }\n"
                 +"\n"
                 +"  template<class T>\n"
                 +"  void append3Bytes(const T value)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    this->appendInteger(value,3);\n"
                 +"  }\n"
                 +"\n"
                 +"  template<class T>\n"
                 +"  void append4Bytes(const T value)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    this->appendInteger(value,4);\n"
                 +"  }\n"
                 +"\n"
                 +"  void appendCString(const std::string& value)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    this->insert(this->end(),\n"
                 +"                 value.begin(),\n"
                 +"                 value.end());\n"
                 +"    this->push_back(0);\n"
                 +"  }\n"
                 +"\n"
                 +"  //Read value from this message\n"
                 +"  template<class T>\n"
                 +"  static bool read1Byte(Message::const_iterator&it,\n"
                 +"                        const Message::const_iterator&messageEnd,\n"
                 +"                        T&result)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    if(it+1>messageEnd)\n"
                 +"      return false;\n"
                 +"\n"
                 +"    result=(*it++);\n"
                 +"    \n"
                 +"    return true;\n"
                 +"  }\n"
                 +"\n"
                 +"  //Read value from this message\n"
                 +"  template<class T>\n"
                 +"  static bool read2Bytes(Message::const_iterator&it,\n"
                 +"                         const Message::const_iterator&messageEnd,\n"
                 +"                         T&result)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    if(it+2>messageEnd)\n"
                 +"      return false;\n"
                 +"\n"
                 +"    result=0;\n"
                 +"\n"
                 +"    result|=(static_cast<short>(*it++))<<8;\n"
                 +"    result|=(static_cast<short>(*it++));\n"
                 +"    \n"
                 +"    return true;\n"
                 +"  }\n"
                 +"\n"
                 +"  //Read value from this message\n"
                 +"  template<class T>\n"
                 +"  static bool read3Bytes(Message::const_iterator&it,\n"
                 +"                         const Message::const_iterator&messageEnd,\n"
                 +"                         T&result)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    if(it+3>messageEnd)\n"
                 +"      return false;\n"
                 +"    \n"
                 +"    result=0;\n"
                 +"\n"
                 +"    result|=(static_cast<short>(*it++))<<16;\n"
                 +"    result|=(static_cast<short>(*it++))<<8;\n"
                 +"    result|=(static_cast<short>(*it++));\n"
                 +"    \n"
                 +"    return true;\n"
                 +"  }\n"
                 +"\n"
                 +"  //Read value from this message\n"
                 +"  template<class T>\n"
                 +"  static bool read4Bytes(Message::const_iterator&it,\n"
                 +"                         const Message::const_iterator&messageEnd,\n"
                 +"                         T&result)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    if(it+4>messageEnd)\n"
                 +"      return false;\n"
                 +"\n"
                 +"    result=0;\n"
                 +"\n"
                 +"    result|=(static_cast<short>(*it++))<<24;\n"
                 +"    result|=(static_cast<short>(*it++))<<16;\n"
                 +"    result|=(static_cast<short>(*it++))<<8;\n"
                 +"    result|=(static_cast<short>(*it++));\n"
                 +"    \n"
                 +"    return true;\n"
                 +"  }\n"
                 +"\n"
                 +"  static bool readCString(Message::const_iterator&it,\n"
                 +"                          const Message::const_iterator&messageEnd,\n"
                 +"                          std::string&result)\n"
                 +"  {\n"
                 +"    std::ostringstream output;\n"
                 +"    char c;\n"
                 +"    while(true)\n"
                 +"      {\n"
                 +"        if(it==messageEnd)\n"
                 +"          return false;\n"
                 +"        c=(*it++);\n"
                 +"        if(c==0)\n"
                 +"          {\n"
                 +"            result=output.str();\n"
                 +"            return true;\n"
                 +"          }\n"
                 +"        output<<c;\n"
                 +"      }\n"
                 +"  }\n"
                 +"  \n"
                 +"\n");
        hppWriteGetMessageType();
        hppWrite("};\n"
                 +"\n\n\n");
    }

    private void writeFooter() throws IOException{
        hppWrite("};\n");
        javaWrite("}\n");
    }

    private void writeFlagSetDefinition(final FlagSetDefinition flagSetDefinition)
    throws Exception
    {
        hppWrite("  //Flags '"+flagSetDefinition.name+"':\n"
                   +"  //"+flagSetDefinition.comment+"\n");
        javaWrite("  //Flags '"+flagSetDefinition.name+"':\n"
                   +"  //"+flagSetDefinition.comment+"\n");
        hppWrite("  enum "+flagSetDefinition.name+"\n"
                 +"  {\n");

        int flagValue = 1;
        for(int i=0;i<flagSetDefinition.flagDefinitions.length;i++){
            
            final FlagDefinition flagDefinition
                = flagSetDefinition.flagDefinitions[i];

            hppWrite("    //"+flagDefinition.comment+"\n");
            javaWrite("    //"+flagDefinition.comment+"\n");
            hppWrite("    "+flagDefinition.name+" = "+flagValue
                     +(i<flagSetDefinition.flagDefinitions.length-1?",":"")
                     +"\n");
            javaWrite("  public static final int "
                      +flagDefinition.name+" = "+flagValue+";\n");
            flagValue*=2;
        }

        hppWrite("  };\n\n");
        javaWrite("\n\n");
    }

    private void hppWriteMessageTypes()
        throws IOException
    {

        this.hppWrite("  //Can be used for switching to detect\n"
                      +"  //which deserializer should be used.\n");

        this.hppWrite("    static const int8_t UNKNOWN_MESSAGE_"+this.protocolVersion
                      +" = 0;\n");
        
        for(int i=0;i<this.messageDefinitions.size();i++){
            
            final MessageDefinition messageDefinition
                = messageDefinitions.get(i);
            
            hppWrite("    //"+messageDefinition.comment+"\n"
                     +"    static const int8_t "+messageDefinition.name
                     +"_"+this.protocolVersion);
            hppWrite(" = "+messageDefinition.identifier+";\n");
        }

        this.hppWrite("\n\n");
    }

    private void hppWriteMessageToString()
        throws IOException
    {
        hppWrite("  //Represent message as string\n"
                 +"  static std::string messageToString(const Message& message)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    std::ostringstream output;\n"
                 +"\n"
                 +"    output\n"
                 +"      <<\"Message type: \"<<"
                 +"messageTypeToString(message.getMessageType())"
                 +"      <<\", number of bytes: \"<<message.size()<<std::endl;\n"
                 +"\n"
                 +"    const char*const hex = \"0123456789abcdef\";\n"
                 +"\n"
                 +"    int_fast16_t i=0;\n"
                 +"    for(std::vector<char>::const_iterator it=message.begin();\n"
                 +"        it!=message.end() && i<1024;\n"
                 +"        it++)\n"
                 +"      {\n"
                 +"        output\n"
                 +"          <<hex[((*it)>>4) & 0x0F]\n"
                 +"          <<hex[(*it) & 0x0F]\n"
                 +"          <<\" (\"<<(std::isprint(*it)?(*it):'_')<<\") \";\n"
                 +"        i++;\n"
                 +"        if(i%10==0)\n"
                 +"          output<<std::endl;\n"
                 +"      }\n"
                 +"\n"
                 +"    return output.str();\n"
                 +"  }\n");

    }

    private void hppWriteMessageTypeToString()
        throws IOException
    {
        //Enum to string conversion:

        this.hppWrite("  //Can be used for printing message type\n"
                      +"  //in human-readable form.\n"
                      +"  static std::string messageTypeToString(int8_t messageType)"
                      +" throw()");

        this.hppWrite("\n  {");
        this.hppWrite("\n    switch(messageType)");
        this.hppWrite("\n    {");
        this.hppWrite("\n      case UNKNOWN_MESSAGE_"+this.protocolVersion
                      +": return \"UNKNOWN_MESSAGE\";");
        
        for(int i=0;i<this.messageDefinitions.size();i++){
            
            final MessageDefinition messageDefinition
                = messageDefinitions.get(i);
            
            hppWrite("\n"
                     +"      case "+messageDefinition.name
                     +"_"+this.protocolVersion
                     +": return \""+messageDefinition.name+"\";");
        }
        this.hppWrite("\n    }\n\n");
        this.hppWrite("\n    return \"ERROR. No such message in this protocol version!\";");
        this.hppWrite("\n  }\n\n");
        
    }

    private void javaWriteMessageEnum()
        throws IOException
    {
        this.javaWrite("  //Can be used for switching to detect\n"
                       +"  //which deserializer should be used.\n"
                       +"  enum MessageType");

        this.javaWrite("{");
        this.javaWrite("\n    UNKNOWN_MESSAGE_"+this.protocolVersion);
        
        for(int i=0;i<this.messageDefinitions.size();i++){
            
            final MessageDefinition messageDefinition
                = messageDefinitions.get(i);
            
            javaWrite(",\n    //"+messageDefinition.comment+"\n"
                      +"    "+messageDefinition.name
                      +"_"+this.protocolVersion);
        }

        this.javaWrite("\n  }\n\n");
    }

    private void hppWriteGetMessageType()
        throws IOException
    {
        this.hppWrite("  //This method can be used for rapid message\n"
                      +"  //type lookup, so you don't need to try\n"
                      +"  //deserializing using all deserializers.\n"
                      +"  //Remember that deserialization can still\n"
                      +"  //fail, even if this method returns\n"
                      +"  //some known type. It doesn't read the whole\n"
                      +"  //message, just the part where message type\n"
                      +"  //is present. If the message type cannot be\n"
                      +"  //determined, 0 is returned. This could happen\n"
                      +"  //e.g. if message is empty.\n");

        //final String highestMessageName
        //    = this.messageDefinitions.lastElement().name;

        this.hppWrite("  int8_t getMessageType() const throw()\n"
                      +"  {\n"
                      +"    const Message& message = *this;\n"
                      +"    if(message.size()<2)\n"
                      +"      return 0;\n"
                      +"    \n"
                      +"    return message[1];\n"
                      +"  }\n\n");
    }
    
    private void javaWriteLookupMessageType()
        throws IOException
    {
        this.javaWrite("  //This method can be used for rapid message\n"
                       +"  //type lookup, so you don't need to try\n"
                       +"  //deserializing using all deserializers.\n"
                       +"  //Remember that deserialization can still\n"
                       +"  //always fail, even if this method returns\n"
                       +"  //some known type. It doesn't read the whole\n"
                       +"  //message, just the part where message type\n"
                       +"  //is present.\n");

        final String highestMessageName
            = this.messageDefinitions.lastElement().name;

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

        final String s0
            ="  //"+"Message "+messageDefinition.name+":\n\n"
            +"  //This message is sent by "+messageDefinition.sentBy
            +".\n\n"
            +"  //In protocol version "+protocolVersion
            +" this message has id "+messageDefinition.identifier
            +".\n"
            +"  //"+messageDefinition.comment+"\n\n";

        hppWrite(s0);
        javaWrite(s0);

        //Generate serializer:
        if(messageDefinition.sentBy.equals(Sender.CLIENT)){
            //hppWrite("  /* Message sent by "+messageDefinition.sentBy
            //+" only,\n"
            //+"     no need to serialize on other side (here).\n");
        }else{
            javaWrite("  /* Message sent by "+messageDefinition.sentBy
                      +" only,\n"
                      +"     no need to serialize on other side (here).\n");
        }

        hppWrite("  static void serialize_"
                 +protocolVersion+"_"
                 +messageDefinition.name+"(");
        javaWrite("  public static Message serialize_"
                  +protocolVersion+"_"
                  +messageDefinition.name+"(");

        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){

            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];

            hppWrite("\n        //"+pieceDefinition.comment+"\n"
                     +"        ");
            hppWrite(pieceDefinition.type.toCppConstType(this.flagSetDefinitions)
                     +" "+pieceDefinition.name+",\n");

            javaWrite("\n        //"+pieceDefinition.comment+"\n"
                      +"        ");
            javaWrite(pieceDefinition.type.toJavaFinalType(this.flagSetDefinitions)
                      +" "+pieceDefinition.name
                      +(i<messageDefinition.pieceDefinitions.length-1
                        ?","
                        :""));
        }

        hppWrite("    Message&outputMessage)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    outputMessage.resize(0);\n");
        
        javaWrite("){\n"
                  +"    final Message outputMessage\n"
                  +"     = new Message();\n\n");

        final String s1
            ="    //Let the receiver know which "
            +"protocol version this is:\n"
            +"    outputMessage.append1Byte("
            +protocolVersion+");\n"
            +"    //Let the receiver know what kind "
            +"of message this is:\n"
            +"    outputMessage.append1Byte("
            +messageDefinition.identifier+");\n\n";

        hppWrite(s1);
        javaWrite(s1);
        
        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){

            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];

            final String s2
                ="    //Serialize "+pieceDefinition.name+":\n"
                +"    outputMessage."
                +pieceDefinition.type.getAppender
                (this.flagSetDefinitions)
                +"("+pieceDefinition.name+");\n";

            hppWrite(s2);
            javaWrite(s2);
        }
            
        javaWrite("    return outputMessage;\n");
        hppWrite("  }\n\n");
        javaWrite("  }\n\n");

        if(messageDefinition.sentBy.equals(Sender.CLIENT)){
            //hppWrite("  */\n\n");
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
            //hppWrite("  /* Message sent by "+messageDefinition.sentBy
            //+" only,\n"
            //+"     no need to serialize on other side (here).\n");
        }

        //Generate class for deserialized object:
        hppWrite("  class Deserialized_"+this.protocolVersion+"_"
                 +messageDefinition.name+"\n"
                 +"  {\n"
                 +"  public:\n");
        javaWrite("  public static class Deserialized_"+this.protocolVersion+"_"
                  +messageDefinition.name+"{\n");
                
        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){
                    
            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];
            
            hppWrite("    //"+pieceDefinition.comment+"\n");
            hppWrite("    ");
            javaWrite("    //"+pieceDefinition.comment+"\n");
            javaWrite("    ");
            javaWrite("public final ");

            hppWrite(pieceDefinition.type.toCppType(this.flagSetDefinitions));
            javaWrite(pieceDefinition.type.toJavaType(this.flagSetDefinitions));

            hppWrite(" "+pieceDefinition.name+";\n");
            javaWrite(" "+pieceDefinition.name+";\n");
        }

        //C++ class ends here, but Java later.
        hppWrite("  };\n\n");

        //Generate deserializer: C++ deserializer takes form
        //of static method, which writes result into
        //pre-existing object and returns false if
        //deserialization failed for performance reasons.
        //Java deserializer takes form of constructor and
        //throws exception if deserialization failed for
        //clarity reasons.
        hppWrite("  static bool deserialize_"
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

            hppWrite("    //Deserialize "+pieceDefinition.name+":\n");
            hppWrite("    if(!Message::"
                     +pieceDefinition.type.getReader(this.flagSetDefinitions)
                     +"(it,messageEnd,output."
                     +pieceDefinition.name+"))\n"
                     +"      return false;\n");
            javaWrite("    //Deserialize "+pieceDefinition.name+":\n");
            javaWrite("      this."+pieceDefinition.name
                      +" = Message."
                      +pieceDefinition.type.getReader(this.flagSetDefinitions)
                      +"(iterator);\n");
        }
        
        hppWrite("    return true;\n"
                 +"  }\n\n");

        javaWrite("      }catch(NoSuchElementException e){\n"
                  +"        throw new MessageDeserializationException(e);\n"
                  +"      }\n"
                  +"    }\n"
                  +"  }\n\n");

        if(messageDefinition.sentBy.equals(Sender.CLIENT)){
            javaWrite("  */\n\n");
        }else{
            //hppWrite("  */\n\n");
        }
        //End of deserialization.
    }

    /** Writes necessary enums, etc. for use in card games. */
    private void writeCards()
        throws IOException
    {
        final String s0
            ="  //Used in card games. You can encode a card on \n"
            +"  //one byte by bitwise disjunction of value and\n"
            +"  //color. Use VALUE_MASK and COLOR_MASK to decode.\n";

        hppWrite(s0);
        javaWrite(s0);

        hppWrite("  enum Card\n"
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

    private void hppWriteHandler() throws Exception{
        hppWrite("\n"
                 +"class "+protocolName+"Handler\n"
                 +"{\n"
                 +"  protected:\n"
                 +"  //Objects for temporary deserialization (to avoid creating\n"
                 +"  //new ones all the time):\n");

        for(MessageDefinition md : this.messageDefinitions){
            if(md.sentBy.equals(Sender.CLIENT)
               &&md.pieceDefinitions.length>0)
                hppWrite("  "+protocolName+"Protocol::Deserialized"
                         +"_"+protocolVersion+"_"+md.name
                         +" deserialized_"+md.name+";\n");
        }

        hppWrite("\n"
                 +"  bool handle(const Message&message) throw();\n"
                 +"\n");

        cppWrite("\n"
                 +"  bool "+protocolName+"Handler::handle(const Message&message) throw()\n"
                 +"  {\n"
                 +"    switch(message.getMessageType())\n"
                 +"    {\n");

        for(MessageDefinition md : this.messageDefinitions){
            if(md.sentBy.equals(Sender.CLIENT)){
                cppWrite("    case "+protocolName+"Protocol::"
                         +md.name+"_"+protocolVersion+":\n"
                         +"      return "+protocolName+"Protocol::deserialize_"
                         +protocolVersion+"_"+md.name+"(message");
                if(md.pieceDefinitions.length>0)
                    cppWrite(",\n"
                             +"                              "
                             +"this->deserialized_"+md.name+")\n");
                else
                    cppWrite(")\n");
                cppWrite("             && this->handle_"+protocolVersion
                         +"_"+md.name+"();\n");
            }
        }
        
        cppWrite("    default:\n"
                 +"      return false;\n"
                 +"    }\n"
                 +"  }\n");

        hppWrite("  //Handlers for various message types:\n");
        for(MessageDefinition md : this.messageDefinitions){
            if(md.sentBy.equals(Sender.CLIENT))
                hppWrite("  virtual bool handle"+"_"+protocolVersion
                         +"_"+md.name+"() throw() =0;\n");
        }

        hppWrite("  virtual ~"+protocolName+"Handler() throw() {}");

        hppWrite("};\n");

    }

    public void write()
        throws Exception
    {
        this.writeHeader();

        if(this.addCardsDefinition){
            this.writeCards();
        }

        for(FlagSetDefinition flagSetDefinition
                : this.flagSetDefinitions){
            this.writeFlagSetDefinition(flagSetDefinition);
        }

        this.javaWriteLookupMessageType();
        this.hppWriteMessageTypeToString();
        this.hppWriteMessageToString();

        for(MessageDefinition messageDefinition : this.messageDefinitions){
            this.writeMessageDefinition(messageDefinition);
        }

        this.writeFooter();

        this.hppWriteHandler();


        this.javaWriter.flush();
        this.javaWriter.close();
        this.hppWriter.flush();
        this.hppWriter.close();
        this.cppWriter.flush();
        this.cppWriter.close();
    }
}
