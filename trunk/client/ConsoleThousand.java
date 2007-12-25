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
        e("Missing value for the "+key+" key.");
        e(usage);
        System.exit(1);
    }

    private static void checkMissingKey(final String key,
                                        final String value){
        if(value==null){
            e("You have forgotten about the "+key+" key.");
            e(usage);
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
                e("Unrecognized option: "+args[i]);
                e(usage);
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

    /** Debug */
    private static void d(final String message){
        System.out.println(message);
    }

    /** Error */
    private static void e(final String message){
        System.out.println(message);
    }

    /** Information */
    private static void i(final String message){
        System.out.println(message);
    }

    /** Warning */
    private static void w(final String message){
        System.out.println(message);
    }


    private final static float[] timeouts = {0.1F,0.25F,0.5F,1.0F,2.0F,5.0F,10.0F};

    /**
       Send message and await for response of type within types. This
       will work as long as necessary, even forever.
     */
    private Message sendAndReceive(final Message message,
                                   Protocol.MessageType... types){

        d("sendAndReceive: "+message+"MessageType types: "+types);
        sending: for(int i=0;;i=Math.min(i+1,timeouts.length-1)){
            final float timeout = timeouts[i];
            try{
                d("Sending...");
                mySocket.sendMessage(message,this.serverAddress);
                
                d("Receiving with timeout "+timeout+"s...");
                final Message r = mySocket.receiveMessage(timeout);
                if(r==null){
                    d("No response. Datagram lost? Network OK? Server running?");
                    continue;
                }
                for(Protocol.MessageType type : types)
                    if(type.equals(Protocol.lookupMessageType(r))){
                        d("Correct response: "+r);
                        return r;
                    }
                w("UNEXPECTED message received: "+r);
            }catch(final Exception e){
                e.printStackTrace();
            }
        }
    }

    public void run() throws Exception{

        final Message logIn = Protocol.serialize_1_LOG_IN("nick","password");

        final Message logInReply = sendAndReceive(logIn,
                                                  Protocol.MessageType.LOG_IN_CORRECT_1,
                                                  Protocol.MessageType.LOG_IN_INCORRECT_1);

        if(Protocol.lookupMessageType(logInReply)
           .equals(Protocol.MessageType.LOG_IN_INCORRECT_1)){
            i("Could not log in. Reason: "
              +(new Protocol.Deserialized_1_LOG_IN_INCORRECT(logInReply)).reason);
        }

        i("Logged in.");

        final Message getStatistics = Protocol.serialize_1_GET_STATISTICS();

        final Message statisticsReply
            = sendAndReceive(getStatistics,
                             Protocol.MessageType.RETURN_STATISTICS_1);

        final Protocol.Deserialized_1_RETURN_STATISTICS statistics
            = new Protocol.Deserialized_1_RETURN_STATISTICS(statisticsReply);

        i("Number of logged in players: "+statistics.numberOfUsers);
        i("Number of games: "+statistics.numberOfGames);
        i("Number of partner-searching players: "+statistics.numberOfSearchers);

        mySocket.close();
    }
}
