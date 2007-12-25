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


#include "Thousand.hpp"
#include "Protocol.hpp"

std::vector<char> Thousand::deck = createDeck();

Thousand::Thousand(const char numberOfPlayers,
                   const int32_t flags) throw()
  :flags(Thousand::purifyFlags(flags)),
   state(WAITING_FOR_START),
   numberOfPlayers(numberOfPlayers),
   players(numberOfPlayers),
   //Choose the dealer:
   dealer(std::rand()%numberOfPlayers)
{
  //Reserve size for vectors:
  this->players.reserve(numberOfPlayers);
  switch(numberOfPlayers)
    {
    case 2:
      this->must.reserve(0);
      this->smallMust1.reserve(2);
      this->smallMust2.reserve(2);
      break;
    case 3:
    case 4:
      this->must.reserve(3);
      this->smallMust1.reserve(0);
      this->smallMust2.reserve(0);
      break;
    default:
      break;
    }
}

void Thousand::deal() throw()
{
  //Shuffle the global deck:
  std::random_shuffle(Thousand::deck.begin(),
                      Thousand::deck.end());

  //Now we will copy cards from the shuffled deck.

  std::vector<char>::const_iterator deckBegin
    = Thousand::deck.begin();

  switch(this->numberOfPlayers)
    {
      case 4:
        {
          const char playing0 = (this->dealer+1)%4;
          const char playing1 = (this->dealer+2)%4;
          const char playing2 = (this->dealer+3)%4;
          
          this->players[playing0]->cards.resize(0);
          this->players[playing0]->cards.insert
            (this->players[playing0]->cards.end(),
             deckBegin,
             deckBegin+7);
          
          this->players[playing1]->cards.resize(0);
          this->players[playing1]->cards.insert
            (this->players[playing1]->cards.end(),
             deckBegin+7,
             deckBegin+14);
          
          this->players[playing2]->cards.resize(0);
          this->players[playing2]->cards.insert
            (this->players[playing2]->cards.end(),
             deckBegin+14,
             deckBegin+21);
          
          this->must.resize(0);
          this->must.insert(this->must.end(),
                            deckBegin+21,
                            deckBegin+24);
          break;
        }
    case 3:
      this->players[0]->cards.resize(0);
      this->players[0]->cards.insert
        (this->players[0]->cards.end(),
         deckBegin,
         deckBegin+7);
      
      this->players[1]->cards.resize(0);
      this->players[1]->cards.insert
        (this->players[1]->cards.end(),
         deckBegin+7,
         deckBegin+14);
      
      this->players[2]->cards.resize(0);
      this->players[2]->cards.insert
        (this->players[2]->cards.end(),
         deckBegin+14,
         deckBegin+21);
      
      this->must.resize(0);
      this->must.insert(this->must.end(),
                        deckBegin+21,
                        deckBegin+24);
      break;
      
    case 2:

      //Two players:
      this->players[0]->cards.resize(0);
      this->players[0]->cards.insert(this->players[0]->cards.end(),
                                    deckBegin,
                                    deckBegin+10);
      this->players[1]->cards.resize(0);
      this->players[1]->cards.insert(this->players[1]->cards.end(),
                                    deckBegin+10,
                                    deckBegin+20);
      this->smallMust1.resize(0);
      this->smallMust1.insert(this->smallMust1.end(),
                              deckBegin+20,
                              deckBegin+22);
      this->smallMust2.resize(0);
      this->smallMust2.insert(this->smallMust2.end(),
                              deckBegin+22,
                              deckBegin+24);
      break;
    default:
      //TODO Unknown number of players.
      break;
    }
}

std::vector<char> Thousand::createDeck() throw()
{
  std::vector<char> result;

  result.push_back(Protocol::ACE|Protocol::HEARTS);
  result.push_back(Protocol::TEN|Protocol::HEARTS);
  result.push_back(Protocol::KING|Protocol::HEARTS);
  result.push_back(Protocol::QUEEN|Protocol::HEARTS);
  result.push_back(Protocol::JACK|Protocol::HEARTS);
  result.push_back(Protocol::NINE|Protocol::HEARTS);

  result.push_back(Protocol::ACE|Protocol::DIAMONDS);
  result.push_back(Protocol::TEN|Protocol::DIAMONDS);
  result.push_back(Protocol::KING|Protocol::DIAMONDS);
  result.push_back(Protocol::QUEEN|Protocol::DIAMONDS);
  result.push_back(Protocol::JACK|Protocol::DIAMONDS);
  result.push_back(Protocol::NINE|Protocol::DIAMONDS);

  result.push_back(Protocol::ACE|Protocol::CLUBS);
  result.push_back(Protocol::TEN|Protocol::CLUBS);
  result.push_back(Protocol::KING|Protocol::CLUBS);
  result.push_back(Protocol::QUEEN|Protocol::CLUBS);
  result.push_back(Protocol::JACK|Protocol::CLUBS);
  result.push_back(Protocol::NINE|Protocol::CLUBS);

  result.push_back(Protocol::ACE|Protocol::SPADES);
  result.push_back(Protocol::TEN|Protocol::SPADES);
  result.push_back(Protocol::KING|Protocol::SPADES);
  result.push_back(Protocol::QUEEN|Protocol::SPADES);
  result.push_back(Protocol::JACK|Protocol::SPADES);
  result.push_back(Protocol::NINE|Protocol::SPADES);

  return result;
}

int32_t Thousand::purifyFlags(const int32_t flags)
  throw()
{
  int32_t result = 0;

  //Bombs:
  if(flags&Protocol::NO_BOMBS)
    result|=Protocol::NO_BOMBS;
  else if(flags&Protocol::BOMBS)
    result|=Protocol::BOMBS;
  else
    result|=Protocol::UNLIMITED_BOMBS;

  
  if(flags&Protocol::BID_INCREMENT_10)
    result|=Protocol::BID_INCREMENT_10;
  else
    result|=Protocol::BID_INCREMENT_ANY;

  if(flags&Protocol::MUST_PLAY_FROM_800)
    result|=Protocol::MUST_PLAY_FROM_800;
  else
    result|=Protocol::MUST_PLAY_FROM_900;

  if(flags&Protocol::SHOW_MUST_100)
    result|=Protocol::SHOW_MUST_100;
  else
    result|=Protocol::SHOW_MUST_110;

  if(flags&Protocol::PUBLIC)
    result|=Protocol::PUBLIC;
  else
    result|=Protocol::PRIVATE;


  if(flags&Protocol::RANKING_GAME)
    result|=Protocol::RANKING_GAME;
  else
    result|=Protocol::SPARRING_GAME;


  if(flags&Protocol::TIME_7)
    result|=Protocol::TIME_7;
  else if(flags&Protocol::TIME_10)
    result|=Protocol::TIME_10;
  else if(flags&Protocol::TIME_15)
    result|=Protocol::TIME_15;
  else if(flags&Protocol::TIME_20)
    result|=Protocol::TIME_20;
  else
    result|=Protocol::TIME_30;

  return result;
}
