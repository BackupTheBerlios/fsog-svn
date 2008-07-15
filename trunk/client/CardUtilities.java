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

//For Color:
import java.awt.*;
import java.util.*;

public class CardUtilities{

    public static final String[] valueStrings = getValueStrings();

    public static final String[] suitStrings = getSuitStrings();

    public static final Color[] colors4 = getColors4();

    /** Space-efficient representation of card set for 55-card deck. Each
     * card's presence is specified by one bit in a 64-bit long value.
     */
    public static class CardSet55{
        private long value;

        public CardSet55(){
            this.value = 0L;
        }

        public void addCard(final byte card){
            this.value|=cardBit(card);
        }

        public boolean contains(final byte card){
            return (this.value & cardBit(card))!=0l;
        }

        public Vector<Byte> toVector(){
            final Vector<Byte> result = new Vector<Byte>();

            final byte[] someSuits
                = {Card.HEART,Card.DIAMOND,Card.CLUB,Card.SPADE};

            final byte[] someValues
                = {Card.ACE,Card.TWO,Card.THREE,Card.FOUR,
                   Card.FIVE,Card.SIX,Card.SEVEN,Card.
                   EIGHT,Card.NINE,Card.TEN,Card.JACK,
                   Card.QUEEN,Card.KING};

            for(byte suit : someSuits)
                for(byte value : someValues){
                    final byte card = (byte)(suit|value);
                    if(this.contains(card))
                        result.add(card);
                }

            final byte[] jokerSuits
                = {Card.RED,Card.BLACK,Card.BLUE};

            for(byte suit : jokerSuits){
                final byte card = (byte)(suit|Card.JOKER);
                if(this.contains(card))
                    result.add(card);
            }
            return result;
        }

        //For each card assigns which bit represents presence of that
        //card.
        private long cardBit(final byte card){
            final long value = card & Card.VALUE_MASK;
            final int suit = card & Card.SUIT_MASK;
            switch(suit){
            case Card.HEART: return 1l<<value;
            case Card.DIAMOND: return 1l<<(13+value);
            case Card.CLUB: return 1l<<(26+value);
            case Card.SPADE: return 1l<<(39+value);
                //For Jokers (we know the value is Card.JOKER):
            case Card.RED: return 1l<<52;
            case Card.BLACK: return 1l<<53;
            case Card.BLUE: return 1l<<54;
            }
            return 0l;
        }
    }

    /** Space-efficient representation of card set for 24-card deck. Each
     * card's presence is specified by one bit in a 32-bit int value.
     */
    public static class CardSet24{
        private int value;

        public CardSet24(final int value){
            this.value = value;
        }

        public boolean contains(final byte card){
            return (this.value & cardBit(card))!=0;
        }

        public static byte[] THOUSAND_ORDER = {
            Card.ACE|Card.HEART,
            Card.TEN|Card.HEART,
            Card.KING|Card.HEART,
            Card.QUEEN|Card.HEART,
            Card.JACK|Card.HEART,
            Card.NINE|Card.HEART,
            Card.ACE|Card.DIAMOND,
            Card.TEN|Card.DIAMOND,
            Card.KING|Card.DIAMOND,
            Card.QUEEN|Card.DIAMOND,
            Card.JACK|Card.DIAMOND,
            Card.NINE|Card.DIAMOND,
            Card.ACE|Card.CLUB,
            Card.TEN|Card.CLUB,
            Card.KING|Card.CLUB,
            Card.QUEEN|Card.CLUB,
            Card.JACK|Card.CLUB,
            Card.NINE|Card.CLUB,
            Card.ACE|Card.SPADE,
            Card.TEN|Card.SPADE,
            Card.KING|Card.SPADE,
            Card.QUEEN|Card.SPADE,
            Card.JACK|Card.SPADE,
            Card.NINE|Card.SPADE
        };

        public Vector<Byte> toVector(final byte[] order){
            final Vector<Byte> result = new Vector<Byte>();

            for(byte card : order)
                if(this.contains(card))
                    result.add(card);

            return result;
        }

        //For each card assigns which bit represents presence of that
        //card.
        private int cardBit(final byte card){
            final int value = card & Card.VALUE_MASK;
            final int suit = card & Card.SUIT_MASK;
            switch(suit){
            case Card.HEART: return 1<<value;
            case Card.DIAMOND: return 1<<(6+value);
            case Card.CLUB: return 1<<(12+value);
            case Card.SPADE: return 1<<(18+value);
            }
            return 0;
        }
    }

    private static final String[] getValueStrings(){
        final String[] result = new String[128];
        final byte[] someSuits
            = {Card.HEART,Card.DIAMOND,Card.CLUB,Card.SPADE};
        for(byte suit : someSuits){
            result[Card.ACE|suit] = "A";
            result[Card.TWO|suit] = "2";
            result[Card.THREE|suit] = "3";
            result[Card.FOUR|suit] = "4";
            result[Card.FIVE|suit] = "5";
            result[Card.SIX|suit] = "6";
            result[Card.SEVEN|suit] = "7";
            result[Card.EIGHT|suit] = "8";
            result[Card.NINE|suit] = "9";
            result[Card.TEN|suit] = "10";
            result[Card.JACK|suit] = "J";
            result[Card.QUEEN|suit] = "Q";
            result[Card.KING|suit] = "K";
        }
        result[Card.JOKER|Card.BLACK] = "*";
        result[Card.JOKER|Card.RED] = "*";
        result[Card.JOKER|Card.BLUE] = "*";
        return result;
    }

    private static final String[] getSuitStrings(){
        final String[] result = new String[128];

        final byte[] someValues
            = {Card.ACE,Card.TWO,Card.THREE,Card.FOUR,
               Card.FIVE,Card.SIX,Card.SEVEN,Card.
               EIGHT,Card.NINE,Card.TEN,Card.JACK,
               Card.QUEEN,Card.KING};
        for(byte value : someValues){
            result[value|Card.HEART] = "\u2665";
            result[value|Card.DIAMOND] = "\u2666";
            result[value|Card.CLUB] = "\u2663";
            result[value|Card.SPADE] = "\u2660";
        }

        //Jokers don't have any suit symbol printed.
        result[Card.JOKER|Card.BLACK] = "";
        result[Card.JOKER|Card.RED] = "";
        result[Card.JOKER|Card.BLUE] = "";

        return result;
    }

    private static final Color[] getColors4(){
        final Color[] result = new Color[128];

        final byte[] someValues
            = {Card.ACE,Card.TWO,Card.THREE,Card.FOUR,
               Card.FIVE,Card.SIX,Card.SEVEN,Card.EIGHT,
               Card.NINE,Card.TEN,
               Card.JACK,Card.QUEEN,Card.KING};
        for(byte value : someValues){
            result[value|Card.HEART] = Color.RED;
            result[value|Card.DIAMOND] = Color.BLUE.darker();
            result[value|Card.CLUB] = Color.GREEN.darker().darker();
            result[value|Card.SPADE] = Color.BLACK;
        }

        //Jokers don't have any suit symbol printed.
        result[Card.JOKER|Card.BLACK] = Color.BLACK;
        result[Card.JOKER|Card.RED] = Color.RED;
        result[Card.JOKER|Card.BLUE] = Color.BLUE;

        return result;
    }
}
