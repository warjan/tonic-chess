package free.jin.seek.prefs;

import free.jin.BadChangesException;
import free.jin.seek.SoughtGraphPlugin;
import free.jin.ui.PreferencesPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by wojtek on 24.06.13.
 */
public class SoughtGraphPrefsPanel extends PreferencesPanel {

  private SoughtGraphPlugin soughtGraphPlugin;
  private JCheckBox showImageCheckbox;
  //private JButton chooseImageButton;

  public SoughtGraphPrefsPanel(SoughtGraphPlugin soughtGraphPlugin) {
    this.soughtGraphPlugin = soughtGraphPlugin;
    createControls();
    createUI();
  }

  @Override
  public void applyChanges() throws BadChangesException {
    soughtGraphPlugin.getPrefs().setBool("background.image.show", showImageCheckbox.isSelected());
  }

  private void createUI() {
    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateContainerGaps(true);
    layout.setAutoCreateGaps(true);
    setLayout(layout);

    layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(showImageCheckbox)
                    //.addComponent(chooseImageButton)
    ));

    layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(showImageCheckbox)
            //.addComponent(chooseImageButton)
    );

  }

  private void createControls() {
    showImageCheckbox = new JCheckBox("Show image in the background?");
    showImageCheckbox.setSelected(soughtGraphPlugin.getPrefs().getBool("background.image.show"));
    showImageCheckbox.addActionListener(new PrefsActionListener());
    //chooseImageButton = new JButton("Choose image for background");
  }

  private class PrefsActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      fireStateChanged();
    }
  }
}
