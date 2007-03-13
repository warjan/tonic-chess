package free.jin.channels.prefs;

import free.jin.ui.PreferencesPanel;
import free.jin.channels.ChannelsManager;
import free.jin.BadChangesException;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;


/**
 * Panel for setting preferences for Channels Manager plugin
 */



public class ChannelsManagerPrefsPanel extends PreferencesPanel {

    /**
     * Channels Manager plugin
     */

    private ChannelsManager channelsManager;

    /**
     * Check box for setting if date should be shown in channels manager consoles.
     */

    private JCheckBox dateCheckBox;

    /**
     * Check box for setting preference if tabs should be closable.
     */

    private JCheckBox closeableCheckBox;

    /**
     * Group for setting preference what kind of prefix
     * should be displayed initially in consoles in channels manager.
     */

    private JRadioButton tellRadio;

    /**
     * Check box for setting preference whether shout tab should be displayed.
     */

    private JCheckBox shoutCheckBox;

    /**
     * Check box for setting preference whether c-shout tab should be displayed.
     */
    private JCheckBox cshoutCheckBox;

    /**
     * Check box for setting preference whether plain text tab should be displayed.
     */
    private JCheckBox plainCheckBox;

    /**
     * The channels manager
     * @param channelsManager
     */

    public ChannelsManagerPrefsPanel(ChannelsManager channelsManager) {
        this.channelsManager = channelsManager;
        dateCheckBox = new JCheckBox("Display timestamp for tells?");
        closeableCheckBox = new JCheckBox("Use closeable tabs?");
        shoutCheckBox = new JCheckBox("Display tab for shouts?");
        cshoutCheckBox = new JCheckBox("Display tab for cshouts (chess related shouts)?");
        plainCheckBox = new JCheckBox("Display tab for plain text?");

        dateCheckBox.addActionListener(new PrefsActionListener());
        dateCheckBox.setSelected(channelsManager.getPrefs().getBool("date.display"));

        closeableCheckBox.addActionListener(new PrefsActionListener());
        closeableCheckBox.setSelected(channelsManager.getPrefs().getBool("tabs.closeable"));

        shoutCheckBox.addActionListener(new PrefsActionListener());
        shoutCheckBox.setSelected(channelsManager.getPrefs().getBool("tabs.shout"));

        cshoutCheckBox.addActionListener(new PrefsActionListener());
        cshoutCheckBox.setSelected(channelsManager.getPrefs().getBool("tabs.cshout"));

        plainCheckBox.addActionListener(new PrefsActionListener());
        plainCheckBox.setSelected(channelsManager.getPrefs().getBool("tabs.plain"));
        createUI();
    }

    private void createUI() {

        //JLabel dateLabel = new JLabel("Display timestamp for tells?");
        //JLabel closeableLabel = new JLabel("Use closeable tabs?");
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Channels Manager settings"),
        BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        panel.setLayout(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.5;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.WEST;
        //panel.add(dateLabel);
        //c.gridx = 2;
        panel.add(dateCheckBox, c);

        c.gridy = 2;
        c.gridx = 0;
        //panel.add(closeableLabel, c);
        //c.gridx = 2;
        panel.add(closeableCheckBox, c);

        c.gridy = 3;
        panel.add(shoutCheckBox, c);

        c.gridy = 4;
        panel.add(cshoutCheckBox, c);

        c.gridy = 5;
        panel.add(plainCheckBox, c);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;
        add(panel,c);

    }


    public void applyChanges() throws BadChangesException {
        channelsManager.getPrefs().setBool("tabs.closeable", closeableCheckBox.isSelected());
        channelsManager.getPrefs().setBool("date.display", dateCheckBox.isSelected());
        channelsManager.getPrefs().setBool("tabs.shout", shoutCheckBox.isSelected());
        channelsManager.getPrefs().setBool("tabs.cshout", cshoutCheckBox.isSelected());
        channelsManager.getPrefs().setBool("tabs.plain", plainCheckBox.isSelected());
    }

    private class PrefsActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            fireStateChanged();
        }
    }
}
