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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.font.*;

public class JThousandBoard
    extends JComponent
    implements MouseMotionListener, MouseListener{

    //Clip rectangle:
    private final Rectangle clipRectangle;

    //Background:
    private BufferedImage backgroundImage;

    //Virtual image used for deciding whether mouse is over some
    //card. When component is resized, cards are painted into this
    //image, each with one color (no symbols, etc.). Each card has
    //different color, according to its index in cardsAtHand. Later
    //when mouse is moved over this component, we see, whether it's
    //some cards color in this image. If so, it means mouse is over a
    //card.
    private BufferedImage virtualImage;

    //Cards the player has at hand to play with.
    private Vector<Byte> cardsAtHand;

    //Which card is selected (mouse over it).
    private int selected;
    //If card is selected, we only have to redraw cards (no
    //background, etc.).
    private boolean repaintOnlyCards;

    private static final int NO_CARD = 127;

    //Objects for painting:
    private final BasicStroke basicStroke;
    //Everything will be scaled according to size.
    private int size;
    private float bigCardWidth;
    private float bigCardHeight;
    private RoundRectangle2D.Float bigCard;
    private float cardsStartX;
    private float cardsStartY;
    private float rotationRadius;
    private Font font;
    private double singleRotation;
    private float bigCardMarginLeft;
    private float bigCardMarginTopFirst;
    private float bigCardMarginTopSecond;

    public JThousandBoard(){

        this.clipRectangle = new Rectangle();
        this.backgroundImage = null;
        this.virtualImage = null;

        this.cardsAtHand = new Vector<Byte>();
        this.selected = NO_CARD;
        this.repaintOnlyCards = false;
        this.basicStroke = new BasicStroke(1);

        final Byte[] cards
            = {Card.ACE|Card.SPADE,
               Card.JOKER|Card.BLUE,               
               Card.KING|Card.DIAMOND,
               Card.QUEEN|Card.CLUB,
               Card.JACK|Card.HEART,
               Card.TEN|Card.CLUB,
               Card.NINE|Card.DIAMOND,
               Card.EIGHT|Card.SPADE,
               Card.SEVEN|Card.CLUB,
               Card.SIX|Card.CLUB,
               Card.FIVE|Card.HEART,
               Card.FOUR|Card.DIAMOND,
               Card.THREE|Card.SPADE,
               Card.TWO|Card.CLUB
        };

        for(Byte card : cards)
            this.cardsAtHand.add(card);

        this.setBorder(BorderFactory.createEmptyBorder());
        this.setOpaque(true);

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public void mouseDragged(final MouseEvent e){
    }

    public void mouseMoved(final MouseEvent e){
        //Get the blue component of the pixel:
        final int virtualColor
            = 0x00FF & this.virtualImage.getRGB(e.getX(),e.getY());

        if(virtualColor != this.selected){
            this.selected = virtualColor;
            this.repaintOnlyCards = true;
            final JThousandBoard me = this;
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        me.repaint();
                    }
                });
            //System.out.println("("+e.getX()+","+e.getY()+"): "+virtualColor);
        }
    }

    public void mouseClicked(final MouseEvent e){
    }

    public void mousePressed(final MouseEvent e){
        //Get the blue component of the pixel:
        final int virtualColor
            = 0x00FF & this.virtualImage.getRGB(e.getX(),e.getY());

        if(virtualColor != NO_CARD){
            this.selected = NO_CARD;
            this.cardsAtHand.remove(virtualColor);
            this.virtualImage = null;
            final JThousandBoard me = this;
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        me.repaint();
                    }
                });
            //System.out.println("("+e.getX()+","+e.getY()+"): "+virtualColor);
        }
    }

    public void mouseReleased(final MouseEvent e){
    }

    public void mouseEntered(final MouseEvent e){
    }

    public void mouseExited(final MouseEvent e){
    }


    public void initializeBackgroundImage(){

        int w=this.getWidth();
        int h=this.getHeight();

        if(w<10) w=10;
        if(h<10) h=10;

        this.backgroundImage
            =new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

        //final Random seeds = new Random(13);

        final Random r = new Random(0);

        //Draw background curves:
        final int dx=(w<50?10:w/30);
        final int dy=(h<50?10:h/30);

        final Graphics2D g =this.backgroundImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(dx));

        int startx=-dx;
        int starty=0;
        //top-left above diagonal
        for(;starty<=h+dy;starty+=(dy/2)){
            drawCurve(g,w,startx,starty,dx,dy,r);
        }
        //bottom-right above diagonal
        for(;startx<=w+dx;startx+=(dx/2)){
            drawCurve(g,w,startx,starty,dx,dy,r);
        }
    }

    public void initializeVirtualImage(){

        int w=this.getWidth();
        int h=this.getHeight();

        if(w<10) w=10;
        if(h<10) h=10;

        this.virtualImage
            =new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

        final Graphics2D vg = this.virtualImage.createGraphics();
        vg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);

        final Color color = new Color(0,0,NO_CARD);
        vg.setColor(color);
        vg.fillRect(0,0,w,h);
        //System.out.println("Filled "+w+"x"+h+" using "+color);
    }

    private static void drawCurve(final Graphics2D g,
                                  final int w,
                                  final int startx,
                                  final int starty,
                                  final int dx,
                                  final int dy,
                                  final Random r){
        int x=startx;
        int y=starty;
        float tempx=x;
        float tempy=y;
        final GeneralPath path
            = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        path.moveTo(x,y);
        for(;y>=-dy && x<=w+dx;x+=dx,y-=dy){
            path.curveTo(x+(x-tempx),y+(y-tempy),
                         tempx=(float)(x+2*dx/3+(r.nextGaussian()*dx/3)),
                         tempy=(float)(y-2*dy/3+(r.nextGaussian()*dy/3)),
                         x+dx,y-dy);
        }
        g.setColor(new Color(0F,
                             treshold(0.7+r.nextGaussian()/5),
                             treshold(0.3+r.nextGaussian()/3),
                             treshold(0.5+r.nextGaussian()/4)));
        g.draw(path);
    }

    private static float treshold(double v){
        if(v>1.0)
            return 1F;
        if(v<0.0)
            return 0F;
        return (float)v;
    }
    
    public Dimension getPreferredSize() {
     	return new Dimension(800,600);
    }
    
    public Dimension getMinimumSize() {
        return new Dimension(50,50);
    }

    private void initializeSizes(){

        final int width = this.getWidth();
        final int height = this.getHeight();

        size = Math.min(width,height);
        bigCardWidth = 0.5f*size;
        bigCardHeight = 1.5f*bigCardWidth;
        bigCard
            = new RoundRectangle2D.Float(0,0,
                                         bigCardWidth,bigCardHeight,
                                         0.1f*bigCardWidth,0.1f*bigCardWidth);

        cardsStartX = 0.5f*width;
        cardsStartY = 0.75f*height;
        rotationRadius = 2*bigCardHeight;
        font = new Font("Lucida Sans",Font.BOLD,10)
            .deriveFont(0.1f*bigCardHeight);
        singleRotation=Math.PI/60.0;
        bigCardMarginLeft = 0.01f*bigCardWidth;
        bigCardMarginTopFirst = 0.11f*bigCardWidth;
        bigCardMarginTopSecond = 0.21f*bigCardWidth;
    }

    protected void paintComponent(final Graphics g) {

        //System.out.println("paintComponent");
        
        final Graphics2D g2d = (Graphics2D)g.create();

        this.paintMe(g2d);

        g2d.dispose();
    }

    private void drawBigCard(final byte card,
                             final Graphics2D g,
                             final boolean highlight){
        if(highlight)
            g.setColor(Color.YELLOW);
        else
            g.setColor(Color.WHITE);

        g.fill(bigCard);
        g.setColor(Color.GRAY);
        g.draw(bigCard);

        final String valueString = Card.valueStrings[card];

        LineMetrics metrics = font.getLineMetrics(valueString,
                                                  g.getFontRenderContext());

        // Try omitting the descent from the height variable.
        final float ascent = metrics.getAscent();
        final float descent = metrics.getDescent();


        //         double width = font.getStringBounds(s, frc).getWidth();
        //         int w = getWidth();
        //         int h = getHeight();
        //         double xScale = w/width;
        //         double yScale = (double)h/height;
        //         double x = (w - xScale*width)/2;
        //         double y = (h + yScale*height)/2 - yScale*metrics.getDescent();
        //         AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        //         at.scale(xScale, yScale);
        //         g2.setFont(font.deriveFont(at));
        //g2.drawString(s, 0, 0);


        g.setColor(Card.colors4[card]);
        g.drawString(valueString,
                     bigCardMarginLeft,1.1f*ascent);
        //bigCardMarginLeft,bigCardMarginTopFirst);
        g.drawString(Card.suitStrings[card],
                     bigCardMarginLeft,2.1f*ascent+descent);

        //g.fill(heart);
    }

    /*
      @param g Graphics object of what user will see.
    */
    private void paintMe(final Graphics2D g){

        final int width = this.getWidth();
        final int height = this.getHeight();

        //Is virtualImage the wrong size?
        if(this.virtualImage==null
           ||this.virtualImage.getWidth()!=width
           ||this.virtualImage.getHeight()!=height){

            this.repaintOnlyCards = false;

            //Is backgroundImage not big enough?
            final Boolean shouldInitializeBackgroundImage
                = new Boolean(this.backgroundImage==null
                              ||this.backgroundImage.getWidth()<width
                              ||this.backgroundImage.getHeight()<height);
            
            final JThousandBoard me = this;
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        me.initializeSizes();
                        me.initializeVirtualImage();
                        if(shouldInitializeBackgroundImage)
                            me.initializeBackgroundImage();
                        me.repaint();
                    }
                });
            return;
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                           RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        final Graphics2D vg = this.virtualImage.createGraphics();

        if(!repaintOnlyCards){

            vg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_OFF);
            vg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            vg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            //GeneralPath heart=svg2path(S.heart);

            g.getClipBounds(clipRectangle);

            //Paste background:
            g.drawImage(this.backgroundImage.getSubimage(clipRectangle.x,
                                                         clipRectangle.y,
                                                         clipRectangle.width,
                                                         clipRectangle.height),
                        clipRectangle.x,
                        clipRectangle.y,
                        null);
        }
        //g.setRenderingHint(RenderingHints.KEY_RENDERING,
        //		   RenderingHints.VALUE_RENDER_QUALITY);

        g.setStroke(this.basicStroke);
        if(!repaintOnlyCards){
            vg.setStroke(this.basicStroke);
        }

        //TODO: Don't multiply so often.
        //final RoundRectangle2D.Float picture
        //    = new RoundRectangle2D.Float(30,30,240,390,20,20);

        g.translate(cardsStartX,cardsStartY);
        if(!repaintOnlyCards){
            vg.translate(cardsStartX,cardsStartY);
        }

        g.setFont(font);

        final int cardsAtHandSize = cardsAtHand.size();

        g.rotate(-singleRotation*0.5f*(cardsAtHandSize-1),0,rotationRadius);
        if(!repaintOnlyCards){
            vg.rotate(-singleRotation*0.5f*(cardsAtHandSize-1),0,rotationRadius);
        }

        for(int i=0;i<cardsAtHandSize;i++){

            final Byte card = cardsAtHand.get(i);

            drawBigCard(card,g,selected == i);

            if(!repaintOnlyCards){
                vg.setColor(new Color(0,0,i));
                vg.fill(bigCard);
            }

            g.rotate(singleRotation,0,rotationRadius);
            if(!repaintOnlyCards){
                vg.rotate(singleRotation,0,rotationRadius);
            }
        }
        this.repaintOnlyCards = false;
    }

    //Look at S class to see how we represent svg.
    private GeneralPath svg2path(final float[] svg){

        float x=0F;//current location
        float y=0F;
        float x1=0F;//first control point
        float y1=0F;
        float x2=0F;//second control point
        float y2=0F;

        final GeneralPath path
            = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        for(int i=0;i<svg.length;/*NOT i++ !!!*/){

            final float command
                =svg[i++];

            if(command==S.M){
                x=svg[i++];
                y=svg[i++];
                path.moveTo(x,y);
            }else if(command==S.c){

                x1=x+svg[i++];
                y1=y+svg[i++];
                x2=x+svg[i++];
                y2=y+svg[i++];
                x=x+svg[i++];
                y=y+svg[i++];
                path.curveTo(x1,y1,
                             x2,y2,
                             x,y);
            }else if(command==S.s){
                x1=x+(x-x2);
                y1=y+(y-y2);
		
                x2=x+svg[i++];
                y2=y+svg[i++];
                x=x+svg[i++];
                y=y+svg[i++];

                path.curveTo(x1,y1,
                             x2,y2,
                             x,y);
            }else if(command==S.z){
                path.closePath();
            }else{
                //System.err.println("Invalid: "+command);
            }
        }

        return path;
    }   
}
