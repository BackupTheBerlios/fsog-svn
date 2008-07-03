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

public class Sender extends Thread{

    private Socket socket;
    private final java.util.LinkedList<Message> messages;

    private static Sender instance = new Sender();

    private Sender(){
        this.socket = null;
        this.messages = new java.util.LinkedList<Message>();
        this.start();
    }

    public static void setSocket(final Socket socket){
        Sender.instance.socket(socket);
    }        

    public static void send(final Message message){
        Sender.instance.queue(message);
    }

    public synchronized void socket(final Socket socket){
        this.socket = socket;
        this.notify();
    }

    public synchronized void queue(final Message message){
        this.messages.addLast(message);
        this.notify();
    }

    private synchronized Message get() throws InterruptedException{
        while(this.messages.size()==0 || this.socket==null)
            this.wait();
        return messages.removeFirst();
    }

    public void run(){

        try{
            while(true){
                final Message message
                    = this.get();
                
                TransportProtocol.send(message,
                                       this.socket);
            }
        }catch(final InterruptedException e){
            //Thread interrupted, so we exit the loop.
        }catch(final Exception e){
            System.err.println("Exception: "+e);
            System.err.println("Stack trace:");
            e.printStackTrace();
            //TODO: Terminate program?
        }
    }
}
