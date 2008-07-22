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

/** WARNING!!!!!!!!!!!!  This class is rewritten from C++, to be used
    on the client side. If you change anything here, you need to
    modify the C++ version as well.
*/

import java.util.*;

public class ThousandCardSet{

    private static byte points[] = {11,10,4,3,2,0,
                                    11,10,4,3,2,0,
                                    11,10,4,3,2,0,
                                    11,10,4,3,2,0};

    private static byte marriagePoints[] = {0,0,100,100,0,0,
                                            0,0,80,80,0,0,
                                            0,0,60,60,0,0,
                                            0,0,40,40,0,0};


    public static byte valueShift(final byte shift){
        return (byte)(shift%6);
    }

    public static byte suitShift(final byte shift){
        return (byte)(6*(shift/6));
    }

    private boolean hasAnyOf(final int set){
        return (value&set)!=0;
    }

    public int value;

    public ThousandCardSet(){
        value = 0;
    }

    public void setEmpty(){
        value = 0;
    }

    public boolean isEmpty(){
        return value == 0;
    }

    public void addShift(final byte shift){
        value|=(1<<shift);
    }

    public void addAll(final ThousandCardSet other){
        value|=other.value;
    }

    public boolean containsShift(final byte shift){
        return (value & (1<<shift)) != 0;
    }

    public boolean containsShift(final int shift){
        return (value & (1<<shift)) != 0;
    }

    public void removeShift(final byte shift){
        value &= (~(1<<shift));
    }

    public boolean removeFirstShift(final byte shift,
                                    B trumpShift,
                                    S firstSmallPoints)
    {
        //Did the player have the card?
        if(!containsShift(shift))
            return false;

        //Remove the card:
        removeShift(shift);

        //Shall we set new trump?
        if((valueShift(shift)==ThousandProtocol.QUEEN_SHIFT
            && containsShift(shift+1))
           ||(valueShift(shift)==ThousandProtocol.KING_SHIFT
              && containsShift(shift-1)))
            {
                firstSmallPoints.value+=marriagePoints[shift];
                trumpShift.value = suitShift(shift);
            }
        return true;
    }

    public boolean removeSecondShift(final byte firstShift,
                                     final byte secondShift,
                                     final byte trumpShift){
        
        //If there's no such card, return false.
        if(!containsShift(secondShift))
            return false;
        
        //Remove the card:
        removeShift(secondShift);
  
        final byte firstValueShift = valueShift(firstShift);
        final byte firstSuitShift = suitShift(firstShift);
        final byte secondSuitShift = suitShift(secondShift);
  
        //Is it the same suit?
        if(firstSuitShift==secondSuitShift)
            {//Same suit
                //If higher card was played, it's a valid move.
                if(secondShift>firstShift)
                    return true;
                else
                    {//Lower card was played. It's a valid move, if there was no
                        //higher.
                        // 000100 000000 - Queen of clubs played first (shift 6+2)
                        // 000000 111111 - 0x3f
                        // 000000 000111 - 0x3f >> 2+1
                        // 111000 000000 - All higher in same suit
                        return !hasAnyOf((0x3F>>(firstValueShift+1))
                                         <<(firstShift+1));
                    }
            }
        else
            {//Different suit was played.
                //If the player has cards of first card suit, move is invalid.
                if(hasAnyOf(0x3F<<firstSuitShift))
                    return false;
                //Was trump played?
                if(secondSuitShift==trumpShift)
                    {//Trump on non--trump was played, so it's OK.
                        return true;
                    }
                else
                    {//Non--trump played. It's OK if we don't have a trump.
                        return !hasAnyOf(0x3F<<trumpShift);
                    }
            }
    }

