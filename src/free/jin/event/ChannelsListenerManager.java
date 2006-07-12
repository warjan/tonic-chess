/*
 * ChannelsListenerManager.java
 *
 * Created on 16 marzec 2006, 07:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.event;

import free.jin.event.ChannelsListener;

/**
 * The extension to ListnerManager allowing registering and unregisetering
 * ChannelsLiteners
 * @author whp
 */
public interface ChannelsListenerManager extends ListenerManager {

    void addChannelsListener(ChannelsListener listener);
    void removeChannelsListener(ChannelsListener listener);
    
}
