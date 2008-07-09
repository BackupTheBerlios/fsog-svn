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

#include <stdint.h>

//Used in TurnGame to identify players.
//First player - 0, second - 1, third - 2, etc...
typedef int8_t Player;

/** Values representing what effect a move has on the game. This is
    specified as a bit flag, where the bits look as follows:
    
    ...more significant bits... | VALIDITY_BIT | CONTINUITY_BIT
*/
typedef uint_fast8_t MoveResult;


//Bit mask for specifying whether the game shall continue or finish.
static const MoveResult CONTINUITY_MASK = 0x01;
static const MoveResult CONTINUE = 0x00;
static const MoveResult END = 0x01;

//Bit mask for specifying whether the move sent was valid.
static const MoveResult VALIDITY_MASK = 0x02;
static const MoveResult VALID = 0x00;
static const MoveResult INVALID = 0x02;



//Secret for joining tables.
typedef int64_t TableId;

/**
   0 is not a valid TablePlayerId. It starts at 1.
   
*/
typedef int8_t TablePlayerId;
  
typedef int32_t SessionId;
