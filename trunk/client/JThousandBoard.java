/* -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 * vim:expandtab:shiftwidth=4:tabstop=4: */


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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.font.*;

public class JThousandBoard
    extends JCardBoard{
    
    private static enum Stage
    {
      BIDDING,
      SELECTING_FIRST,
      SELECTING_SECOND,
      CONTRACTING,
      PLAYING_FIRST,
      PLAYING_SECOND,
      PLAYING_THIRD,
      ENDED
    };

    private static byte[] THOUSAND_SHIFT_TO_CARD = {
        Card.NINE|Card.SPADE,
        Card.JACK|Card.SPADE,
        Card.QUEEN|Card.SPADE,
        Card.KING|Card.SPADE,
        Card.TEN|Card.SPADE,
        Card.ACE|Card.SPADE,
        Card.NINE|Card.CLUB,
        Card.JACK|Card.CLUB,
        Card.QUEEN|Card.CLUB,
        Card.KING|Card.CLUB,
        Card.TEN|Card.CLUB,
        Card.ACE|Card.CLUB,
        Card.NINE|Card.DIAMOND,
        Card.JACK|Card.DIAMOND,
        Card.QUEEN|Card.DIAMOND,
        Card.KING|Card.DIAMOND,
        Card.TEN|Card.DIAMOND,
        Card.ACE|Card.DIAMOND,
        Card.NINE|Card.HEART,
        Card.JACK|Card.HEART,
        Card.QUEEN|Card.HEART,
        Card.KING|Card.HEART,
        Card.TEN|Card.HEART,
        Card.ACE|Card.HEART
    };

    private Stage stage;

    private byte startsBidding;

    private byte biddingWinner;

    private ThousandCardSet must;
    private final ThousandCardSet[] sets;

    private final byte[] bids10;
    private byte minimumNextBid10;

    private byte trumpShift;
    private byte firstShift;
    private byte secondShift;
    private byte thirdShift;

    private final S[] smallPoints;
    private final int[] bigPoints10;

    //GUI stuff:
    private Hand myHand;
    private Hand mustHand;
    private Hand firstOpponentHand;
    private Hand secondOpponentHand;

    public JThousandBoard(final byte numberOfPlayers,
                          final Table table,
                          final MoveListener moveListener,
                          final JTabbedPane jTabbedPane){
        super(numberOfPlayers,
              table,
              moveListener,
              jTabbedPane);
        this.must = new ThousandCardSet();
        this.sets = new ThousandCardSet[]
            {new ThousandCardSet(),new ThousandCardSet(),new ThousandCardSet()};
        this.bids10 = new byte[]{0,0,0};
        this.smallPoints = new S[]{new S(),new S(),new S()};
        this.bigPoints10 = new int[]{0,0,0};
    }

    private String makeStateString(){
        return "stage="+stage
            + " startsBidding="+(int)startsBidding
            + " biddingWinner="+(int)biddingWinner
            + " bids10={"+bids10[0]+"," +bids10[1]+"," +bids10[2]+"}"
            + " minimumNextBid10="+(int)minimumNextBid10
            + " trumpShift="+(int)trumpShift
            + " firstShift="+(int)firstShift
            + " secondShif=t"+(int)secondShift
            + " thirdShift="+(int)thirdShift
            + " smallPoints={"+smallPoints[0]+"," +smallPoints[1]+"," +smallPoints[2]+"}"
            + " bigPoints10={"+bigPoints10[0]+"," +bigPoints10[1]+"," +bigPoints10[2]+"}";
    }

    private void deal(){
        must.setEmpty();
        sets[0].setEmpty();
        sets[1].setEmpty();
        sets[2].setEmpty();

        bids10[turn]=-1;
        bids10[getPreviousPlayer()]=10;
        bids10[getNextPlayer()]=-1;
        Output.d("bids10: {"+bids10[0]+"," +bids10[1]+"," +bids10[2]+"}");

        minimumNextBid10=11;

        trumpShift = ThousandProtocol.NO_TRUMP_SHIFT;

        smallPoints[0].value=0;
        smallPoints[1].value=0;
        smallPoints[2].value=0;
    }

    private byte maxBid10()
    {
        final byte absoluteMaximum = 12+10+8+6+4;
        final ThousandCardSet set = sets[turn];
        //TODO: precompute sets and check intersection.
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.HEART_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.HEART_SHIFT))
            return absoluteMaximum;
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.DIAMOND_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.DIAMOND_SHIFT))
            return absoluteMaximum;
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.CLUB_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.CLUB_SHIFT))
            return absoluteMaximum;
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.SPADE_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.SPADE_SHIFT))
            return absoluteMaximum;
        return 12;
    }

    private String makeBidString(final byte player){
        if(stage==Stage.BIDDING
           || stage==Stage.SELECTING_FIRST
           || stage==Stage.SELECTING_SECOND
           || stage==Stage.CONTRACTING){
            if(bids10[player]==0)
                return " (passed)";
            else if(bids10[player]==-1)
                return "";
            return " ("+(10*bids10[player])+")";
        }else{
            if(player==biddingWinner)
                return " (plays "+(10*bids10[player])+")";
            else
                return "";
        }
    }

    private void setArrows(){
        myHand.hasArrowAbove = this.myTurn();
        firstOpponentHand.hasArrowBelow = this.opponentsTurn(1);
        secondOpponentHand.hasArrowBelow = this.opponentsTurn(2);

        myHand.label
            = this.table.getMe().getScreenName()
            + makeBidString(table.myTurnGamePlayer);

        firstOpponentHand.label
            = this.table.getMyFirstOpponent().getScreenName()
            + makeBidString(table.getMyOpponentTurnGamePlayer(1));

        secondOpponentHand.label
            = this.table.getMySecondOpponent().getScreenName()
            + makeBidString(table.getMyOpponentTurnGamePlayer(2));
    }

    public boolean initialize(final java.util.Vector<Byte> initialMessage){

        try{
            //Initialization fails if table was created not for 3 players:
            if(numberOfPlayers!=3)
                return false;

            //We expect move only from the first player:
            setFirstPlayer();

            //Game starts with bidding:
            stage = Stage.BIDDING;
            //Player 0 will start bidding:
            startsBidding = turn;

            deal();

            Arrays.fill(bigPoints10,0);

            //Handle message with what cards we have:

            final ThousandProtocol.Deserialized_1_DEAL_7_CARDS deserialized
                = new ThousandProtocol.Deserialized_1_DEAL_7_CARDS(initialMessage);

            Output.d("cardSet24: "+deserialized.thousandCardSet);

            sets[table.myTurnGamePlayer].value = deserialized.thousandCardSet;

            myHand = new Hand("Me",0.5f,0.75f,0.5f);

            for(byte shift=0;shift<24;shift++)
                if(sets[table.myTurnGamePlayer].containsShift(shift))
                    myHand.cards.insertElementAt
                        (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                      NO_CARD),
                         0);

            mustHand = new Hand(null,0.5f,0.1f,0.15f);

            for(int i=0; i<3; i++)
                mustHand.cards.add(new HandCard(Card.UNKNOWN,NO_CARD));

            firstOpponentHand
                = new Hand(this.table.getMyFirstOpponent().getScreenName(),
                           0.1f,0.05f,0.15f);

            secondOpponentHand
                = new Hand(this.table.getMySecondOpponent().getScreenName(),
                           0.8f,0.05f,0.15f);

            for(int i=0; i<7; i++){
                firstOpponentHand.cards.add(new HandCard(Card.UNKNOWN,NO_CARD));
                secondOpponentHand.cards.add(new HandCard(Card.UNKNOWN,NO_CARD));
            }
            
            this.setArrows();
            this.hands.clear();
            this.hands.add(myHand);
            this.hands.add(mustHand);
            this.hands.add(firstOpponentHand);
            this.hands.add(secondOpponentHand);

            if(this.myTurn())
                showBidding(false);

            this.repaint();
            return true;
        }catch(MessageDeserializationException e){
            Output.p("Problem: "+e);
            return false;
        }
    }

    private void bid(final byte bid10){

        if(stage == Stage.BIDDING){
            final Vector<Byte> move
                = ThousandProtocol.serialize_1_BID(bid10);

            Sender.send(GeneralProtocol.serialize_1_MAKE_MOVE(move));
            //If it's second pass, we can't send the move to ourselves by
            //invoking moveListener, because instead of BID message we
            //should get BID_END_..._MUST from the server:
            final boolean secondPass
                = (bid10==0) && (bids10[0]==0 || bids10[1]==0 || bids10[2]==0);
            if(!secondPass)
                this.moveListener.handle_1_MOVE_MADE(move);
        }else if(stage == Stage.CONTRACTING){
            final Vector<Byte> move
                = ThousandProtocol.serialize_1_CONTRACT(bid10);

            Sender.send(GeneralProtocol.serialize_1_MAKE_MOVE(move));
            this.moveListener.handle_1_MOVE_MADE(move);
        }else{
            Output.p("ERROR! JTB::bid() Can't bid now!");
        }
    }

    private static class JRunnableButton extends JButton implements ActionListener{
        private final Runnable runnable;

        public JRunnableButton(final String label,
                               final Runnable runnable){
            super(label);
            this.runnable = runnable;
            this.addActionListener(this);
        }

        public void actionPerformed(ActionEvent e){
            this.runnable.run();
        }
    }

    private void showBidding(final boolean contracting){
        final byte minBid10 = (contracting?bids10[turn]:minimumNextBid10);
        byte maxBid10 = maxBid10();
        //In case player bids 130, but later throws away a queen by mistake:
        if(maxBid10<bids10[turn])
            maxBid10=bids10[turn];
        final JPanel biddingPanel = new JPanel();
        final JThousandBoard me = this;
        if(!contracting)
            biddingPanel.add(new JRunnableButton
                             ("Pass",
                              new Runnable(){
                                  public void run(){
                                      me.bid((byte)0);
                                      me.jTabbedPane.remove(biddingPanel);
                                  }
                              }));
        for(byte i=minBid10; i<=maxBid10; i++){
            final byte bid10 = i;
            biddingPanel.add(new JRunnableButton
                             (""+10*((int)i),
                              new Runnable(){
                                  public void run(){
                                      me.bid(bid10);
                                      me.jTabbedPane.remove(biddingPanel);
                                  }
                              }));
        }
        this.jTabbedPane.addTab("Bidding", null, biddingPanel,
                                "Bidding in the game of Thousand");
        final int index = this.jTabbedPane.getTabCount()-1;
        this.jTabbedPane.setMnemonicAt(index, KeyEvent.VK_B);
        this.jTabbedPane.setSelectedIndex(index);
    }

    public void cardClicked(final int virtualColor){
        if(stage == Stage.SELECTING_FIRST
           || stage == Stage.SELECTING_SECOND){
            final byte shift = (byte)virtualColor;
            final Vector<Byte> move
                = ThousandProtocol.serialize_1_SELECT(shift);
            Sender.send(GeneralProtocol.serialize_1_MAKE_MOVE(move));
            this.moveListener.handle_1_MOVE_MADE(move);
        }else if(stage == Stage.PLAYING_FIRST
                 || stage == Stage.PLAYING_SECOND
                 || stage == Stage.PLAYING_THIRD){
            final byte shift = (byte)virtualColor;
            final Vector<Byte> move
                = ThousandProtocol.serialize_1_PLAY(shift);
            Sender.send(GeneralProtocol.serialize_1_MAKE_MOVE(move));
            this.moveListener.handle_1_MOVE_MADE(move);
        }else{
            Output.p("ERROR! JTB::cC("+virtualColor+") called in stage "+stage);
        }

        /*
        final byte card = this.hands.get(0).removeCard(virtualColor);

        this.cardsAtTable.add
            (new CardAtTable(card,
                             0f,//(float)Math.random()-0.5f,
                             0f,//(float)Math.random()-0.5f,
                             0.0,//Math.PI*Math.random(),
                             NO_CARD));
        */
    }

    
    public int moveMade(final Vector<Byte> move,
                        final int[] endResult){
        Output.d("BEFORE: "+makeStateString());
        Output.d("JTB.MM "+ThousandProtocol.lookupMessageType(move)
                 +" "+Message.toString(move));

        //Are we still biding?
        if(stage==Stage.BIDDING){

            switch(ThousandProtocol.lookupMessageType(move)){
            case BID_1:
                //First we deserialize the move.
                try{
                    ThousandProtocol.Deserialized_1_BID deserialized
                        = new ThousandProtocol.Deserialized_1_BID(move);
                    bids10[turn] = deserialized.bid10;
                }catch(MessageDeserializationException e){
                    Output.p("Deserialized_1_BID failed.");
                    return INVALID|END;
                }

                if(bids10[turn]!=0)
                    minimumNextBid10=(byte)(bids10[turn]+1);
    
                //Skip one player if passed.
                if(bids10[getNextPlayer()]!=0)
                    setNextPlayer();
                else
                    setNextPlayer(2);

                if(this.myTurn())
                    showBidding(false);

                break;
            case BID_END_SHOW_MUST_1:

                try{
                    ThousandProtocol.Deserialized_1_BID_END_SHOW_MUST deserialized
                        = new ThousandProtocol.Deserialized_1_BID_END_SHOW_MUST
                        (move);
                    this.must.value = deserialized.must;
                }catch(MessageDeserializationException e){
                    Output.p("Deserialized_1_BID_END_SHOW_MUST failed.");
                    return INVALID|END;
                }
                
            case BID_END_HIDDEN_MUST_1:

                bids10[turn] = 0;

                biddingWinner = (byte)((bids10[0]==0 && bids10[1]==0)?2
                                       :((bids10[1]==0 && bids10[2]==0)?0
                                         :((bids10[2]==0 && bids10[0]==0)?1
                                           :-1)));

                turn = biddingWinner;

                //GUI stuff:
                if(myTurn()){
                    //I take cards from must.
                    sets[turn].addAll(must);

                    hands.remove(mustHand);
                    myHand.cards.clear();
                    for(byte shift=0;shift<24;shift++)
                        if(sets[turn].containsShift(shift))
                            myHand.cards.insertElementAt
                                (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              shift),
                                 0);
                }else{
                    //Someone else takes must.
                    if(!must.isEmpty()){
                        //Must should be shown.
                        mustHand.cards.clear();
                        for(byte shift=0;shift<24;shift++)
                            if(must.containsShift(shift))
                                mustHand.cards.insertElementAt
                                    (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                                  NO_CARD),
                                     0);
                    }else{
                        //Must not shown.
                        hands.remove(mustHand);
                    }

                    //Add 3 cards to the must taker.
                    if(opponentsTurn(1))
                        for(int i=0;i<3;i++)
                            firstOpponentHand.cards.add
                                (new HandCard(Card.UNKNOWN,NO_CARD));
                    if(opponentsTurn(2))
                        for(int i=0;i<3;i++)
                            secondOpponentHand.cards.add
                                (new HandCard(Card.UNKNOWN,NO_CARD));
                }

                stage = Stage.SELECTING_FIRST;
                break;
            default:
                Output.p("UNEXPECTED MESSAGE TYPE!");
                return INVALID|END;
            }
        }
        else if(stage==Stage.SELECTING_FIRST
                || stage==Stage.SELECTING_SECOND){

            switch(ThousandProtocol.lookupMessageType(move)){
            case SELECT_1:
                if(!myTurn()){
                    //Not my turn -- card for me!
                    try{
                        ThousandProtocol.Deserialized_1_SELECT deserialized
                            = new ThousandProtocol.Deserialized_1_SELECT(move);
                        sets[table.myTurnGamePlayer].addShift(deserialized.shift);
                    }catch(MessageDeserializationException e){
                        Output.p("Deserialized_1_SELECT failed.");
                        return INVALID|END;
                    }

                    //GUI stuff:
                    if(biddingWinner==table.getMyOpponentTurnGamePlayer(1)){
                        firstOpponentHand.cards.remove
                            (firstOpponentHand.cards.lastElement());
                    }else{
                        secondOpponentHand.cards.remove
                            (secondOpponentHand.cards.lastElement());
                    }

                    myHand.cards.clear();
                    for(byte shift=0;shift<24;shift++)
                        if(sets[table.myTurnGamePlayer].containsShift(shift))
                            myHand.cards.insertElementAt
                                (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              NO_CARD),
                                 0);
                }else{
                    //My turn. I selected a card for someone.
                    try{
                        ThousandProtocol.Deserialized_1_SELECT deserialized
                            = new ThousandProtocol.Deserialized_1_SELECT(move);
                        sets[table.myTurnGamePlayer].removeShift(deserialized.shift);
                    }catch(MessageDeserializationException e){
                        Output.p("Deserialized_1_SELECT failed.");
                        return INVALID|END;
                    }

                    //GUI stuff:
                    myHand.cards.clear();
                    for(byte shift=0;shift<24;shift++)
                        if(sets[table.myTurnGamePlayer].containsShift(shift))
                            myHand.cards.insertElementAt
                                (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              (stage==Stage.SELECTING_FIRST
                                               ?shift
                                               :NO_CARD)),
                                 0);

                    //Who did I select a card for?
                    if(stage == Stage.SELECTING_FIRST){
                        firstOpponentHand.cards.add
                            (new HandCard(Card.UNKNOWN,NO_CARD));
                    }else{
                        secondOpponentHand.cards.add
                            (new HandCard(Card.UNKNOWN,NO_CARD));
                    }
                }
                break;
            case SELECT_HIDDEN_1:
                //Card not from me and not for me.
                try{
                    ThousandProtocol.Deserialized_1_SELECT_HIDDEN deserialized
                        = new ThousandProtocol.Deserialized_1_SELECT_HIDDEN(move);
                }catch(MessageDeserializationException e){
                    Output.p("Deserialized_1_SELECT_HIDDEN failed.");
                    return INVALID|END;
                }

                //GUI stuff:
                //Add one hidden card to first or second opponent?
                if(biddingWinner==table.getMyOpponentTurnGamePlayer(1)){
                    firstOpponentHand.cards.remove
                        (firstOpponentHand.cards.lastElement());
                    secondOpponentHand.cards.add
                        (new HandCard(Card.UNKNOWN,NO_CARD));
                }else{
                    secondOpponentHand.cards.remove
                        (secondOpponentHand.cards.lastElement());
                    firstOpponentHand.cards.add
                        (new HandCard(Card.UNKNOWN,NO_CARD));
                }
                break;
            default:
                Output.p("UNEXPECTED MESSAGE TYPE!");
                return INVALID|END;
            }
            
            if(stage == Stage.SELECTING_FIRST)
                stage = Stage.SELECTING_SECOND;
            else{
                stage = Stage.CONTRACTING;
                if(myTurn())
                    showBidding(true);
            }
        }else if(stage == Stage.CONTRACTING){
            try{
                ThousandProtocol.Deserialized_1_CONTRACT deserialized
                    = new ThousandProtocol.Deserialized_1_CONTRACT(move);
                bids10[turn] = deserialized.contract10;
            }catch(MessageDeserializationException e){
                Output.p("Deserialized_1_CONTRACT failed.");
                return INVALID|END;
            }
                
            //GUI stuff:
            //If it's me, enable my cards for move:
            if(myTurn()){
                myHand.cards.clear();
                for(byte shift=0;shift<24;shift++)
                    if(sets[turn].containsShift(shift))
                        myHand.cards.insertElementAt
                            (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          shift),
                             0);
            }
            stage = Stage.PLAYING_FIRST;
        }else if(stage == Stage.PLAYING_FIRST){

            final byte oldTrumpShift = trumpShift;

            switch(ThousandProtocol.lookupMessageType(move)){
            case PLAY_NEW_TRUMP_1:
                try{
                    ThousandProtocol.Deserialized_1_PLAY_NEW_TRUMP deserialized
                        = new ThousandProtocol.Deserialized_1_PLAY_NEW_TRUMP(move);
                    firstShift = deserialized.shift;
                    trumpShift = ThousandCardSet.suitShift(deserialized.shift);
                }catch(MessageDeserializationException e){
                    Output.p("Deserialized_1_PLAY_NEW_TRUMP failed.");
                    return INVALID|END;
                }
                break;
            case PLAY_1:
                try{
                    ThousandProtocol.Deserialized_1_PLAY deserialized
                        = new ThousandProtocol.Deserialized_1_PLAY(move);
                    firstShift = deserialized.shift;
                }catch(MessageDeserializationException e){
                    Output.p("Deserialized_1_PLAY failed.");
                    return INVALID|END;
                }
                break;
            default:
                Output.p("UNEXPECTED message type in stage "+stage+"!");
                return INVALID|END;
            }

            this.cardsAtTable.clear();
            this.cardsAtTable.add
                (new CardAtTable(THOUSAND_SHIFT_TO_CARD[firstShift],
                                 0f,//(float)Math.random()-0.5f,
                                 0f,//(float)Math.random()-0.5f,
                                 (opponentsTurn(1)
                                  ?-Math.PI/4:(opponentsTurn(2)
                                               ?Math.PI/4
                                               :0.0)),
                                 NO_CARD));
            
            if(myTurn()){
                final B dummyB = new B();
                sets[turn].removeFirstShift(firstShift,
                                            dummyB,
                                            smallPoints[turn]);
                myHand.cards.clear();
                for(byte shift=0;shift<24;shift++)
                    if(sets[turn].containsShift(shift))
                        myHand.cards.insertElementAt
                            (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          NO_CARD),
                                 0);
            }else{
                //Not my turn.

                //We might not know the cards of the player, but we have
                //to calculate points, etc. anyway using the same method
                //that server uses. So we create a set having the played
                //card.
                final ThousandCardSet set = new ThousandCardSet();
                set.addShift(firstShift);
                //If trump changed, we need to add king (or quuen) to
                //the set. We do it by adding all cards:
                if(trumpShift!=oldTrumpShift)
                    set.value = 0xFFFFFF;

                final B dummyB = new B();
                set.removeFirstShift(firstShift,
                                     dummyB,
                                     smallPoints[turn]);

                if(opponentsTurn(1)){
                    firstOpponentHand.cards.remove
                        (firstOpponentHand.cards.lastElement());
                }else{
                    secondOpponentHand.cards.remove
                        (secondOpponentHand.cards.lastElement());
                }
            }
            setNextPlayer();
            if(myTurn()){
                myHand.cards.clear();
                for(byte shift=0;shift<24;shift++)
                    if(sets[turn].containsShift(shift)){
                        final ThousandCardSet copy = new ThousandCardSet();
                        copy.value = sets[turn].value;
                        final boolean valid = copy.removeSecondShift(firstShift,
                                                                     shift,
                                                                     trumpShift);
                        myHand.cards.insertElementAt
                            (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          (valid?shift:NO_CARD)),
                             0);
                    }
            }
            stage = Stage.PLAYING_SECOND;
        }else if(stage == Stage.PLAYING_SECOND){
            try{
                ThousandProtocol.Deserialized_1_PLAY deserialized
                    = new ThousandProtocol.Deserialized_1_PLAY(move);
                secondShift = deserialized.shift;
            }catch(MessageDeserializationException e){
                Output.p("Deserialized_1_PLAY failed.");
                return INVALID|END;
            }

            this.cardsAtTable.add
                (new CardAtTable(THOUSAND_SHIFT_TO_CARD[secondShift],
                                 0f,//(float)Math.random()-0.5f,
                                 0f,//(float)Math.random()-0.5f,
                                 (opponentsTurn(1)
                                  ?-Math.PI/4:(opponentsTurn(2)
                                               ?Math.PI/4
                                               :0.0)),
                                 NO_CARD));

            if(myTurn()){
                sets[turn].removeShift(secondShift);
                myHand.cards.clear();
                for(byte shift=0;shift<24;shift++)
                    if(sets[turn].containsShift(shift))
                        myHand.cards.insertElementAt
                            (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          NO_CARD),
                             0);
            }else if(opponentsTurn(1)){
                firstOpponentHand.cards.remove
                    (firstOpponentHand.cards.lastElement());
            }else{
                secondOpponentHand.cards.remove
                    (secondOpponentHand.cards.lastElement());
            }
            setNextPlayer();
            if(myTurn()){
                myHand.cards.clear();
                for(byte shift=0;shift<24;shift++)
                    if(sets[turn].containsShift(shift)){
                        final ThousandCardSet copy = new ThousandCardSet();
                        copy.value = sets[turn].value;
                        final S dummyS = new S();
                        final B dummyB = new B();
                        final boolean valid = copy.removeThirdShift(firstShift,
                                                                    secondShift,
                                                                    shift,
                                                                    trumpShift,
                                                                    dummyS,
                                                                    dummyS,
                                                                    dummyS,
                                                                    dummyB);
                        myHand.cards.insertElementAt
                            (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          (valid?shift:NO_CARD)),
                             0);
                    }
            }
            stage = Stage.PLAYING_THIRD;
        }else if(stage == Stage.PLAYING_THIRD){
            try{
                ThousandProtocol.Deserialized_1_PLAY deserialized
                    = new ThousandProtocol.Deserialized_1_PLAY(move);
                thirdShift = deserialized.shift;
            }catch(MessageDeserializationException e){
                Output.p("Deserialized_1_PLAY failed.");
                return INVALID|END;
            }

            //We might not know the cards of the player, but we have
            //to calculate points, etc. anyway using the same method
            //that server uses. So we create a set having the played
            //card.
            final ThousandCardSet set = new ThousandCardSet();
            set.addShift(thirdShift);
            final B turnIncrement = new B();
            set.removeThirdShift(firstShift,
                                 secondShift,
                                 thirdShift,
                                 trumpShift,
                                 smallPoints[getPreviousPlayer(2)],
                                 smallPoints[getPreviousPlayer()],
                                 smallPoints[turn],
                                 turnIncrement);

            this.cardsAtTable.add
                (new CardAtTable(THOUSAND_SHIFT_TO_CARD[thirdShift],
                                 0f,//(float)Math.random()-0.5f,
                                 0f,//(float)Math.random()-0.5f,
                                 (opponentsTurn(1)
                                  ?-Math.PI/4:(opponentsTurn(2)
                                               ?Math.PI/4
                                               :0.0)),
                                 NO_CARD));

            if(myTurn())
                sets[turn].removeShift(thirdShift);
            
            if(sets[table.myTurnGamePlayer].isEmpty()){
                //No more cards
                //Add points for each player:
                for(byte p = 0; p<numberOfPlayers; p++){
                    if(p==biddingWinner){
                        if(smallPoints[p].value>=10*bids10[biddingWinner])
                            bigPoints10[p] += bids10[biddingWinner];
                        else                  
                            bigPoints10[p] -= bids10[biddingWinner];
                    }
                    else
                        bigPoints10[p] += (smallPoints[p].value+4)/10;
                }

                //The end?
                if(bigPoints10[biddingWinner]>=100){
                    stage = Stage.ENDED;
                    hands.clear();
                    setArrows();
                    this.repaint();
                    Output.d("AFTER: "+makeStateString());
                    return VALID|END;
                }else{
                    //Not the end, but next bidding.
                    stage = Stage.BIDDING;
                    startsBidding = (byte)((startsBidding+1)%numberOfPlayers);
                    turn = startsBidding;
                    deal();
                }
            }
            else{
                //Still some cards in the set.
                //GUI stuff:
                if(myTurn()){
                    myHand.cards.clear();
                    for(byte shift=0;shift<24;shift++)
                        if(sets[turn].containsShift(shift))
                            myHand.cards.insertElementAt
                                (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              NO_CARD),
                                 0);
                }else if(opponentsTurn(1)){
                    firstOpponentHand.cards.remove
                        (firstOpponentHand.cards.lastElement());
                }else{
                    secondOpponentHand.cards.remove
                        (secondOpponentHand.cards.lastElement());
                }
                setNextPlayer(turnIncrement.value);
                if(myTurn()){
                    myHand.cards.clear();
                    for(byte shift=0;shift<24;shift++)
                        if(sets[turn].containsShift(shift)){
                            myHand.cards.insertElementAt
                                (new HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              shift),
                                 0);
                        }
                }
                stage = Stage.PLAYING_FIRST;
            }
        }
        /*
  elseif(stage==CONTRACTING)
    {
      //First we deserialize the move. If deserialization fails, current
      //player sent invalid move.
      if(!ThousandProtocol::deserialize_1_CONTRACT(move,contract10))
        {
          Output.p("deserialize_1_CONTRACT failed."
                   
          return INVALID|END;
        }
      //Let's see whether the move is valid:
      if(contract10>maxBid10() || contract10<bids10[turn])
        {
          Output.p("Incorrect contract: contract10=="+(int)(contract10)
                   
          return INVALID|END;
        }
      
      sendToOthers(move,moveMessages);
      stage = PLAYING_FIRST;
      return VALID|CONTINUE;
    }
  else if(stage==PLAYING_FIRST)
    {
      if(!ThousandProtocol::deserialize_1_PLAY(move,firstShift))
        {
          Output.p("deserialize_1_PLAY failed."
                   
          return INVALID|END;
        }

      const int_fast8_t oldTrumpShift = trumpShift;
      
      if(!sets[turn].removeFirstShift(firstShift,
                                       trumpShift,
                                       smallPoints[turn]))
        {
          Output.p("!sets[turn].removeFirstShift(firstShift,trumpShift)"
                   <<" firstShift=="
                   +(int)(firstShift)<<" sets[turn].value=="
                   <<sets[turn].value<<" trumpShift=="<<trumpShift
                   
          return INVALID|END;
        }

      if(trumpShift==oldTrumpShift)
        sendToOthers(move,moveMessages);
      else
        {//New trump!
          std::vector<char> message;
          ThousandProtocol::serialize_1_PLAY_NEW_TRUMP(firstShift,message);
          sendToOthers(message,moveMessages);
        }
      
      stage = PLAYING_SECOND;
      setNextPlayer();
      return VALID|CONTINUE;
    }
  else if(stage==PLAYING_SECOND)
    {
      if(!ThousandProtocol::deserialize_1_PLAY(move,secondShift))
        {
          Output.p("deserialize_1_PLAY failed."
                   
          return INVALID|END;
        }
      if(!sets[turn].removeSecondShift(firstShift,secondShift,trumpShift))
        {
          std::cout
            <<"!sets[turn].removeSecondShift(firstShift,secondShift,"
            <<"trumpShift) firstShift=="+(int)(firstShift)
            <<" secondShift=="+(int)(secondShift)
            <<" sets[turn].value=="<<sets[turn].value
            <<" trumpShift=="<<trumpShift
            
          return INVALID|END;
        }

      sendToOthers(move,moveMessages);
      
      stage = PLAYING_THIRD;
      setNextPlayer();
      return VALID|CONTINUE;
    }
  else if(stage==PLAYING_THIRD)
    {
      if(!ThousandProtocol::deserialize_1_PLAY(move,thirdShift))
        {
          Output.p("deserialize_1_PLAY failed."
                   
          return INVALID|END;
        }
      if(!sets[turn].removeThirdShift(firstShift,
                                       secondShift,
                                       thirdShift,
                                       trumpShift,
                                       smallPoints[getPreviousPlayer(2)],
                                       smallPoints[getPreviousPlayer()],
                                       smallPoints[turn]))
        {
          std::cout
            <<"!sets[turn].removeThirdShift(firstShift,secondShift,"
            <<"thirdShift,trumpShift) firstShift=="+(int)(firstShift)
            <<" secondShift=="+(int)(secondShift)
            <<" thirdShift=="+(int)(thirdShift)
            <<" sets[turn].value=="<<sets[turn].value
            <<" trumpShift=="<<trumpShift
            
          return INVALID|END;
        }

      sendToOthers(move,moveMessages);

      if(sets[turn].isEmpty())
        {//No more cards
          //Add points for each player:
          for(Player p = 0; p<numberOfPlayers; p++)
            {
              if(p==biddingWinner)
                {
                  if(smallPoints[p]>=10*contract10)
                    bigPoints10[p] += contract10;
                  else                  
                    bigPoints10[p] -= contract10;
                }
              else
                bigPoints10[p] += (smallPoints[p]+4)/10;
            }
          //The end?
          if(bigPoints10[biddingWinner]>=100)
            {
              stage = ENDED;
              return VALID|END;
            }

          stage = BIDDING;
          startsBidding = (startsBidding+1)%numberOfPlayers;
          deal();
          turn = startsBidding;
        }
      else
        {//Still some cards in the set.
          stage = PLAYING_FIRST;
          setNextPlayer();
        }
      return VALID|CONTINUE;
    }
        */
        else{
            Output.p("Incorrect stage: "+stage);
            return INVALID|END;
        }

        setArrows();
        this.repaint();
        Output.d("AFTER: "+makeStateString());
        return VALID|CONTINUE;
    }
    
}
