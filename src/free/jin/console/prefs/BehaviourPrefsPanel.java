/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2005 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.jin.console.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import free.jin.BadChangesException;
import free.jin.GameListConnection;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;
import free.util.swing.PreferredSizedPanel;



/**
 * A preferences panel which allows the user to select how the console behaves
 * in certain cases.
 */

public class BehaviourPrefsPanel extends PreferencesPanel{
  
  
  
  /**
   * The console manager whose preferences we're displaying/modifying.
   */
  
  private final ConsoleManager consoleManager;
  
  
  
  /**
   * The radio button specifying "embedded" game lists.
   */
  
  private final JRadioButton embeddedGameLists;
  
  
  
  /**
   * The radio button specifying "external" game lists.
   */
  
  private final JRadioButton externalGameLists;
  
  
  
  /**
   * The checkbox specifying copy-on-select policy.
   */
  
  private final JCheckBox copyOnSelect;
  
  
  
  /**
   * The radio button specifying that game lists not be displayed in any special
   * manner.
   */
  
  private final JRadioButton noGameLists;

    /**
     * The text fields for specifing new consoles' height and width
     */
  private JSpinner newconsoleHeight;
    private JSpinner newconsoleWidth;

    /**
     * Labels for height and width
     */

    private JLabel heightLabel;
    private JLabel widthLabel;

    /**
     * Models for spinners.
     */

    private SpinnerNumberModel heightModel;
    private SpinnerNumberModel widthModel;


    /**
   * Creates a new <code>BehaviourPrefsPanel</code>.
   */
  
