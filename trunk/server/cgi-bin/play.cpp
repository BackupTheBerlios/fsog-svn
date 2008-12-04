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
#include "CGI.hpp"

void print_cgi_header() throw()
{
  std::cout<<"Content-type: text/html"<<std::endl<<std::endl;
}

void validate(const Parameters& parameters) throw(std::exception)
{
  //if(!(game=="k"||game=="ttt"))
  //TODO: Finish.
}

void print_html(const Parameters& parameters) throw(std::exception)
{
  std::cout
    <<"<html>"<<std::endl
    <<"  <head>"<<std::endl
    <<"    <title>FSOG - Thousand</title>"<<std::endl
    <<"  </head>"<<std::endl
    <<"  <body>"<<std::endl
    <<"    <applet codebase=\"http://fsog.org/thousand/\" code=\"JTableApplet\" archive=\"table.jar\" width=\"950\" height=\"700\">"<<std::endl
    <<"      <param name=\"p\" value=\""<<parameters.get("p")<<"\">"<<std::endl
    <<"      <param name=\"t\" value=\""<<parameters.get("t")<<"\">"<<std::endl
    <<"      <param name=\"n\" value=\""<<parameters.get("n")<<"\">"<<std::endl
    <<"      Your browser is completely ignoring the &lt;APPLET&gt; tag!"<<std::endl
    <<"    </applet>"<<std::endl
    <<"  </body>"<<std::endl
    <<"</html>"<<std::endl;
}

int main()//const int argc, const char*const*const argv)
{
  try
    {
      const Parameters parameters;

      print_cgi_header();
      print_html(parameters);
    }
  catch(std::exception& e)
    {
      std::cerr<< "Exception: " << e.what() << std::endl;
      return -1;
    }
  
  return 0;
}
