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

public class TransportProtocol{

    public static void send(final Vector<Byte> message,
                            final Socket socket)
    throws java.io.IOException
    {
        Output.d("TP.snd "+GeneralProtocol.lookupMessageType(message)
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

    public static Vector<Byte> receive(Socket socket)
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

        //System.err.println("Detected length "+incomingMessageLength);

        final Vector<Byte> message = new Vector<Byte>();

        for(int i=0;i<incomingMessageLength;i++){
            final int b = stream.read();
            if(b==-1)
                throw new Exception("TransportProtocol.receive(...) EOF problem.");
            message.add((byte)b);
        }

        Output.d("TP.rcv "+GeneralProtocol.lookupMessageType(message)
                 +" "+Message.toString(message));
        return message;
    }
}