  public BehaviourPrefsPanel(ConsoleManager consoleManager){
    this.consoleManager = consoleManager;
    
    if (consoleManager.getConn() instanceof GameListConnection){
      embeddedGameLists = new JRadioButton();
      externalGameLists = new JRadioButton();
      noGameLists = new JRadioButton();
      
      switch (consoleManager.getGameListsDisplayStyle()){
        case ConsoleManager.EMBEDDED_GAME_LISTS: embeddedGameLists.setSelected(true); break;
        case ConsoleManager.EXTERNAL_GAME_LISTS: externalGameLists.setSelected(true); break;
        case ConsoleManager.NO_GAME_LISTS: noGameLists.setSelected(true); break;
        default:
          throw new IllegalStateException();
      }
      
      ActionListener gameListsDisplayStyleListener = new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          fireStateChanged();
        }
      };
      embeddedGameLists.addActionListener(gameListsDisplayStyleListener);
      externalGameLists.addActionListener(gameListsDisplayStyleListener);
      noGameLists.addActionListener(gameListsDisplayStyleListener);
    }
    else{
      embeddedGameLists = null;
      externalGameLists = null;
      noGameLists = null;
    }
    
    copyOnSelect = new JCheckBox();
    copyOnSelect.setSelected(consoleManager.isCopyOnSelect());
    copyOnSelect.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        fireStateChanged();
      }
    });
       //int initialHeight = consoleManager.getNCInitHeight();
        //int initalWidth = consoleManager.getNCInitWidth();


        heightModel = new SpinnerNumberModel(consoleManager.getPrefs().getInt("newconsole-height"), 100, (Toolkit.getDefaultToolkit().getScreenSize().height - 100), 1);
        widthModel = new SpinnerNumberModel(consoleManager.getPrefs().getInt("newconsole-width"), 100, (Toolkit.getDefaultToolkit().getScreenSize().width - 100), 1);

        heightLabel = new JLabel("Height:");
        widthLabel = new JLabel("Width:");
        newconsoleHeight = new JSpinner(heightModel);
        newconsoleWidth = new JSpinner(widthModel);

         newconsoleHeight.addChangeListener(new ChangeListener(){

             public void stateChanged(ChangeEvent e) {
                 fireStateChanged();
             }
         });
        newconsoleWidth.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                fireStateChanged();
            }
         });

    
    createUI();
  }
  
  
  
  /**
   * Creates the UI of this panel.
   */
  
  private void createUI(){
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    if (embeddedGameLists != null){
      embeddedGameLists.setText("Embedded in the console");
      externalGameLists.setText("External to the console");
      noGameLists.setText("Not displayed specially");
      
      embeddedGameLists.setMnemonic('E');
      externalGameLists.setMnemonic('x');
      noGameLists.setMnemonic('N');
      
      embeddedGameLists.setToolTipText("Display game lists as a table inside the console");
      externalGameLists.setToolTipText("Display game lists as a table in a separate frame");
      noGameLists.setToolTipText("Do not display game lists as a table");
      
      ButtonGroup bg= new ButtonGroup();
      bg.add(embeddedGameLists);
      bg.add(externalGameLists);
      bg.add(noGameLists);
      
      JPanel panel = new PreferredSizedPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Game Lists Display"),
        BorderFactory.createEmptyBorder(0, 5, 5, 5)));
          
      embeddedGameLists.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      externalGameLists.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      noGameLists.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      
      panel.add(embeddedGameLists);
      panel.add(externalGameLists);
      panel.add(noGameLists);
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridheight = 2;

      c.gridx = 0;
        c.gridy = 0;
      add(panel, c);


    }

    copyOnSelect.setText("Copy on select");
    copyOnSelect.setMnemonic('C');
    copyOnSelect.setToolTipText("Copy text to the clipboard as it is being selected");
    
    JPanel panel = new PreferredSizedPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("Text Selection"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    copyOnSelect.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    panel.add(copyOnSelect);
      c.weightx = 0.5;
        c.weighty = 0.5;
      c.gridheight = 2;
      c.gridx = 0;
    c.gridy = 1;
      c.anchor = GridBagConstraints.NORTHWEST;
    add(panel, c);

      JPanel newconsoleHW = new PreferredSizedPanel();
      newconsoleHW.setLayout(new GridBagLayout());
    newconsoleHW.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("New console height and width"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
      GridBagConstraints c2 = new GridBagConstraints();
      c2.gridheight = 2;
      c2.gridwidth = 2;
      c2.weightx = 1;
      c2.weighty = 1;
      c2.gridx = 0;
      c2.gridy = 0;
      c2.anchor = GridBagConstraints.WEST;
      newconsoleHW.add(heightLabel,c2);

      c2.gridx = 0;
      c2.gridy = 2;
      c2.anchor = GridBagConstraints.WEST;
      newconsoleHW.add(widthLabel,c2);

      c2.gridx = 2;
      c2.gridy = 0;
      c2.anchor = GridBagConstraints.EAST;
      c2.fill = GridBagConstraints.HORIZONTAL;
      newconsoleHW.add(newconsoleHeight,c2);

      c2.gridx = 2;
      c2.gridy = 2;
      c2.anchor = GridBagConstraints.EAST;
      c2.fill = GridBagConstraints.HORIZONTAL;
      newconsoleHW.add(newconsoleWidth,c2);

      c.gridheight = 2;
      c.gridx = 0;
      c.gridy = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.WEST;
      add(newconsoleHW,c);

  }
  
  
  
  /**
   * Applies any changes made by the user.
   */

  public void applyChanges() throws BadChangesException{
    if (embeddedGameLists != null){
      if (embeddedGameLists.isSelected())
        consoleManager.setGameListsDisplayStyle(ConsoleManager.EMBEDDED_GAME_LISTS);
      else if (externalGameLists.isSelected())
        consoleManager.setGameListsDisplayStyle(ConsoleManager.EXTERNAL_GAME_LISTS);
      else
        consoleManager.setGameListsDisplayStyle(ConsoleManager.NO_GAME_LISTS);
    }
    
    consoleManager.setCopyOnSelect(copyOnSelect.isSelected());
      consoleManager.getPrefs().setInt("newconsole-height", getSpinnerValue(newconsoleHeight));
      consoleManager.getPrefs().setInt("newconsole-width", getSpinnerValue(newconsoleWidth));
  }

    private int getSpinnerValue(JSpinner newconsoleSpinner) {
           try {
       newconsoleSpinner.commitEdit();
   }
   catch (ParseException pe) {
       // Edited value is invalid, spinner.getValue() will return
       // the last valid value, you could revert the spinner to show that:
       JComponent editor = newconsoleSpinner.getEditor();
       if (editor instanceof JSpinner.DefaultEditor) {
           ((JSpinner.DefaultEditor)editor).getTextField().setValue(newconsoleSpinner.getValue());
       }
       // reset the value to some known value:
       //newconsoleSpinner.setValue(fallbackValue);
       // or treat the last valid value as the current, in which
       // case you don't need to do anything.
   }
        Integer integerValue = (Integer)newconsoleSpinner.getValue();
   return integerValue.intValue();
    }


}
