/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import free.jin.*;

import java.util.ResourceBundle;


/**
 * The base class for jin actions. Jin actions are miniature plugins, which
 * encapsulate a single action. An action can be created in two ways - either
 * it is created by the Jin framework as a standalone action or it can be
 * created and exported by a plugin. Subclasses which implement standalone
 * actions must have a no-arg constructor. For convenience,
 * <code>JinAction</code> implements <code>ActionListener</code> which simply
 * runs the action.
 */
 
public abstract class JinAction implements ActionListener{
  
  
  
  /**
   * The action's context.
   */
   
  private ActionContext context;
  
  
  
  /**
   * The <code>Preferences</code> object this action uses to store its
   * preferences.
   */
  
  private Preferences prefs;


//TODO make use of id and name to make it i18n-friendly
//half way there now I need to find a way to let the properties files go with
//seperate actions





  /**
   * Sets the action's context. Returns whether the context is supported.
   */
   
  public boolean setContext(ActionContext context){
    if (this.context != null)
      throw new IllegalStateException("ActionContext already set");
    
    this.context = context;
    
    return isContextSupported(context);
  }
  
  
  
  /**
   * Returns whether the specified context is supported by this action. The
   * default implementation always returns <code>true</code>. 
   */
   
  protected boolean isContextSupported(ActionContext context){
    return true;
  }
  
  
  
  /**
   * Returns the connection to the server.
   */
   
  public Connection getConn(){
    return context.getConnection();
  }
  
  
  
  /**
   * Returns the user for the session.
   */
  
  public User getUser(){
    return context.getUser();
  }
  
  
  
  /**
   * Returns the server for the session.
   */
   
  public Server getServer(){
    return getUser().getServer();
  }
  
  
  /**
   * Returns the action's preferences.
   */
   
  public Preferences getPrefs(){
    if (prefs == null){
      Preferences actionPrefs = context.getPreferences();
      Preferences userPrefs = getUser().getPrefs();
      prefs = Preferences.createBackedUp(Preferences.createWrapped(userPrefs, "action." + getId() + "."), actionPrefs);
    }

    return prefs;
  }
  
  
  
  
  /**
   * <code>ActionListener</code> implementation. Simply forwards the call to the
   * <code>go</code> method.
   */
   
  public void actionPerformed(ActionEvent evt){
    go();
  }
  
  
  
  /**
   * Invokes the action.
   */
  
  public abstract void go();
  
  
  
  /**
   * Returns the action's name. This may be displayed to the user.
   */
   
   public abstract String getName();
  
  
  
  /**
   * Returns the action's id. This will not be displayed to the user and must
   * be unique between all actions.
   */
   
  public abstract String getId();

    /**
     * Method that helps translation effort.
     * @return a path to ResourceBundle holding translation to action calling this method
     */

  public ResourceBundle getBundlePath(JinAction action){
        
      ResourceBundle bundle = ResourceBundle.getBundle("free.jin.action." + action.getId() + '.' + action.getId());
      return bundle;
  }

  //public ResourceBundle getActionTranslation(){
  //     ResourceBundle actionTranslation = Jin.getTranslations();

   //   return actionTranslation;
  //}
  
  
   
}

