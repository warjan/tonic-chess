/*
 * MessageListener.java
 *
 * Created on 24 maj 2006, 18:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.event;

import java.util.EventListener;

/**
 *
 * @author whp
 */
public interface MessageListener extends EventListener {
    
    /**
     * Method called when information about user's messages if received from server. 
     */
    
    public void messageInfoReceived(MessageEvent evt);
    
    /**
     * Method called when message is received from server.
     */
    
    public void messageReceived(MessageEvent evt);
    
    /**
     * Method called when message/messages is cleared from server.
     */
    
    public void messagesCleared(MessageEvent evt);
    
    
    
}
