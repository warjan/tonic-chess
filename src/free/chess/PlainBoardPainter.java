/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.*;


/**
 * An implementation of BoardPainter which fills the squares with a solid color.
 */

public class PlainBoardPainter extends AbstractColoredBoardPainter{


  
  /**
   * Creates a new <code>PlainBoardPainter</code> which paints squares with the
   * given light square color and dark square color.
   */

  public PlainBoardPainter(Color lightColor, Color darkColor){
    super(lightColor, darkColor);
  }
  
  
  
  /**
   * Creates a new <code>PlainBoardPainter</code> with some default colors for
   * light and dark squares.
   */
   
  public PlainBoardPainter(){
    this(new Color(255,207,144),new Color(143,96,79));
  }
  
  
  
  /**
   * Does nothing, since we have no external resources to load.
   */
   
  public void loadResources(){
    
  }

  
  
  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size.
   */

  public void paintBoard(Graphics g, Component component, int x, int y, int width, int height){

      JBoard board = (JBoard)component;
      Rectangle boardRect = board.getBoardRect(null);
    Graphics2D g2 = (Graphics2D)g.create();
    int squareWidth = boardRect.width/8;
    int squareHeight = boardRect.height/8;
    Color lightColor = getLightColor();
    Color darkColor = getDarkColor();
    
    for (int i=0;i<8;i++)
      for (int j=0;j<8;j++){
        if ((i+j)%2==0)
          g2.setColor(lightColor);
        else
          g2.setColor(darkColor);

        g2.fillRect(x+i*squareWidth, y+j*squareHeight, squareWidth, squareHeight);

      }
      /*int borderWidth;
      int borderHeight;
      int coordsHeight = 0;
      int coordsWidth = 0;

      if (board.getBorder() != null){
      Insets borderInsets = board.getBorder().getBorderInsets(component);
        borderWidth = borderInsets.left;
        borderHeight = borderInsets.top;
      }else{
          borderWidth = 0;
          borderHeight = 0;
      }

      if (board.getTextHeight() != 0){
          coordsHeight = board.getTextHeight();
          coordsWidth = board.getTextWidth();

      } */

      /* Draws lines between squares */
      g2.setColor(Color.black);
      g2.setStroke(new BasicStroke(1f));
      for (int i = 1; i < 8; i++){
          g2.drawLine(i*squareWidth + boardRect.x, boardRect.y, i*squareWidth + boardRect.x, height + boardRect.y);
          g2.drawLine( boardRect.x,i*squareHeight+boardRect.y, width + boardRect.x, i*squareHeight+boardRect.y);

      }
      g2.dispose();
  }


}
