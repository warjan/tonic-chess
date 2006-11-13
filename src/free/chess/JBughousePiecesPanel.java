package free.chess;

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
public class JBughousePiecesPanel extends JComponent {

    /**
     * Array of rectangles containing pieces.
     */
    private final Rectangle[][] piecesRectangles = new Rectangle[2][5];

    /**
     * Array of strings that display the quantities of pieces.
     */
    private final String[][] piecesQuantities = new String[2][5];

    /**
     * Variables stating whether board is flipped.
     */
    private boolean isBoardFlipped;

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
     * String array holding names for white pieces. Black pieces are
     * made of this with appropriate method.
     */

    private static final String[] piecesNames = {"Q", "R", "B", "N", "P"};

    /**
     * Creates new JBughousePiecesPanel with JBoard that it displays pieces for.
     *
     * @param board - the board we draw pieces for
     */

    public JBughousePiecesPanel(JBoard board) {
        this.board = board;
        this.piecePainter = board.getPiecePainter();
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

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();


        if (isOpaque()) { //paint background
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        int size = board.getHeight() / 10;

        int k = isBoardFlipped == true ? piecesRectangles.length : -1;
        for (Rectangle[] rectArray : piecesRectangles) {
            if (isBoardFlipped) {
                k--;
            } else {
                k++;
            }
            int i = -1;
            for (Rectangle rect : rectArray) {
                i++;
                rect = new Rectangle(k * size, i * size, size, size);
                g2d.setColor(getBackground());
                g2d.fill(rect);
                if (!isBoardFlipped) {
                    if (k == 0) {
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i]), g2d, this, rect, false);
                    } else {
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i].toLowerCase()), g2d, this, rect, false);
                    }
                } else {
                    if (k == piecesRectangles.length){
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i]), g2d, this, rect, false);
                    }else{
                        piecePainter.paintPiece(ChessPiece.fromShortString(piecesNames[i].toLowerCase()), g2d, this, rect, false);
                    }
                }

            }
        }
        g2d.dispose();

    }

}
