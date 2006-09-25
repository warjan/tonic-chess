package free.jin.tray.prefs;

import free.jin.ui.PreferencesPanel;
import free.jin.tray.TrayManager;
import free.jin.BadChangesException;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * This is a class for setting TrayManager plugin preferences.
 */
public class TrayPrefsPanel extends PreferencesPanel {

    private TrayManager trayManager;

    private JCheckBox displayTray;

    public TrayPrefsPanel(TrayManager trayManager) {
        this.trayManager = trayManager;
        displayTray = new JCheckBox("Display tray icon?");
        displayTray.addActionListener(new PrefsActionListener());
        displayTray.setSelected(trayManager.getPrefs().getBool("display.tray", true));
       createUI();
    }

    private void createUI() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Tray Manager settings"),
        BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        panel.setLayout(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;
        panel.add(displayTray,c);
        add(panel, c);



    }

    public void applyChanges() throws BadChangesException {
        trayManager.getPrefs().setBool("display.tray", displayTray.isSelected());

    }

       private class PrefsActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            fireStateChanged();
        }
    }
}
