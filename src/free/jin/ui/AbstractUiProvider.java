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

import free.jin.*;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.util.AWTUtilities;
import free.util.Pair;
import free.util.RectDouble;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



/**
 * A skeleton implementation of the most likely way a UIProvider would work.
 */

public abstract class AbstractUiProvider implements UIProvider, SessionListener{
  
  
  
  /**
   * Maps Pair<Plugin, String> (the string being plugin container ids) to
   * the PluginContainers for that plugin and container id.
   */

  private final HashMap pluginContainers = new HashMap();
  
  
  
  /**
   * The list of <code>PluginContainersMenu<code>s we notify when a new plugin
   * ui container is created. This should be a proper listener list, but it
   * seems an overkill for just notifying <code>PluginContainersMenu</code>.
   * Promote to a proper listener list (with listener interface and event type)
   * if more code needs to be notified.   
   */
  
  private final ArrayList creationNotifiedMenus = new ArrayList();
  

  
  /**
   * Performs certain initializations. 
   */
  
  public void start(){
    Jin.getInstance().getConnManager().addSessionListener(this);
  }
  
  
  
  /**
   * SessionListener implementation. Restores various preferences.
   */
  
  public void sessionEstablished(SessionEvent evt){
    loadSelectedFrame(evt.getSession());
    
    Iterator pluginContainers = getExistingPluginUIContainers();
    while (pluginContainers.hasNext()){
      AbstractPluginUIContainer container = 
        (AbstractPluginUIContainer)pluginContainers.next();
      if (container.getMode() == HIDEABLE_CONTAINER_MODE)
        container.setVisible(container.getPlugin().getPrefs().getBool(container.getPrefsPrefix() + "visible", true));
    }
  }
  
  
  
  /**
   * SessionListener implementation. Stores various preferences.
   */
  
  public void sessionClosed(SessionEvent evt){
    saveSelectedFrame(evt.getSession());
    
    Iterator pluginContainers = getExistingPluginUIContainers();
    while (pluginContainers.hasNext()){
      AbstractPluginUIContainer container = 
        (AbstractPluginUIContainer)pluginContainers.next();
      if (container.getMode() == HIDEABLE_CONTAINER_MODE)
        container.getPlugin().getPrefs().setBool(container.getPrefsPrefix() + "visible", container.isVisible());
    }
    
    removePluginContainers();
  }
  
  
  
  /**
   * Sets the selected frame to the frame we remembered was selected last time.
   */

  private void loadSelectedFrame(Session session){
    User user = session.getUser();
    String pluginId = user.getPrefs().getString("selected.plugin", null);
    String containerId = user.getPrefs().getString("selected.container", null);

    Iterator e = pluginContainers.values().iterator();
    while (e.hasNext()){
      PluginUIContainer c = (PluginUIContainer)e.next();

      if (c.getPlugin().getId().equals(pluginId) && c.getId().equals(containerId)){
        if (c.isVisible())
          c.setActive(true);
        break;
      }
    }
  }



  /**
   * Sets a preference specifying which frame was selected.
   */

  private void saveSelectedFrame(Session session){
    User user = session.getUser();

    Iterator e = pluginContainers.values().iterator();
    while (e.hasNext()){
      PluginUIContainer c = (PluginUIContainer)e.next();
      if (c.isActive()){
        String id = c.getId();
        String pluginId = c.getPlugin().getId();

        if (id != null){
          user.getPrefs().setString("selected.plugin", pluginId);
          user.getPrefs().setString("selected.container", id);
        }
        
        break;
      }
    }
  }
  
  

  /**
   * Adds the specified plugin ui container to the table of 
   * <code>pluginContainers</code>. This method should be called by the
   * implementation's <code>createPluginUIContainer</code> method.
   */
  
  protected final void addPluginContainer(Plugin plugin, String id, AbstractPluginUIContainer container){
    if (id != null){
      Object key = new Pair(plugin, id);
      Object oldContainer = pluginContainers.put(key, container);
      
      if (oldContainer != null){
        pluginContainers.put(key, oldContainer);
        throw new IllegalArgumentException("Cannot allocate a container with the same id twice");
      }
    }
    
    for (int i = 0; i < creationNotifiedMenus.size(); i++){
      PluginContainersMenu menu = (PluginContainersMenu)creationNotifiedMenus.get(i);
      menu.pluginContainerAdded(container);
    }
  }
  
  /**
   * Removes the specified plugin ui container from the table of <code>pluginContainers</code>.
   * I think it will ease using multiple consoles. @author: whp 2006
   */
  
