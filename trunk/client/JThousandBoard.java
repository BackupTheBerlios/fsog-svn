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
import javax.swing.table.*;

public class JThousandBoard
    extends JBoard{
    
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

    private final B trumpShift;
    private byte firstShift;
    private byte secondShift;
    private byte thirdShift;

    private final S[] smallPoints;
    private final int[] bigPoints10;
    private final Vector<Vector<Integer>> savedPoints;

    //GUI stuff:
    private final JCards jCards;
    private final JCards jLastTrick;
    private JCards.Hand myHand;
    private JCards.Hand mustHand;
    private JCards.Hand firstOpponentHand;
    private JCards.Hand secondOpponentHand;
    private AbstractTableModel pointsTableModel;
    private JTable pointsTable;

    public JThousandBoard(final byte numberOfPlayers,
                          final Table table,
                          final MoveListener moveListener,
                          final JTabbedPane jTabbedPane){
        super(numberOfPlayers,
              table,
              moveListener,
              jTabbedPane);
        this.must = new ThousandCardSet();
        this.sets = new ThousandCardSet[]{new ThousandCardSet(),
                                          new ThousandCardSet(),
                                          new ThousandCardSet()};
        this.bids10 = new byte[]{0,0,0};
        this.trumpShift = new B();
        this.smallPoints = new S[]{new S(),new S(),new S()};
        this.bigPoints10 = new int[]{0,0,0};
        this.savedPoints = new Vector<Vector<Integer>>();

        final JThousandBoard me = this;

        this.jCards = new JCards(){
                public void cardClicked(final int virtualColor){
                    me.cardClicked(virtualColor);
                }
            };

        this.jLastTrick = new JCards(){
                public void cardClicked(final int virtualColor){}
            };

        this.add(jCards);

        this.pointsTableModel = null;
        this.pointsTable = null;
    }

    private String makeStateString(){
        return "stage="+stage
            + " startsBidding="+(int)startsBidding
            + " biddingWinner="+(int)biddingWinner
            + " bids10={"+bids10[0]+"," +bids10[1]+"," +bids10[2]+"}"
            + " minimumNextBid10="+(int)minimumNextBid10
            + " trumpShift="+(int)trumpShift.value
            + " firstShift="+(int)firstShift
            + " secondShift="+(int)secondShift
            + " thirdShift="+(int)thirdShift
            + " smallPoints={"+smallPoints[0]+"," +smallPoints[1]+"," +smallPoints[2]+"}"
            + " bigPoints10={"+bigPoints10[0]+"," +bigPoints10[1]+"," +bigPoints10[2]+"}";
    }

    private void addPointsTableRow(){
        if(pointsTableModel==null){
            this.pointsTableModel
                = new AbstractTableModel() {
                        public String getColumnName(final int c) {
                            return table.getPlayer(c).getScreenName();
                        }
                        public int getRowCount() { return savedPoints.size(); }
                        public int getColumnCount() { return 3; }
                        public Object getValueAt(final int r, final int c) {
                            return savedPoints.get(r).get(c);
                        }
                        //public boolean isCellEditable(int row, int col){return false;}
                        //public void setValueAt(Object value, int row, int col){}
                    };
            this.pointsTable = new JTable(pointsTableModel);

            this.jTabbedPane.addTab("Score", null,
                                    new JScrollPane(pointsTable),
                                    "Score in the game of Thousand");
            final int index = this.jTabbedPane.getTabCount()-1;
            this.jTabbedPane.setMnemonicAt(index, KeyEvent.VK_S);
        }
        final Vector<Integer> row = new Vector<Integer>();
        for(byte p=0;p<3;p++)
            row.add(10*bigPoints10[p]);
        final int temp = savedPoints.size();
        this.savedPoints.add(row);
        this.pointsTableModel.fireTableRowsInserted(temp,temp);
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

        trumpShift.value = ThousandProtocol.NO_TRUMP_SHIFT;

        smallPoints[0].value=0;
        smallPoints[1].value=0;
        smallPoints[2].value=0;
    }

    private byte maxBid10()
    {
        byte result = 12;
        final ThousandCardSet set = sets[turn];
        //TODO: precompute sets and check intersection.
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.HEART_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.HEART_SHIFT))
            result+=10;
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.DIAMOND_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.DIAMOND_SHIFT))
            result+=8;
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.CLUB_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.CLUB_SHIFT))
            result+=6;
        if(set.containsShift(ThousandProtocol.QUEEN_SHIFT
                             +ThousandProtocol.SPADE_SHIFT)
           && set.containsShift(ThousandProtocol.KING_SHIFT
                                +ThousandProtocol.SPADE_SHIFT))
            result+=4;
        return result;
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

    public void initializeHands(){
        myHand = new JCards.Hand("Me",0.5f,0.75f,0.5f);

        for(byte shift=0;shift<24;shift++)
            if(sets[table.myTurnGamePlayer].containsShift(shift))
                myHand.cards.insertElementAt
                    (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                  JCards.UNCLICKABLE),
                     0);

        mustHand = new JCards.Hand(null,0.5f,0.1f,0.15f);

        for(int i=0; i<3; i++)
            mustHand.cards.add(new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));

        firstOpponentHand
            = new JCards.Hand(this.table.getMyFirstOpponent().getScreenName(),
                       0.1f,0.05f,0.15f);

        secondOpponentHand
            = new JCards.Hand(this.table.getMySecondOpponent().getScreenName(),
                       0.8f,0.05f,0.15f);

        for(int i=0; i<7; i++){
            firstOpponentHand.cards.add(new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
            secondOpponentHand.cards.add(new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
        }
            
        this.jCards.hands.clear();
        this.jCards.hands.add(myHand);
        this.jCards.hands.add(mustHand);
        this.jCards.hands.add(firstOpponentHand);
        this.jCards.hands.add(secondOpponentHand);

        this.setArrows();

        if(this.myTurn())
            showBidding(false);

        this.repaint();
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

            final ThousandProtocol.Deserialized_1_DEAL deserialized
                = new ThousandProtocol.Deserialized_1_DEAL(initialMessage);

            Output.d("cardSet24: "+deserialized.thousandCardSet);

            sets[table.myTurnGamePlayer].value = deserialized.thousandCardSet;

            this.initializeHands();

            //Insert row of zeros into the table:
            addPointsTableRow();

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
        final Box biddingPanel = Box.createVerticalBox();
        final JScrollPane biddingScrollPane = new JScrollPane(biddingPanel);
        final JThousandBoard me = this;
        final JLabel label = new JLabel("Select your "
                                        +(contracting?"contract":"bid")+":");
        label.setAlignmentX(0.5f);
        biddingPanel.add(label);

        if(!contracting){
            final JRunnableButton button
                = new JRunnableButton
                ("Pass",
                 new Runnable(){
                     public void run(){
                         me.bid((byte)0);
                         me.jTabbedPane.remove(biddingScrollPane);
                     }
                 });
            button.setAlignmentX(0.5f);
            biddingPanel.add(button);
        }

        for(byte i=minBid10; i<=maxBid10; i++){
            final byte bid10 = i;
            final JRunnableButton button
                = new JRunnableButton
                (""+10*((int)i),
                 new Runnable(){
                     public void run(){
                         me.bid(bid10);
                         me.jTabbedPane.remove(biddingScrollPane);
                     }
                 });
            button.setAlignmentX(0.5f);
            biddingPanel.add(button);
        }

        this.jTabbedPane.addTab("Bidding", null, biddingScrollPane,
                                "Bidding in the game of Thousand");
        final int index = this.jTabbedPane.getTabCount()-1;
        this.jTabbedPane.setMnemonicAt(index, KeyEvent.VK_B);
        this.jTabbedPane.setSelectedIndex(index);
    }

    private void cardClicked(final int virtualColor){
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
            //We play a card! Let's see which one:
            final byte shift = (byte)virtualColor;

            //We send the move to the server.

            final Vector<Byte> move
                = ThousandProtocol.serialize_1_PLAY(shift);
            Sender.send(GeneralProtocol.serialize_1_MAKE_MOVE(move));

            //To save bandwidth, server will not send the move back,
            //so we pretend it's sent by invoking this.moveListener's
            //method. However, we can only do it, if we really know
            //what the server would send us. In one case we don't
            //know, when new cards are dealt.
            if(!(stage == Stage.PLAYING_THIRD && myHand.cards.size()==1))
                this.moveListener.handle_1_MOVE_MADE(move);
        }else{
            Output.p("ERROR! JTB::cC("+virtualColor
                     +") called in stage "+stage);
        }
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

                    jCards.hands.remove(mustHand);
                    myHand.cards.clear();
                    for(byte shift=0;shift<24;shift++)
                        if(sets[turn].containsShift(shift))
                            myHand.cards.insertElementAt
                                (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
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
                                    (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                                  JCards.UNCLICKABLE),
                                     0);
                    }else{
                        //Must not shown.
                        jCards.hands.remove(mustHand);
                    }

                    //Add 3 cards to the must taker.
                    if(opponentsTurn(1))
                        for(int i=0;i<3;i++)
                            firstOpponentHand.cards.add
                                (new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
                    if(opponentsTurn(2))
                        for(int i=0;i<3;i++)
                            secondOpponentHand.cards.add
                                (new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
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
                                (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              JCards.UNCLICKABLE),
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
                                (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              (stage==Stage.SELECTING_FIRST
                                               ?shift
                                               :JCards.UNCLICKABLE)),
                                 0);

                    //Who did I select a card for?
                    if(stage == Stage.SELECTING_FIRST){
                        firstOpponentHand.cards.add
                            (new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
                    }else{
                        secondOpponentHand.cards.add
                            (new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
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
                        (new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
                }else{
                    secondOpponentHand.cards.remove
                        (secondOpponentHand.cards.lastElement());
                    firstOpponentHand.cards.add
                        (new JCards.HandCard(Card.UNKNOWN,JCards.UNCLICKABLE));
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
                            (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          shift),
                             0);
            }
            stage = Stage.PLAYING_FIRST;
        }else if(stage == Stage.PLAYING_FIRST){

            //final byte oldTrumpShift = trumpShift;
            boolean trumpChanges = false;

            switch(ThousandProtocol.lookupMessageType(move)){
            case PLAY_NEW_TRUMP_1:
                try{
                    ThousandProtocol.Deserialized_1_PLAY_NEW_TRUMP deserialized
                        = new ThousandProtocol.Deserialized_1_PLAY_NEW_TRUMP(move);
                    firstShift = deserialized.shift;
                    trumpChanges = true;
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

            mustHand.cards.clear();

            this.jCards.cardsAtTable.clear();
            this.jCards.cardsAtTable.add
                (new JCards.CardAtTable(THOUSAND_SHIFT_TO_CARD[firstShift],
                                 (opponentsTurn(1)
                                  ?-0.25f:(opponentsTurn(2)
                                           ?0.25f
                                           :0f)),
                                 (opponentsTurn(1)
                                  ?-0.25f:(opponentsTurn(2)
                                           ?-0.25f
                                           :0.25f)),
                                 (opponentsTurn(1)
                                  ?-Math.PI/4:(opponentsTurn(2)
                                               ?Math.PI/4
                                               :0.0)),
                                 JCards.UNCLICKABLE));
            
            if(myTurn()){
                sets[turn].removeFirstShift(firstShift,
                                            trumpShift,
                                            smallPoints[turn]);
                myHand.cards.clear();
                for(byte shift=0;shift<24;shift++)
                    if(sets[turn].containsShift(shift))
                        myHand.cards.insertElementAt
                            (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          JCards.UNCLICKABLE),
                                 0);
            }else{
                //Not my turn.

                //We might not know the cards of the player, but we have
                //to calculate points, etc. anyway using the same method
                //that server uses. So we create a set having the played
                //card.
                final ThousandCardSet set = new ThousandCardSet();
                set.addShift(firstShift);
                //If trump changed, we need to add king (or queen) to
                //the set. We do it by adding all cards:
                if(trumpChanges)
                    set.value = 0xFFFFFF;

                set.removeFirstShift(firstShift,
                                     trumpShift,
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
                        final boolean valid
                            = copy.removeSecondShift(firstShift,
                                                     shift,
                                                     trumpShift.value);
                        myHand.cards.insertElementAt
                            (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          (valid?shift:JCards.UNCLICKABLE)),
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

            this.jCards.cardsAtTable.add
                (new JCards.CardAtTable(THOUSAND_SHIFT_TO_CARD[secondShift],
                                 0f,//(float)Math.random()-0.5f,
                                 0f,//(float)Math.random()-0.5f,
                                 (opponentsTurn(1)
                                  ?-Math.PI/4:(opponentsTurn(2)
                                               ?Math.PI/4
                                               :0.0)),
                                 JCards.UNCLICKABLE));

            if(myTurn()){
                sets[turn].removeShift(secondShift);
                myHand.cards.clear();
                for(byte shift=0;shift<24;shift++)
                    if(sets[turn].containsShift(shift))
                        myHand.cards.insertElementAt
                            (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          JCards.UNCLICKABLE),
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
                        final boolean valid
                            = copy.removeThirdShift(firstShift,
                                                    secondShift,
                                                    shift,
                                                    trumpShift.value,
                                                    dummyS,
                                                    dummyS,
                                                    dummyS,
                                                    dummyB);
                        myHand.cards.insertElementAt
                            (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                          (valid?shift:JCards.UNCLICKABLE)),
                             0);
                    }
            }
            stage = Stage.PLAYING_THIRD;
        }else if(stage == Stage.PLAYING_THIRD){

            ThousandCardSet newCards = null;

            switch(ThousandProtocol.lookupMessageType(move)){
            case PLAY_1:
                try{
                    final ThousandProtocol.Deserialized_1_PLAY deserialized
                        = new ThousandProtocol.Deserialized_1_PLAY(move);
                    thirdShift = deserialized.shift;
                }catch(MessageDeserializationException e){
                    Output.p("Deserialized_1_PLAY failed.");
                    return INVALID|END;
                }
                break;
            case PLAY_AND_DEAL_1:
                try{
                    final ThousandProtocol.Deserialized_1_PLAY_AND_DEAL d
                        = new ThousandProtocol.Deserialized_1_PLAY_AND_DEAL
                        (move);
                    thirdShift = d.shift;
                    newCards = new ThousandCardSet();
                    newCards.value = d.thousandCardSet;
                }catch(MessageDeserializationException e){
                    Output.p("Deserialized_1_PLAY_AND_DEAL failed.");
                    return INVALID|END;
                }
                break;
            default:
                Output.p("UNEXPECTED message type in stage "+stage+"!");
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
                                 trumpShift.value,
                                 smallPoints[getPreviousPlayer(2)],
                                 smallPoints[getPreviousPlayer()],
                                 smallPoints[turn],
                                 turnIncrement);

            this.jCards.cardsAtTable.add
                (new JCards.CardAtTable(THOUSAND_SHIFT_TO_CARD[thirdShift],
                                 0f,//(float)Math.random()-0.5f,
                                 0f,//(float)Math.random()-0.5f,
                                 (opponentsTurn(1)
                                  ?-Math.PI/4:(opponentsTurn(2)
                                               ?Math.PI/4
                                               :0.0)),
                                 JCards.UNCLICKABLE));

            if(myTurn())
                sets[turn].removeShift(thirdShift);
            
            if(sets[table.myTurnGamePlayer].isEmpty()){
                //No more cards
                //Add points for each player:
                for(byte p = 0; p<numberOfPlayers; p++){
                    if(p==biddingWinner){
                        if(smallPoints[p].value >= 10*bids10[p])
                            bigPoints10[p] += bids10[p];
                        else
                            bigPoints10[p] -= bids10[p];
                    }else{
                        if(bigPoints10[p] < 80)
                            bigPoints10[p] += (smallPoints[p].value+4)/10;
                    }
                }

                addPointsTableRow();

                //The end?
                if(bigPoints10[biddingWinner]>=100){
                    stage = Stage.ENDED;
                    jCards.hands.clear();
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
                    sets[table.myTurnGamePlayer].value = newCards.value;
                    initializeHands();
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
                                (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
                                              JCards.UNCLICKABLE),
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
                                (new JCards.HandCard(THOUSAND_SHIFT_TO_CARD[shift],
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
