/**
 *  Simple inteface for sending messages.
 */
package free.jin.action.sendmessage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;
import java.util.Locale;

import javax.swing.*;

import free.jin.action.JinAction;
import free.jin.ui.DialogPanel;
import free.jin.Jin;


/**
 * @author whp
 *
 */
public class SendMessage extends JinAction {


    private ResourceBundle translation = ResourceBundle.getBundle("free.jin.action.sendmessage.sendmessage");
   // private ResourceBundle translation = getBundle(this);

    public String getId() {
        return "sendmessage";
    }

    public void go() {
        final String mess = new  MessagePanel().getMessage();

        if ((mess != null) && ( mess.trim().length() != 0 )) {
         
            String[] messInArray = mess.split("\\[i\\]");
            String[] receivers = messInArray[0].split(" ");
            for (int i = 0; i < receivers.length; i++){
                getConn().sendCommand("mess " + receivers[i] + " " + messInArray[1]);
            }

        }
    }
    public String getName(){

        String s = translation.getString("Send_message");
        //s = translate(s);
        return s;
//        return "Wy\u015blij wiadomo\u015b\u0107";

    }

    private class MessagePanel extends DialogPanel {

        private final JTextField receiversField;
        private final JScrollPane messageSPane;
        private final JTextArea messageField;
        private final JButton sendButton;
        private final JButton cancelButton;
        private final JPanel buttonsPanel;
        private final JLabel receiverLabel;
        private final JPanel receiverPanel;



        public MessagePanel() {
            setLayout(new BorderLayout(10, 10));

            this.setPreferredSize(new Dimension(320, 200));
            receiversField = new JTextField();
            messageField = new JTextArea();
            messageField.setLineWrap(true);
            messageField.setWrapStyleWord(true);
            messageSPane = new JScrollPane(messageField);
            messageSPane.setPreferredSize(new Dimension( 35, 180));

            sendButton = new JButton(translation.getString("Send"));
            cancelButton = new JButton(translation.getString("Cancel"));
            receiverLabel = new JLabel(translation.getString("Type_receiver(s)_name(s)"));
            buttonsPanel = new JPanel();
            //titleLabel = new JLabel("Send a message.");
            buttonsPanel.setLayout(new FlowLayout(2, 5, FlowLayout.CENTER));
            buttonsPanel.add(sendButton);
            buttonsPanel.add(cancelButton);
            //buttonsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

            cancelButton.addActionListener(new ClosingListener(null));
            sendButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    close(getReceiverAndText());
                }
            });

            receiverPanel = new JPanel();
            receiverPanel.setLayout(new BorderLayout());
            receiverPanel.add(receiverLabel, BorderLayout.WEST);
            receiverPanel.add(receiversField, BorderLayout.CENTER);
            add(receiverPanel, BorderLayout.NORTH);

            add(messageSPane, BorderLayout.CENTER);
            add(buttonsPanel, BorderLayout.SOUTH);



        }


        public String getTitle() {

            return translation.getString("Sending_message(s)");
        }
        public String getMessage() {
            return (String)askResult();
        }
        public String getReceiverAndText() {
            final String s = receiversField.getText().trim() + "[i]" + messageField.getText().trim();
            System.out.println(s);
            return s;
        }
    }
}
