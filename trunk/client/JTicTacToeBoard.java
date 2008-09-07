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

public abstract class JTicTacToeBoard extends JBoard{

    private enum Field{EMPTY,X,O}
    private Field[][] board;
    private int empty;

    private final JLabel label;
    private final JMoveButton[][] buttons;

    public JTicTacToeBoard(final Table table,
                           final MoveListener moveListener,
                           final JTabbedPane jTabbedPane){
        super((byte)2,table,moveListener,jTabbedPane);

        this.setLayout(new BorderLayout());
        
        this.board = new Field[3][3];
        this.label = new JLabel("Game not started yet.",SwingConstants.CENTER);
        this.buttons = new JMoveButton[3][3];

        final JPanel squares = new JPanel(new GridLayout(0,3));
        final JTicTacToeBoard me = this;
        for(byte row=0; row<3; row++)
            for(byte column=0; column<3; column++){
                this.board[row][column] = Field.EMPTY;
                this.buttons[row][column]
                    = new JMoveButton(row,column,moveListener){
                            public void sendMove(final Vector<Byte> move){
                                me.sendMove(move);
                            }
                        };
                squares.add(this.buttons[row][column]);
            }
        this.add(this.label,BorderLayout.PAGE_START);
        this.add(squares,BorderLayout.CENTER);
    }

    private Field currentPlayersField(){
        return (turn==0 ? Field.X : Field.O);
    }

    private static abstract class JMoveButton extends JButton implements ActionListener{
        private final byte row;
        private final byte column;
        private final MoveListener moveListener;
        //private final JTicTacToeBoard jBoard;
        private Field field;

        public JMoveButton(final byte row,
                           final byte column,
                           final MoveListener moveListener){
            super("");
            this.row = row;
            this.column = column;
            this.moveListener = moveListener;
            this.field = Field.EMPTY;

            this.addActionListener(this);
            this.setEnabled(false);
            this.setFont(this.getFont().deriveFont(100.0F));
            this.setMinimumSize(new Dimension(100,100));
        }

        public abstract void sendMove(final Vector<Byte> move);

        public void reset(){
            this.field = Field.EMPTY;
            this.setText("");
            this.setEnabled(false);
        }

        public void mark(final Field field){
            this.field = field;
            this.setText(field.name());
        }

        public void maybeEnable(){
            this.setEnabled(this.field==Field.EMPTY);
        }

        public void actionPerformed(ActionEvent e){
            final Vector<Byte> move
                = TicTacToeProtocol.serialize_1_TIC_TAC_TOE_MOVE(this.row,
                                                                 this.column);

            this.moveListener.handle_1_MOVE_MADE(move);
            sendMove(move);

            /*
            this.jBoard.board[this.row][this.column]
                = (this.jBoard.turn==0?Field.X:Field.O);
            this.mark();
            this.jBoard.label.setText("Awaiting opponent's move...");
            for(byte row=0; row<3; row++)
                for(byte column=0; column<3; column++){
                    this.jBoard.buttons[row][column].setEnabled(false);
            }

            this.jBoard.setNextPlayer();

            //Let outer GUI know to redraw some stuff.
            this.jBoard.moveListener.moveMade();
            */
        }
    }

    public boolean initialize(final Vector<Byte> initialMessage){
        //My turn?
        //TODO: Check whether 2 players are present. If not, return false.

        //We expect move only from the first player:
        setFirstPlayer();

        //Initialize board to be 3x3 with all empty fields:
        for(byte row=0; row<3; row++)
            for(byte column=0; column<3; column++){
                this.board[row][column] = Field.EMPTY;
                this.buttons[row][column].reset();
            }

        this.empty = 9;

        if(this.myTurn()){
            this.label.setText("You make the first move!");
            for(byte row=0; row<3; row++)
                for(byte column=0; column<3; column++){
                    this.buttons[row][column].maybeEnable();
            }
        }else{
            this.label.setText("Awaiting opponent's move...");
        }
        //TODO: Should we call something like repaint()?
        return true;
    }
    
    public int moveMade(final Vector<Byte> move,
                        final int[] endResult){

        TicTacToeProtocol.Deserialized_1_TIC_TAC_TOE_MOVE deserialized;
        try{
            deserialized
                = new TicTacToeProtocol.Deserialized_1_TIC_TAC_TOE_MOVE(move);
        }catch(final MessageDeserializationException e){
            d("JTTTB.mM deserizlization failed.");
            e.printStackTrace();
            return INVALID|END;
        }

        final byte row = deserialized.row;
        final byte column = deserialized.column;

        d("JTTTB.mM row=="+(int)row+" column=="+(int)column
                 +"board[row][column]=="+board[row][column]);

        //Let's see whether the move is valid (both coordinates are within
        //<0,2> and the pointed field is still empty):
        if(row<0 || row>2 || column<0 || column>2 || board[row][column]!=Field.EMPTY)
            return INVALID|END;

        //OK, move is valid. Let's introduce game state change.
  
        final Field c = currentPlayersField();
  
        board[row][column] = c;
        empty--;
        buttons[row][column].mark(c);

        //Let's see whether we have 3--in--a--row after this move:
        if( (board[row][0]==c && board[row][1]==c && board[row][2]==c)
            ||(board[0][column]==c && board[1][column]==c && board[2][column]==c)
            ||(board[0][0]==c && board[1][1]==c && board[2][2]==c)
            ||(board[0][2]==c && board[1][1]==c && board[2][0]==c)){

            //Yes. The player who made a move just won. We need to
            //set "endResult" correctly and return "END":
            endResult[turn] = 1000000;
            endResult[1-turn] = 0;
            
            this.label.setText("Game over.");
            //We disable remaining buttons:
            for(int i=0; i<3; i++){
                for(int j=0; j<3; j++){
                    this.buttons[i][j].setEnabled(false);
                }
            }
            //We change color to indicate three--in--a--row:
            if(board[row][0]==c && board[row][1]==c && board[row][2]==c){
                buttons[row][0].setBackground(Color.green);
                buttons[row][1].setBackground(Color.green);
                buttons[row][2].setBackground(Color.green);
            }
            if(board[0][column]==c && board[1][column]==c && board[2][column]==c){
                buttons[0][column].setBackground(Color.green); 
                buttons[1][column].setBackground(Color.green); 
                buttons[2][column].setBackground(Color.green);
                }
            if(board[0][0]==c && board[1][1]==c && board[2][2]==c){
                buttons[0][0].setBackground(Color.green); 
                buttons[1][1].setBackground(Color.green); 
                buttons[2][2].setBackground(Color.green);
            }
            if(board[0][2]==c && board[1][1]==c && board[2][0]==c){
                buttons[0][2].setBackground(Color.green); 
                buttons[1][1].setBackground(Color.green); 
                buttons[2][0].setBackground(Color.green);
            }
            return VALID|END;
        }
        
        //If all fields are filled, but three--in--a--row was not detected,
        //it's a draw.
        if(empty==0)
            {
                endResult[0] = 0;
                endResult[1] = 0;
                return VALID|END;
            }

        setNextPlayer();

        //GUI Stuff:
        if(this.myTurn()){
            this.label.setText("It's your turn!");
            for(int i=0; i<3; i++){
                for(int j=0; j<3; j++){
                    this.buttons[i][j].maybeEnable();
                }
            }
        }else{
            this.label.setText("Awaiting opponent's move...");
            for(int i=0; i<3; i++){
                for(int j=0; j<3; j++){
                    this.buttons[i][j].setEnabled(false);
                }
            }
        }
        //Game shall continue.
        return VALID|CONTINUE;
    }

}
