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

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientUDPSocket{

    //private final int myPort;
    private final DatagramSocket mySocket;

    private final byte[] buffer;

    public ClientUDPSocket()
        throws SocketException,IOException{

        //this.myPort = 17389;

        //Create a datagram socket on any port.
        this.mySocket = new DatagramSocket();

        //TODO buffer size guarantee.
        this.buffer = new byte [100*1024];
    }

    public void sendMessage(final Message message,
                            final InetAddress serverAddress)
        throws IOException
    {
        final Vector<Byte> rawData = message.getRawData();

        for(int i=0;i<rawData.size();i++)
            buffer[i]=rawData.get(i);

        final DatagramPacket packet
            = new DatagramPacket(buffer,
                                 rawData.size(),
                                 serverAddress,
                                 Protocol.getServerUDPPort_1());

        this.mySocket.send(packet);
    }

    /**
       @param timeout Timeout in seconds.Timeout can also be zero. A
       timeout of zero is interpreted as an infinite timeout.
       @return Returns null if timeout expired or other socket-related
       problem was found. Otherwise, returns an instance of Message.
     */
    final Message receiveMessage(final float timeout){
        try{
            final DatagramPacket packet
                = new DatagramPacket(this.buffer,this.buffer.length);

            this.mySocket.setSoTimeout((int)(1000*timeout));

            this.mySocket.receive(packet);

            // display response
            //final String received
            //    = new String(packet.getData(), 0, packet.getLength());
            //System.out.println("The received packet: " + received);

            return new Message(this.buffer,packet.getLength());
        }catch(Exception e){
            return null;
        }
    }

    final void close(){
        this.mySocket.close();
    }
}
