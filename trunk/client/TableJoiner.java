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

import java.net.Socket;

public class TableJoiner{

    private static boolean printDebug = false;
    private static void d(final String s){
        if(printDebug) System.out.println(s);
    }

    public static void main(final String[] arguments){
        try{
            final String serverHost = arguments[0];
            final int serverPort = Integer.parseInt(arguments[1]);
            final long tableId = Long.parseLong(arguments[2]);
            final String screenName = arguments[3];
            if(arguments.length>=5 && arguments[4].equals("-d"))
                printDebug = true;

            d("Debug mode enabled.");

            d("Creating socket...");
            final Socket socket
                = new Socket(serverHost,
                             serverPort);
            d("Socket created: "+socket);

            final Table table = new Table();

            final UserInterface ui = new CommandLineUserInterface(table);

            final Sender sender = new Sender(socket);
            sender.start();

            final GeneralProtocol.AbstractGeneralHandler handler
                = new GeneralHandler(ui,sender,table);

            final Receiver receiver = new Receiver(socket,
                                                   handler);
            receiver.start();

            sender.send
                (GeneralProtocol.serialize_1_JOIN_TABLE_TO_PLAY(tableId,
                                                                screenName));

        }catch(final Exception e){
            System.err.println("Exception: "+e);
            System.err.println("Stack trace:");
            e.printStackTrace();
        }
    }
}
