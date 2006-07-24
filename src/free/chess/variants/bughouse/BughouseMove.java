package free.chess.variants.bughouse;

import free.chess.*;

/**
 * A move class for supporting Bughouse and Crazyhouse chess variants in Tonic.
 */
public class BughouseMove extends Move {

    private Square endingSquare;

    private Piece dropingPiece;

    private Player movingPlayer;

    private String moveSAN;




    public BughouseMove(Piece dropingPiece, Square endingSquare, Player movingPlayer, String moveSAN) {

        super(dropingPiece, endingSquare, movingPlayer, moveSAN);
        this.dropingPiece = dropingPiece;

    }


    public Piece getDropingPiece() {
        return dropingPiece;
    }
}
