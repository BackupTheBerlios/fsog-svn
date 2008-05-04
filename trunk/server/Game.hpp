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

#include <list>
#include <string>
#include <vector>
#include <set>
#include "Message.hpp" //For Message class

/** Some thread will create an object of a class deriving from
    Game. The IO model used for networking, as well as socket details
    are not visible from within Game.
*/

class Game
{
public:
  
  typedef int32_t Identifier;

  //TODO: Does the game need to know the nicks? Probably no...
  std::vector<std::string> nicks;
  uint16_t currentPlayerNumber;

  /** Can be called from derived classes to shuffle order of
      "nicks". Also sets "currentPlayerNumber" to 0.
   */
  void shuffleNicks() throw();

  /** Can be called from derived classes to change
      "currentPlayerNumber". It simply does "currentPlayerNumber =
      (currentPlayerNumber + 1) % nicks.size()".
   */
  void nextPlayer() throw();

  Game() throw() {};
  virtual ~Game() throw() {};

  /** After all the players have joined the game, server will call
      "initialize(...)". A game should become initialized, so that the
      first player can make a move. "initialize(...)" can,
      e.g. randomize the number of players, deal cards, set pieces on
      chessboard, etc. Before server calls "initialize(...)", "nicks"
      contains the nicks of players in the order they joined. After
      the function is called, the server will send "nicks" to clients,
      so they know in what order they play. If some additional
      information needs to be sent to the players, e.g. what cards
      they got, it can be put in "initialMessages". The size of
      "initialMessages" is at least that of "nicks". "initialize(...)"
      should put additional information for "nick[i]" in
      "initialMessages[i]". "initialize(...)" shall not resize
      "initialMessages". First "nicks.size()" elements of
      "initialMessages" are empty messages before "initialize(...)" is
      called. After server calls "initialize(...)", it awaits for the
      move of the first player.

      "initialize(...)" shall return "true" if game can be started
      normally and "false" if game cannot be started for some reason.
  */
  virtual bool initialize(std::vector<Message>& initialMessages) throw() =0;

  /** Values representing what effect a move has on the game. Values
      >=0 mean that a move is invalid. In such a case the value says
      which player sent an invalid move. Negative values are reserved
      for valid moves. See constants "CONTINUE" and "END". 
  */
  typedef int16_t MoveResult;

  //Move is valid and the game is going on
  static const MoveResult CONTINUE = -1;
  //Move is valid and the game is finished
  static const MoveResult END = -2;

};

class AlternatingTurnGame : public Game
{  

  /** Move of each player is described  as a bit vector. Each game has
     to define  how moves are encoded  within such a  vector.  A valid
     move is  a move which  is correct according  to the rules  of the
     game  and  current settings  and  game  state.   Observe that  in
     "literaki"  game, placing non--existing  word on  the board  is a
     valid move, because  one can do it according to  the rules of the
     game. It is  just not a very  good move, but a valid  one. On the
     other hand  moving a pawn 5 fields  away in chess is  not a valid
     move.
  */

  /** Very often server awaits for a move from a player. When move
      information arrives, "move(...)" is called. "move(...)" shall
      verify whether "move" is a valid move. If it's not, "move(...)"
      shall return "MoveResult::INVALID". If it is, "move(...)" shall
      update the game state to reflect that a move was made and return
      "MoveResult::CONTINUE" or "MoveResult::END".  After the function
      is called, the server will send "moveMessages" to clients, so
      they know what happened. All information that needs to be sent
      to players, e.g. what cards was played, shall be put in
      "moveMessages". Observe that sometimes diferent information is
      sent to different players, e.g. in the game Thousand, a player
      gives a card to another player, so that the third player doesn't
      see what card it is.  The size of "moveMessages" is at least
      that of "nicks". "move(...)"  shall put information for
      "nick[i]" in "moveMessages[i]". "move(...)" shall not resize
      "moveMessages". First "nicks.size()" elements of "moveMessages"
      are empty messages before "move(...)" is called.

      The last parameter is only used when the game is finished,
      i.e. when "move(...)" returns "END". In such a case "move(...)"
      should fill "endResult" with nicks of players according to what
      places they got in the game. Let's assume that Superman, Batman,
      Spiderman and Flash played a 4--player game. Let's say that
      Batman won and got 113 points, Superman and Flash took both
      second place, gathering 72 points each, and Spiderman was last
      with 64 points. In such a case, "move(...)" should set
      "endResult" to look like:
      [{Batman},{Superman,Flash},{Spiderman}].

      "endResult" is empty before "move(...)" is called. "move(...)"
      can only modify "endResult" if it returns "END". If "move(...)"
      returns anything other than "END", "endResult" should be left
      empty.

      "move(...)" has to take care of changing "currentPlayerNumber"
      if it returns "CONTINUE". After "move(...)" returns "CONTINUE",
      server will expect a move from
      "nicks[currentPlayerNumber]". "currentPlayerNumber" has to
      remain in the interval <0,nicks.size()-1>.
  */
  virtual MoveResult move(const Message& move,
                          std::vector<Message>& moveMessages,
                          std::list< std::set<int16_t> >& endResult)
    throw() =0;

};
