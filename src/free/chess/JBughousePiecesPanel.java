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
    private int[][] piecesQuantities = new int[2][5];
     //private final String[][] piecesQuantities = {{"1" ,"2" ,"3", "4", "5"},{"5", "4", "3", "2", "1"}};
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
     * String arrays holding names for white pieces. Black pieces are
     * made of this with appropriate string method.
     */

    private static final String[] piecesNamesQtoP = {"Q", "R", "B", "N", "P"};
    private static final String[] piecesNamesPtoQ = {"P", "N", "B", "R", "Q"};

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

    public void setFlipped(boolean isBoardFlipped) {
        boolean oldValue = this.isBoardFlipped;
        this.isBoardFlipped = isBoardFlipped;
        repaint();
        firePropertyChange("boardflipped", oldValue, isBoardFlipped);
    }

    /**
     * Method that sets new PiecePainter for this JBughousePiecesPanel. Usually called
     * when PiecePainter of the chess board is changed.
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
     * Method that determines what pieces (white or black) are closer to the board.
     * It may flip the pieces when
     * @param ptoQ - true if white should be closer, false if black.
     */

    public void setPtoQ(boolean ptoQ){
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


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();


        if (isOpaque()) { //paint background
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        int size = getPiecesSize();

        int k = isBoardFlipped == true ? piecesRectangles.length : -1;
        String[] piecesNames = isPtoQ == true ? piecesNamesPtoQ : piecesNamesQtoP;
        for (Rectangle[] rectArray : piecesRectangles) {
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
                if (!isBoardFlipped) {
                    if (k == 0) {
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i]), g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    } else {
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i].toLowerCase()), g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    }
                } else {
                    if (k == piecesRectangles.length - 1){
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i]), g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    }else{
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i].toLowerCase()), g2d, this, rect, false);
                        drawQuantity(g2d, k, i);
                    }
                }

            }
        }
        g2d.dispose();

    }

    private void drawQuantity(Graphics2D g2d, int k, int i) {
        int qk = -1;
        if (isBoardFlipped){
            switch(k){
                case 0: qk = 1; break;
                case 1: qk = 0; break;
            }
        } else {
            qk = k;
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
        int pieceQuantity = piecesQuantities[qk][i];
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
