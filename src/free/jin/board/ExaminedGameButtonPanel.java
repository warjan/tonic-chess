/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.board;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;

import free.jin.Connection;
import free.jin.Game;
import free.jin.plugin.Plugin;
import free.workarounds.FixedJPanel;


/**
 * The panel which contains all the action buttons for an examined game of type
 * Game.MY_GAME.
 */

public class ExaminedGameButtonPanel extends FixedJPanel implements ActionListener{



  /**
   * The Plugin we're being used by.
   */

  protected final Plugin plugin;




  /**
   * The Game for which this ExaminedGameButtonPanel is used.
   */

  protected final Game game;



  /**
   * The button which makes the game jump to its beginning.
   */

  protected JButton startButton;
  



  /**
   * The button which makes the game go one ply backward.
   */

  protected JButton backwardButton;




  /**
   * The button which makes the game go one ply forward.
   */

  protected JButton forwardButton;




  /**
   * The button which makes the game jump to its end.
   */

  protected JButton endButton;


 /**
  * The button which commit the line.
  */
  
  protected JButton commitButton;
  
 /**
  * The button which revert the line.
  */

  protected JButton revertButton;
  
  /**
   * Creates a new ExaminedGameButtonPanel. It will be used by the given Plugin
   * for the given Game.
   */

  public ExaminedGameButtonPanel(Plugin plugin, Game game){
    this.plugin = plugin;
    this.game = game;

    init(plugin, game);
  }




  /**
   * Initializes this ExaminedGameButtonPanel. This method calls delegates to
   * {@link #createComponents(Plugin, Game)} and
   * {@link #addComponents(Plugin, Game)}
   */

  protected void init(Plugin plugin, Game game){

    createComponents(plugin, game);
    addComponents(plugin, game);
  }




  /**
   * Creates all the components of this ExaminedGameButtonPanel.
   */

  protected void createComponents(Plugin plugin, Game game){
    startButton = createStartGameButton(plugin, game);
    backwardButton = createBackwardButton(plugin, game);
    forwardButton = createForwardButton(plugin, game);
    endButton = createEndGameButton(plugin, game);
    commitButton = createCommitButton(plugin, game);
    revertButton = createRevertButton(plugin, game);
  }




  /**
   * Creates the button which makes the game go its start.
   */

  protected JButton createStartGameButton(Plugin plugin, Game game){
    JButton button = new JButton(new ImageIcon(getClass().getResource("images/start.gif")));
    button.setMargin(new Insets(5,5,5,5));
    button.setToolTipText("Go to the beginning of the game");
    button.addActionListener(this);
    button.registerKeyboardAction(this, KeyStroke.getKeyStroke("alt HOME"), WHEN_IN_FOCUSED_WINDOW);    
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }




  /**
   * Creates the button which makes the game go its end.
   */

  protected JButton createEndGameButton(Plugin plugin, Game game){
    JButton button = new JButton(new ImageIcon(getClass().getResource("images/end.gif")));
    button.setMargin(new Insets(5,5,5,5));
    button.setToolTipText("Go to the end of the game");
    button.addActionListener(this);
    button.registerKeyboardAction(this, KeyStroke.getKeyStroke("alt END"), WHEN_IN_FOCUSED_WINDOW);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }




  /**
   * Creates the button which makes the game go one ply backward.
   */

  protected JButton createBackwardButton(Plugin plugin, Game game){
    JButton button = new JButton(new ImageIcon(getClass().getResource("images/backward.gif")));
    button.setMargin(new Insets(5,5,5,5));
    button.setToolTipText("Go one ply backward");
    button.addActionListener(this);
    button.registerKeyboardAction(this, KeyStroke.getKeyStroke("alt LEFT"), WHEN_IN_FOCUSED_WINDOW);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }



  /**
   * Creates the button which makes the game go one ply forward.
   */

  protected JButton createForwardButton(Plugin plugin, Game game){
    JButton button = new JButton(new ImageIcon(getClass().getResource("images/forward.gif")));
    button.setMargin(new Insets(5,5,5,5));
    button.setToolTipText("Go one ply forward");
    button.addActionListener(this);
    button.registerKeyboardAction(this, KeyStroke.getKeyStroke("alt RIGHT"), WHEN_IN_FOCUSED_WINDOW);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }
  
  /**
   * Creates the button which commits the line.
   */
  
  protected JButton createCommitButton(Plugin plugin, Game game){
      Connection conn = plugin.getConn();
      JButton button = new JButton(new ImageIcon(getClass().getResource("images/commit.gif")));
      button.setMargin(new Insets(5, 5, 5, 5));
      button.setToolTipText("Commit the line.");
      button.addActionListener(this);
      button.setDefaultCapable(false);
      button.setRequestFocusEnabled(false);
      
      if (conn.getConnectionName().equals("FreechessConnection") ){
          button.setEnabled(true);
      }
      
      else {
           button.setText("commit's supported on fics");
           button.setEnabled(false);
      }
      
      return button;
  }
  
  protected JButton createRevertButton(Plugin plugin, Game game){

      JButton button = new JButton(new ImageIcon(getClass().getResource("images/revert.gif")));
      button.setMargin(new Insets(5, 5, 5, 5));
      button.setToolTipText("Revert to the main line");
      button.addActionListener(this);
      button.setDefaultCapable(false);
      button.setRequestFocusEnabled(false);
      
    
      
      return button;
  }




  /**
   * Adds all the components to this ExaminedGameButtonPanel.
   */

  protected void addComponents(Plugin plugin, Game game){
    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    
    add(revertButton);
    add(startButton);
    add(backwardButton);
    add(commitButton);
    add(forwardButton);
    add(endButton);
   
  }




  /**
   * ActionListener implementation. Executes the appropriate command depending
   * on the button that was pressed.
   */

  public void actionPerformed(ActionEvent evt){
    Object source = evt.getSource();
    Connection conn = plugin.getConn();

    if (source==startButton){
      conn.goToBeginning(game);
    }
    else if (source==endButton){
      conn.goToEnd(game);
    }
    else if (source==forwardButton){
      conn.goForward(game, 1);
    }
    else if (source==backwardButton){
      conn.goBackward(game, 1);
    }
    else if (source==commitButton){
      conn.sendCommand("commit");
    }
    else if (source==revertButton){
      conn.sendCommand("revert");
    }
  }



  /**
   * Overrides getMaximumSize() to return the value of getPreferredSize().
   */

  public Dimension getMaximumSize(){
    return getPreferredSize();
  }


}
