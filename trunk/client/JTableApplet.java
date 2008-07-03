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

    protected void loadAppletParameters() {
        //Get the applet parameters.
        String at = getParameter("t");
    }

    /**
     * Create the GUI. For thread safety, this method should
     * be invoked from the event-dispatching thread.
     */
    private void createGUI() {
        System.err.println("Creating...");
        this.add(new JTablePanel());
        System.err.println("Created...");
    }

    //Called when this applet is loaded into the browser.
    public void init() {
        loadAppletParameters();

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {createGUI();}
                });
        } catch (Exception e) { 
            System.err.println("createGUI didn't successfully complete");
        }
        System.err.println("init() done.");
    }

    public void start() {
    }

    public void stop() {
    }
}
