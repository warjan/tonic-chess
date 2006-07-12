package free.jin;

import free.jin.event.ChannelsListenerManager;

/**
 * Class that deals with channels managment. Implementation differs according to
 * server rules.
 */
public interface ChannelsConnection extends Connection {

    /**
     * Returns ListenerManager that lets us register listeners
     */

     ChannelsListenerManager getChannelsListenerManager();

    /**
     * Method called when channel is added to user's channel list.
     * @param channelNumber - the number of added channel.
     */

     void addChannel(int channelNumber);

    /**
     * Method called when channel is removed from user's channel list.
     * @param channelNumber - the number of removed channel.
     */

    void removeChannel(int channelNumber);

    /**
     * Method called to get user's channel list from server.
     */
    void updateChannelsList();

}
