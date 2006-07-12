
/*
 * MessagesConnection.java
 *
 * Created on 24 maj 2006, 18:29
 *
 * Interface to supplies method for managing messages.
 */
package free.jin;

/**
 * This inteface defines method for interacting with messages.
 * @author whp
 */
public interface MessagesConnection extends Connection {

    /**
     * Method for clearing  messages from server.
     * @param messageFromNumber - number of message from which clearing messages will start
     * @param messageToNumber - number of message to wich clearing messages will end
     */
    public void clearMessage(int messageFromNumber, int messageToNumber);



    /**
     * Method for calling for messages from server. Check implementation javadoc for
     * possible values.
     * @param messageFromNumber - number of message from which will start received list
     * @param messageToNumber - number of message to wich will end received list
     */
    public void getMessages(int messageFromNumber, int messageToNumber);
}



