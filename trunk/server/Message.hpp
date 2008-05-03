/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- 
 * vim:expandtab:shiftwidth=2:tabstop=2: */


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

/**
   This class is used for sending messages between client and
   server. It's just a binary piece of data. It has methods for
   putting primitive data into it and reading it back. When you
   generate a protocol using ProtocolDefinition.java, it will have
   serializers and deserializers that use this class as output and
   input, respectively.
*/

class Message : public std::vector<char>
{
private:
  //Appends value to message
  template<class T>
  void appendInteger(const T value,
                     const unsigned numberOfBytes)
    throw()
  {
    for(unsigned i=0;i<numberOfBytes;i++)
      {
        this->push_back(static_cast<char>(0xFF & (value>>(8*(numberOfBytes-1-i)))));
      }
  }

public:
  template<class T>
  void append1Byte(const T value)
    throw()
  {
    this->appendInteger(value,1);
  }

  template<class T>
  void append2Bytes(const T value)
    throw()
  {
    this->appendInteger(value,2);
  }

  template<class T>
  void append3Bytes(const T value)
    throw()
  {
    this->appendInteger(value,3);
  }

  template<class T>
  void append4Bytes(const T value)
    throw()
  {
    this->appendInteger(value,4);
  }

  void appendCString(const std::string& value)
    throw()
  {
    this->insert(this->end(),
                 value.begin(),
                 value.end());
    this->push_back(0);
  }

  //Read value from this message
  template<class T>
  static bool read1Byte(Message::const_iterator&it,
                        const Message::const_iterator&messageEnd,
                        T&result)
    throw()
  {
    if(it+1>messageEnd)
      return false;

    result=(*it++);
    
    return true;
  }

  //Read value from this message
  template<class T>
  static bool read2Bytes(Message::const_iterator&it,
                         const Message::const_iterator&messageEnd,
                         T&result)
    throw()
  {
    if(it+2>messageEnd)
      return false;

    result=0;

    result|=(static_cast<short>(*it++))<<8;
    result|=(static_cast<short>(*it++));
    
    return true;
  }

  //Read value from this message
  template<class T>
  static bool read3Bytes(Message::const_iterator&it,
                         const Message::const_iterator&messageEnd,
                         T&result)
    throw()
  {
    if(it+3>messageEnd)
      return false;
    
    result=0;

    result|=(static_cast<short>(*it++))<<16;
    result|=(static_cast<short>(*it++))<<8;
    result|=(static_cast<short>(*it++));
    
    return true;
  }

  //Read value from this message
  template<class T>
  static bool read4Bytes(Message::const_iterator&it,
                         const Message::const_iterator&messageEnd,
                         T&result)
    throw()
  {
    if(it+4>messageEnd)
      return false;

    result=0;

    result|=(static_cast<short>(*it++))<<24;
    result|=(static_cast<short>(*it++))<<16;
    result|=(static_cast<short>(*it++))<<8;
    result|=(static_cast<short>(*it++));
    
    return true;
  }

  static bool readCString(Message::const_iterator&it,
                          const Message::const_iterator&messageEnd,
                          std::string&result)
  {
    std::ostringstream output;
    char c;
    while(true)
      {
        if(it==messageEnd)
          return false;
        c=(*it++);
        if(c==0)
          {
            result=output.str();
            return true;
          }
        output<<c;
      }
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
  int8_t getMessageType() const throw()
  {
    const Message& message = *this;
    if(message.size()<2)
      return 0;
    
    return message[1];
  }

};
