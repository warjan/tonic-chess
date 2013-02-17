/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * A component which allows the user to select a color.
 */

public class ColorChooser extends JComponent implements ActionListener{



  /**
   * The button we're using.
   */

  private final JButton button;
  
  
  
  /**
   * The label we're using.
   */
   
  private final JLabel label;



  /**
   * The icon's size.
   */

  private static final Dimension ICON_SIZE = new Dimension(30, 20);



  /**
   * The current color.
   */

  private Color color;



  /**
   * The sole ChangeEvent we need.
   */

  private final ChangeEvent changeEvent = new ChangeEvent(this);



  /**
   * Creates a new <code>ColorChooser</code> with no text and the given initial
   * color.
   */

  public ColorChooser(Color initialColor){
    this(null, initialColor);
  }



  /**
   * Creates a new <code>ColorChooser</code> with the given text and initial
   * color.
   */

  public ColorChooser(String text, Color initialColor){
    button = new JButton(new SolidColorRectangleIcon(ICON_SIZE, initialColor));
    label = new JLabel(text);

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    if (text != null){
      add(label);
      add(Box.createHorizontalStrut(4));
      add(Box.createHorizontalGlue());
    }
    add(button);

    label.setLabelFor(button);
    button.setDefaultCapable(false);

    color = initialColor;

    button.addActionListener(this);
  }



  /**
   * Sets the mnemonic.
   */

  public void setMnemonic(char mnemonic){
    label.setDisplayedMnemonic(mnemonic);
  } 



  /**
   * Sets the mnemonic.
   */

  public void setMnemonic(int mnemonic){
    label.setDisplayedMnemonic(mnemonic);
  }
  
  
  
  /**
   * Sets the enabled state of this color chooser.
   */
   
  public void setEnabled(boolean enabled){
    label.setEnabled(enabled);
    button.setEnabled(enabled);
    
    super.setEnabled(enabled);
  }
   



  /**
   * Adds a ChangeListener to the list of listeners receiving notifications when
   * one of the text properties changes.
   */

  public void addChangeListener(ChangeListener listener){
    listenerList.add(ChangeListener.class, listener);
  }



  /**
   * Removes the given Changelistener from the list of listeners receiving
   * notifications when one of the text properties changes.
   */

  public void removeChangeListener(ChangeListener listener){
    listenerList.remove(ChangeListener.class, listener);
  }



  /**
   * Fires a ChangeEvent to all interested listeners.
   */

  protected void fireStateChanged(){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ChangeListener.class){
        ChangeListener listener = (ChangeListener)listeners[i+1];
        listener.stateChanged(changeEvent);
      }
    }
  }
  
  
  
  /**
   * Returns the currently selected color.
   */

  public Color getColor(){
    return color;
  }



  /**
   * Sets the current color.
   */

  public void setColor(Color color){
    this.color = color;
    button.setIcon(new SolidColorRectangleIcon(ICON_SIZE, color));
    fireStateChanged();
  }




  /**
   * Shows the color chooser.
   */

  public void actionPerformed(ActionEvent evt){
    Color newColor = JColorChooser.showDialog(SwingUtilities.windowForComponent(this),
      "Choose a color", color);
    if (newColor != null)
      setColor(newColor);
  }
  

  
}
