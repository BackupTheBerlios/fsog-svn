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
#include <set>
#include <iostream>
#include "CommandLine.hpp"

std::vector<int8_t> Thousand::deck;

void Thousand::deal() throw()
{
  if(Thousand::deck.empty())
    for(int8_t shift=0;shift<24;shift++)
      Thousand::deck.push_back(shift);

  //Randomize global deck:
  std::random_shuffle(Thousand::deck.begin(),
                      Thousand::deck.end());

  //First three cards for must:
  must.setEmpty();
  for(uint_fast8_t i=0;i<3;i++)
    must.addShift(deck[i]);

  cards[0].setEmpty();
  for(uint_fast8_t i=3;i<10;i++)
    cards[0].addShift(deck[i]);
  
  cards[1].setEmpty();
  for(uint_fast8_t i=10;i<17;i++)
    cards[1].addShift(deck[i]);

  cards[2].setEmpty();
  for(uint_fast8_t i=17;i<24;i++)
    cards[2].addShift(deck[i]); 
}

int8_t Thousand::maxBid10() const throw()
{
  const int8_t absoluteMaximum = 12+10+8+6+4;
  const ThousandCardSet& set = cards[turn];
  //TODO: precompute sets and check intersection.
  if(set.containsShift(ThousandProtocol::QUEEN_SHIFT
                       +ThousandProtocol::HEART_SHIFT)
     && set.containsShift(ThousandProtocol::KING_SHIFT
                          +ThousandProtocol::HEART_SHIFT))
    return absoluteMaximum;
  if(set.containsShift(ThousandProtocol::QUEEN_SHIFT
                       +ThousandProtocol::DIAMOND_SHIFT)
     && set.containsShift(ThousandProtocol::KING_SHIFT
                          +ThousandProtocol::DIAMOND_SHIFT))
    return absoluteMaximum;
  if(set.containsShift(ThousandProtocol::QUEEN_SHIFT
                       +ThousandProtocol::CLUB_SHIFT)
     && set.containsShift(ThousandProtocol::KING_SHIFT
                          +ThousandProtocol::CLUB_SHIFT))
    return absoluteMaximum;
  if(set.containsShift(ThousandProtocol::QUEEN_SHIFT
                       +ThousandProtocol::SPADE_SHIFT)
     && set.containsShift(ThousandProtocol::KING_SHIFT
                          +ThousandProtocol::SPADE_SHIFT))
    return absoluteMaximum;
  return 12;
}


bool Thousand::initialize(std::list<PlayerAddressedMessage>& messages) throw()
{
  //Initialization fails if table was created not for 3 players:
  if(numberOfPlayers!=3)
    return false;

  //We expect move only from the first player:
  setFirstPlayer();

  deal();

  bids10.resize(3,10);
  minimumNextBid10=11;

  //Send messages to clients with info about what's their cards:
  messages.push_back(PlayerAddressedMessage(0));
  ThousandProtocol::serialize_1_DEAL_7_CARDS(cards[0].getValue(),
                                             messages.back().message);
  
  messages.push_back(PlayerAddressedMessage(1));
  ThousandProtocol::serialize_1_DEAL_7_CARDS(cards[1].getValue(),
                                             messages.back().message);

  messages.push_back(PlayerAddressedMessage(2));
  ThousandProtocol::serialize_1_DEAL_7_CARDS(cards[2].getValue(),
                                             messages.back().message);

  //Ready for playing! After this function returns server will send
  //initial information to clients and will await move from the
  //players specified.
  return true;
}

