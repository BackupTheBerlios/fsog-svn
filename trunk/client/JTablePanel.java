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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class JTablePanel
    extends JSplitPane
    implements GeneralProtocol.GeneralHandler, MoveListener
{
    private final Table table;
    private JTabbedPane tabbedPane;
    private final JChatPanel jChatPanel;
    private final JSplitPane splitPane0;
    private final JBoard jBoard;

    public JTablePanel(){
        super(JSplitPane.HORIZONTAL_SPLIT);

        this.table = new Table();

        //Empty list of players:
        final JScrollPane tablePlayerListScrollPane
            = new JScrollPane(makeTablePlayerListPanel(this.table));

        //Tabbed pane:
        this.tabbedPane = new JTabbedPane();

        final ImageIcon icon = null;

        this.jChatPanel = new JChatPanel();
        tabbedPane.addTab("Chat",icon,this.jChatPanel,"Chat window");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_C);

        JComponent panel4 = new JPanel();
        //panel4.setPreferredSize(new Dimension(410, 50));
        tabbedPane.addTab("Players", icon, panel4,
                          "List of players");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_L);

        JComponent panel5 = new JPanel();
        //panel4.setPreferredSize(new Dimension(410, 50));
        tabbedPane.addTab("Settings", icon, panel5,
                          "Current game settings");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_S);

        tabbedPane.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.black));
        //tabbedPane.setPreferredSize(new Dimension(200, 300));

        this.splitPane0 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                         tablePlayerListScrollPane,
                                         tabbedPane);
        splitPane0.setOneTouchExpandable(true);
        splitPane0.setDividerLocation(150);

        this.jBoard
            = new JThousandBoard((byte)3,this.table,
                                 this,
                                 this.tabbedPane);
        //= new JTicTacToeBoard(this.table,this);

        //JLabel label2 = new JLabel("TWO");
        //label2.setHorizontalAlignment(JLabel.CENTER);
        //label2.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.black));
        //label2.setPreferredSize(new Dimension(500, 500));
        JScrollPane playAreaScrollPane = new JScrollPane(this.jBoard);

        this.setLeftComponent(splitPane0);
        this.setRightComponent(playAreaScrollPane);
        this.setOneTouchExpandable(true);
        this.setDividerLocation(220);
    }

    private void redrawTablePlayerList(){

        final JPanel panel = makeTablePlayerListPanel(this.table);
        final JScrollPane scrollPane
            = new JScrollPane(panel);

        this.splitPane0.setTopComponent(scrollPane);
        //scrollPane.setPreferredSize(panel.getPreferredSize());
                    
        //this.splitPane0.resetToPreferredSizes();
        //this.splitPane0.setDividerLocation(panel.getPreferredSize().getHeight());
        //this.splitPane0.setDividerLocation(Math.min(panel.getPreferredSize().getHeight(),
        //this.getDividerLocation()));
        //this.splitPane0.repaint();
    }

    private JPanel makeTablePlayerListPanel(final Table table){
        /*
        final int rows = 0;
        final int columns = 1;
        final int horizontalGap = 3;
        final int verticalGap = 3;
        new GridLayout(rows,columns,
        horizontalGap,verticalGap)
        */

        final JPanel tablePlayerListPanel
            = new JPanel();
        tablePlayerListPanel.setBorder(BorderFactory.createMatteBorder
                                       (1,1,1,1,Color.black));
        
        int preferredHeight = 3;

        if(table.gameOn){
            //If the game is on, display players in the order they play:
            //final Vector<Byte> turnGamePlayerToTablePlayerId
            //    = table.turnGamePlayerToTablePlayerId;
            for(byte turnGamePlayer = 0;
                turnGamePlayer<table.turnGamePlayerToTablePlayerId.size();
                turnGamePlayer++){
                final JPanel panel
                    = makeTablePlayerPanel
                    (table.tablePlayerIdToTablePlayer.get
                     (table.turnGamePlayerToTablePlayerId.get(turnGamePlayer)),
                     turnGamePlayer == jBoard.getTurn());
                tablePlayerListPanel.add(panel);
                preferredHeight+=panel.getPreferredSize().getHeight()+6;
            }
        }else{
            //If game is not going on, display players in arbitrary order:
            for(Map.Entry<Byte,TablePlayer> e
                    : table.tablePlayerIdToTablePlayer.entrySet()){
                final JPanel panel
                    = makeTablePlayerPanel(e.getValue(),
                                           false);
                tablePlayerListPanel.add(panel);
                preferredHeight+=panel.getPreferredSize().getHeight()+6;
            }
        }

        Output.d("Preferred height: "+ preferredHeight);
        tablePlayerListPanel.setPreferredSize(new Dimension(206,preferredHeight));
        return tablePlayerListPanel;
    }

    private JPanel makeTablePlayerPanel(final TablePlayer tablePlayer,
                                        final boolean herTurn){
        final JPanel tablePlayerPanel
            = new JPanel();
        tablePlayerPanel.setBorder(BorderFactory.createMatteBorder
                                   (1,1,1,1,Color.black));
        tablePlayerPanel.setPreferredSize(new Dimension(200, 50));
        final String s
            = (tablePlayer==null
               ?"!!!NOT HERE!!!"
               :(herTurn
                 ?"--> "+tablePlayer.getScreenName()+" <--"
                 :tablePlayer.getScreenName()));
        tablePlayerPanel.add(new JLabel(s));
        return tablePlayerPanel;
    }

    public boolean handle_1_TABLE_CREATED(final long id){
        //TODO: Something wrong with protocol definitions, we should
        //not need to handle this message here.
        this.jChatPanel.appendLine("Table was successfully created. New table's id: "
                                   +id);
        return false;
    }

    public boolean handle_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID(){
        this.jChatPanel.appendLine("Couldn't join table!"
                                   +" Perhaps the table expired.");
        //TODO: Maybe some big message.
        return true;
    }

    public boolean handle_1_YOU_JOINED_TABLE(final byte tablePlayerId){
        final TablePlayer player = new TablePlayer("Me");
        this.table.addMe(tablePlayerId,player);
        redrawTablePlayerList();
        this.jChatPanel.appendLine("I joined table.");
        return true;
    }

    public boolean handle_1_NEW_PLAYER_JOINED_TABLE(final String screenName,
                                                    final byte tablePlayerId){
        final TablePlayer player = new TablePlayer(screenName);
        this.table.addPlayer(tablePlayerId,player);
        redrawTablePlayerList();
        this.jChatPanel.appendLine(""+screenName+" joined table.");
        return true;
    }

    public boolean handle_1_PLAYER_LEFT_TABLE(final byte tablePlayerId){
        final TablePlayer leaver
            = //this.table.tablePlayerIdToTablePlayer.get(tablePlayerId);
        this.table.tablePlayerIdToTablePlayer.remove(tablePlayerId);
        this.table.gameOn=false;
        redrawTablePlayerList();
        this.jChatPanel.appendLine(""+leaver+" left table.");
        return true;
    }

    public boolean handle_1_SAID(final byte tablePlayerId,
                                 final Vector<Byte> text_UTF8){
        final TablePlayer tablePlayer
            = this.table.tablePlayerIdToTablePlayer.get(tablePlayerId);
        if(tablePlayer==null){
            System.err.println("No table player with id "+tablePlayerId);
            return false;
        }
        try{
            this.jChatPanel.appendLine(""+tablePlayer.getScreenName()+": "
                                       +new String(Message.toArray(text_UTF8),
                                                   "UTF8"));
        }catch(final java.io.UnsupportedEncodingException e){
            System.out.println("Couldn't understand chat text: "+e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean handle_1_GAME_STARTED_WITH_INITIAL_MESSAGE
        (final Vector<Byte> turnGamePlayerToTablePlayerId,
         final Vector<Byte> initialMessage){

        //Set table information:
        this.table.turnGamePlayerToTablePlayerId
            =turnGamePlayerToTablePlayerId;
        for(int i=0;i<turnGamePlayerToTablePlayerId.size();i++){
            final Byte b = turnGamePlayerToTablePlayerId.get(i);
            if(b.equals(this.table.myTablePlayerId))
                this.table.myTurnGamePlayer = (byte)i;
        }
        this.table.gameOn=true;

        final boolean initializationResult
            = this.jBoard.initialize(initialMessage);
        if(initializationResult){
            this.jChatPanel.appendLine("Game has started!");
            this.redrawTablePlayerList();
            return true;
        }else{
            this.table.gameOn=false;
            return false;
        }
    }

    public boolean handle_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE
        (final Vector<Byte> turnGamePlayerToTablePlayerId){

        return this.handle_1_GAME_STARTED_WITH_INITIAL_MESSAGE
            (turnGamePlayerToTablePlayerId,
             null);
    }

    public boolean handle_1_MOVE_MADE(final Vector<Byte> move){

        final int[] endResult = new int[jBoard.getNumberOfPlayers()];
        int moveResult = this.jBoard.moveMade(move,
                                              endResult);
        //Move was invalid:
        if((moveResult&JBoard.VALIDITY_MASK)!=JBoard.VALID){
            Output.d("(moveResult&JBoard.VALIDITY_MASK)!=JBoard.VALID");
            this.table.gameOn=false;
            this.redrawTablePlayerList();
            return false;
        }

        //Continue the game.
        if((moveResult&JBoard.CONTINUITY_MASK)==JBoard.CONTINUE){
            this.redrawTablePlayerList();
            return true;
        }

        //End the game.
        this.table.gameOn=false;
        this.redrawTablePlayerList();
        
        this.jChatPanel.appendLine("Game over.");

        if(jBoard.getNumberOfPlayers()==2){
            if(endResult[0]==endResult[1])
                this.jChatPanel.appendLine("Draw.");
            else if(endResult[this.table.myTurnGamePlayer]
                    >endResult[1-this.table.myTurnGamePlayer])
                this.jChatPanel.appendLine("You won!");
            else
                this.jChatPanel.appendLine("You lost.");
            return true;
        }
        return false;
    }
}
