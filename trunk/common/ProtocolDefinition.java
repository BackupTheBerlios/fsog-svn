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
    private final String className;
    private final String hppFileName;
    private final FileWriter cppWriter;
    private final int protocolVersion;
    private final String protocolName;

    private final Vector<MessageDefinition> messageDefinitions;
    private final Vector<FlagSetDefinition> flagSetDefinitions;
    private final Vector<ConstantDefinition> constantDefinitions;
    
    //ctor:
    public ProtocolDefinition(final String protocolName,
                              final int protocolVersion,
                              final String className,
                              final String hppFileName,
                              final String cppFileName,
                              final String javaFileName)
        throws Exception
    {
        if(protocolVersion<1 || protocolVersion>126)
            throw new Exception("Protocol version error.");

        this.protocolName = protocolName;

        this.protocolVersion = protocolVersion;

        this.className = className;

        this.hppFileName = hppFileName;

        this.hppWriter
            = new FileWriter(hppFileName);

        this.cppWriter
            = (cppFileName==null
               ?null
               :new FileWriter(cppFileName));

        this.javaWriter
            = new FileWriter(javaFileName);

        this.messageDefinitions
            = new Vector<MessageDefinition>();

        this.flagSetDefinitions
            = new Vector<FlagSetDefinition>();

        this.constantDefinitions = new Vector<ConstantDefinition>();
    }

    public void defineMessage(final String name,
                              final String comment,
                              final EnumSet<Create> create,
                              final PieceDefinition ... pieceDefinitions)
        throws Exception
    {
        this.messageDefinitions.add
            (new MessageDefinition(name,
                                   comment,
                                   create,
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

    public void defineConstants(final ConstantDefinition... definitions){
        this.constantDefinitions.addAll(Arrays.asList(definitions));
    }

    private void hppWrite(final String s)
    throws IOException
    {
        this.hppWriter.write(s);
    }

    private void cppWrite(final String s)
    throws IOException
    {
        if(this.cppWriter!=null)
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
                 +"#include <list>\n"
                 +"#include \"Time.hpp\"\n"
                 +"#include \"Message.hpp\"\n"
                 +"#include \"SessionAddressedMessage.hpp\"\n"
                 +"\n");

        //Class Message is now in fixed file, not generated one.
        //hppWriteMessageClass();
        
        hppWrite("class "+className+"\n");
        hppWrite("{\n");
        hppWrite("public:\n");
        hppWrite("\n");
        hppWriteMessageTypes();
        hppWrite("\n");

        cppWrite(license);
        cppWrite("\n"
                 +"#include \""+className+".hpp\"\n");

        javaWrite(license);
        javaWrite("\n");
        javaWrite("import java.util.*;\n");
        javaWrite("\n");
        javaWrite("public class "+className+"{\n");
        javaWrite("\n");
        javaWriteMessageEnum();
        javaWrite("\n");
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

    private void writeConstantDefinitions()
    throws Exception
    {
        hppWrite("  //Constants:\n");
        javaWrite("  //Constants:\n");
        for(ConstantDefinition constantDefinition : this.constantDefinitions){

            if(constantDefinition.comment!=null){
                hppWrite("  //"+constantDefinition.comment+"\n");
                javaWrite("  //"+constantDefinition.comment+"\n");
            }
            hppWrite("  static "+constantDefinition.type.toCppConstType(this.flagSetDefinitions)
                     +" "+constantDefinition.name+" = "
                     +constantDefinition.value+";\n");
            javaWrite("  public static "
                      +constantDefinition.type.toJavaFinalType(this.flagSetDefinitions)
                      +" "+constantDefinition.name+" = "
                      +constantDefinition.value+";\n");
        }

        hppWrite("\n\n");
        javaWrite("\n\n");
    }

    private void hppWriteMessageTypes()
        throws IOException
    {
        if(this.messageDefinitions.isEmpty())
            return;

        //TODO: When only one message, don't waste one byte in
        //serialized message for message type.

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

    private void hppWriteGetMessageType()
        throws IOException
    {
        if(this.messageDefinitions.isEmpty())
            return;

        hppWrite("  //This method can be used for rapid message\n"
                 +"  //type lookup, so you don't need to try\n"
                 +"  //deserializing using all deserializers.\n"
                 +"  //Remember that deserialization can still\n"
                 +"  //fail, even if this method returns\n"
                 +"  //some known type. It doesn't read the whole\n"
                 +"  //message, just the part where message type\n"
                 +"  //is present. If the message type cannot be\n"
                 +"  //determined, 0 is returned. This could happen\n"
                 +"  //e.g. if message is empty.\n"
                 +"  static int8_t getMessageType(const std::vector<char>& message)"
                 +" throw()\n"
                 +"  {\n"
                 +"    if(message.size()<2)\n"
                 +"      return 0;\n"
                 +"    \n"
                 +"    return message[1];\n"
                 +"  }\n\n");
    }

    private void hppWriteMessageToString()
        throws IOException
    {
        if(this.messageDefinitions.isEmpty())
            return;

        hppWrite("  //Represent message as string\n"
                 +"  static std::string messageToString(const std::vector<char>& message)\n"
                 +"    throw()\n"
                 +"  {\n"
                 +"    std::ostringstream output;\n"
                 +"\n"
                 +"    output\n"
                 +"      <<\"MSG \"<<"
                 +"messageTypeToString(getMessageType(message))\n"
                 +"      <<\" ("+protocolName+" v"+protocolVersion
                 +", \"<<message.size()<<\"B):\"<<std::endl;\n"
                 +"\n"
                 +"    const char*const hex = \"0123456789ABCDEF\";\n"
                 +"\n"
                 +"    for(uint_fast16_t line=0;20*line<message.size();line++)\n"
                 +"    {\n"
                 +"      //Print hexadecimal line:\n"
                 +"      for(uint_fast16_t i=20*line;i<20*(line+1) && i<message.size();i++)\n"
                 +"        {\n"
                 +"          const char c = message[i];\n"
                 +"          output<<hex[(c>>4) & 0x0F]<<hex[c & 0x0F]<<' ';\n"
                 +"        }\n"
                 +"      output<<std::endl;\n"
                 +"      //Print human--readable line:\n"
                 +"      for(uint_fast16_t i=20*line;i<20*(line+1) && i<message.size();i++)\n"
                 +"        {\n"
                 +"          const char c = message[i];\n"
                 +"          if(std::isprint(c))\n"
                 +"            output<<c<<\"  \";\n"
                 +"          else\n"
                 +"            output<<\"__ \";\n"
                 +"        }\n"
                 +"      output<<std::endl;\n"
                 +"    }\n"
                 +"    return output.str();\n"
                 +"  }\n");
    }

    private void hppWriteMessageTypeToString()
        throws IOException
    {
        if(this.messageDefinitions.isEmpty())
            return;

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
        if(this.messageDefinitions.isEmpty())
            return;

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

    private void javaWriteLookupMessageType()
        throws IOException
    {
        if(this.messageDefinitions.isEmpty())
            return;

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
                       +"final Vector<Byte> message){\n"
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
            +"  //This message will create: "+messageDefinition.create
            +".\n\n"
            +"  //In protocol version "+protocolVersion
            +" this message has id "+messageDefinition.identifier
            +".\n"
            +"  //"+messageDefinition.comment+"\n\n";

        hppWrite(s0);
        javaWrite(s0);

        //Generate serializer:
        if(messageDefinition.create.contains(Create.CPP_SERIALIZER))
            hppWrite("  static void serialize_"
                     +protocolVersion+"_"
                     +messageDefinition.name+"(");
        if(messageDefinition.create.contains(Create.JAVA_SERIALIZER))
            javaWrite("  public static Vector<Byte> serialize_"
                      +protocolVersion+"_"
                      +messageDefinition.name+"(");

        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){

            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];

            if(messageDefinition.create.contains(Create.CPP_SERIALIZER)){
                hppWrite("\n        //"+pieceDefinition.comment+"\n"
                         +"        ");
                hppWrite(pieceDefinition.type.toCppConstType(this.flagSetDefinitions)
                         +" "+pieceDefinition.name+",\n");
            }

            if(messageDefinition.create.contains(Create.JAVA_SERIALIZER)){
                javaWrite("\n        //"+pieceDefinition.comment+"\n"
                          +"        ");
                javaWrite(pieceDefinition.type.toJavaFinalType(this.flagSetDefinitions)
                          +" "+pieceDefinition.name
                          +(i<messageDefinition.pieceDefinitions.length-1
                            ?","
                            :""));
            }
        }

        if(messageDefinition.create.contains(Create.CPP_SERIALIZER))
            hppWrite("    std::vector<char>&outputMessage)\n"
                     +"    throw()\n"
                     +"  {\n"
                     +"    outputMessage.resize(0);\n"
                     +"    //Let the receiver know which "
                     +"protocol version this is:\n"
                     +"    Message::append1Byte("
                     +protocolVersion+",outputMessage);\n"
                     +"    //Let the receiver know what kind "
                     +"of message this is:\n"
                     +"    Message::append1Byte("+messageDefinition.identifier
                     +",outputMessage);\n\n");

        if(messageDefinition.create.contains(Create.JAVA_SERIALIZER))
            javaWrite("){\n"
                      +"    final Vector<Byte> outputMessage\n"
                      +"     = new Vector<Byte>();\n\n"
                      +"    //Let the receiver know which "
                      +"protocol version this is:\n"
                      +"    Message.append1Byte("+protocolVersion
                      +",outputMessage);\n"
                      +"    //Let the receiver know what kind "
                      +"of message this is:\n"
                      +"    Message.append1Byte("+messageDefinition.identifier
                      +",outputMessage);\n\n");

        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){

            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];

            if(messageDefinition.create.contains(Create.CPP_SERIALIZER))
                hppWrite("    //Serialize "+pieceDefinition.name+":\n"
                         +"    Message::"+pieceDefinition.type.getAppender
                         (this.flagSetDefinitions)
                         +"("+pieceDefinition.name+",outputMessage);\n");

            if(messageDefinition.create.contains(Create.JAVA_SERIALIZER))
                javaWrite("    //Serialize "+pieceDefinition.name+":\n"
                          +"    Message."+pieceDefinition.type.getAppender
                          (this.flagSetDefinitions)
                          +"("+pieceDefinition.name+",outputMessage);\n");
        }
            
        if(messageDefinition.create.contains(Create.JAVA_SERIALIZER))
            javaWrite("    return outputMessage;\n"
                      +"  }\n\n");

        if(messageDefinition.create.contains(Create.CPP_SERIALIZER))
            hppWrite("  }\n\n");
        //End of serialization.

        //Deserialization.

        //Generate class for deserialized object:
        if(messageDefinition.create.contains(Create.CPP_DESERIALIZER))
            hppWrite("  class Deserialized_"+this.protocolVersion+"_"
                     +messageDefinition.name+"\n"
                     +"  {\n"
                     +"  public:\n");
        if(messageDefinition.create.contains(Create.JAVA_DESERIALIZER))
            javaWrite("  public static class Deserialized_"+this.protocolVersion+"_"
                      +messageDefinition.name+"{\n");
                
        for(int i=0;i<messageDefinition.pieceDefinitions.length;i++){
                    
            final PieceDefinition pieceDefinition
                = messageDefinition.pieceDefinitions[i];
            
            if(messageDefinition.create.contains(Create.CPP_DESERIALIZER))
                hppWrite("    //"+pieceDefinition.comment+"\n"
                         +"    "
                         +pieceDefinition.type.toCppType(this.flagSetDefinitions)
                         +" "+pieceDefinition.name+";\n");
            if(messageDefinition.create.contains(Create.JAVA_DESERIALIZER))
                javaWrite("    //"+pieceDefinition.comment+"\n"
                          +"    "
                          +"public final "
                          +pieceDefinition.type.toJavaType(this.flagSetDefinitions)
                          +" "+pieceDefinition.name+";\n");
        }

        //C++ class ends here, but Java later.
        if(messageDefinition.create.contains(Create.CPP_DESERIALIZER))
            hppWrite("  };\n\n");

        //Generate deserializer: C++ deserializer takes form
        //of static method, which writes result into
        //pre-existing object and returns false if
        //deserialization failed for performance reasons.
        //Java deserializer takes form of constructor and
        //throws exception if deserialization failed for
        //clarity reasons.
        if(messageDefinition.create.contains(Create.CPP_DESERIALIZER)){

            //Deserialize into one object:
            hppWrite("  static bool deserialize_"
                     +this.protocolVersion+"_"
                     +messageDefinition.name+"(const std::vector<char>&inputMessage"
                     +(messageDefinition.pieceDefinitions.length>0
                       ?",\n        Deserialized_"+this.protocolVersion+"_"
                       +messageDefinition.name
                       +"&output"
                       :"")
                     +")\n"
                     +"  throw()\n"
                     +"  {\n"
                     +"    std::vector<char>::const_iterator it\n"
                     +"     = inputMessage.begin();\n"
                     +"    const std::vector<char>::const_iterator messageEnd\n"
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

            for(PieceDefinition pieceDefinition
                    : messageDefinition.pieceDefinitions){
                hppWrite("    //Deserialize "+pieceDefinition.name+":\n"
                         +"    if(!Message::"
                         +pieceDefinition.type.getReader(this.flagSetDefinitions)
                         +"(it,messageEnd,output."
                         +pieceDefinition.name+"))\n"
                         +"      return false;\n");
            }

            hppWrite("    return true;\n"
                     +"  }\n\n");
            //End deserialize into single object.

            if(messageDefinition.pieceDefinitions.length>0){
                //Deserialize into multiple objects:
                hppWrite("  static bool deserialize_"
                         +this.protocolVersion+"_"
                         +messageDefinition.name
                         +"(const std::vector<char>&inputMessage");

                for(PieceDefinition pieceDefinition
                        :messageDefinition.pieceDefinitions)
                    hppWrite(",\n        "
                             +pieceDefinition.type.toCppType(this.flagSetDefinitions)
                             +"& "+pieceDefinition.name);

                hppWrite
                    (")\n"
                     +"  throw()\n"
                     +"  {\n"
                     +"    std::vector<char>::const_iterator it\n"
                     +"     = inputMessage.begin();\n"
                     +"    const std::vector<char>::const_iterator messageEnd\n"
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
                
                for(PieceDefinition pieceDefinition
                        : messageDefinition.pieceDefinitions){
                    hppWrite("    //Deserialize "+pieceDefinition.name+":\n"
                             +"    if(!Message::"
                             +pieceDefinition.type.getReader(this.flagSetDefinitions)
                             +"(it,messageEnd,"
                             +pieceDefinition.name+"))\n"
                             +"      return false;\n");
                }

                hppWrite("    return true;\n"
                         +"  }\n\n");
                //End deserialize into multiple objects.
            }
        }

        if(messageDefinition.create.contains(Create.JAVA_DESERIALIZER)){
            javaWrite("    public Deserialized_"
                      +this.protocolVersion+"_"
                      +messageDefinition.name+"(final Vector<Byte> inputMessage)\n"
                      +"      throws MessageDeserializationException{\n"
                      +"      try{\n"
                      +"        final Iterator<Byte> iterator\n"
                      +"         = inputMessage.iterator();\n\n"
                      +"        //Check protocol version:\n"
                      +"        if(iterator.next()!="+this.protocolVersion+")\n"
                      +"          throw new MessageDeserializationException();\n\n"
                      +"        //Check kind of message:\n"
                      +"        if(iterator.next()!="
                      +messageDefinition.identifier+")\n"
                      +"          throw new MessageDeserializationException();\n\n");
        
            for(PieceDefinition pieceDefinition
                    : messageDefinition.pieceDefinitions){
                javaWrite("    //Deserialize "+pieceDefinition.name+":\n"
                          +"      this."+pieceDefinition.name
                          +" = Message."
                          +pieceDefinition.type.getReader(this.flagSetDefinitions)
                          +"(iterator);\n");
            }
        
            javaWrite("      }catch(NoSuchElementException e){\n"
                      +"        throw new MessageDeserializationException(e);\n"
                      +"      }\n"
                      +"    }\n"
                      +"  }\n\n");
        }
        //End of deserialization.
    }

    private void hppWriteHandler() throws Exception{
        if(this.messageDefinitions.isEmpty())
            return;
        
        hppWrite("\n"
                 +"class "+protocolName+"Handler\n"
                 +"{\n"
                 +"  private:\n"
                 +"  //Objects for temporary deserialization (to avoid creating\n"
                 +"  //new ones all the time):\n");

        for(MessageDefinition md : this.messageDefinitions){
            if(md.create.contains(Create.CPP_DESERIALIZER)
               &&md.pieceDefinitions.length>0)
                hppWrite("  "+className+"::Deserialized"
                         +"_"+protocolVersion+"_"+md.name
                         +" deserialized_"+md.name+";\n");
        }

        hppWrite("\n"
                 +"public:\n"
                 +"  bool handle(const std::vector<char>& message,\n"
                 +"              const SessionId sessionID,\n"
                 +"              std::list<SessionAddressedMessage>& toBeSent,\n"
                 +"              TimeMicro& timeout) throw();\n"
                 +"\n");

        cppWrite("\n"
                 +"  bool "+protocolName+"Handler::handle(const std::vector<char>& message,\n"
                 +"                  const SessionId sessionID,\n"
                 +"                  std::list<SessionAddressedMessage>& toBeSent\n,"
                 +"                  TimeMicro& timeout) throw()\n"
                 +"  {\n"
                 +"\n"
                 +"    switch("+className+"::getMessageType(message))\n"
                 +"    {\n");

        for(MessageDefinition md : this.messageDefinitions){
            if(md.create.contains(Create.CPP_DESERIALIZER)){
                cppWrite("    case "+className+"::"
                         +md.name+"_"+protocolVersion+":\n"
                         +"      return "+className+"::deserialize_"
                         +protocolVersion+"_"+md.name+"(message");
                if(md.pieceDefinitions.length>0)
                    cppWrite(",\n"
                             +"                              "
                             +"this->deserialized_"+md.name);
                cppWrite(")\n");
                cppWrite("             && this->handle_"+protocolVersion
                         +"_"+md.name+"(sessionID,toBeSent,timeout");
                for(PieceDefinition pd : md.pieceDefinitions)
                    cppWrite(",\n"
                             +"                      "
                             +"this->deserialized_"+md.name+"."+pd.name);
                cppWrite(");\n");
            }
        }
        
        cppWrite("    default:\n"
                 +"      return false;\n"
                 +"    }\n"
                 +"  }\n");

        hppWrite("  //Handlers for various message types:\n");
        for(MessageDefinition md : this.messageDefinitions){
            if(md.create.contains(Create.CPP_DESERIALIZER)){
                hppWrite("  virtual bool handle"+"_"+protocolVersion
                         +"_"+md.name+"(const SessionId sessionID,\n"
                         +"                  "
                         +"std::list<SessionAddressedMessage>& toBeSent,\n"
                         +"                  "
                         +"TimeMicro& timeout");
                for(PieceDefinition pd : md.pieceDefinitions)
                    hppWrite(",\n"
                             +"                  "
                             +pd.type.toCppConstType(this.flagSetDefinitions)
                             +" "+pd.name);

                hppWrite(") throw() =0;\n");
            }
        }

        hppWrite("  virtual ~"+protocolName+"Handler() throw() {}\n");

        hppWrite("};\n");

    }

    private void javaWriteHandler() throws Exception{
        if(this.messageDefinitions.isEmpty())
            return;

        javaWrite("\n"
                  +"  public static boolean handle(final Vector<Byte> message,\n"
                  +"                               final "+protocolName
                  +"Handler handler){\n"
                  +"\n"
                  +"    switch("+className
                  +".lookupMessageType(message))\n"
                  +"    {\n");

        for(MessageDefinition md : this.messageDefinitions){
            if(md.create.contains(Create.JAVA_DESERIALIZER)){
                javaWrite("    case "
                          +md.name+"_"+protocolVersion+":\n"
                          +"      {\n"
                          +"        try{\n"
                          +"          final "+className+".Deserialized_"
                          +protocolVersion+"_"
                          +md.name+" deserialized = \n"
                          +"              new "+className+".Deserialized_"
                          +protocolVersion+"_"+md.name+"(message);\n");
                javaWrite("        return handler.handle_"+protocolVersion
                          +"_"+md.name+"(");
                boolean first = true;
                for(PieceDefinition pd : md.pieceDefinitions){
                    if(!first)
                        javaWrite(",\n"
                                  +"                      ");
                    javaWrite("deserialized."+pd.name);
                    first = false;
                }
                javaWrite(");\n");
                javaWrite("        }catch(final MessageDeserializationException e){\n"
                          +"          return false;\n"
                          +"        }\n"
                          +"    }\n");
            }
        }
        
        javaWrite("    default:\n"
                 +"      return false;\n"
                 +"    }\n"
                 +"  }\n");

        javaWrite("\n"
                 +"public static interface "+protocolName+"Handler\n"
                 +"{\n"
                 +"\n");

        javaWrite("  //Handlers for various message types:\n");
        for(MessageDefinition md : this.messageDefinitions){
            if(md.create.contains(Create.JAVA_DESERIALIZER)){
                javaWrite("  public abstract boolean handle"+"_"+protocolVersion
                         +"_"+md.name+"(");
                boolean first = true;
                for(PieceDefinition pd : md.pieceDefinitions){
                    if(!first)
                        javaWrite(",\n"
                                  +"                  ");
                    javaWrite(pd.type.toJavaFinalType(this.flagSetDefinitions)
                              +" "+pd.name);
                    first = false;
                }
                javaWrite(");\n");
            }
        }

        javaWrite("}\n");

    }

    public void write()
        throws Exception
    {
        this.writeHeader();

        this.writeConstantDefinitions();

        for(FlagSetDefinition flagSetDefinition
                : this.flagSetDefinitions){
            this.writeFlagSetDefinition(flagSetDefinition);
        }

        this.javaWriteLookupMessageType();
        this.hppWriteMessageTypeToString();
        this.hppWriteGetMessageType();
        this.hppWriteMessageToString();

        for(MessageDefinition messageDefinition : this.messageDefinitions){
            this.writeMessageDefinition(messageDefinition);
        }

        this.javaWriteHandler();

        this.writeFooter();

        this.hppWriteHandler();

        this.javaWriter.flush();
        this.javaWriter.close();
        this.hppWriter.flush();
        this.hppWriter.close();
        if(this.cppWriter!=null){
            this.cppWriter.flush();
            this.cppWriter.close();
        }
    }
}
