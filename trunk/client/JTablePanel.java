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

public class JTablePanel
    extends JSplitPane
    implements GeneralProtocol.GeneralHandler
{
    private final Table table;
    private final JChatPanel jChatPanel;
    private final JSplitPane splitPane0;

    public JTablePanel(){
        super(JSplitPane.HORIZONTAL_SPLIT);

        this.table = new Table();

        //Empty list of players:
        final JScrollPane tablePlayerListScrollPane
            = new JScrollPane(makeTablePlayerListPanel(this.table));

        //Tabbed pane:
        JTabbedPane tabbedPane = new JTabbedPane();
        ImageIcon icon = null;

        this.jChatPanel = new JChatPanel();
        tabbedPane.addTab("Chat",icon,this.jChatPanel,"Chat window");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_C);

        JComponent panel2 = new JPanel();
        tabbedPane.addTab("Points", icon, panel2,
                          "Current game points");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_P);

        JComponent panel3 = new JPanel();
        tabbedPane.addTab("History", icon, panel3,
                          "Current game history");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_H);

        JComponent panel4 = new JPanel();
        panel4.setPreferredSize(new Dimension(410, 50));
        tabbedPane.addTab("Players", icon, panel4,
                          "List of players");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_L);

        JComponent panel5 = new JPanel();
        panel4.setPreferredSize(new Dimension(410, 50));
        tabbedPane.addTab("Settings", icon, panel5,
                          "Current game settings");
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_S);

        tabbedPane.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.black));
        tabbedPane.setPreferredSize(new Dimension(200, 300));
        JScrollPane tableInfoScrollPane = new JScrollPane(tabbedPane);

        this.splitPane0 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                         tablePlayerListScrollPane,
                                         tableInfoScrollPane);
        splitPane0.setOneTouchExpandable(true);
        splitPane0.setDividerLocation(150);

        //JLabel label2 = new JLabel("TWO");
        //label2.setHorizontalAlignment(JLabel.CENTER);
        //label2.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.black));
        //label2.setPreferredSize(new Dimension(500, 500));
        JScrollPane playAreaScrollPane = new JScrollPane(new Board());

        this.setLeftComponent(splitPane0);
        this.setRightComponent(playAreaScrollPane);
        this.setOneTouchExpandable(true);
        this.setDividerLocation(200);
    }

    private void redrawTablePlayerList(){
        final JTablePanel me = this;
        //TODO: What if called twice before runnable invoked? What's
        //the order?
        SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    final JScrollPane tablePlayerListScrollPane
                        = new JScrollPane(makeTablePlayerListPanel(me.table));
                    
                    me.splitPane0.setTopComponent(tablePlayerListScrollPane);
                    //TODO: Is repaint necessary?
                    me.repaint();
                }
            });
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
        tablePlayerListPanel.setPreferredSize(new Dimension(200, 300));
        for(java.util.Map.Entry<Byte,TablePlayer> e
                : table.getPlayersDeepCopy().entrySet()){
            tablePlayerListPanel.add(makeTablePlayerPanel(e.getValue()));
        }
        return tablePlayerListPanel;
    }

    private JPanel makeTablePlayerPanel(final TablePlayer tablePlayer){
        final JPanel tablePlayerPanel
            = new JPanel();
        tablePlayerPanel.setBorder(BorderFactory.createMatteBorder
                                   (1,1,1,1,Color.black));
        tablePlayerPanel.setPreferredSize(new Dimension(200, 50));
        tablePlayerPanel.add(new JLabel(tablePlayer.getScreenNameCopy()));
        return tablePlayerPanel;
    }

    public boolean handle_1_TABLE_CREATED(final long id){
        //TODO: Something wrong with protocol definitions, we should
        //not need to handle this message here.
        return false;
    }

    public boolean handle_1_JOINING_TABLE_FAILED_INCORRECT_TABLE_ID(){
        return false;
    }

    public boolean handle_1_YOU_JOINED_TABLE(final byte tablePlayerId){
        final TablePlayer player = new TablePlayer("Me");
        this.table.addMe(tablePlayerId,player);
        redrawTablePlayerList();
        return true;
    }

    public boolean handle_1_NEW_PLAYER_JOINED_TABLE(final String screenName,
                                                    final byte tablePlayerId){
        final TablePlayer player = new TablePlayer(screenName);
        this.table.addPlayer(tablePlayerId,player);
        redrawTablePlayerList();
        return true;
    }

    public boolean handle_1_PLAYER_LEFT_TABLE(final byte tablePlayerId){
        this.table.removePlayer(tablePlayerId);
        redrawTablePlayerList();
        return true;
    }

    public boolean handle_1_SAID(final byte tablePlayerId,
                                 final java.lang.String sentence){
        final TablePlayer copy
            = this.table.getTablePlayerCopy(tablePlayerId);
        if(copy==null){
            System.err.println("No table player with id "+tablePlayerId);
            return false;
        }
        this.jChatPanel.appendLine(""
                                   +copy.getScreenNameCopy()
                                   +": "+sentence);

        return true;
    }

    public boolean handle_1_GAME_STARTED_WITH_INITIAL_MESSAGE
        (final java.util.Vector<java.lang.Byte> turnGamePlayerToTablePlayerId,
         final java.util.Vector<java.lang.Byte> initialMessage){
        
        return false;
    }

    public boolean handle_1_GAME_STARTED_WITHOUT_INITIAL_MESSAGE
        (final java.util.Vector<java.lang.Byte> turnGamePlayerToTablePlayerId){
        
        return false;
    }

    public boolean handle_1_MOVE_MADE(final java.util.Vector<Byte> move){
        return false;
    }


}
