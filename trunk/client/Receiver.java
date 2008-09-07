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
import javax.swing.*;

public abstract class Receiver extends Thread{

    private final Socket socket;
    private final GeneralProtocol.GeneralHandler generalHandler;

    public Receiver(final Socket socket,
                    final GeneralProtocol.GeneralHandler generalHandler,
                    final String nameSuffix){
        super("RCV "+nameSuffix);
        this.socket = socket;
        this.generalHandler = generalHandler;
        if(socket != null)
            this.start();
    }

    public abstract void d(final String message);
    public abstract void e(final String message,final Throwable t);

    public void run(){

        try{
            while(true){

                //Receive message:
                //TODO: Is Socket's buffer long enough for storing,
                //say, 100 messages if handling takes a long time?
                final Vector<Byte> message
                    = transportReceive(this.socket);

                //TODO: What if called twice before runnable invoked? What's
                //the order?
                SwingUtilities.invokeAndWait(new Runnable(){
                        public void run() {
                            if(!GeneralProtocol.handle(message,
                                                       generalHandler)){
                                d("Message handling failed. Message type:"
                                  +GeneralProtocol.lookupMessageType(message));
                                d("Message:");
                                d(Message.toString(message));
                            }
                        }
                    });
            }
        }catch(final SocketException e){
            d("Socket closed.");
        }catch(final Exception e){
            e("Unexpected problem.",e);
            //TODO: Maybe this.socket.close()?
        }
        d("Thread terminated.");
    }

    private Vector<Byte> transportReceive(final Socket socket)
        throws Exception
    {
        final java.io.InputStream stream
            = socket.getInputStream();

        final int firstByte = stream.read();
        if(firstByte==-1)
            throw new Exception("TransportProtocol.receive(...) EOF problem.");

        final int secondByte = stream.read();
        if(secondByte==-1)
            throw new Exception("TransportProtocol.receive(...) EOF problem.");

        final int incomingMessageLength
            = (firstByte<<8)|secondByte;

        final Vector<Byte> message = new Vector<Byte>();

        for(int i=0;i<incomingMessageLength;i++){
            final int b = stream.read();
            if(b==-1)
                throw new Exception("TransportProtocol.receive(...) EOF problem.");
            message.add((byte)b);
        }

        d("TP.rcv "+GeneralProtocol.lookupMessageType(message)
          +" "+Message.toString(message));
        return message;
    }

}
