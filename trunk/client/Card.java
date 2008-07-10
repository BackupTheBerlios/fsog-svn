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

//Used in card games. You can encode a card on one byte by bitwise
//disjunction of value and color. Use VALUE_MASK and SUIT_MASK to
//decode. The guarantee is that 0<(value|suit)<=127, so it can be used
//for lookup in an array.

public class Card{
    public static final byte VALUE_MASK = (byte) 0x0F;

    public static final byte ACE = (byte) 0x01;
    public static final byte TWO = (byte) 0x02;
    public static final byte THREE = (byte) 0x03;
    public static final byte FOUR = (byte) 0x04;
    public static final byte FIVE = (byte) 0x05;
    public static final byte SIX = (byte) 0x06;
    public static final byte SEVEN = (byte) 0x07;
    public static final byte EIGHT = (byte) 0x08;
    public static final byte NINE = (byte) 0x09;
    public static final byte TEN = (byte) 0x0A;
    public static final byte JACK = (byte) 0x0B;
    public static final byte QUEEN = (byte) 0x0C;
    public static final byte KING = (byte) 0x0D;
    public static final byte JOKER = (byte) 0x0E;

    public static final byte SUIT_MASK = (byte) 0xF0;

    //Suits used by all cards except for Joker:
    public static final byte HEART = (byte) 0x10;
    public static final byte DIAMOND = (byte) 0x20;
    public static final byte CLUB = (byte) 0x30;
    public static final byte SPADE = (byte) 0x40;
    //Suits used for Joker:
    public static final byte BLACK = (byte) 0x50;
    public static final byte RED = (byte) 0x60;
    public static final byte BLUE = (byte) 0x70;

    public static final String[] valueStrings = getValueStrings();

    public static final String[] suitStrings = getSuitStrings();

    public static final Color[] colors4 = getColors4();

    public static final Color[] colors2 = getColors2();

    public static final String[] getValueStrings(){
        final String[] result = new String[127];
        final byte[] someSuits = {HEART,DIAMOND,CLUB,SPADE};
        for(byte suit : someSuits){
            result[ACE|suit] = "A";
            result[TWO|suit] = "2";
            result[THREE|suit] = "3";
            result[FOUR|suit] = "4";
            result[FIVE|suit] = "5";
            result[SIX|suit] = "6";
            result[SEVEN|suit] = "7";
            result[EIGHT|suit] = "8";
            result[NINE|suit] = "9";
            result[TEN|suit] = "10";
            result[JACK|suit] = "J";
            result[QUEEN|suit] = "Q";
            result[KING|suit] = "K";
        }
        result[JOKER|BLACK] = "*";
        result[JOKER|RED] = "*";
        result[JOKER|BLUE] = "*";
        return result;
    }

    public static final String[] getSuitStrings(){
        final String[] result = new String[127];

        final byte[] someValues
            = {ACE,TWO,THREE,FOUR,FIVE,SIX,SEVEN,
               EIGHT,NINE,TEN,JACK,QUEEN,KING};
        for(byte value : someValues){
            result[value|HEART] = "\u2665";
            result[value|DIAMOND] = "\u2666";
            result[value|CLUB] = "\u2663";
            result[value|SPADE] = "\u2660";
        }

        //Jokers don't have any suit symbol printed.
        result[JOKER|BLACK] = "";
        result[JOKER|RED] = "";
        result[JOKER|BLUE] = "";

        return result;
    }

    public static final Color[] getColors4(){
        final Color[] result = new Color[127];

        final byte[] someValues
            = {ACE,TWO,THREE,FOUR,FIVE,SIX,SEVEN,
               EIGHT,NINE,TEN,JACK,QUEEN,KING};
        for(byte value : someValues){
            result[value|HEART] = Color.RED;
            result[value|DIAMOND] = Color.BLUE.darker();
            result[value|CLUB] = Color.GREEN.darker();
            result[value|SPADE] = Color.BLACK;
        }

        //Jokers don't have any suit symbol printed.
        result[JOKER|BLACK] = Color.BLACK;
        result[JOKER|RED] = Color.RED;
        result[JOKER|BLUE] = Color.BLUE;

        return result;
    }

    public static final Color[] getColors2(){
        final Color[] result = new Color[127];

        final byte[] someValues
            = {ACE,TWO,THREE,FOUR,FIVE,SIX,SEVEN,
               EIGHT,NINE,TEN,JACK,QUEEN,KING};
        for(byte value : someValues){
            result[value|HEART] = Color.RED;
            result[value|DIAMOND] = Color.RED;
            result[value|CLUB] = Color.BLACK;
            result[value|SPADE] = Color.BLACK;
        }

        //Jokers don't have any suit symbol printed.
        result[JOKER|BLACK] = Color.BLACK;
        result[JOKER|RED] = Color.RED;
        result[JOKER|BLUE] = Color.BLUE;

        return result;
    }
}
