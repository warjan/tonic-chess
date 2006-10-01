package free.jin.event;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: whp
 * Date: 2006-10-01
 * Time: 21:15:03
 * To change this template use File | Settings | File Templates.
 */
public interface BughouseListener extends EventListener {


    /**
     * Method called when list of pieces available for dropping arrives from server.
     */
    void bughousePiecesUpdate(BughouseEvent e);

}
