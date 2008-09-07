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

public class JTableApplet extends JApplet{

    JTablePanel jTablePanel = null;

    //TODO: Should we have a constructor here? Is browser calling it?

    /**
     * Create the GUI. For thread safety, this method should
     * be invoked from the event-dispatching thread.
     */
    private void createGUI(){

        int port = 0;
        long tableId = 0L;
        
        try{
            port = Integer.parseInt(getParameter("p"));
        }catch(final Exception e){
        }

        try{
            tableId = Long.parseLong(getParameter("t"));
        }catch(final Exception e){
        }

        this.jTablePanel
            = new JTablePanel(getCodeBase().getHost(),
                              port,
                              getParameter("n"),
                              tableId);
        this.add(this.jTablePanel);
        this.createMenu();
        this.showStatus("FSOG_Status.");
    }

    private void createMenu(){
        /*
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;
        */

        //Create the menu bar.
        final JMenuBar jMenuBar = new JMenuBar();

        //Build the first menu.
        final JMenu jSettingsMenu = new JMenu("Settings");
        jSettingsMenu.setMnemonic(KeyEvent.VK_S);
        jSettingsMenu.getAccessibleContext().setAccessibleDescription
            ("Settings menu");
        jMenuBar.add(jSettingsMenu);

        //a group of check box menu items
        final JCheckBoxMenuItem jShowConsoleMenuItem
            = new JCheckBoxMenuItem("Show debugging console");
        jShowConsoleMenuItem.setMnemonic(KeyEvent.VK_C);
        jSettingsMenu.add(jShowConsoleMenuItem);

        /*
        cbMenuItem = new JCheckBoxMenuItem("Another one");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        menu.add(cbMenuItem);

        //a group of radio button menu items
        menu.addSeparator();
        ButtonGroup group = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Another one");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //a submenu
        menu.addSeparator();
        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                                                       KeyEvent.VK_2, ActionEvent.ALT_MASK));
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        submenu.add(menuItem);
        menu.add(submenu);
        */

        //Build second menu in the menu bar.
        final JMenu jHelpMenu = new JMenu("Help");
        jHelpMenu.setMnemonic(KeyEvent.VK_N);
        jHelpMenu.getAccessibleContext().setAccessibleDescription
            ("Help menu");
        jMenuBar.add(jHelpMenu);
        
        final JMenuItem jAboutMenuItem = new JMenuItem("About",
                                                       KeyEvent.VK_A);
        jAboutMenuItem.setAccelerator(KeyStroke.getKeyStroke
                                      (KeyEvent.VK_1,ActionEvent.ALT_MASK));
        jAboutMenuItem.getAccessibleContext().setAccessibleDescription
            ("Displays information about this program");
        jHelpMenu.add(jAboutMenuItem);

        final JMenuItem jReportBugMenuItem = new JMenuItem("Report bug",
                                                           KeyEvent.VK_B);
        jReportBugMenuItem.setAccelerator
            (KeyStroke.getKeyStroke(KeyEvent.VK_2,ActionEvent.ALT_MASK));
        jReportBugMenuItem.getAccessibleContext().setAccessibleDescription
            ("Report bug in this program");
        jHelpMenu.add(jReportBugMenuItem);

        this.setJMenuBar(jMenuBar);
    }

    //Called when this applet is loaded into the browser.
    @Override public void init(){
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {createGUI();}
                });
        }catch(final Exception e) {
            System.err.println("createGUI didn't complete successfully.");
            e.printStackTrace();
        }
    }

    @Override public void start(){
    }

    @Override public void stop(){
    }

    @Override public void destroy(){
        //TODO: Should we join all the threads?
        if(this.jTablePanel!=null){
            this.jTablePanel.sender.quit();
            try{
                this.jTablePanel.socket.shutdownInput();
                this.jTablePanel.socket.shutdownOutput();
                this.jTablePanel.socket.close();
            }catch(final Exception e){
                System.out.println("Can't close socket: "+e);
                e.printStackTrace();
            }
        }
    }

    @Override public String getAppletInfo(){
        //TODO: more info.
        return "JTableApplet.";
    }

    @Override public String[][] getParameterInfo(){

        final String parameterInfo[][] = {
            {"v", "{\"d\"}", "verbosity"},
            {"p", "int", "server's port"},
            {"n", "String", "player's screen name"},
            {"t", "long", "table id"}
        };
        return parameterInfo;
    }
}
