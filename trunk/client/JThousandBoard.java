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
    
    public JThousandBoard(final byte numberOfPlayers,
                          final Table table,
                          final MoveListener moveListener,
                          final JTabbedPane jTabbedPane){
        super(numberOfPlayers,
              table,
              moveListener,
              jTabbedPane);
    }

    public boolean initialize(final java.util.Vector<Byte> initialMessage){

        try{
            final ThousandProtocol.Deserialized_1_DEAL_7_CARDS deserialized
                = new ThousandProtocol.Deserialized_1_DEAL_7_CARDS(initialMessage);

            Output.d("cardSet24: "+deserialized.cardSet24);

            final Hand myHand = new Hand("Me",0.5f,0.75f,0.5f);

            final Vector<Byte> sevenCards
                = new CardUtilities.CardSet24(deserialized.cardSet24)
                .toVector(CardUtilities.CardSet24.THOUSAND_ORDER);

            for(Byte card : sevenCards)
                myHand.cards.add(new HandCard(card,card));

            final Hand mustHand = new Hand(null,0.5f,0.1f,0.15f);

            for(int i=0; i<3; i++)
                mustHand.cards.add(new HandCard(Card.UNKNOWN,NO_CARD));

            final Hand firstOpponentHand
                = new Hand(this.table.getFirstOpponent().getScreenName(),
                           0.1f,0.05f,0.15f);

            final Hand secondOpponentHand
                = new Hand(this.table.getSecondOpponent().getScreenName(),
                           0.8f,0.05f,0.15f);

            for(int i=0; i<7; i++){
                firstOpponentHand.cards.add(new HandCard(Card.UNKNOWN,NO_CARD));
                secondOpponentHand.cards.add(new HandCard(Card.UNKNOWN,NO_CARD));
            }

            if(this.myTurn())
                myHand.hasArrowAbove = true;
            if(this.opponentsTurn(1))
                firstOpponentHand.hasArrowBelow = true;
            if(this.opponentsTurn(2))
                secondOpponentHand.hasArrowBelow = true;

            this.hands.clear();
            this.hands.add(myHand);
            this.hands.add(mustHand);
            this.hands.add(firstOpponentHand);
            this.hands.add(secondOpponentHand);

            if(this.myTurn())
                showBidding();

            this.repaint();
            return true;
        }catch(MessageDeserializationException e){
            Output.p("Problem: "+e);
            return false;
        }
    }

    private void showBidding(){
        final JPanel panel = new JPanel();
        this.jTabbedPane.addTab("Bidding", null, panel,
                                "Bidding in the game of Thousand");
        final int index = this.jTabbedPane.getTabCount()-1;
        this.jTabbedPane.setMnemonicAt(index, KeyEvent.VK_B);
        this.jTabbedPane.setSelectedIndex(index);
    }

    public void cardClicked(final int virtualColor){
        final byte card = this.hands.get(0).removeCard(virtualColor);

        this.cardsAtTable.add
            (new CardAtTable(card,
                             0f,//(float)Math.random()-0.5f,
                             0f,//(float)Math.random()-0.5f,
                             0.0,//Math.PI*Math.random(),
                             NO_CARD));
    }

    
    public int moveMade(final Vector<Byte> move,
                        final int[] endResult){
        return VALID|CONTINUE;
    }
    
}