  public final void removePluginContainer(Plugin plugin, String id, AbstractPluginUIContainer container){
      if (id != null){
          Object key = new Pair(plugin, id);
          pluginContainers.remove(key);
      }
      
      for (int i = 0; i < creationNotifiedMenus.size(); i++){
          PluginContainersMenu menu = (PluginContainersMenu)creationNotifiedMenus.get(i);
          menu.pluginContainerRemoved(container);
      }
      
      
      
  }
  
  
  /**
   * Returns a list of existing plugin ui containers.
   */
  
  public Iterator getExistingPluginUIContainers(){
    return pluginContainers.values().iterator();
  }
  
  
  
  
  /**
   * Adds a <code>PluginContainersMenu</code> to be notified when a new plugin
   * ui container is created.
   */
  
  public void addPluginUIContainerCreationListener(PluginContainersMenu menu){
    creationNotifiedMenus.add(menu);
  }
  
  
  
  /**
   * Removes a <code>PluginContainersMenu</code> from being notified when a new
   * plugin ui container is created.
   */
  
  public void removePluginUIContainerCreationListener(PluginContainersMenu menu){
    creationNotifiedMenus.remove(menu);
  }
  
  

  /**
   * Disposes of all the plugin containers and removes them from the
   * <code>pluginContainers</code> HashMap.
   */
  
  protected void removePluginContainers(){
    Iterator e = pluginContainers.values().iterator();
    while (e.hasNext())
      ((PluginUIContainer)e.next()).dispose();

    pluginContainers.clear();
  }

  
  
  
  /**
   * Restores the geometry of the specified frame from the preferences.
   */
   
  protected void restoreFrameGeometry(Preferences prefs, JFrame frame, String prefNamePrefix, RectDouble defaultFrameBounds){
    Dimension screenSize = AWTUtilities.getUsableScreenBounds().getSize();
    Rectangle realDefaultBounds = 
        defaultFrameBounds.scale(screenSize.width, screenSize.height).toRect();
    
    // Restore bounds      
    Rectangle frameBounds = prefs.getRect(prefNamePrefix + "bounds", realDefaultBounds);
    frameBounds = frameBoundsOk(screenSize, frameBounds) ? frameBounds : realDefaultBounds;
    frame.setBounds(frameBounds);

    
    // Restore maximized state 
    boolean vertMaximized = prefs.getBool(prefNamePrefix + "maximized.vert", false);
    boolean horizMaximized = prefs.getBool(prefNamePrefix + "maximized.horiz", false);

    // Bugfix for Java bug 4464714 - setExtendedState only works once the
    // the window is realized.
    if (frame.getPeer() == null)
      frame.addNotify();
   
    int state = ((vertMaximized ? Frame.MAXIMIZED_VERT : 0) | (horizMaximized ? Frame.MAXIMIZED_HORIZ : 0));
    AWTUtilities.setExtendedFrameState(frame, state);
  }
  
  
  
  /**
   * Saves the geometry of the specified frame into the preferences
   * with preference names prefixed with the specified string.
   */

  protected void saveFrameGeometry(Preferences prefs, JFrame frame, String prefNamePrefix){
    
    // Save bounds on screen
    Point frameLocation = frame.isVisible() ? frame.getLocationOnScreen() : frame.getLocation();
    Dimension frameSize = frame.getSize();
    prefs.setRect(prefNamePrefix + "bounds", new Rectangle(frameLocation, frameSize));
    
    // Save maximized state
    int state = AWTUtilities.getExtendedFrameState(frame);
    prefs.setBool(prefNamePrefix + "maximized.vert", (state & Frame.MAXIMIZED_VERT) != 0);
    prefs.setBool(prefNamePrefix + "maximized.horiz", (state & Frame.MAXIMIZED_HORIZ) != 0);
  }
  
  

  /**
   * Returns whether the specified frame bounds are reasonably placed on a
   * screen of the specified dimensions. This is used to avoid situations where
   * a frame is displayed outside of the screen where the user can't change its
   * size and/or move it (can happen for example if the resolution is changed
   * between runs).
   */

  protected boolean frameBoundsOk(Dimension screenSize, Rectangle frameBounds){
    if (frameBounds.x + frameBounds.width < 50)
      return false;
    if (frameBounds.y < -10)
      return false;
    if (frameBounds.width < 30)
      return false;
    if (frameBounds.height < 40)
      return false;
    if (frameBounds.x > screenSize.width - 10)
      return false;
    if (frameBounds.y > screenSize.height - 20)
      return false;

    return true;
  }
  
  
  
}
