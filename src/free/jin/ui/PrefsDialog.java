/*
 * PrefsDialog.java
 *
 * Created on 26 styczeń 2006, 19:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
     * Creates a new <code>PrefsDialog</code> with the specified parent frame,
     * title and preferences panel.
     */

package free.jin.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.jin.BadChangesException;
import free.util.WindowDisposingListener;
import free.util.swing.SwingUtils;

 /**
   * The dialog displaying the preferences panel for a specified plugin.
   */

  public class PrefsDialog extends JDialog implements ChangeListener, ActionListener{



    /**
     * The preferences panel.
     */

    private final PreferencesPanel prefsPanel;



    /**
     * The ok button.
     */

    private final JButton okButton;



    /**
     * The apply button.
     */

    private final JButton applyButton;



    /**
     * The cancel button.
     */

    private final JButton cancelButton;



    /**
     * Creates a new <code>PrefsDialog</code> with the specified parent frame,
     * title and preferences panel.
     */

    public PrefsDialog(Frame parent, String title, PreferencesPanel prefsPanel){
      super(parent, title, true);

      this.prefsPanel = prefsPanel;
      this.applyButton = new JButton("Apply");
      this.okButton = new JButton("OK");
      this.cancelButton = new JButton("Cancel");

      createUI();

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      SwingUtils.registerEscapeCloser(this);

      okButton.addActionListener(this);
      applyButton.addActionListener(this);
      cancelButton.addActionListener(new WindowDisposingListener(this));
      prefsPanel.addChangeListener(this);
    }



    /**
     * Creates the UI.
     */

    private void createUI(){
      Container content = getContentPane();
      content.setLayout(new BorderLayout());

      JPanel prefWrapperPanel = new JPanel(new BorderLayout());
      prefWrapperPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      prefWrapperPanel.add(prefsPanel, BorderLayout.CENTER);
      content.add(prefWrapperPanel, BorderLayout.CENTER);

      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.CENTER);
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

      applyButton.setEnabled(false);
      applyButton.setMnemonic('A');

      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      buttonPanel.add(applyButton);
      bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

      content.add(bottomPanel, BorderLayout.SOUTH);
      this.getRootPane().setDefaultButton(okButton);
    }



    /**
     * ChangeListener implementation. Registered with the preferences panel,
     * enables the apply button when invoked.
     */

    public void stateChanged(ChangeEvent evt){
      applyButton.setEnabled(true);
    }



    /**
     * ActionListener implementation. Registered with the ok and apply buttons.
     */

    public void actionPerformed(ActionEvent evt){
      try{
        if (applyButton.isEnabled())
          prefsPanel.applyChanges();
        applyButton.setEnabled(false);

        if (evt.getSource() == okButton)
          dispose();
      } catch (BadChangesException e){
          OptionPanel.error("Error Setting Preference(s)", e.getMessage());
          if (e.getErrorComponent() != null)
            e.getErrorComponent().requestFocus();
        }
    }

  }

