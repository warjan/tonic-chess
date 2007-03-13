package free.jin.console;

import free.jin.ui.OptionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
* User: whp
* Date: 2007-02-11
* Time: 20:53:08
* This is a panel in which user specifies the type of new console.
*/

//TODO: Rewrite code responsible for choosing new console type. Check this class, ConsoleManager and Console.
//TODO: Create getter for toolbar in Console ui or something else :].    
class NewConsolePanel extends JWindow {
    private ConsoleManager consoleManager;
    private JButton newConsoleButton;
    private JComboBox newConsleSpec;


    NewConsolePanel(ConsoleManager consoleManager){

        this.consoleManager = consoleManager;
        setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    newConsoleButton = new JButton("N C");
        /*newConsoleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (
                        ((String) newConsoleSpec.getSelectedItem()).equals("Channel tells") && !(chanGameNumberInput.getText().matches("\\d{1,3}")))
                {
                    OptionPanel.error(console, "Channel number error", "Type a number of channel you would like to here, please.");
                }

                if (((String) newConsoleSpec.getSelectedItem()).matches("(Kibitzes)|(Whispers)") && !(chanGameNumberInput.getText().matches("\\d{1,4}")))
                {
                    OptionPanel.error(console, "Game number error", "Type a game number for which you would like hear " + newConsoleSpec.getSelectedItem().toString().toLowerCase() + " , please.");
                } else {
                    if (((String) newConsoleSpec.getSelectedItem()).equals("Private tells")) {

                        openNewConsole(chanGameNumberInput.getText());
                    } else {
                        System.out.println("EQUALS!!! " + chanGameNumberInput.getText() + "<->" + newConsoleSpec.getSelectedItem());
                        openNewConsole();
                    }
                }
            }
        });*/
    consoleManager.newConsoleSpec = consoleManager.getNewConsoleSpec();
    consoleManager.chanGameNumberInput = consoleManager.getChanGameNrInput();
    //chanGameNumberInput = new JTextField("" ,10);
    consoleManager.chanGameNumberInput.setEnabled(false);

    consoleManager.listenToPlain = new JCheckBox("Receive plain text.");
    consoleManager.listenToPlain.addItemListener(new MyItemListener(consoleManager));
    consoleManager.consoleTypes = consoleManager.getConsoleTypes();

    JLabel newConsoleLabel = new JLabel("Specify new console type:");
    JLabel newConsoleSubtypeLabel = new JLabel("Custom subtype(s):");
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.1;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.WEST;
    add(newConsoleLabel, gbc);
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.EAST;
    add(consoleManager.newConsoleSpec, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.1;
    gbc.anchor = GridBagConstraints.WEST;
    add(newConsoleSubtypeLabel, gbc);
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.EAST;
    add(consoleManager.chanGameNumberInput, gbc);
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.weightx = 0.1;
    add(consoleManager.listenToPlain, gbc);

    gbc.gridx = 2;
    gbc.gridy = 3;
    gbc.weightx = 0;
    gbc.weighty = 1.0;
    gbc.anchor = GridBagConstraints.EAST;
    add(consoleManager.newConsoleButton, gbc);
    }

    private static class MyItemListener implements ItemListener {
        private final ConsoleManager consoleManager;

        public MyItemListener(ConsoleManager consoleManager) {
            this.consoleManager = consoleManager;
        }

        public void itemStateChanged(ItemEvent e) {
            String oldChanGameText = consoleManager.chanGameNumberInput.getText().trim();
            String newChanGameText = null;
            if (e.getStateChange() == ItemEvent.SELECTED) {
                newChanGameText = oldChanGameText.concat(" plain");
                newChanGameText = newChanGameText.trim();
                consoleManager.chanGameNumberInput.setText(newChanGameText);
            } else {
                newChanGameText = oldChanGameText.replace("plain", "");
                consoleManager.chanGameNumberInput.setText(newChanGameText);
            }
        }
    }
}
