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


#include "CommandLine.hpp"

bool CommandLine::s_printNetworkPackets=false;
long CommandLine::s_port=false;

bool CommandLine::parse(const int argc,
                        const char*const*const argv)
  {
    s_printNetworkPackets=false;
    s_port=-1;

    for(int i=1;i<argc;i++)
      {
        const std::string option=std::string(argv[i]);
        if(option=="--print-packets")
          s_printNetworkPackets=true;
        else if(option=="-p")
          {
            //Interpret as port.
            i++;
            if(i>=argc)
              {
                std::cerr<<"Missing argument for -p option."<<std::endl;
                return false;
              }
            std::istringstream stream(argv[i]);
            if(!(stream>>s_port))
              {
                std::cerr<<"Incorrect argument for -p option."<<std::endl;
                return false;
              }
          }
        else
          {
            std::cerr<<"Unrecognized option: "<<option<<std::endl;
            return false;
          }
      }
    //Check mandatory options:
    if(s_port==-1)
      {
        std::cerr<<"Missing mandatory -p option."<<std::endl;
        return false;
      }

    return true;
  }
