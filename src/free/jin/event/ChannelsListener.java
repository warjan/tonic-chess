/*
 * ChannelsListener.java
 *
 * Created on 16 marzec 2006, 07:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.event;

import java.util.EventListener;

/**
 * Listeners that spread the events about adding, removing channels to user's
 * channels list and about viewing it.
 * @author whp
 */
public interface ChannelsListener extends EventListener {

    /**
     * Method called when channel is added to user's channel list.
     */

    void channelAdded(ChannelsEvent evt);

    /**
     * Method called when channel is removed from user's channel list.
     */

    void channelRemoved(ChannelsEvent evt);

    /**
     * Method called when user's channels list is received from server.
     */

    void channelListReceived(ChannelsEvent evt);


    
}
