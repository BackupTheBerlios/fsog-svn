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

public abstract class JCards
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
    protected Vector<Hand> hands;

    //Cards put at the center of the table.
    protected Vector<CardAtTable> cardsAtTable;

    //Which card is selected (mouse over it).
    private int selected;
    protected static final int UNCLICKABLE = 127;

    protected JCards(){
        this.clipRectangle = new Rectangle();
        this.backgroundImage = null;
        this.virtualImage = null;

        this.hands = new Vector<Hand>();
        this.cardsAtTable = new Vector<CardAtTable>();
        this.selected = UNCLICKABLE;

        this.setBorder(BorderFactory.createEmptyBorder());
        this.setOpaque(true);

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public static class HandCard{

        //What card it is:
        public final byte card;
        //Is it clickable?
        public final int virtualColor;
        //Should this card be drawn upside--down?
        public final boolean upsidedown;
    
        public HandCard(final byte card,
                        final int virtualColor){
            this.card = card;
            this.virtualColor = virtualColor;
            this.upsidedown = (Math.random()>0.5);
        }
    }

    /** Represents a 'hand' of card, that is a collection of card,
        which can be drawn as if it was held in player's hand.
     */
    public static class Hand{
        //What cards are at hand:
        public final Vector<HandCard> cards;
        //Label for this hand (e.g. player's name)
        public String label;
        //Location -- between 0f and 1f specifying where to draw
        //the hand:
        public final float x;
        public final float y;
        //How big should cards be? <0f,1f>
        public final float cardWidth;

        //For printing arrows that indicate player's turn.
        public boolean hasArrowAbove;
        public boolean hasArrowBelow;

        /**
           @param label Can be null to indicate no label.
        */

        public Hand(final String label,
                    final float x,
                    final float y,
                    final float cardWidth){
            this.cards = new Vector<HandCard>();
            this.label = label;
            this.x = x;
            this.y = y;
            this.cardWidth = cardWidth;
            this.hasArrowAbove = false;
            this.hasArrowBelow = false;
        }

        public byte removeCard(final int virtualColor){
            for(int i=0;i<cards.size();i++){
                if(cards.get(i).virtualColor == virtualColor){
                    final HandCard handCard = cards.remove(i);
                    return handCard.card;
                }
            }
            return Card.UNKNOWN;
        }
    }

    public static class CardAtTable{

        //What card it is:
        public final byte card;
        //Where's its center:
        public final float x;
        public final float y;
        //What's its rotation:
        public final double theta;
        //Is it clickable?
        public final int virtualColor;
    
        public CardAtTable(final byte card,
                           final float x,
                           final float y,
                           final double theta,
                           final int virtualColor){
            this.card = card;
            this.x = x;
            this.y = y;
            this.theta = theta;
            this.virtualColor = virtualColor;
        }
    }

    public void mouseDragged(final MouseEvent e){
    }

    public void mouseMoved(final MouseEvent e){
        final int x = e.getX();
        final int y = e.getY();
        final BufferedImage v = this.virtualImage;

        if(v==null || x>=v.getWidth() || y>=v.getHeight())
            return;

        //Get the blue component of the pixel:
        final int virtualColor = 0x00FF & v.getRGB(x,y);

        if(virtualColor != this.selected){
            this.selected = virtualColor;
            //TODO: Some fine-grained "repaint only" mechanism, not to
            //repaint everything each time, especially when only a
            //card is hovered.
            this.repaint();
            //System.out.println("("+e.getX()+","+e.getY()+"): "+virtualColor);
        }
    }

    public void mouseClicked(final MouseEvent e){
    }

    public void mousePressed(final MouseEvent e){
        final int x = e.getX();
        final int y = e.getY();
        final BufferedImage v = this.virtualImage;

        if(v==null || x>=v.getWidth() || y>=v.getHeight())
            return;

        //Get the blue component of the pixel:
        final int virtualColor = 0x0000FF & v.getRGB(x,y);

        if(virtualColor == UNCLICKABLE)
            return;

        this.selected = UNCLICKABLE;

        this.cardClicked(virtualColor);

        this.virtualImage = null;
        this.repaint();
        //System.out.println("("+e.getX()+","+e.getY()+"): "+virtualColor);
    }

    public abstract void cardClicked(final int virtualColor);

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

        //Eliminate Alpha component, so the image is completely opaque:
        for(int i=0; i<w;i++)
            for(int j=0; j<h; j++)
                backgroundImage.setRGB
                    (i,j, backgroundImage.getRGB(i,j) | 0xFF000000);
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

        vg.setColor(new Color(0,0,UNCLICKABLE));
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

    protected void paintComponent(final Graphics g) {

        //System.out.println("paintComponent");
        //final long before = System.nanoTime();
        final Graphics2D g2d = (Graphics2D)g.create();

        this.paintMe(g2d);

        g2d.dispose();
        //final long duration = System.nanoTime()-before;
        //Output.d("Painting took "
        //         +(duration/1000000)+"ms.");
    }

    /** @param everything When a card is fully visible, set everything
        to true. If it's hidden by another card at hand, set it to
        false.
    */
    private void drawCard(final byte card,
                          final Graphics2D g,
                          final RoundRectangle2D.Float shape,
                          final boolean highlight,
                          final boolean everything,
                          final boolean upsidedown){

        final AffineTransform originalTransform = g.getTransform();

        if(highlight)
            g.setColor(Color.YELLOW);
        else
            g.setColor(Color.WHITE);
        g.fill(shape);

        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(1));
        g.draw(shape);

        final float shapeWidth = (float)shape.getWidth();
        final float shapeHeight = (float)shape.getHeight();

        final float shapeCenterX = (float)shape.getCenterX();
        final float shapeCenterY = (float)shape.getCenterY();

        final Font font = new Font("Lucida Sans",Font.BOLD,10)
            .deriveFont(0.1f*shapeHeight);

        final Font bigFont = font.deriveFont(0.2f*shapeHeight);

        if(card==Card.UNKNOWN){

            //Sometimes draw upside--down:
            if(upsidedown)
                g.rotate(Math.PI,shapeCenterX,shapeCenterY);

            final float margin = 0.1f*shapeWidth;
            final float temp = shapeWidth-2*margin;
            g.setColor(Color.BLUE);
            g.fill(new RoundRectangle2D.Float(margin,margin,
                                              temp,temp,
                                              0.15f*temp,0.15f*temp));

            g.fill(new RoundRectangle2D.Float(margin,shapeWidth,
                                              temp,shapeHeight-3*margin-temp,
                                              0.15f*temp,0.15f*temp));

            if(everything){
                paintString(g,
                            "FSOG.net",
                            font,
                            0.5f*shapeWidth,
                            2*margin+temp+0.5f*(shapeHeight-3*margin-temp),
                            Color.WHITE,
                            null);
                g.translate(shapeCenterX,margin+0.5f*temp);

                g.rotate(0.25*Math.PI,0f,0f);
                paintString(g,
                            CardUtilities.suitStrings[Card.ACE|Card.HEART],
                            bigFont,
                            0f,
                            0.5f*temp,
                            Color.WHITE,
                            null);
                g.rotate(0.5*Math.PI,0f,0f);
                paintString(g,
                            CardUtilities.suitStrings[Card.ACE|Card.DIAMOND],
                            bigFont,
                            0f,
                            0.5f*temp,
                            Color.WHITE,
                            null);
                g.rotate(0.5*Math.PI,0f,0f);
                paintString(g,
                            CardUtilities.suitStrings[Card.ACE|Card.CLUB],
                            bigFont,
                            0f,
                            0.5f*temp,
                            Color.WHITE,
                            null);
                g.rotate(0.5*Math.PI,0f,0f);
                paintString(g,
                            CardUtilities.suitStrings[Card.ACE|Card.SPADE],
                            bigFont,
                            0f,
                            0.5f*temp,
                            Color.WHITE,
                            null);

            }
            g.setTransform(originalTransform);
            return;
        }

        final byte value = (byte)(card&Card.VALUE_MASK);

        final String valueString = CardUtilities.valueStrings[card];
        final String suitString = CardUtilities.suitStrings[card];

        final Font hugeFont = font.deriveFont(0.5f*shapeHeight);

        final FontRenderContext fontRenderContext
            = g.getFontRenderContext();

        final LineMetrics metrics = font.getLineMetrics(valueString,
                                                        fontRenderContext);

        // Try omitting the descent from the height variable.
        final float ascent = metrics.getAscent();
        final float descent = metrics.getDescent();

        final float valueStringWidth
            = (float)font.getStringBounds(valueString,
                                          fontRenderContext).getWidth();

        final float suitStringWidth
            = (float)font.getStringBounds(suitString,
                                          fontRenderContext).getWidth();

        final float horizontalMargin = 0.02f*shapeWidth;

        g.setColor(CardUtilities.colors4[card]);
        g.setFont(font);

        //Sometimes draw upside--down. Only if everything is requested
        //to be drawn.
        if(everything && upsidedown)
            g.rotate(Math.PI,shapeCenterX,shapeCenterY);

        //Top--left corner:
        g.drawString(valueString,
                     horizontalMargin,1.1f*ascent);
        g.drawString(suitString,
                     horizontalMargin,2.1f*ascent+descent);

        //Top--right corner value:
        g.drawString(valueString,
                     shapeWidth-horizontalMargin-valueStringWidth,
                     1.1f*ascent);
        if(everything){
            //Top--right corner suit:
            g.drawString(suitString,
                         shapeWidth-horizontalMargin-suitStringWidth,
                         2.1f*ascent+descent);

            //Center graphics:
            //Print huge suit in the center on 1.
            if(value==Card.ACE)
                paintString(g,
                            suitString,
                            hugeFont,
                            shapeCenterX,
                            shapeCenterY);

            //Print "Joker" in the center on *.
            if(value==Card.JOKER)
                paintString(g,
                            "Joker",
                            bigFont,
                            shapeCenterX,
                            shapeCenterY);
            //Print value in the center on J,Q,K.
            if(value==Card.JACK || value==Card.QUEEN || value==Card.KING)
                paintString(g,
                            valueString,
                            hugeFont,
                            shapeCenterX,
                            shapeCenterY);
            
            //Print suit in the center on 3,5,9.
            if(value==Card.THREE || value==Card.FIVE || value==Card.NINE)
                paintString(g,
                            suitString,
                            bigFont,
                            shapeCenterX,
                            shapeCenterY);
            //Print the rest of 2,3.
            if(value==Card.TWO || value==Card.THREE)
                paintString(g,
                            suitString,
                            bigFont,
                            shapeCenterX,
                            shapeHeight/6f);

            //Print two corner suits on 4,5,6,7,8,9,10.
            if(value==Card.FOUR || value==Card.FIVE || value==Card.SIX
               || value==Card.SEVEN || value==Card.EIGHT || value==Card.NINE
               || value==Card.TEN){
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth/4f,
                            shapeHeight/6f);
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth*3f/4f,
                            shapeHeight/6f);
            }

            //Print two suits on center line on 6,7,8:
            if(value==Card.SIX || value==Card.SEVEN || value==Card.EIGHT){
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth/4f,
                            shapeCenterY);
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth*3f/4f,
                            shapeCenterY);
            }

            //Print suit in the upper--half center on 7,8.
            if(value==Card.SEVEN || value==Card.EIGHT)
                paintString(g,
                            suitString,
                            bigFont,
                            shapeCenterX,
                            shapeHeight/3f);

            //Print two above--center line suits on 9,10.
            if(value==Card.NINE || value==Card.TEN){
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth/4f,
                            shapeHeight*(1f/6f+2f/9f));
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth*3f/4f,
                            shapeHeight*(1f/6f+2f/9f));
            }

            //Print suit in the upper--half center on 10.
            if(value==Card.TEN)
                paintString(g,
                            suitString,
                            bigFont,
                            shapeCenterX,
                            shapeHeight*(1f/6f+1f/9f));

            //And we rotate to draw bottom part of the card:
            g.rotate(Math.PI,shapeCenterX,shapeCenterY);

            g.setFont(font);
            //Bottom--right corner:
            g.drawString(valueString,
                         horizontalMargin,1.1f*ascent);
            g.drawString(suitString,
                         horizontalMargin,2.1f*ascent+descent);
            //Bottom--left corner:
            g.drawString(valueString,
                         shapeWidth-horizontalMargin-valueStringWidth,
                         1.1f*ascent);
            g.drawString(suitString,
                         shapeWidth-horizontalMargin-suitStringWidth,
                         2.1f*ascent+descent);

            //Center graphics:
            //Print the rest of 2,3.
            if(value==Card.TWO || value==Card.THREE)
                paintString(g,
                            suitString,
                            bigFont,
                            shapeCenterX,
                            shapeHeight/6f);

            //Print two corner suits on 4,5,6,7,8,9,10.
            if(value==Card.FOUR || value==Card.FIVE || value==Card.SIX
               || value==Card.SEVEN || value==Card.EIGHT || value==Card.NINE
               || value==Card.TEN){
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth/4f,
                            shapeHeight/6f);
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth*3f/4f,
                            shapeHeight/6f);
            }

            //Print suit in the upper--half center on 8.
            if(value==Card.EIGHT)
                paintString(g,
                            suitString,
                            bigFont,
                            shapeCenterX,
                            shapeHeight/3f);

            //Print two above--center line suits on 9,10.
            if(value==Card.NINE || value==Card.TEN){
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth/4f,
                            shapeHeight*(1f/6f+2f/9f));
                paintString(g,
                            suitString,
                            bigFont,
                            shapeWidth*3f/4f,
                            shapeHeight*(1f/6f+2f/9f));
            }

            //Print suit in the upper--half center on 10.
            if(value==Card.TEN)
                paintString(g,
                            suitString,
                            bigFont,
                            shapeCenterX,
                            shapeHeight*(1f/6f+1f/9f));

        }

        //g.fill(heart);
        g.setTransform(originalTransform);
    }

    private void paintString(final Graphics2D g,
                             final String string,
                             final Font font,
                             final float x,
                             final float y){
        paintString(g,string,font,x,y,null,null);
    }

    /** Paints string using font and g in such a way that (x,y) is in
        the center of the drawn string. Uses fontColor for font and
        backgroundColor for rectangular background.
     */
    private void paintString(final Graphics2D g,
                             final String string,
                             final Font font,
                             final float x,
                             final float y,
                             final Color fontColor,
                             final Color backgroundColor){
        
        final FontRenderContext fontRenderContext
            = g.getFontRenderContext();

        //For some reason these two different ways give good results
        //for calculating symbol's width and height.
        //TODO: perform those calculations not so often.
        final float stringWidth
            = (float)font.getStringBounds(string,
                                          fontRenderContext).getWidth();

        final float stringHeight
            = (float)new TextLayout(string,
                                    font,
                                    fontRenderContext).getBounds().getHeight();

        if(backgroundColor!=null){
            g.setColor(backgroundColor);
            g.fill(new RoundRectangle2D.Float(x-0.5f*stringWidth-10,
                                              y-0.5f*stringHeight-5,
                                              stringWidth+20,
                                              stringHeight+10,
                                              20f,20f));
        }

        g.setFont(font);
        if(fontColor!=null)
            g.setColor(fontColor);
        g.drawString(string,
                     x-0.5f*stringWidth,
                     y+0.5f*stringHeight);
    }

    /*
      @param g Graphics object of what user will see.
    */
    private int getMinimumDimension(){
        return Math.min(this.getWidth(),this.getHeight());
    }

    private void paintMe(final Graphics2D g){

        final int width = this.getWidth();
        final int height = this.getHeight();

        final int size = this.getMinimumDimension();

        final float cardAtTableWidth = 0.25f*size;
        final float cardAtTableHeight = 1.5f*cardAtTableWidth;

        final RoundRectangle2D.Float cardAtTableShape
            = new RoundRectangle2D.Float
            (0,0,
             cardAtTableWidth,cardAtTableHeight,
             0.15f*cardAtTableWidth,0.15f*cardAtTableWidth);

        //Is virtualImage the wrong size?
        if(this.virtualImage==null
           ||this.virtualImage.getWidth()<width
           ||this.virtualImage.getHeight()<height){

            this.initializeVirtualImage();
            this.initializeBackgroundImage();
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                           RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        final Graphics2D vg = this.virtualImage.createGraphics();

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

        //g.setRenderingHint(RenderingHints.KEY_RENDERING,
        //		   RenderingHints.VALUE_RENDER_QUALITY);
        
        // Get the current transform
        final AffineTransform originalTransform = g.getTransform();
        final AffineTransform originalVirtualTransform = vg.getTransform();
        
        final float cardsAtTableStartX
            = 0.5f*width - 0.5f*cardAtTableWidth;
        final float cardsAtTableStartY
            = 0.5f*height - 0.5f*cardAtTableHeight;

        g.translate(cardsAtTableStartX,cardsAtTableStartY);
        vg.translate(cardsAtTableStartX,cardsAtTableStartY);

        paintCardsAtTable(g,vg,cardAtTableShape);

        // Restore original transform
        g.setTransform(originalTransform);
        vg.setTransform(originalVirtualTransform);
        
        final Font font = new Font("Lucida Sans",Font.BOLD,10)
            .deriveFont(0.025f*height);

        for(Hand hand : this.hands){

            g.translate(hand.x*width,hand.y*height);
            vg.translate(hand.x*width,hand.y*height);

            paintHand(g,vg,hand);

            // Restore original transform
            g.setTransform(originalTransform);
            vg.setTransform(originalVirtualTransform);
        }

    }

    private void paintHand(final Graphics2D g,
                           final Graphics2D vg,
                           final Hand hand){

        final AffineTransform originalTransform = g.getTransform();
        final AffineTransform originalVirtualTransform = vg.getTransform();

        final float cardAtHandWidth = hand.cardWidth*this.getWidth();
        final float cardAtHandHeight = 1.5f * cardAtHandWidth;

        if(hand.label!=null){
            final int fontSize = Math.max(10,(int)(0.1*cardAtHandHeight));
            final Font font = new Font("Lucida Sans",Font.BOLD,fontSize);

            paintString(g,
                        hand.label,
                        font,
                        0f,
                        1.2f*cardAtHandHeight,
                        Color.WHITE,
                        Color.BLACK);
        }

        final RoundRectangle2D.Float shape
            = new RoundRectangle2D.Float
            (0,0,cardAtHandWidth,cardAtHandHeight,
             0.15f*cardAtHandWidth,0.15f*cardAtHandWidth);

        final float rotationRadius = (float)(2*shape.getHeight());
        final double singleRotation=Math.PI/60.0;

        final int numberOfCardsAtHand = hand.cards.size();

        g.rotate(-singleRotation*0.5f*(numberOfCardsAtHand-1),0,
                 rotationRadius);
        vg.rotate(-singleRotation*0.5f*(numberOfCardsAtHand-1),0,
                  rotationRadius);

        for(int i=0;i<numberOfCardsAtHand;i++){

            final HandCard handCard = hand.cards.get(i);
            final byte card = handCard.card;

            drawCard(card,g,shape,
                     (selected!=UNCLICKABLE) && (selected == handCard.virtualColor),
                     i==numberOfCardsAtHand-1,
                     handCard.upsidedown);

            vg.setColor(new Color(0,0,handCard.virtualColor));
            vg.fill(shape);

            //We need more rotation for 10:
            if((card & Card.VALUE_MASK)==Card.TEN){
                g.rotate(1.5f*singleRotation,0,rotationRadius);
                vg.rotate(1.5f*singleRotation,0,rotationRadius);
            }else{
                g.rotate(singleRotation,0,rotationRadius);
                vg.rotate(singleRotation,0,rotationRadius);
            }
        }
        
        if(hand.hasArrowAbove || hand.hasArrowBelow){
            // Restore original transform
            g.setTransform(originalTransform);
            vg.setTransform(originalVirtualTransform);

            final Polygon downArrow = new Polygon();
            final float unit = 0.01f*this.getMinimumDimension();
            downArrow.addPoint(0,0);
            downArrow.addPoint((int)(4*unit),0);
            downArrow.addPoint((int)(2*unit),(int)(7*unit));
            downArrow.addPoint((int)(7*unit),(int)(6*unit));
            downArrow.addPoint(0,(int)(15*unit));
            downArrow.addPoint((int)(-7*unit),(int)(6*unit));
            downArrow.addPoint((int)(-2*unit),(int)(7*unit));
            downArrow.addPoint((int)(-4*unit),0);
            downArrow.translate(0,(int)(-15.1f*unit));

            if(hand.hasArrowAbove){
                g.setColor(Color.ORANGE);
                g.fill(downArrow);
                g.setStroke(new BasicStroke(3));
                g.setColor(Color.YELLOW);
                g.draw(downArrow);

                vg.setColor(new Color(0,0,UNCLICKABLE));
                vg.fill(downArrow);
            }
            if(hand.hasArrowBelow){
                g.translate(0f,1.3f*cardAtHandHeight);
                g.rotate(Math.PI);
                g.setColor(Color.ORANGE);
                g.fill(downArrow);
                g.setStroke(new BasicStroke(3));
                g.setColor(Color.YELLOW);
                g.draw(downArrow);

                vg.setColor(new Color(0,0,UNCLICKABLE));
                vg.fill(downArrow);

            }
        }
    }

    /** In most card games cards at table are not clickable, so vg
     * argument will be unused.
     */
    private void paintCardsAtTable(final Graphics2D g,
                                   final Graphics2D vg,
                                   final RoundRectangle2D.Float shape){

        final float shapeW = (float)shape.getWidth();
        final float shapeH = (float)shape.getHeight();
        final float shapeCenterX = (float)shape.getCenterX();
        final float shapeCenterY = (float)shape.getCenterY();
        
        final int numberOfCardsAtTable = cardsAtTable.size();

        for(int i=0;i<numberOfCardsAtTable;i++){

            final CardAtTable cardAtTable = cardsAtTable.get(i);

            // Get the current transform
            final AffineTransform originalTransform = g.getTransform();
            final AffineTransform originalVirtualTransform = vg.getTransform();

            g.translate(cardAtTable.x*shapeW,cardAtTable.y*shapeH);
            vg.translate(cardAtTable.x*shapeW,cardAtTable.y*shapeH);


            g.rotate(cardAtTable.theta,shapeCenterX,shapeCenterY);
            vg.rotate(cardAtTable.theta,shapeCenterX,shapeCenterY);

            drawCard(cardAtTable.card,g,shape,
                     (cardAtTable.virtualColor!=UNCLICKABLE)
                     && (selected == cardAtTable.virtualColor),
                     true,
                     false);

            vg.setColor(new Color(0,0,cardAtTable.virtualColor));
            vg.fill(shape);

            // Restore original transform
            g.setTransform(originalTransform);
            vg.setTransform(originalVirtualTransform);
        }
    }

    //Look at Shapes class to see how we represent svg.
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

            if(command==Shapes.M){
                x=svg[i++];
                y=svg[i++];
                path.moveTo(x,y);
            }else if(command==Shapes.c){

                x1=x+svg[i++];
                y1=y+svg[i++];
                x2=x+svg[i++];
                y2=y+svg[i++];
                x=x+svg[i++];
                y=y+svg[i++];
                path.curveTo(x1,y1,
                             x2,y2,
                             x,y);
            }else if(command==Shapes.s){
                x1=x+(x-x2);
                y1=y+(y-y2);
		
                x2=x+svg[i++];
                y2=y+svg[i++];
                x=x+svg[i++];
                y=y+svg[i++];

                path.curveTo(x1,y1,
                             x2,y2,
                             x,y);
            }else if(command==Shapes.z){
                path.closePath();
            }else{
                //System.err.println("Invalid: "+command);
            }
        }

        return path;
    }   
}
