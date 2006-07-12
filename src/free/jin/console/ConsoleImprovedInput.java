package free.jin.console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: whp
 * Date: 2006-05-22
 * Time: 09:14:41
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleImprovedInput extends JPanel {

    /**
     * Normal console input component.
     */

    private ConsoleTextField normalInput;

    /**
     * Button that hides input helper panel.
     */
    private JButton hideButton;

    /**
     * Text fields that hold prefix for command issued to server.
     */

    private JTextField prefixHolder;

    /**
     * Panel that holds button and special text field.
     */

    private JPanel inputHelper;

    /**
     * Prefix of command send to server.
     */

    private String commandPrefix;

    /**
     * Console for creating console input.
     */

    private Console console;

    /**
     * Constructor.
     */

    public ConsoleImprovedInput(Console console, String s){
        this.commandPrefix = s;
        this.console = console;
        createUI(commandPrefix);
    }

    /**
     * Creates ui for ConsoleTextField with server command passed to constructor.
     */
    private void createUI(String s) {
        this.setLayout(new BorderLayout());
        inputHelper = new JPanel();
        inputHelper.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        final URL hideIconURL = ConsoleImprovedInput.class.getResource("hide.png");
        final ImageIcon hideIcon = new ImageIcon(hideIconURL);
        final URL showIconURL = ConsoleImprovedInput.class.getResource("show.png");
        final ImageIcon showIcon = new ImageIcon(showIconURL);
        hideButton = new JButton(hideIcon);
        hideButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                if(hideButton.getIcon().equals(hideIcon)){
                    hideButton.setIcon(showIcon);
                }
                else{
                    hideButton.setIcon(hideIcon);
                }
            }
        });

        prefixHolder = new JTextField(s);

        inputHelper.add(hideButton);
        inputHelper.add(prefixHolder);

        this.add(inputHelper, BorderLayout.WEST);
        normalInput = new ConsoleTextField(console, commandPrefix);

    }
}