MoveResult Thousand::move(const std::vector<char>& move,
                          std::list<PlayerAddressedMessage>& moveMessages,
                          std::list< std::set<Player> >& endResult)
  throw()
{
  if(CommandLine::printNetworkPackets())
    std::cout<<"T::MV "
             <<ThousandProtocol::messageToString(move);

  //Are we still biding?
  if(stage==BIDDING)
    {
      int8_t bid10;
      //First we deserialize the move. If deserialization fails, current
      //player sent invalid move.
      if(!ThousandProtocol::deserialize_1_BID(move,bid10))
        {
          std::cout<<"deserialize_1_BID failed."
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      //Let's see whether the move is valid:
      if(bid10>maxBid10() || (bid10!=0 && bid10<minimumNextBid10))
        {
          std::cout<<"Incorrect bid: bid10=="<<static_cast<int>(bid10)
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      
      //OK, move is valid. Let's introduce game state change.

      bids10[turn]=bid10;
      if(bid10!=0)
        minimumNextBid10=bid10+1;
    
      //Shall bidding end? Yes if two passes.
      const Player winner
        =((bids10[0]==0 && bids10[1]==0)?2
          :((bids10[1]==0 && bids10[2]==0)?0
            :((bids10[2]==0 && bids10[0]==0)?1
              :-1)));

      if(winner!=-1)
        {//Bidding is over.
          //Add must cards to the person with highest bid:
          cards[winner].addAll(must);
          std::vector<char> message;
          //Shall we show must?
          if(bids10[winner]>10)
            {//We show must.
              ThousandProtocol::serialize_1_BID_END_SHOW_MUST(must.getValue(),
                                                              message);
              sendToAll(message,moveMessages);
            }
          else
            {//We don't show must.
              ThousandProtocol::serialize_1_BID_END_HIDDEN_MUST(message);
              sendToOthers(message,moveMessages);
              moveMessages.push_back(PlayerAddressedMessage(winner));
              ThousandProtocol::serialize_1_BID_END_SHOW_MUST
                (must.getValue(),moveMessages.back().message);
            }
          turn = winner;
          stage = SELECTING_FIRST;
        }
      else
        {//Bidding is not over yet.
          //Send a message to all other people:
          sendToOthers(move,moveMessages);
          
          //Game shall continue.
          //Skip one player if passed.
          if(bids10[getNextPlayer()]!=0)
            setNextPlayer();
          else
            setNextPlayer(2);
          return VALID|CONTINUE;
        }
    }
  else if(stage==SELECTING_FIRST)
    {
      int8_t shift;
      if(!ThousandProtocol::deserialize_1_SELECT(move,shift))
        {
          std::cout<<"deserialize_1_SELECT failed."
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      if(!cards[turn].containsShift(shift))
        {
          std::cout<<"!cards[turn].containsShift(shift)"
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      
      //Pass the selected card to first opponent:
      cards[turn].removeShift(shift);
      cards[getNextPlayer()].addShift(shift);
      //Let the receiver of the card know which is it:
      moveMessages.push_back(PlayerAddressedMessage(getNextPlayer()));
      moveMessages.back().message = move;
      //Let the other player know some (unknown for her) card was passed:
      moveMessages.push_back(PlayerAddressedMessage(getNextPlayer(2)));
      ThousandProtocol::serialize_1_SELECT_HIDDEN
        (moveMessages.back().message);
      
      stage = SELECTING_SECOND;
      //It's the same player making the move again.
      return VALID|CONTINUE;
    }
  else if(stage==SELECTING_SECOND)
    {
      int8_t shift;
      if(!ThousandProtocol::deserialize_1_SELECT(move,shift))
        {
          std::cout<<"deserialize_1_SELECT failed."
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      if(!cards[turn].containsShift(shift))
        {
          std::cout<<"!cards[turn].containsShift(shift)"
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      
      //Pass the selected card to second opponent:
      cards[turn].removeShift(shift);
      cards[getNextPlayer(2)].addShift(shift);
      //Let the receiver of the card know which is it:
      moveMessages.push_back(PlayerAddressedMessage(getNextPlayer(2)));
      moveMessages.back().message = move;
      //Let the other player know some (unknown for her) card was passed:
      moveMessages.push_back(PlayerAddressedMessage(getNextPlayer()));
      ThousandProtocol::serialize_1_SELECT_HIDDEN
        (moveMessages.back().message);
      
      stage = CONTRACTING;
      //It's the same player making the move again.
      return VALID|CONTINUE;
    }
  else if(stage==CONTRACTING)
    {
      int8_t contract10;
      if(!ThousandProtocol::deserialize_1_CONTRACT(move,contract10))
        {
          std::cout<<"deserialize_1_CONTRACT failed."
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      if(contract10>maxBid10() || contract10<bids10[turn])
        {
          std::cout<<"Incorrect contract10: contract10=="
                   <<static_cast<int>(contract10)
                   <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
          return INVALID|END;
        }
      
      sendToOthers(move,moveMessages);
      
      stage = PLAYING_FIRST;
      //It's the same player making the move again.
      return VALID|CONTINUE;
    }
  else
    {
      std::cout<<"Incorrect stage: "<<static_cast<int>(stage)
               <<" @"<<__func__<<"@"<<__FILE__<<":"<<__LINE__<<std::endl;
      return INVALID|END;
    }
}
