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

public class Receiver extends Thread{

    private final Socket socket;
    private final GeneralProtocol.GeneralHandler generalHandler;

    public Receiver(final Socket socket,
                    final GeneralProtocol.GeneralHandler generalHandler){
        this.socket = socket;
        this.generalHandler = generalHandler;
    }

    public void run(){

        try{
            while(true){
                //Receive message:
                //TODO: Is Socket's buffer long enough for storing,
                //say, 100 messages if handling takes a long time?
                final Message message
                    = TransportProtocol.receive(this.socket);
                
                if(!GeneralProtocol.handle(message,this.generalHandler)){
                    System.err.println("Message handling failed. Message type:"
                                       +GeneralProtocol.lookupMessageType(message));
                    System.err.println("Message:");
                    System.err.println(message.toString());
                }
            }
        }catch(final Exception e){
            System.err.println("Exception: "+e);
            System.err.println("Stack trace:");
            e.printStackTrace();
            //TODO: Maybe this.socket.close()?
        }
    }
}
