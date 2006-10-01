package free.jin.event;

/**
 * Created by IntelliJ IDEA.
 * User: whp
 * Date: 2006-10-01
 * Time: 21:04:03
 * To change this template use File | Settings | File Templates.
 */
public interface BughouseListenerManager {
    void addBughouseListener(BughouseListener l);
    void removeBughouseListener(BughouseListener l);
}
