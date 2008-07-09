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

public class JChatPanel extends JPanel implements ActionListener {

    //Text is displayed in:
    private final JTextArea jTextArea;
    //Text is entered in:
    private final JTextField jTextField;

    public JChatPanel(){
        super(new BorderLayout());

        this.jTextArea = new JTextArea(5, 10);
        this.jTextArea.setEditable(false);
        final JScrollPane scrollPane
            = new JScrollPane(jTextArea);

        this.jTextField = new JTextField(10);
        this.jTextField.addActionListener(this);

        //Add Components to this panel.
        this.add(scrollPane,BorderLayout.CENTER);
        this.add(jTextField,BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent evt) {
        byte[] text;
        try{
            text = jTextField.getText().getBytes("UTF8");
        }catch(final java.io.UnsupportedEncodingException e){
            text = new byte[0];
        }

        Sender.send(GeneralProtocol.serialize_1_SAY(Message.toVector(text)));

        //Make sure the new text is visible, even if there
        //was a selection in the text area.
        jTextField.selectAll();
    }

    public void appendLine(final String text){
        SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    jTextArea.append(text + "\n");
                    jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
                }
            });
    }
}
