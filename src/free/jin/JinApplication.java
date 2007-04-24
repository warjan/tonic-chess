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

package free.jin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.*;

import javax.swing.JOptionPane;

import free.jin.action.ActionInfo;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginInfo;
import free.util.ChildClassLoader;
import free.util.DelegatingClassLoader;
import free.util.ExtensionFilenameFilter;
import free.util.IOUtilities;
import free.util.MemoryFile;
import free.util.MultiOutputStream;
import free.util.PlatformUtils;
import free.util.Utilities;
import free.util.zip.ZipClassLoader;
import free.util.zip.ZipURLStreamHandler;



/**
 * A <code>JinContext</code> implementation for running Jin as a standalone
 * application.
 */
 
public class JinApplication implements JinContext{
  
  

  /**
   * Set a URLStreamHandlerFactory which knows about our "zip" protocol.
   */

  static{
    URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory(){
      public URLStreamHandler createURLStreamHandler(final String protocol){
        if (protocol.equals("zip"))
          return new ZipURLStreamHandler();
        return null;
      }
    });
  }



  /**
   * The current directory. This should be the Jin directory.
   */

  private static final File JIN_DIR = new File(System.getProperty("user.dir"));
  
  
  
  /**
   * Are we loading plugins, actions etc. dynamically with special classloaders?
   */
  
  private static final boolean dynamicLoad = !"true".equals(System.getProperty("jin.noDynamicLoad"));


  
  /**
   * The commandline parameters.
   */
   
  private final Properties params;
  
  
  
  /**
   * The main directory where we load/save preferences from/to.
   */
   
  private final File prefsDir;
  
  
  
  /**
   * Application-wide user preferences.
   */
   
  private final Preferences userPrefs;
  
  
  
  /**
   * Our class loader. Initially it has no delegates - they are added as the
   * various jars for plugins, server definitions etc. are loaded. The structure
   * of the classloader is:
   * <pre>
   *  _______________ _______________ _______________
   *  |             | |             | |             |
   *  | plugin1.jar | | plugin2.jar | | action1.jar |   
   *  |_____________| |_____________| |_____________|   
   *         |               |               |          
   *         |               V               |          
   *         |        _______________        |          
   *         |------> |             | <------|          
   *                  |    main     |                  
   *         ######## |_____________| ########                                 
   *         #               |               #
   *         V               |               V          ______________________ 
   *  _______________        |        _______________   |                    | 
   *  |             |        |        |             |   | Legend:            | 
   *  | server1.jar |        |        | server2.jar |   |                    | 
   *  |_____________|        |        |_____________|   |   ----> : parent   | 
   *         |               |               |          |   ####> : delegate | 
   *         |               V               |          |____________________| 
   *         |        _______________        |
   *         |------> |             | <------|
   *                  |    libs     |
   *         |------> |_____________| <------| 
   *         |          #   #  ^  #          | 
   *   ______|_____     #   #  |  #     _____|______
   *   |          | <####   #  |  ####> |          |
   *   | lib1.jar |         V  |        | lib3.jar |
   *   |__________|     _______|____    |__________|
   *                    |          |
   *                    | lib2.jar |
   *                    |__________|
   *                    
   * </pre>
   *
   * This structure allows:
   * <ul>
   *   <li> A plugin to access all the servers and libs but not other plugins.
   *   <li> A server to access all the libs but not other servers or plugins.
   *   <li> A lib to access all the other libs but not servers or plugins.
   * </ul>
   *
   * This variable is a reference to the top delegating classloader.
   * Note: this is <code>null</code> when in jin.noDynamicLoad mode.
   */

  private final DelegatingClassLoader mainLoader;



  /**
   * The libraries' classloader. See the documentation of the
   * <code>mainClassLoader</code> instance variable for more details.
   * Note: this is <code>null</code> when in jin.noDynamicLoad mode.
   */

  private final ChildClassLoader libsLoader;
  
  
  
  /**
   * A list of <code>Server</code> objects representing the supported servers.
   */

  private final Server [] servers;
  
  
  
  /**
   * The list of known server accounts.
   */
   
  private User [] users;
  
  
  
  /**
   * The list of known server accounts at the moment Jin is started. 
   */
   
  private final User [] originalUsers;
  
   
  
  /**
   * Maps <code>Server</code> objects to arrays of <code>ActionInfo</code>
   * instances describing standalone actions for that server.
   */
   
  private final HashMap serversToActions;


  
  /**
   * Maps <code>Server</code> objects to arrays of <code>PluginInfo</code>
   * instances describing plugins for that server.
   */

  private final HashMap serversToPlugins;

    /**
     * Creates a new <code>JinApplication</code> with the specified commandline
     * parameters and preferences directory.
     */

    private JinApplication(final Properties params, final File prefsDir) throws IOException,
        ClassNotFoundException, InstantiationException, IllegalAccessException{

      this.params = params;
      this.prefsDir = prefsDir;

      // Load user preferences
      userPrefs = loadUserPrefs();


      if (dynamicLoad){
        // Create our own special classloader. The delegate classloaders will be
        // added as the various jars (for plugins, server definitions) are loaded.
        libsLoader = createLibsClassLoader();
        mainLoader = new DelegatingClassLoader(libsLoader);
      }
      else{
        libsLoader = null;
        mainLoader = null;
      }

      // Load servers
      servers = loadServers();

      // Load known users
      originalUsers = users = loadUsers();

      // Load actions
      serversToActions = loadActions();

      // Load plugins
      serversToPlugins = loadPlugins();
    }
  
  
  
  /**
   * Returns value of the specified commandline parameter passed to Jin. 
   */
   
  public String getParameter(final String paramName){
    return params.getProperty(paramName);
  }
  
  
  
  /**
   * Loads the application-wide user preferences.
   */

  private Preferences loadUserPrefs() throws IOException{
    final File prefsFile = new File(prefsDir, "user.prefs");
    if (!prefsFile.exists())
      return Preferences.createNew();

    return Preferences.load(prefsFile);
  }
  
  
  
  /**
   * Returns the application-wide user preferences.
   */

  public final Preferences getPrefs(){
    return userPrefs;
  }
  
  
  
  /**
   * Stores the application-wide user preferences.
   */

  private void storeUserPrefs(){
    final File userPrefsFile = new File(prefsDir, "user.prefs");
    
    try{
      userPrefs.save(userPrefsFile);
    } catch (SecurityException e){
        showErrorMessage("Saving Preferences Error",
          "The security manager doesn't allow writing:\n" + userPrefsFile);
      }
      catch (IOException e){
        showErrorMessage("Saving Preferences Error",
          "Unable to save preferences into:\n" + userPrefsFile + "\n" +
          "Perhaps you don't have permissions to write it?");
      }
  }



  
  /**
   * If the specified file does not exist or does not denote a directory, throws
   * an appropriate <code>FileNotFoundException</code>. Otherwise simply
   * returns. Helper method for various methods.
   */

  private static void checkDirectoryExists( final File file) throws FileNotFoundException{
    if (!(file.exists() && file.isDirectory()))
      throw new FileNotFoundException("Can't find directory:\n" + file + "\n" +
        "The most likely reason is that you are not running Tonic from its directory.");
  }
  


  /**
   * Creates the classloader that will load the various libraries required by
   * the plugins and server support.
   */

  private DelegatingClassLoader createLibsClassLoader() throws IOException{
    final DelegatingClassLoader libsLoader = new DelegatingClassLoader();
    final File libDir = new File(JIN_DIR, "libs");
    checkDirectoryExists(libDir);
    
    addDelegateZipClassLoaders(libsLoader, libDir);
    
    final String [] files = libDir.list();
    for (int i = 0; i < files.length; i++){
      final File file = new File(libDir, files[i]);
      if (file.isDirectory())
        addDelegateZipClassLoaders(libsLoader, file);
    }

    return libsLoader;
  }
  


  /**
   * Adds delegate zip classloaders for all jar files inside the specified
   * directory. Helper method for <code>createLibsClassLoader</code>.          
   */
   
  private static void addDelegateZipClassLoaders(final DelegatingClassLoader loader, final File dir) throws IOException{
    final String [] jars = dir.list(new ExtensionFilenameFilter(".jar"));
    for (int i = 0; i < jars.length; i++){
      final File jar = new File(dir, jars[i]);
      loader.addDelegate(new ZipClassLoader(jar, loader));
    }
  }

  
  
  /**
   * Loads the <code>Server</code> objects that implement support for the
   * various servers.
   */

  private Server [] loadServers() throws IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException{

    File serversDir = new File(JIN_DIR, "servers");
    checkDirectoryExists(serversDir);

    String [] jars = serversDir.list(new ExtensionFilenameFilter(".jar"));
    if (jars.length == 0)
      throw new IllegalStateException("No server specifications found in:\n" + serversDir);

    Server [] servers = new Server[jars.length];
    for (int i = 0; i < jars.length; i++){
      servers[i] = loadServer(new File(serversDir, jars[i]));
    }
    
    return servers;
  }



  /**
   * Loads a <code>Server</code> from the specified jar. Helper method for
   * <code>loadServers</code>.
   */

  private Server loadServer( final File jar) throws IOException,
      ClassNotFoundException, InstantiationException, IllegalAccessException{

    if (!jar.isFile())
      throw new FileNotFoundException(jar + " does not exist or is a directory");

    ChildClassLoader loader = new ZipClassLoader(jar, libsLoader);

    InputStream serverDefIn = loader.getResourceAsStream("definition");
    if (serverDefIn == null)
      throw new FileNotFoundException("Unable to find server definition file in " + jar);
    Properties serverDef = IOUtilities.loadProperties(serverDefIn);

    String classname = serverDef.getProperty("classname");
    if (classname == null)
      throw new IOException("Server definition file in " + jar + " does not contain a classname property");

    Class serverClass;
    if (dynamicLoad){
      serverClass = loader.loadClass(classname);
      mainLoader.addDelegate(loader);
    }
    else
      serverClass = Class.forName(classname);
    
    Server server = (Server)serverClass.newInstance();
    
    File guestDir = new File(new File(new File(prefsDir, "accounts"), server.getId()),
      server.getUsernamePolicy().getGuestUsername());
    server.setGuestUser(loadUser(guestDir, server));
    return server;
  }
  
  
  
  /**
   * Returns the list of supported servers.
   */

  public final Server [] getServers(){
    return servers;
  }


  
  
  
  /**
   * Loads the known user accounts.
   */

  private User [] loadUsers() throws IOException{
    File usersDir = new File(prefsDir, "accounts");
    
    ArrayList usersVector = new ArrayList();
    
    if (usersDir.exists()){
      for (int i = 0; i < servers.length; i++){
        Server server = servers[i];
        File serverSpecificUserDir = new File(usersDir, server.getId());
        if (!serverSpecificUserDir.exists())
          continue;
  
        UsernamePolicy policy = server.getUsernamePolicy();
        String [] userDirs = serverSpecificUserDir.list();
        for (int j = 0; j < userDirs.length; j++){
          File userDir = new File(serverSpecificUserDir, userDirs[j]);
          User user = loadUser(userDir, null);
  
          // Skip if the user wasn't loaded or is a guest
          if ((user != null) && !policy.isSame(policy.getGuestUsername(), user.getUsername()))
            usersVector.add(user);
        }
      }
    }
    
    User [] usersArr =  new User[usersVector.size()];
      usersArr = (User[]) usersVector.toArray(usersArr);
    //usersVector.copyInto(usersArr);

    return usersArr;
  }



  /**
   * Loads a User object from the specified directory. Returns <code>null</code>
   * if any required information about the user is missing. The specified server
   * argument indicates the server for the loaded user - it may be null, in
   * which case the server is determined by the serverId property of the user.
   * Helper method for <code>loadUsers</code> and <code>loadServer</code>.
   */

  private User loadUser(final File dir, Server server) throws IOException{
    if (!dir.isDirectory())
      return null;

    File propsFile = new File(dir, "properties");
    File prefsFile = new File(dir, "preferences");
    File filesFile = new File(dir, "files");

    if (!(propsFile.isFile() && prefsFile.isFile()))
      return null;

    Properties props = IOUtilities.loadProperties(propsFile);
    Preferences prefs = Preferences.load(prefsFile);
    HashMap files = loadUserFiles(filesFile);

    // We don't use the directories' names for server id and username because
    // we don't know whether the filesystem allows filenames to be what servers
    // allow them to be.
    String serverId = props.getProperty("serverId");
    String username = props.getProperty("username");

    if ((serverId == null) || (username == null))
      return null;

    if (server == null)
      server = getServerById(serverId);

    return new User(server, username, prefs, files);
  }



  /**
   * Loads the user (memory) files from the specified file. Returns a hashtable
   * mapping filenames to <code>MemoryFile</code> objects. Helper method for
   * <code>loadUser</code>.
   */

  private HashMap loadUserFiles( final File file) throws IOException{
    HashMap files = new HashMap();
    if (!file.isFile())
      return files;

    DataInputStream in =
      new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    int filesCount = in.readInt();
    for (int i = 0; i < filesCount; i++){
      String filename = in.readUTF();
      int length = in.readInt();
      byte [] data = IOUtilities.read(in, length);

      files.put(filename, new MemoryFile(data));
    }
    
    in.close();

    return files;
  }
  
  
  
  /**
   * Returns the list of known users.
   */
   
  public final User [] getUsers(){
    return users;
  }
  
  
  
  /**
   * Sets the list of known users so that we can properly store them when
   * shutting down.
   */
   
  public void setUsers( final User [] users){
    this.users = users; 
  }
  
  
  
  /**
   * Stores all the known users. 
   */
   
  private void storeUsers(){
    
    for (int i = 0; i < users.length; i++){
      User user = users[i];
      
      // Modified existing or new user
      if (user.isDirty() || (Utilities.indexOf(originalUsers, user) == -1))  
        storeUser(user);
    }
    
    
    // Save guest users
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];
      User guest = server.getGuest();
      
      if (guest.isDirty())
        storeUser(guest);
    }
    
    
    // We also need to delete the directories of any removed users, once
    // there's UI that allows the user to actually do this.
    
  }
  
  
  
  /**
   * Stores the specified user. Returns whether successful.
   * Helper method for <code>storeUsers</code>.
   */

  private boolean storeUser(final User user){
    // We don't need a special case for guest users because they are stored
    // in the same manner as everyone else.

    String username = user.getUsername();

    File userDir = usersDir(user);

    try{    
      if (!(userDir.isDirectory() || userDir.mkdirs())){
        showErrorMessage("Saving Account Error", "Unable to create directory:\n" + userDir);
        return false;
      }
    } catch (SecurityException e){
        showErrorMessage("Saving Account Error",
          "Security manager doesn't allow creating directory:\n" + userDir);
        return false;
      }

    Properties props = new Properties();
    props.put("serverId", user.getServer().getId());
    props.put("username", username);

    File propsFile = new File(userDir, "properties");
    try{
      OutputStream out = new FileOutputStream(propsFile);
      props.save(out, "");
      out.close();
    } catch (IOException e){
        showErrorMessage("Saving Account Error",
          "Error writing user properties to file :\n" + propsFile);
        return false;
      }
      catch (SecurityException e){
        showErrorMessage("Saving Account Error",
          "Security manager doesn't allow writing to file :\n" + propsFile);
        return false;
      }

    File prefsFile = new File(userDir, "preferences");
    try{
      OutputStream out = new FileOutputStream(prefsFile);
      user.getPrefs().save(out);
      out.close();
    } catch (IOException e){
        showErrorMessage("Saving Account Error",
            "Error writing user preferences to file :\n" + prefsFile);
        return false;
      }
      catch (SecurityException e){
        showErrorMessage("Saving Account Error",
            "Security manager doesn't allow writing to file :\n" + prefsFile);
        return false;
      }

    File filesFile = new File(userDir, "files"); 
    try{
      storeUserFiles(user.getFilesMap(), filesFile);
    } catch (IOException e){
        showErrorMessage("Saving Account Error",
            "Error writing user files to file :\n" + filesFile);
        return false;
      }
      catch (SecurityException e){
        showErrorMessage("Saving Account Error",
            "Security manager doesn't allow writing to file :\n" + filesFile);
        return false;
      }

    return true;
  }
  
  
  
  /**
   * Stores the specified map of filenames to <code>MemoryFile</code> objects
   * into the specified file. Helper method for <code>storeUser</code>.
   */

  private void storeUserFiles(final HashMap files, final File filesFile) throws IOException{
    DataOutputStream out = 
      new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filesFile)));
    out.writeInt(files.size());
    Iterator filenames = files.keySet().iterator();
    while (filenames.hasNext()){
      String filename = (String)filenames.next();
      MemoryFile memFile = (MemoryFile)files.get(filename);
      out.writeUTF(filename);
      synchronized(memFile){
        out.writeInt(memFile.getSize());
        memFile.writeTo(out);
      }
    }
    out.close();
  }
  
  
  
  
  /**
   * Loads the actions for all servers. Returns a hashtable mapping
   * <code>Server</code> objects to arrays of <code>ActionInfo</code> objects
   * describing the standalone actions for that server.
   */
   
  private HashMap loadActions() throws IOException, ClassNotFoundException{
    HashMap actions = new HashMap();
    for (int i = 0; i < servers.length; i++){
      actions.put(servers[i], new Vector());
    }

    // actions that are shared between all users - usually the ones that come with Jin
    loadActions(actions, new File(JIN_DIR, "actions")); 

    // user specific actions, from his own preferences directory
    loadActions(actions, new File(prefsDir, "actions"));


    // Convert the Server->Vector map to Server->ActionInfo[] map
    HashMap result = new HashMap();
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];
      ArrayList actionsVector = (ArrayList)actions.get(server);
      ActionInfo [] actionsArray = new ActionInfo[actionsVector.size()];
      actionsVector.toArray(actionsArray);

      result.put(server, actionsArray);
    }

    return result;
  }
  
  
  
  /**
   * Loads actions from the specified directory into the specified hashtable.
   * Helper method for <code>loadActions()</code>.
   */
   
  private void loadActions(final HashMap actions, final File dir) throws IOException, ClassNotFoundException{
    if (!dir.isDirectory())
      return;

    String [] jars;
    FilenameFilter jarsFilter = new ExtensionFilenameFilter(".jar");

    // Load actions that are for all servers
    jars = dir.list(jarsFilter);
    for (int i = 0; i < jars.length; i++){
      ActionInfo actionInfo = loadActionInfo(new File(dir, jars[i]));
      if (actionInfo == null){
        continue;
      }

      for (int j = 0; j < servers.length; j++)
        ((ArrayList)actions.get(servers[j])).add(actionInfo);
    }


    // Load server specific actions
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];

      File serverSpecificDir = new File(dir, server.getId());
      if (!serverSpecificDir.isDirectory())
        continue;

      jars = serverSpecificDir.list(jarsFilter);
      for (int j = 0; j < jars.length; j++){
        ActionInfo actionInfo = loadActionInfo(new File(serverSpecificDir, jars[j]));
        if (actionInfo == null)
          continue;

        ((ArrayList)actions.get(server)).add(actionInfo);
      }
    }
  }
  
  
  
  /**
   * Loads a single action description from the specified jar. Returns 
   * <code>null</code> if unable to load the action. Helper method for
   * <code>loadActions</code>.
   */

  private ActionInfo loadActionInfo(final File jar) throws IOException, ClassNotFoundException{
    if (!jar.isFile())
      return null;
    
    ChildClassLoader loader = new ZipClassLoader(jar, mainLoader);

    InputStream actionDefIn = loader.getResourceAsStream("definition");
    if (actionDefIn == null){
      System.err.println(jar + " does not contain an action definition file");
      return null;
    }
    Properties actionDef = IOUtilities.loadProperties(actionDefIn);

    String classname = actionDef.getProperty("classname");
    if (classname == null){
      System.out.println("The action definition file in " + jar + " does not contain a classname property");
      return null;
    }
    
    Class actionClass = dynamicLoad ? loader.loadClass(classname) : Class.forName(classname);
    
    InputStream actionPrefsIn = actionClass.getResourceAsStream("preferences");
    Preferences actionPrefs = (actionPrefsIn == null ? Preferences.createNew() : Preferences.load(actionPrefsIn));

    if (actionPrefsIn != null)
      actionPrefsIn.close();

    return new ActionInfo(actionClass, actionPrefs);
  }
  
  
  
  /**
   * Returns the list of actions for the specified server.
   */
   
  public ActionInfo [] getActions(final Server server){
    return (ActionInfo [])serversToActions.get(server);
  }
  



  /**
   * Loads the plugin classes for all servers. Returns a hashtable that maps
   * <code>Server</code> objects to arrays of PluginInfo objects describing
   * the plugins for that server.
   */

  private HashMap loadPlugins() throws IOException, ClassNotFoundException{
    HashMap plugins = new HashMap();
    for (int i = 0; i < servers.length; i++)
      plugins.put(servers[i], new Vector());

    // plugins that are shared between all users - usually the ones that come with Jin
    loadPlugins(plugins, new File(JIN_DIR, "plugins")); 

    // user specific plugins, from his own preferences directory
    loadPlugins(plugins, new File(prefsDir, "plugins"));


    // Convert the Server->Vector map to Server->PluginInfo[] map
    HashMap result = new HashMap();
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];
      ArrayList pluginsVector = (ArrayList)plugins.get(server);
      PluginInfo [] pluginsArray = new PluginInfo[pluginsVector.size()];
      pluginsVector.toArray(pluginsArray);

      result.put(server, pluginsArray);
    }

    return result;
  }



  /**
   * Loads plugins from the specified directory into the specified hashtable.
   * Helper method for <code>loadPlugins()</code>.
   */

  private void loadPlugins(final HashMap plugins, final File dir) throws IOException, ClassNotFoundException{
    if (!dir.isDirectory())
      return;

    String [] jars;
    FilenameFilter jarsFilter = new ExtensionFilenameFilter(".jar");

    // Load plugins that are for all servers
    jars = dir.list(jarsFilter);
    for (int i = 0; i < jars.length; i++){
      PluginInfo pluginInfo = loadPluginInfo(new File(dir, jars[i]));
      if (pluginInfo == null)
        continue;

      for (int j = 0; j < servers.length; j++)
        ((ArrayList)plugins.get(servers[j])).add(pluginInfo);
    }


    // Load server specific plugins
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];

      File serverSpecificDir = new File(dir, server.getId());
      if (!serverSpecificDir.isDirectory())
        continue;

      jars = serverSpecificDir.list(jarsFilter);
      for (int j = 0; j < jars.length; j++){
        PluginInfo pluginInfo = loadPluginInfo(new File(serverSpecificDir, jars[j]));
        if (pluginInfo == null)
          continue;

        ((ArrayList)plugins.get(server)).add(pluginInfo);
      }
    }
  }



  /**
   * Loads a single plugin description from the specified jar. Returns 
   * <code>null</code> if unable to load the plugin. Helper method for
   * <code>loadPlugins</code>.
   */

  private PluginInfo loadPluginInfo(final File jar) throws IOException,
      ClassNotFoundException{
    if (!jar.isFile())
      return null;

    ChildClassLoader loader = new ZipClassLoader(jar, mainLoader);

    InputStream pluginDefIn = loader.getResourceAsStream("definition");
    if (pluginDefIn == null){
      System.err.println(jar + " does not contain a plugin definition file");
      return null;
    }
    Properties pluginDef = IOUtilities.loadProperties(pluginDefIn);

    String classname = pluginDef.getProperty("classname");
    if (classname == null){
      System.out.println("The plugin definition file in " + jar + " does not contain a classname property");
      return null;
    }
    
    Class pluginClass = dynamicLoad ? loader.loadClass(classname) : Class.forName(classname);

    InputStream pluginPrefsIn = pluginClass.getResourceAsStream("preferences");
    Preferences pluginPrefs = (pluginPrefsIn == null ? Preferences.createNew() : Preferences.load(pluginPrefsIn));

    if (pluginPrefsIn != null)
      pluginPrefsIn.close();

    return new PluginInfo(pluginClass, pluginPrefs);
  }


  
  /**
   * Returns the list of plugins for the specified server.
   */

  public PluginInfo [] getPlugins(final Server server){
    return (PluginInfo [])serversToPlugins.get(server);
  }


  

  /**
   * Returns the directory where we store the information of the specified user.
   * Helper methodfor <code>storeUser</code>.
   */

  private File usersDir(final User user){
    File usersPrefsDir = new File(prefsDir, "accounts");
    File serverSpecificDir = new File(usersPrefsDir, user.getServer().getId());
    return new File(serverSpecificDir, user.getUsername());
  }
  
  
  
  /**
   * Returns all the resources for the specified resource type. Resources are
   * assumed to be zip or jar files and are looked up in three directories:
   * <code>JIN_DIR/resources/resType</code>,
   * <code>JIN_DIR/resources/resType/serverId</code> and
   * <code>prefsDir/resources/resType</code>.
   */
   
  public Resource [] getResources(final String resType, final Plugin plugin){
    ArrayList resources = new ArrayList();
    
    String serverId = plugin.getServer().getId();
    
    File userResDir = new File(new File(prefsDir, "resources"), resType);
    File jinResDir = new File(new File(JIN_DIR, "resources"), resType);
    File jinServerResDir = new File(new File(new File(JIN_DIR, "resources"), resType), serverId);
                            
    loadResources(userResDir, resources, plugin);
    loadResources(jinResDir, resources, plugin);
    loadResources(jinServerResDir, resources, plugin);
     
    Resource [] resArr = new Resource[resources.size()];
    resources.toArray(resArr);
    
    return resArr;
  }
   
   
   
  /**
   * Loads resources from the specified directory, adding them to the
   * specified <code>Vector</code>. Helper method for <code>getResources</code>.
   */
   
  private void loadResources(final File dir, final ArrayList v, final Plugin plugin){
    if (!dir.exists() || !dir.isDirectory())
      return;
    
    String [] filenames = dir.list(new ExtensionFilenameFilter(new String[]{".jar", ".zip"}));
    if (filenames == null)
      return;
    
    for (int i = 0; i < filenames.length; i++){
      File resourceFile = new File(dir, filenames[i]);
      try{
        Resource resource = loadResource(resourceFile, plugin);
        
        if (resource != null)
          v.add(resource);
      } catch (IOException e){
          System.out.println("Failed to load resource from " + resourceFile);
          e.printStackTrace();
        }
    }
  }
  
  
  
  /**
   * Returns the resource with the specified type and id.
   */
   
  public Resource getResource(final String type, final String id, final Plugin plugin){
    String serverId = plugin.getServer().getId();

    File userResDir = new File(new File(prefsDir, "resources"), type);
    File jinResDir = new File(new File(JIN_DIR, "resources"), type);
    File jinServerResDir = new File(new File(new File(JIN_DIR, "resources"), type), serverId);
    
    File [] files = new File[]{
      new File(userResDir, id + ".jar"), new File(userResDir, id + ".zip"),
      new File(jinServerResDir, id + ".jar"), new File(jinServerResDir, id + ".zip"),
      new File(jinResDir, id + ".jar"), new File(jinResDir, id + ".zip")
    };
      
   
    for (int i = 0; i < files.length; i++){
      try{
        File file = files[i];
        if (file.exists())
          return loadResource(file, plugin);
      } catch (IOException e){e.printStackTrace();}
    }
    
    return null;
  }
  
  
  
  /**
   * Loads a single resource from the specified file. Returns <code>null</code>
   * if unsuccessful. Helper method for <code>loadResources</code> and
   * <code>getResource</code>.
   */
   
  private Resource loadResource(final File file, final Plugin plugin) throws IOException{
    ZipClassLoader cl = new ZipClassLoader(file); 
    Properties def = IOUtilities.loadProperties(cl.getResourceAsStream("definition"));
    if (def == null)
      return null;
    String classname = def.getProperty("classname");
    if (classname == null)
      return null;
   
    // Hack to support the old pieces/boards pack format.
    if ("ImagePieceSetLoader".equals(classname))
      classname = "free.jin.board.PieceSet";
    else if ("ImageBoardLoader".equals(classname))
      classname = "free.jin.board.BoardPattern";
    
    try{
      // We need to load it with the plugin's classloader because the
      // resource may be of a type which is a part of the plugin.
      Class resourceClass = plugin.getClass().getClassLoader().loadClass(classname);
      Resource resource = (Resource)resourceClass.newInstance();
      if (resource.load(cl.getResource("/"), plugin))
        return resource;
      else
        return null;
    } catch (ClassNotFoundException e){e.printStackTrace(); return null;}
      catch (InstantiationException e){e.printStackTrace(); return null;}
      catch (IllegalAccessException e){e.printStackTrace(); return null;}
  }
  
  
  
  /**
   * Returns <code>true</code>.
   */
   
  public boolean isSavePrefsCapable(){
    return true;
  }
  
  
  
  /**
   * Returns text warning the user about saving his password and asking him to
   * confirm it.
   */
   
  public String getPasswordSaveWarning(){
    return "Your password will be stored in your home directory, on your computer,\n" +
           "as plain text. If there are other people using this computer, they may\n" +
           "be able to obtain this password.\n" +
           "Are you sure you want to save your password?";
  }
  
  
  
  /**
   * Returns <code>true</code>. 
   */
  
  public boolean isUserExtensible(){
    return true;
  }
  
  
  
  
  /**
   * Stores all preferences and calls <code>System.exit(0)</code>.
   */

  public void shutdown(){
    storeUserPrefs();
    storeUsers();   

    System.exit(0);
  }
  
  
  
 
  /* ======================================================================== */
  /* ============================ Utility methods =========================== */
  /* ======================================================================== */
  

  
  
  /**
   * Displays an error message to the user.
   */
   
  private void showErrorMessage(final String title, final String message){
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);     
  }
  
  /**
   * Returns the server with the specified id. Returns <code>null</code> if no
   * such server found.
   */

  private Server getServerById( final String id){
    for (int i = 0; i < servers.length; i++)
      if (servers[i].getId().equals(id))
        return servers[i];

    return null;
  }
  
  
  
  
  
  /* ======================================================================== */
  /* ================================== Main ================================ */
  /* ======================================================================== */
  
  
  
  public static void main(String [] args){
    try{
      Properties params = parseCommandlineArgs(args);
      
      osxMenubarFix();
      
      File prefsDir = new File(System.getProperty("user.home"), ".tonic");
      createPreferencesDir(prefsDir);
      
      // Redirect output and error streams to a MultiOutputStream which
      // writes both to the original location and a log
      try{
        FileOutputStream log = new FileOutputStream(new File(prefsDir, "log"));
        PrintStream printLog = new PrintStream(new MultiOutputStream(System.out, log));
        System.setOut(printLog);
        System.setErr(printLog);
      } catch (IOException e){e.printStackTrace();}

      Jin.createInstance(new JinApplication(params, prefsDir));
      
      // Perform some plaform specific things.
      doPlatformSpecificStuff();
      
      Jin.getInstance().start();
      
    } catch (Throwable t){
        if (t instanceof ThreadDeath)
          throw (ThreadDeath)t;
        t.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error type: " + t.getClass().getName() + "\n" +
          "Error message: " + t.getMessage(), "Tonic launch error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
  }
  
  
  
  /**
   * Parses the commandline arguments passed to Jin and returns a
   * <code>Properties</code> object containing them.
   */
   
  private static Properties parseCommandlineArgs(String [] args){
    Properties props = new Properties();
    for (int i = 0; i < args.length; i++){
      String arg = args[i];
      int equalsIndex = arg.indexOf("=");
      if (equalsIndex == -1)
        props.put(arg, "true");
      else
        props.put(arg.substring(0, equalsIndex), arg.substring(equalsIndex+1));
    }
    
    return props;
  }
  


  /**
   * If the specified directory does not exist, attempts to create it. If
   * creating the directory fails, throws an appropriate IOException.
   */

  private static void createPreferencesDir(final File dir) throws IOException{
    // delete the old-style preference files, if they exist
    if (new File(dir, "user.properties").exists())
      IOUtilities.rmdir(dir);

    if (!dir.exists()){
      if (!dir.mkdirs())
        throw new IOException("Unable to create preferences directory: " + dir);
    }
    else if (!dir.isDirectory())
      throw new IOException(dir.toString() + " exists but is not a directory");
  }
  
  
  
  /**
   * Set the value of the apple.laf.useScreenMenuBar system property depending
   * on the Java VM we're running in. Versions prior to 1.4.2 seem to be broken
   * with the native OS X menubar.
   */
   
  private static void osxMenubarFix(){
    String javaVersion = System.getProperty("java.version");
    System.getProperties().put("apple.laf.useScreenMenuBar",
      javaVersion.compareTo("1.4.2") >= 0 ? "true" : "false");
  }
  
  
  
  /**
   * Performs some platform specific stuff.
   */
   
  private static void doPlatformSpecificStuff(){
    if (PlatformUtils.isMacOSX()){
      try{
        Class.forName("free.jin.MacOSXSpecific");
      } catch (ClassNotFoundException e){}
    }
  }
  
}