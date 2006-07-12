/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import free.jin.Jin;
import free.util.IOUtilities;


/**
 * A panel displaying information about Jin. Normally displayed in the
 * "Help->About..." dialog.
 */

public class AboutPanel extends DialogPanel{



  /**
   * Creates a new <code>AboutPanel</code>.
   */

  public AboutPanel(){
    createUI();
  }



  /**
   * Returns the title of this <code>DialogPanel</code>.
   */

  protected String getTitle(){
    return "About Tonic";
  }



  /**
   * Displays this panel.
   */

  public void display(){
    super.askResult();
  }



  /**
   * Creates the user interface.
   */

  private void createUI(){
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    
    Icon jinIcon = new ImageIcon(Jin.class.getResource("resources/logo.png"));
    JLabel jinLabel = new JLabel(Jin.getInstance().getAppName() + " " 
      + Jin.getInstance().getAppVersion(), jinIcon, JLabel.LEFT);
    jinLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));

    
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(0,0,5,0);
    add(jinLabel, c);


    String copyright; 
    try{
      copyright = IOUtilities.loadText(Jin.class.getResource("legal/copyright.txt"));
    } catch (java.io.IOException e){
        add(new JLabel("Unable to load copyright file"));
        return;
      }
    StringTokenizer copyrightLines = new StringTokenizer(copyright, "\r\n");

    Font font = new Font("SansSerif", Font.PLAIN, 12);
    JTextArea textArea = new JTextArea();
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    //textArea.setBackground(System.getProperty("button.background", def))
    

    while (copyrightLines.hasMoreTokens()){
      String line = copyrightLines.nextToken();
      textArea.append(line + "\n");
      
      
    }
    textArea.setFont(font);
    c.gridx = 0;
    c.gridy = 1;
    
    add(textArea, c);
    



    JButton closeButton = new JButton("OK");
    closeButton.addActionListener(new ClosingListener(null));
    c.gridx = 0;
    c.gridy = 2;
    c.anchor = GridBagConstraints.SOUTHEAST;
    c.insets = new Insets(5, 0, 5, 0);
    add(closeButton, c);

    setDefaultButton(closeButton);
  }



}
