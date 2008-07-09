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

class Message
{
private:

  //Appends value to message
  template<class T>
  static void appendInteger(const T value,
                            const unsigned numberOfBytes,
                            std::vector<char>&message)
    throw()
  {
    for(unsigned i=0;i<numberOfBytes;i++)
      {
        message.push_back(static_cast<char>
                          (0x00FF & (value>>(8*(numberOfBytes-1-i)))));
      }
  }

  //Read value from message
  template<class T>
  static bool readNBytes(std::vector<char>::const_iterator&it,
                         const std::vector<char>::const_iterator&messageEnd,
                         const char n,
                         T&result)
    throw()
  {
    if(it+n>messageEnd)
      return false;

    result=static_cast<T>(0);
    T tempByte;
    const T mask = static_cast<T>(0x00FF);

    for(char i=8*(n-1);i>=0;i-=8)
      {
        tempByte = (static_cast<T>(*it++)) & mask;
        result|=(tempByte<<i);
      }
    return true;
  }

public:
  template<class T>
  static void append1Byte(const T value,
                          std::vector<char>&message)
    throw()
  {
    appendInteger(value,1,message);
  }

  template<class T>
  static void append2Bytes(const T value,
                           std::vector<char>&message)
    throw()
  {
    appendInteger(value,2,message);
  }

  template<class T>
  static void append3Bytes(const T value,
                           std::vector<char>&message)
    throw()
  {
    appendInteger(value,3,message);
  }

  template<class T>
  static void append4Bytes(const T value,
                           std::vector<char>&message)
    throw()
  {
    appendInteger(value,4,message);
  }

  template<class T>
  static void append8Bytes(const T value,
                           std::vector<char>&message)
    throw()
  {
    appendInteger(value,8,message);
  }

  static void appendCString(const std::string& value,
                            std::vector<char>&message)
    throw()
  {
    message.insert(message.end(),
                   value.begin(),
                   value.end());
    message.push_back(0);
  }

  static void appendBinary(const std::vector<char>& value,
                           std::vector<char>&message)
    throw()
  {
    //TODO: handle error if value.size()>0x7FFF
    message.reserve(message.size()+2+value.size());
    Message::append2Bytes(value.size(),message);

    message.insert(message.end(),
                   value.begin(),
                   value.end());
  }

  static void appendVector(const std::vector<int8_t>& v,
                           std::vector<char>&message)
    throw()
  {
    //TODO: handle error if value.size()>0x7FFF
    message.reserve(message.size()+2+v.size());
    Message::append2Bytes(v.size(),message);

    for(std::vector<int8_t>::const_iterator it
          = v.begin();
        it != v.end();
        it++)
      Message::append1Byte(*it,message);
  }

  //Read value from message
  template<class T>
  static bool read1Byte(std::vector<char>::const_iterator&it,
                        const std::vector<char>::const_iterator&messageEnd,
                        T&result)
    throw()
  {
    return Message::readNBytes(it,messageEnd,1,result);
  }

  //Read value from message
  template<class T>
  static bool read2Bytes(std::vector<char>::const_iterator&it,
                         const std::vector<char>::const_iterator&messageEnd,
                         T&result)
    throw()
  {
    return Message::readNBytes(it,messageEnd,2,result);
  }

  //Read value from message
  template<class T>
  static bool read3Bytes(std::vector<char>::const_iterator&it,
                         const std::vector<char>::const_iterator&messageEnd,
                         T&result)
    throw()
  {
    return Message::readNBytes(it,messageEnd,3,result);
  }

  //Read value from message
  template<class T>
  static bool read4Bytes(std::vector<char>::const_iterator&it,
                         const std::vector<char>::const_iterator&messageEnd,
                         T&result)
    throw()
  {
    return Message::readNBytes(it,messageEnd,4,result);
  }

  template<class T>
  static bool read8Bytes(std::vector<char>::const_iterator&it,
                         const std::vector<char>::const_iterator&messageEnd,
                         T&result)
    throw()
  {
    return Message::readNBytes(it,messageEnd,8,result);
  }

  static bool readCString(std::vector<char>::const_iterator&it,
                          const std::vector<char>::const_iterator&messageEnd,
                          std::string&result)
  {
    std::ostringstream output;
    char c;
    while(true)
      {
        if(!Message::read1Byte(it,messageEnd,c))
          return false;
        if(c==0)
          {
            result=output.str();
            return true;
          }
        output<<c;
      }
  }
  
  static bool readBinary(std::vector<char>::const_iterator&it,
                         const std::vector<char>::const_iterator&messageEnd,
                         std::vector<char>&result)
  {
    int_fast16_t length;
    if(!Message::read2Bytes(it,messageEnd,length)
       || length<0)
      return false;

    if(it+length>messageEnd)
      return false;

    result.resize(0);
    result.insert(result.end(),
                  it,
                  it+length);

    return true;
  }

  static bool readVector(std::vector<char>::const_iterator&it,
                         const std::vector<char>::const_iterator&messageEnd,
                         std::vector<int8_t>&result)
  {
    //TODO: All methods should resemble this one for its nice calls to
    //simpler methods.
    
    int_fast16_t length;
    if(!Message::read2Bytes(it,messageEnd,length)
       || length<0)
      return false;

    result.reserve(length);

    int8_t temp;
    for(int_fast16_t i=0; i<length; i++)
      {
        if(!Message::read1Byte(it,messageEnd,temp))
          return false;
        result.push_back(temp);
      }
    return true;
  }
};
