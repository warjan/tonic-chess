package free.chess.variants.bughouse;

import free.chess.*;

/**
 * This a variant for supporting Bughouse and Crazyhouse variants of chess.
 */
public class Bughouse extends ChesslikeGenericVariant {

    private static final Bughouse INSTANCE = new Bughouse();

    /**
     * Creates a new ChesslikeGenericVariant with the given initial position and
     * name. The position is specified in FEN format.
     */
    private Bughouse() {
        super(Chess.INITIAL_POSITION_FEN, "Bughouse");

    }

    public static Bughouse getInstance() {
        return INSTANCE;
    }

    /**
     * <P>Makes the given ChessMove in the given Position. <B>This method
     * shoudln't (and can't) be called directly - call
     * {@link free.chess.Position#makeMove(free.chess.Move)} instead.</B>
     *
     * @throws IllegalArgumentException if the given Move is not an instance of
     *                                  <code>ChessMove</code>.
     */

    public void makeMove(Move move, Position pos, Position.Modifier modifier) {
        checkPosition(pos);

        if (!(move instanceof BughouseMove) && !(move instanceof ChessMove))
            throw new IllegalArgumentException("Wrong piece type: " + move.getClass());
        Piece dropingPiece = null;
        Square endingSquare = null;
        if (move.getStartingSquare() == null) {
            BughouseMove bmove = (BughouseMove) move;
            dropingPiece = bmove.getDropingPiece();
            endingSquare = bmove.getEndingSquare();
            modifier.dropPiece(dropingPiece, endingSquare);
        }


        if (dropingPiece == null) {
            ChessMove cmove = (ChessMove) move;
            super.makeMove(cmove, pos, modifier);
        }


    }

    /**
     * This method creates a move from given parameters. The startingSquare parameter is
     * here to make this method unambigous with Kriegspiel createMove method.
     * @param pos - the position in which move is created
     * @param droppedPiece - the piece being dropped on the board in this move
     * @param startingSquare - this is null when we drop a piece
     * @param endingSquare - the square we drop piece on
     * @param promotionTarget - this is null when piece is dropped
     * @param moveSAN - simple algebric notation of the move (P@a5 for dropping pawn on a5 sqaure).
     * @return BughouseMove
     */
    public Move createMove(Position pos, Piece droppedPiece, Square startingSquare, Square endingSquare,
                           Piece promotionTarget, String moveSAN) {

        checkPosition(pos);

        if ((promotionTarget != null) && !(promotionTarget instanceof ChessPiece))
            throw new IllegalArgumentException("Wrong promotion target type: " + promotionTarget.getClass());

        Piece dropingPiece = droppedPiece;

        Player movingPlayer = droppedPiece.getPlayer();


        return new BughouseMove(dropingPiece, startingSquare, endingSquare, movingPlayer, moveSAN);
  }
}
