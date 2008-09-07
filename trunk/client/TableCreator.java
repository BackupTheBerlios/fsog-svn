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
import java.util.concurrent.*;

public abstract class TableCreator implements GeneralProtocol.GeneralHandler{

    final Socket socket;
    final Sender sender;
    final Receiver receiver;
    final BlockingQueue<Long> queue;

    public TableCreator(final Socket socket){
        this.socket = socket;
        final TableCreator me = this;
        this.sender = new Sender(socket,""){
                public void d(final String s){me.d(s);}
                public void e(final String s,final Throwable t){me.e(s,t);}
            };
        this.receiver = new Receiver(socket,this,""){
                public void d(final String s){me.d(s);}
                public void e(final String s,final Throwable t){me.e(s,t);}
            };
        this.queue = new LinkedBlockingQueue<Long>();
    }
    
    public abstract void d(final String s);
    public abstract void e(final String s,final Throwable t);

    public static void main(final String[] arguments){
        try{
            final String host = arguments[0];
            final int port = Integer.parseInt(arguments[1]);

            final TableCreator tableCreator
                = new TableCreator(new Socket(host,port)){
                        public void d(final String s){
                            System.err.println(s);
                        }
                        public void e(final String s,final Throwable t){
                            System.err.println(s);
                            t.printStackTrace();
                        }
                    };

            final long tableId = tableCreator.createTable();

            System.out.println(""+tableId);

            tableCreator.quitAndJoin();

        }catch(final Exception e){
            System.err.println("Exception: "+e);
            System.err.println("Stack trace:");
            e.printStackTrace();
        }
    }

    public long createTable() throws Exception{

        d("Debug mode enabled.");

        d("Serializing message...");
        final Vector<Byte> query
            = GeneralProtocol.serialize_1_CREATE_THOUSAND_TABLE();

        d("Message serialized: "+query);

        d("Sending message...");
        sender.send(query);

        d("Query sent: "+query);

        d("Awaiting response...");
        final Long tableId = this.queue.take();
        d("Response: "+tableId);
        
        return tableId;
    }

    public void quitAndJoin()throws Exception{
        sender.quit();
        socket.close();
        sender.join();
        receiver.join();
    }

    public boolean handle_1_TABLE_CREATED(final long id){
        this.queue.offer(new Long(id));
        return true;
    }

    public boolean handle_1_SAID(final byte tablePlayerId,
                                 final java.util.Vector<Byte> text_UTF8){return false;}
    public boolean handle_1_YOU_JOINED_TABLE(final byte tablePlayerId){return false;}
    public boolean handle_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID(){return false;}
    public boolean handle_1_NEW_PLAYER_JOINED_TABLE(final String screenName,
                                                    final byte tablePlayerId){return false;}
    public boolean handle_1_PLAYER_LEFT_TABLE(final byte tablePlayerId){return false;}
    public boolean handle_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE(final java.util.Vector<Byte> turnGamePlayerToTablePlayerId){return false;}
    public boolean handle_1_GAME_STARTED_WITH_INITIAL_MESSAGE(final java.util.Vector<Byte> turnGamePlayerToTablePlayerId,
                                                              final java.util.Vector<Byte> initialMessage){return false;}
    public boolean handle_1_MOVE_MADE(final java.util.Vector<Byte> gameMove){return false;}
}