    public boolean removeThirdShift(final byte firstShift,
                                    final byte secondShift,
                                    final byte thirdShift,
                                    final byte trumpShift,
                                    S firstSmallPoints,
                                    S secondSmallPoints,
                                    S thirdSmallPoints,
                                    B turnIncrement)
    {
        //If there's no such card, return false.
        if(!containsShift(thirdShift))
            return false;

        //Remove the card:
        removeShift(thirdShift);
        
        final short sum
            = (short) (points[firstShift]
                       + points[secondShift]
                       + points[thirdShift]);
    
        //final byte firstValueShift = valueShift(firstShift);
        final byte firstSuitShift = suitShift(firstShift);
        final byte secondValueShift = valueShift(secondShift);
        final byte secondSuitShift = suitShift(secondShift);
        final byte thirdSuitShift = suitShift(thirdShift);

        //Is it the same suit as the first card?
        if(firstSuitShift==thirdSuitShift)
            {//Same suit
                //If second was trump (and first not), we don't need to play higher:
                if(secondSuitShift==trumpShift && secondSuitShift!=firstSuitShift)
                    {
                        secondSmallPoints.value+=sum;
                        turnIncrement.value=2;
                        return true;
                    }
                //Second was not trump (or all are trumps). Which card should be beaten?
                final boolean secondWins
                    = secondSuitShift==firstSuitShift && secondShift>firstShift;
                final byte maxShift
                    = secondWins ? secondShift:firstShift;
                final byte maxValueShift = valueShift(maxShift);
                //If higher card was played, it's a valid move.
                if(thirdShift>maxShift)
                    {
                        thirdSmallPoints.value+=sum;
                        turnIncrement.value=0;
                        return true;
                    }
                else
                    {//Lower card was played. It's a valid move, if there was no
                        //higher.
                        // 000100 000000 - Queen of clubs played first (shift 6+2)
                        // 000000 111111 - 0x3f
                        // 000000 000111 - 0x3f >> 2+1
                        // 111000 000000 - All higher in same suit
                        if(secondWins)
                            {
                                secondSmallPoints.value+=sum;
                                turnIncrement.value=2;
                            }
                        else
                            {
                                firstSmallPoints.value+=sum;
                                turnIncrement.value=1;
                            }
                        return !hasAnyOf((0x3F>>(maxValueShift+1))
                                         <<(maxShift+1));
                    }
            }
        else
            {//Different suit was played.
                //If the player has cards of first card suit, move is invalid.
                if(hasAnyOf(0x3F<<firstSuitShift))
                    return false;
                //Was trump played?
                if(thirdSuitShift==trumpShift)
                    {//1:non--trump 3:trump
                        //Was the second card also a trump?
                        if(secondSuitShift==trumpShift)
                            {//1:non--trump 2:trump 3:trump
                                //If we can, we have to beat the second card.
                                if(thirdShift>secondShift)
                                    {
                                        thirdSmallPoints.value+=sum;
                                        turnIncrement.value=0;
                                        return true;
                                    }
                                else
                                    {//Lower card was played. It's a valid move, if there was no
                                        //higher.
                                        secondSmallPoints.value+=sum;
                                        turnIncrement.value=2;
                                        //For how this is calculated, read above.
                                        return !hasAnyOf((0x3F>>(secondValueShift+1))
                                                         <<(secondShift+1));
                                    }                
                            }
                        else
                            {//1:non--trump 2:non--trump 3:trump
                                thirdSmallPoints.value+=sum;
                                turnIncrement.value=0;
                                return true;
                            }
                    }
                else
                    {//1:non--trump 3:non--trump
                        if(secondSuitShift==trumpShift
                           ||(secondSuitShift==firstSuitShift && secondShift>firstShift))
                            {
                                secondSmallPoints.value+=sum;
                                turnIncrement.value=2;
                            }
                        else
                            {
                                firstSmallPoints.value+=sum;
                                turnIncrement.value=1;
                            }
                        //Non--trump played. It's OK if we don't have a trump.
                        return !hasAnyOf(0x3F<<trumpShift);
                    }
            }
    }
}
