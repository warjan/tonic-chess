package free.chess;

import free.jin.event.BughouseListener;
import free.jin.event.BughouseEvent;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: whp
 * Date: 2006-11-11
 * Time: 16:07:00
 * This is a class that will display available for player pieces in Bughouse
 * games. It displays always all pieces only when player does not have any
 * available pieces on her hand to drop pieces are drawn in 50% alfa.
 */
public class JBughousePiecesPanel extends JComponent implements BughouseListener {

    /**
     * Array of rectangles containing pieces.
     */
    private final Rectangle[][] piecesRectangles = new Rectangle[2][5];

    /**
     * Array of strings that display the quantities of pieces.
     */
   // private int[][] piecesQuantities = new int[2][5];
     private final int[][] piecesQuantities = {{1 ,2 ,3, 4, 5},{5, 4, 3, 2, 1}};
    /**
     * Variable stating whether board is flipped.
     */
    private boolean isBoardFlipped;

    /**
     * Variable stating whtether user of Tonic currently plays as white.
     */

    private boolean isPtoQ;

    /**
     * The size of pieces.
     */

    private int piecesSize;

    /**
     * JBoard for which this JBughousePiecesPanel displays pieces.
     * From this objects JBughousePiecesPanel obtains PiecePainter.
     */
    private JBoard board;

    /**
     * PiecePainter that draws pieces in
     *
     * @see <code>piecesRectangles</code>
     */

    private PiecePainter piecePainter;

    /**
     * Piece arrays holding pieces. There are assigned later to display pieces in appropriate order.
     * The ones with lower case letters are black pieces.
     */

    private static final Piece[] piecesNamesQtoP = {
                                                    ChessPiece.fromShortString("Q"),
                                                    ChessPiece.fromShortString("R"),
                                                    ChessPiece.fromShortString("B"),
                                                    ChessPiece.fromShortString("N"),
                                                    ChessPiece.fromShortString("P"),
                                                    ChessPiece.fromShortString("q"),
                                                    ChessPiece.fromShortString("r"),
                                                    ChessPiece.fromShortString("b"),
                                                    ChessPiece.fromShortString("n"),
                                                    ChessPiece.fromShortString("p"),
                                                                                    };
    private static final Piece[] piecesNamesPtoQ = {
                                                    ChessPiece.fromShortString("P"),
                                                    ChessPiece.fromShortString("N"),
                                                    ChessPiece.fromShortString("B"),
                                                    ChessPiece.fromShortString("R"),
                                                    ChessPiece.fromShortString("Q"),
                                                    ChessPiece.fromShortString("p"),
                                                    ChessPiece.fromShortString("n"),
                                                    ChessPiece.fromShortString("b"),
                                                    ChessPiece.fromShortString("r"),
                                                    ChessPiece.fromShortString("q"),
                                                                                    };

    /**
     * Creates new JBughousePiecesPanel with JBoard that it displays pieces for.
     *
     * @param board - the board we draw pieces for
     */

    public JBughousePiecesPanel(JBoard board) {
        this.board = board;
        this.piecePainter = board.getPiecePainter();
        this.piecesSize = board.getHeight()/10;

    }

    /**
     * Method that flips this panel - it exchanges the posistion of white and black pieces.
     * @param isBoardFlipped - boolean value - if true is passed then black is displayed nearer to board.
     */

    public void setFlipped(boolean isBoardFlipped) {
        boolean oldValue = this.isBoardFlipped;
        this.isBoardFlipped = isBoardFlipped;
        repaint();
        firePropertyChange("boardflipped", oldValue, isBoardFlipped);
    }

    /**
     * Method that sets new PiecePainter for this JBughousePiecesPanel. Usually called
     * when PiecePainter of the chess board changes.
     *
     * @param boardPiecePainter - PiecePainter changed for the board
     */

    public void setPiecePainter(PiecePainter boardPiecePainter) {
        PiecePainter oldValue = this.piecePainter;
        this.piecePainter = boardPiecePainter;
        repaint();
        firePropertyChange("boardpiecepainter", oldValue, boardPiecePainter);
    }

    /**
     * Method that gets the vertical orientation of the pieces.
     * @return isPtoQ - boolean stating what is the orientation of pieces.
     * True if pieces are display from pawn to queen, from top to bottom.
     */

    public boolean isOrientationPtoQ(){
        return isPtoQ;
    }

    /**
     * Method that determines what is the orientation vertically of the pieces from queen to pawn or the other way around.
     * It may flip the pieces when board is flipped.
     * @param ptoQ - true if pieces are displayed from top to bottom from pawno to queen, false otherwise.
     */

    public void setOrientationPtoQ(boolean ptoQ){
        boolean oldValue = this.isPtoQ;
        this.isPtoQ = ptoQ;
        repaint();
        firePropertyChange("piecesorder", oldValue, ptoQ);
    }

