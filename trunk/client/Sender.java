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

import java.net.*;
import java.util.*;

public abstract class Sender extends Thread{

    private final Socket socket;
    private final LinkedList< Vector<Byte> > messages;

    public Sender(final Socket socket,
                  final String nameSuffix){
        super("SND "+nameSuffix);
        this.socket = socket;
        this.messages = new LinkedList< Vector<Byte> >();
        this.start();
    }

    public abstract void d(final String message);
    public abstract void e(final String message,final Throwable t);

    public synchronized void quit(){
        this.messages.addFirst(null);
        this.notify();
    }

    public synchronized void send(final Vector<Byte> message){
        this.messages.addLast(message);
        this.notify();
    }

    private synchronized Vector<Byte> get() throws InterruptedException{
        while(this.messages.size()==0 || this.socket==null)
            this.wait();
        return messages.removeFirst();
    }

    public void run(){

        try{
            Vector<Byte> message;
            while((message = this.get())!=null){
                this.transportSend(message,this.socket);
            }
            d("Normal loop termination.");
        }catch(final InterruptedException e){
            d("Thread interrupted, so we exit the loop.");
        }catch(final SocketException e){
            d("Socket closed, so we exit the loop.");
        }catch(final Exception e){
            e("Unexpected problem.",e);
            //TODO: Terminate program?
        }
        d("Sender terminated.");
    }

    public void transportSend(final Vector<Byte> message,
                              final Socket socket)
        throws java.io.IOException
    {
        d("TP.snd "+GeneralProtocol.lookupMessageType(message)
          +" "+Message.toString(message));

        final Vector<Byte> bytes
            = message;
        
        final java.io.OutputStream stream
            = socket.getOutputStream();
        
        //Two first bytes transferred represent the length of the message.
        final int length = bytes.size();
        stream.write(0xFF&(length>>8));
        stream.write(0xFF&length);
        for(int i=0;i<length;i++)
            stream.write(bytes.get(i));
        //TODO: Is it OK to do it?
        stream.flush();
    }
}
