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

import java.net.InetAddress;
import java.util.*;

/** This class is a simple client.
 */

public class ConsoleThousand {

    private final ClientUDPSocket mySocket;
    private final InetAddress serverAddress;
    private final String nick;
    private final String password;

    public ConsoleThousand(final InetAddress serverAddress,
                           final String nick,
                           final String password)
        throws java.net.SocketException,
               java.io.IOException
    {
        this.mySocket=new ClientUDPSocket();
        this.serverAddress=serverAddress;
        this.nick=nick;
        this.password=password;
    }

    private static final String usage
        = "Usage: java ConsoleThousand -s <server host name> -n <nickname>"
        + " -p <password>";

    private static void missingValue(final String key){
        System.out.println("Missing value for the "+key+" key.");
        System.out.println(usage);
        System.exit(1);
    }

    private static void checkMissingKey(final String key,
                                        final String value){
        if(value==null){
            System.out.println("You have forgotten about the "+key+" key.");
            System.out.println(usage);
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {

        final Map<String,String> options
            =new TreeMap<String,String>();

        options.put("-s",null);
        options.put("-n",null);
        options.put("-p",null);

        for(int i=0;i<args.length;i++){
            
            final String option = args[i];
            
            if(!options.containsKey(option)){
                System.out.println("Unrecognized option: "+args[i]);
                System.out.println(usage);
                System.exit(1);
            }

            i++;
            if(i>=args.length)
                missingValue(option);
            
            final String argument = args[i];

            options.put(option,argument);
        }
    
        for(Map.Entry<String,String> entry : options.entrySet())
            checkMissingKey(entry.getKey(),entry.getValue());

        final InetAddress serverAddress
            = InetAddress.getByName(options.get("-s"));

        final ConsoleThousand consoleThousand
            = new ConsoleThousand(serverAddress,
                                  options.get("-n"),
                                  options.get("-p"));
        consoleThousand.run();
    }

    public void run() throws Exception{

        final Message logIn = Protocol.serialize_1_LOG_IN("nick","password");

        System.out.println("Sending logIn...");
        mySocket.sendMessage(logIn,serverAddress);
        System.out.println("logIn sent.");

        System.out.println("Receiving r...");
        final Message r = mySocket.receiveMessage();

        System.out.println("Message received: "+r);
        
        mySocket.close();
    }
}