    /**
     * Method that sets the size of pieces.
     * @param piecesSize - the size of pieces.
     */
    public void setPiecesSize(int piecesSize){
        int oldValue = this.piecesSize;
        this.piecesSize = piecesSize;
        repaint();
        firePropertyChange("piecessize", oldValue, piecesSize);
    }

    /**
     * Method that gets the size of pieces.
     * @return
     */
    private int getPiecesSize() {
        return this.piecesSize;
    }

    /**
     * Method that paints the component.
     * @param g - Graphics object that component paints on.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();


        if (isOpaque()) { //paint background
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        int size = getPiecesSize();

        int k = isBoardFlipped == true ? piecesRectangles.length : -1;  //column
        Piece[] piecesNames = isPtoQ == true ? piecesNamesPtoQ : piecesNamesQtoP; //orientation
        for (Rectangle[] rectArray : piecesRectangles) {
            //Counting backwards is board is flipped
            if (isBoardFlipped) {
                k--;
            } else {
                k++;
            }
            int i = -1;
            for (Rectangle rect : rectArray) {
                i++;
                rect = new Rectangle(k * size, i * size + (int)2.5*size, size, size);
                g2d.setColor(getBackground());
                g2d.fill(rect);
                piecesRectangles[k][i] = rect;
                //If board flipped black pieces are drawn nearer to board
                if (!isBoardFlipped) {
                    if (k == 0) {
                        piecePainter.paintPiece(piecesNames[i], g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    } else {
                        piecePainter.paintPiece(piecesNames[piecesNames.length/2 + i], g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    }
                } else {
                    if (k == piecesRectangles.length - 1){
                        piecePainter.paintPiece(piecesNames[i], g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    }else{
                        piecePainter.paintPiece(piecesNames[piecesNames.length/2 + i], g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    }
                }

            }
        }
        g2d.dispose();

    }

    /**
     * Method that displays quantities of available pieces in small rectangles.
     * @param g2d - Graphics2D object passed to method.
     * @param k - the column number
     * @param i - row number
     */

    private void drawQuantity(Graphics2D g2d, int k, int i) {
        int qk = -1;
        int qi = -1;
        if (isPtoQ){
          /* switch(k){
                case 0: qk = 1; break;
                case 1: qk = 0; break;
            }*/
            qk = k;
            qi = (piecesQuantities[0].length-1) - i;

        } else {
            qk = k;
            qi = i;
        }

        Rectangle rect = piecesRectangles[k][i];
        System.out.println("K == " + k);
        System.out.println("Rectangle == " + rect);
        Font quantityFont = new Font("Dialog",Font.BOLD,14);
        g2d.translate(rect.x,  rect.y);
        int fontSize = quantityFont.getSize();
        Rectangle fontBackground = new Rectangle(0, rect.height - (fontSize + 8), fontSize+6, fontSize+6);
        g2d.setColor(new Color(64, 196, 64));
        g2d.fill(fontBackground);
        g2d.setColor(Color.WHITE);
        g2d.draw(fontBackground);
        g2d.setColor(Color.WHITE);
        g2d.setFont(quantityFont);
        int pieceQuantity = piecesQuantities[qk][qi];
        g2d.drawString(String.valueOf(pieceQuantity), (fontBackground.width/2) - (g2d.getFontMetrics(quantityFont).stringWidth(String.valueOf(pieceQuantity)))/2, rect.height - (fontBackground.height/2 - fontSize/4 ));
        g2d.translate(-rect.x, -rect.y);
    }

    /**
     * Adds BughouseListener to this JBughousePiecesPanel.
     */

    public void addBughouseListener(BughouseListener l){
        listenerList.add(BughouseListener.class, l);
    }

    /**
     * Remove BughouseListener from this JBughousePiecesPanel.
     */

    public void removeBughouseListener(BughouseListener l){
        listenerList.add(BughouseListener.class, l);
    }




    /**
     * Method called when list of pieces available for dropping arrives from server.
     */
    public void bughousePiecesUpdate(BughouseEvent e) {
        
        String pieces = e.getWhiteAvailablePieces() + e.getBlackAvaiablePieces();
        for(int i = 0; i<pieces.length()-1; i++){
            switch(pieces.charAt(i)){
                case 'P': piecesQuantities[0][0] =+ 1; break;
                case 'N': piecesQuantities[0][1] =+ 1; break;
                case 'B': piecesQuantities[0][2] =+ 1; break;
                case 'R': piecesQuantities[0][3] =+ 1; break;
                case 'Q': piecesQuantities[0][4] =+ 1; break;
                case 'p': piecesQuantities[1][0] =+ 1; break;
                case 'n': piecesQuantities[1][1] =+ 1; break;
                case 'b': piecesQuantities[1][2] =+ 1; break;
                case 'r': piecesQuantities[1][3] =+ 1; break;
                case 'q': piecesQuantities[1][4] =+ 1; break;


            }
        }


    }
}
