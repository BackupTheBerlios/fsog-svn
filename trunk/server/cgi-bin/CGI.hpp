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
#include <stdlib.h> //For getenv(...)
#include <exception>
#include <string>
#include <sstream>
#include <map>
#include <string.h>
#include "Problem.hpp"

/**
   Objects of this class will be thrown.
 */
class Parameters
{
  std::map<std::string,std::string> parameters;

public:
  Parameters() throw(std::exception)
  :parameters()
  {
    if(strcmp(getenv("GATEWAY_INTERFACE"),"CGI/1.1")!=0)
      throw Problem(__func__,__FILE__,__LINE__,"Wrong gateway interface.",errno);

    if(strcmp(getenv("REQUEST_METHOD"),"GET")!=0)
      throw Problem(__func__,__FILE__,__LINE__,"Wrong request method.",errno);

    const char*const query = getenv("QUERY_STRING");

    std::istringstream input(query);
    std::string n,v;
    bool ok=true;
    do
      {
        n.clear();
        v.clear();
        std::getline(input,n,'=');
        std::getline(input,v,'&');
        ok=!(n.empty()||v.empty());
        if(ok)
          this->parameters[n]=v;
      }
    while(ok);
  }

  std::string get(const std::string& name)const throw(std::exception)
  {
    const std::map<std::string,std::string>::const_iterator entry
      = parameters.find(name);

    if(entry == parameters.end())
      throw Problem(__func__,__FILE__,__LINE__,
                    std::string("No such parameter: ")+name,errno);

    return entry->second;
  }

  virtual ~Parameters() throw(){}
};
