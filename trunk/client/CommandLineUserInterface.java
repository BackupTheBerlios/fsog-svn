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

/*
//Very old, probably won't be used again.
public class CommandLineUserInterface{
    private final Table table;

    public CommandLineUserInterface(final Table table){
        this.table = table;
    }

    private synchronized void printTable(){
        System.out.print("Players at table now: ");
        boolean first = true;
        for(java.util.Map.Entry<Byte,TablePlayer> e
                : this.table.getPlayersDeepCopy().entrySet()){
            if(!first)
                System.out.print(", ");
            System.out.print(""+e.getValue());
            first = false;
        }
        System.out.println(".");
    }

    public synchronized void newPlayer(final Byte tablePlayerId,
                                       final TablePlayer player){
        System.out.println(""+player+" joined the table.");
        this.printTable();
    }
}
*/
