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

//TODO: Minimize libraries.
#include <unistd.h>
#include <errno.h>
#include <iostream>
#include <vector>
#include <string>
#include <sstream>
#include "GeneralProtocol.hpp"

int main(const int argc, const char*const*const argv)
{
  std::cout
    <<"Content-type: text/html"<<std::endl
    <<std::endl;

  std::cout
    <<"<html>"<<std::endl
    <<"  <head>"<<std::endl
    <<"    <title>FSOG - Echo</title>"<<std::endl
    <<"  </head>"<<std::endl
    <<"  <body>"<<std::endl;

  std::cout
    <<"    <p>Script got "<<argc<<" arguments. These are:</p><ol>";
  for(int i=0;i<argc;i++)
    std::cout<<"<li>"<<argv[i]<<"</li>"<<std::endl;
  std::cout
    <<"    </ol><p>Environment:<ul>";
  
  for(int i=0;environ[i]!=0;i++)
    std::cout<<"<li>"<<environ[i]<<"</li>"<<std::endl;

  std::cout
    <<"</ul><p>Standard input follows:</p><pre>";

  char c;
  while(std::cin.good())
    {
      std::cin.get(c);
      std::cout<<'\''<<c<<'\''<<' '<<static_cast<int>(c)<<' ';
    }

  std::cout
    <<"</pre></body>"<<std::endl
    <<"</html>"<<std::endl;

  return 0;
}
