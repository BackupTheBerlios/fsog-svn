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
#include <exception>
#include <string>
#include <sstream>

/**
   Objects of this class will be thrown.
 */
class Problem : public std::exception
{
  const std::string message;
  virtual const char* what() const throw()
  {
    return message.c_str();
  }

  static std::string make_message(const char*const func,
                                  const char*const file,
                                  const int line,
                                  const std::string& description,
                                  const int _errno)
  {
    std::ostringstream o;
    o<<"ERROR: In "<<func<<' '<<file<<':'<<line;
    if(_errno!=0)
      o<<" [errno "<<_errno<<": "<<strerror(_errno)<<']';
    o<<": "<<description;
    return o.str();
  }

public:
  Problem(const char*const func,
          const char*const file,
          const int line,
          const std::string& description,
          const int _errno) throw()
    :message(make_message(func,file,line,description,_errno))
  {}

  virtual ~Problem() throw(){}
};
