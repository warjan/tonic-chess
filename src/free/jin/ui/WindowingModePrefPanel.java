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

package free.jin.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import free.jin.BadChangesException;
import free.jin.Jin;


/**
 * A prefs panel for selecting the current ui mode.
 */

public class WindowingModePrefPanel extends PreferencesPanel{
    
    /**
     * Group of radio buttons.
     */
    
    private ButtonGroup bg;
  
  /**
   * The MDI mode radio button.
   */
  
  private final JRadioButton mdiMode;
  
  
  
  /**
   * The SDI mode radio button.
   */
  
  private final JRadioButton sdiMode;
  
  /**
   * The DDI mode radio button. For now with flexdock as a backend.
   */
  
  private final JRadioButton ddiModeFDock;
  
  
  
  /**
   * Creates a new <code>WindowingModePrefPanel</code>.
   */
  
  public WindowingModePrefPanel(){
    String pref = Jin.getInstance().getPrefs().getString("uiProvider.classname");
    mdiMode = new JRadioButton("Multiple Document Interface", MdiUiProvider.class.getName().equals(pref));
    sdiMode = new JRadioButton("Single Document Interface", SdiUiProvider.class.getName().equals(pref));
    ddiModeFDock = new JRadioButton("Dockable Document Interface - flexdock backend", DdiUiProvider.class.getName().equals(pref));
    
    bg = new ButtonGroup();
    bg.add(mdiMode);
    bg.add(sdiMode);
    bg.add(ddiModeFDock);
    
    ActionListener changeListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        fireStateChanged();
      }
    };
    
    sdiMode.addActionListener(changeListener);
    mdiMode.addActionListener(changeListener);
    ddiModeFDock.addActionListener(changeListener);
    
    createUi();
  }
  
  
  
  /**
   * Creates the UI of this panel.
   */
  
  private void createUi(){
    mdiMode.setMnemonic('M');
    sdiMode.setMnemonic('S');
    ddiModeFDock.setMnemonic('F');
    
    mdiMode.setToolTipText("Jin windows are placed inside one outer window");
    sdiMode.setToolTipText("Jin windows are separate, OS native, windows");
    ddiModeFDock.setToolTipText("Tonic windows are placed with flexdock docking manager");
    
    Container modesPanel = Box.createVerticalBox();
    modesPanel.add(mdiMode);
    modesPanel.add(sdiMode);
    modesPanel.add(ddiModeFDock);
    
    Container restartPane = new JPanel(new GridLayout(2, 1, 5, 5));
    restartPane.add(new JLabel("You must restart Jin to apply"));
    restartPane.add(new JLabel("a different windowing mode"));

    setLayout(new BorderLayout(10, 10));
    add(modesPanel, BorderLayout.CENTER);
    add(restartPane, BorderLayout.SOUTH);
  }



  public void applyChanges() throws BadChangesException{
      /*String uiProviderName;
      Enumeration ebg = bg.getElements();
      while (ebg.hasMoreElements()){
          if(((JRadioButton)ebg.nextElement()).isSelected().e){
              
          }
      }*/
    //Jin.getInstance().getPrefs().setString("uiProvider.classname", 
    //    mdiMode.isSelected() ? MdiUiProvider.class.getName() : SdiUiProvider.class.getName());
    
    Jin.getInstance().getPrefs().setString("uiProvider.classname", mdiMode.isSelected() ? MdiUiProvider.class.getName() : 
        (sdiMode.isSelected() ? SdiUiProvider.class.getName() : DdiUiProvider.class.getName()));
  }
  
  

}
