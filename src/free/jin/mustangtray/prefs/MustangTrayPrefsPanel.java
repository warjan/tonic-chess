package free.jin.mustangtray.prefs;

import free.jin.ui.PreferencesPanel;
import free.jin.BadChangesException;
import free.jin.mustangtray.MustangTray;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: whp
 * Date: 2006-11-08
 * This is the class creating a dialog for setting
 * various preferences for MustangTray plugin.
 */
public class MustangTrayPrefsPanel extends PreferencesPanel {

    private MustangTray mustangTray;

    private JCheckBox displayTray;

    public MustangTrayPrefsPanel(MustangTray mustangTray){
        this.mustangTray = mustangTray;
        displayTray = new JCheckBox("Display tray icon?");
        displayTray.addActionListener(new PrefsActionListener());
        displayTray.setSelected(mustangTray.getPrefs().getBool("display.tray", true));
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
        panel.add(displayTray, c);
        add(panel, c);
    }

    /**
     * Applies the changes done by the user.
     *
     * @throws free.jin.BadChangesException if the changes done by the user are invalid.
     */

    public void applyChanges() throws BadChangesException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private class PrefsActionListener implements ActionListener{

        /**
     * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            fireStateChanged();
        }
    }
}
