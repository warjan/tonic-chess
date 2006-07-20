package free.jin.channels;

import free.jin.ui.DialogPanel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;


/**
 * This panel is used to add/remove arrays of channels from Channels Manager ui.
 */
public class ManageChannelsPanel extends DialogPanel {

   JTextField channelsField;
    JLabel help;
    public ManageChannelsPanel(){


        channelsField = new JTextField();
        help = new JLabel();
        createUI();

    }

    private void createUI() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        help.setText("Type in the field space seperated list of channels to add/remove. For example: -106 2. ");
        channelsField.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                String result = channelsField.getText();
                close(result);
            }
        });
        c.gridheight = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,2,0);
            this.add(help,c);
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
            this.add(channelsField,c);
    }

    protected String getTitle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
