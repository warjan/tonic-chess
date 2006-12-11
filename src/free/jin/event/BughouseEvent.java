package free.jin.event;

import free.jin.BughouseConnection;

/**
 * Created by IntelliJ IDEA.
 * User: whp
 * Date: 2006-09-30
 * Time: 01:18:38
 * This class serves as a event for Bughouse games.
 */
public class BughouseEvent extends JinEvent {

    int gameNumber;

    String whiteAvailablePieces;

    String blackAvailablePieces;

    public BughouseEvent(BughouseConnection conn,int gameNumber, String whiteAvailablePieces, String blackAvailablePieces) {
        super(conn);
        this.gameNumber = gameNumber;
        this.whiteAvailablePieces = whiteAvailablePieces;
        this.blackAvailablePieces = blackAvailablePieces;
    }

    public int getGameNumber(){
        return this.gameNumber;
    }

    public String getWhiteAvailablePieces(){
        return this.whiteAvailablePieces;
    }

    public String getBlackAvaiablePieces(){
        return this.blackAvailablePieces;
    }

}
