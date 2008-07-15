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
import java.util.*;

public class TableCreator{

    private static boolean printDebug = false;
    private static void d(final String s){
        if(printDebug) System.out.println(s);
    }

    public static void main(final String[] arguments){
        try{
            if(arguments.length>=3 && arguments[2].equals("-d"))
                printDebug = true;

            d("Debug mode enabled.");

            d("Creating socket...");
            final Socket socket
                = new Socket(arguments[0],
                             Integer.parseInt(arguments[1]));

            d("Socket created: "+socket);
            d("Serializing message...");
            final Vector<Byte> query
                = GeneralProtocol.serialize_1_CREATE_THOUSAND_TABLE();

            d("Message serialized: "+query);

            d("Sending message...");
            TransportProtocol.send(query,socket);

            d("Query sent: "+query);

            d("Awaiting response...");
            //Receive response:
            final Vector<Byte> response
                = TransportProtocol.receive(socket);
            
            d("Response: "+response);

            final GeneralProtocol.Deserialized_1_TABLE_CREATED deserialized
                = new GeneralProtocol.Deserialized_1_TABLE_CREATED(response);

            socket.close();

            System.out.println(""+deserialized.id);
            
        }catch(final Exception e){
            System.err.println("Exception: "+e);
            System.err.println("Stack trace:");
            e.printStackTrace();
        }
    }
}
