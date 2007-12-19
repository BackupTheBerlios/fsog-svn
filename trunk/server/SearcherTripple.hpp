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


#include "Protocol.hpp"

/**
   SearcherTripple represents 3 people searching for a game.
   The intersection of searching cryteria of all 3 people is
   remembered.
*/

class SearcherTripple
{
public:
  SearcherTripple(const SearcherPair&pair,
                  const std::map<Address,User>::iterator newUser,
                  const Protocol::Deserialized_1_SEARCH_GAME&cryteria)
    :user0(pair.user0),
     user1(pair.user1),
     user2(newUser),
     cryteria(cryteria)
  {}

  //TODO maybe reference to SearcherPair, so we don't need 3, but 2.
  const std::map<Address,User>::iterator user0;
  const std::map<Address,User>::iterator user1;
  const std::map<Address,User>::iterator user2;
  const Protocol::Deserialized_1_SEARCH_GAME cryteria;
};
