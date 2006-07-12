/*
 * MessageEvent.java
 *
 * Created on 23 maj 2006, 20:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.event;

import free.jin.Connection;
import free.jin.event.JinEvent;
/**
 *
 * @author whp
 */
public class MessageEvent extends JinEvent {
    
    /**
     * The id specifying event fired when there are unread messages on the server.
     */
    
    public static final int UNREAD_MESSAGES_PRESENT = 1;
    
    /**
     * The id specyfing event fired when message was received from server.
     */
    
    public static final int MESSAGE_RECEIVED = 2;
    
    /**
     * The id specyfing event fired when one message was cleared.
     */
    
    public static final int MESSAGE_CLEARED = 3;
    
    /**
     * The id specyfing event fired when message was sent.
     */
    
    public static final int MESSAGE_SENT = 4;
    
    /**
     * The id specyfing event fired when message list is received from server.
     */
    
    public static final int MESSAGES_LIST_RECEIVED = 5;
    
    /**
     * The id specyfing event fired when there were problems with sending message to server.
     * Like when receiver's name is badly typed or else (currently no other server responses are implemented).
     */
     
     public static final int MESSAGE_SEND_ERROR = 6;
    
    /**
     * The id of event.
     */
    
    private final int id;
    
    /**
     * Number of all messages.
     */
    
    public int allMessages;
    
    /**
     * Number of unread messages.
     */
    
    public int unreadMessages;
    
    /**
     * The message number.
     */
     
     public int number;
     
     /**
      * The sender of the message.
      */
      
      public String user;
     
     /**
      * The date when message was received.
      */
    
    public String date;
    
    /**
     * Message content.
     */
    
    public String content;
    
    /** Creates a new instance of MessageEvent */
    public MessageEvent(Connection conn, int id, int allMessages, int unreadMessages,int number, String user, String date, String content) {
        super(conn);
        
        switch(id){
            case UNREAD_MESSAGES_PRESENT:
            case MESSAGE_RECEIVED:
            case MESSAGE_CLEARED:
            case MESSAGE_SENT:
            case MESSAGES_LIST_RECEIVED:
            case MESSAGE_SEND_ERROR:
                break;
            default:
                throw new IllegalArgumentException("Bad id for MessageEvent: " + id);

        }
        
        this.id = id;
        this.allMessages = allMessages;
        this.unreadMessages = unreadMessages;
        this.number = number;
        this.user = user;
        this.date = date;
        this.content = content;
    }
    
    /**
     * Method that returns id.
     */
     
     public int getId(){
         return id;
     }
     
     /**
      * Method that returns number of all stored messages.
      */
      
      public int getAllMessages(){
        return allMessages;
      }
      
      /**
       * Method that returns number of all unread messages.
       */
       
       public int getUnread(){
           return unreadMessages;
       }
       
       /**
        * Method that returns number of the just received message.
        */
        
        public int getNumber(){
            return number;
        }
      
      /**
       * Method that returns sender's name.
       */
       
       public String getSenderName(){
           return user;
       }
       
       /**
        * Method that returns date of sending.
        */
        
        public String getDate(){
            return date;
        }
        
        /**
         * Method that returns content of received message.
         */
         
         public String getContent(){
             return content;
         }
    
}
