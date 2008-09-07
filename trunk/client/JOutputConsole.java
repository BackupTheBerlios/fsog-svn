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

public class JOutputConsole extends JPanel{

    //Text is displayed in:
    private final JTextArea jTextArea;
    //Buttons:
    private final JButton jCopyButton;
    private final JButton jClearButton;
    private final JButton jBeepButton;

    public JOutputConsole(){
        super(new BorderLayout());

        this.jTextArea = new JTextArea();
        this.jTextArea.setEditable(false);
        final JScrollPane jScrollPane
            = new JScrollPane(jTextArea);

        final JOutputConsole me = this;

        this.jCopyButton
            = new JRunnableButton("Copy"){
                    public void run(){
                        me.jTextArea.selectAll();
                        me.jTextArea.copy();
                    }
                };

        this.jClearButton
            = new JRunnableButton("Clear"){
                    public void run(){
                        me.jTextArea.setText(null);
                    }
                };

        this.jBeepButton
            = new JRunnableButton("Beep"){
                    public void run(){
                        Toolkit.getDefaultToolkit().beep();
                    }
                };

        //Add Components to this panel.
        this.add(jScrollPane,BorderLayout.CENTER);

        final JPanel jPanel = new JPanel();
        jPanel.add(jCopyButton);
        jPanel.add(jClearButton);
        jPanel.add(jBeepButton);
        this.add(jPanel,BorderLayout.PAGE_END);
    }

    private void appendLine(final String text){
        //TODO: repaint necessary?
        jTextArea.append(text + "\n");
        jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
    }

    public void p(final String message){
        final String prefix = "[P "+Thread.currentThread().getName()+"] ";
        final String output
            = prefix+message.replaceAll("\\n","\n"+prefix);
        this.appendLine(output);
        System.out.println(output);
    }

    public void d(final String message){
        final String prefix = "[D "+Thread.currentThread().getName()+"] ";
        final String output
            = prefix+message.replaceAll("\\n","\n"+prefix);
        this.appendLine(output);
        System.out.println(output);
    }
}