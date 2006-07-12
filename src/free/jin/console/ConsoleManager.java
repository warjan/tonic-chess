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

package free.jin.console;

import free.jin.Jin;
import free.jin.plugin.PluginUIListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import free.jin.Connection;
import free.jin.GameListConnection;
import free.jin.console.prefs.ConsolePrefsPanel;
import free.jin.event.ChatEvent;
import free.jin.event.ChatListener;
import free.jin.event.ConnectionListener;
import free.jin.event.GameListEvent;
import free.jin.event.GameListListener;
import free.jin.event.GameListListenerManager;
import free.jin.event.ListenerManager;
import free.jin.event.PlainTextEvent;
import free.jin.event.PlainTextListener;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIAdapter;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.ui.AbstractPluginUIContainer;
import free.jin.ui.OptionPanel;
import free.jin.ui.PreferencesPanel;
import free.jin.ui.UIProvider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.util.*;

import javax.swing.*;

//TODO I was thinking about adding multiple console feature to ConsoleManager.
//TODO I think I will need to modify Console for it - to make it filer consious.
//TODO And I would be a good thing to modify the GUI of console and make it customizable.
/**
 * A Plugin which implements the consoles functionality. It's responsible for
 * opening, positioning and closing the various consoles used by the user.
 */

public class ConsoleManager extends Plugin implements PlainTextListener, ChatListener, ConnectionListener, GameListListener{
  
  
  
  /**
   * The constant specifying "embedded" game lists display style.
   */
  
  public static final int EMBEDDED_GAME_LISTS = 0;
  
  
  
  /**
   * The constant specifying "external" game lists display style.
   */
  
  public static final int EXTERNAL_GAME_LISTS = 1;
  
  
  
  /**
   * The constant specifying that game lists aren't displayed specially.
   */
  
  public static final int NO_GAME_LISTS = 2;


  
  /**
   * The main console.
   */

  protected Console console;



  /**
   * The container in which the console sits.
   */

  protected PluginUIContainer consoleContainer;



  /**
   * The current game lists display style.
   */

  private int gameListsDisplayStyle;
  
  
  
  /**
   * Are we currently paused?
   */
   
  private boolean isPaused = false;
  
  
  
  /**
   * A queue of the events we've accumulated while being paused.
   */
   
  private final ArrayList pausedEventsQueue = new ArrayList();
  
  /**
   * Containers mapped to consoles - to let closing the window close the coresponding console.
   */
    
    private Map containersToConsoles = Collections.synchronizedMap(new HashMap());

   
   /**
    * Types mapped to consoles.
    */
    
    private Map typesToConsoles = Collections.synchronizedMap(new HashMap());
    
    /**
     * JComboBox that specificates the type of console.
     */

    protected JComboBox newConsoleSpec;
    
    /**
     * Strings for newConsoleSpec JComboBox
     */
    
    protected String[] consoleTypes;
    
    /**
     * JTextField for channel and game numbers.
     */
     
     protected JTextField chanGameNumberInput;

    /**
     * A button that creates new consoles.
     */

    protected JButton newConsoleButton;

    /**
     * A check button that indicates wether console will receive plaint text events.
     */

    protected JCheckBox listenToPlain;


    /**
   * Starts this plugin.
   */

  public void start(){
    initState();
    openConsole();
    registerConnListeners();
  }



  /**
   * Stops the plugin.
   */

  public void stop(){
    unregisterConnListeners();
  }



  /**
   * Sets all the variables representing the current settings from the parameters.
   */

  protected void initState(){
    String gameListsDisplayStyleString = getPrefs().getString("game-list-display-style", "embedded");
    if ("embedded".equals(gameListsDisplayStyleString))
      setGameListsDisplayStyle(EMBEDDED_GAME_LISTS);
    else if ("framed".equals(gameListsDisplayStyleString))
      setGameListsDisplayStyle(EXTERNAL_GAME_LISTS);
    else if ("none".equals(gameListsDisplayStyleString))
      setGameListsDisplayStyle(NO_GAME_LISTS);
  }



  /**
   * Creates and opens the console.
   */

  private void openConsole(){
    console = createConsole();
    
    JSplitPane splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPane.setOneTouchExpandable(true);
    
    consoleContainer = createContainer("console.main", UIProvider.ESSENTIAL_CONTAINER_MODE);
    consoleContainer.setTitle("Main Console");

    URL iconImageURL = ConsoleManager.class.getResource("icon.gif");
    if (iconImageURL != null)
      consoleContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));
    
    Container content = consoleContainer.getContentPane();
    content.setLayout(new BorderLayout());
    
    //content.add(console, BorderLayout.CENTER);
    splitPane.setBottomComponent(console);
    consoleContainer.addPluginUIListener(new PluginUIAdapter(){
      public void pluginUIActivated(PluginUIEvent evt){
        console.requestDefaultFocus();
      }
    });
    JPanel newConsolePanel = new JPanel();
    FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
    newConsolePanel.setLayout(fl);
    newConsoleButton = getNewConsoleButton();
      newConsoleSpec = getNewConsoleSpec();
      
    chanGameNumberInput = new JTextField("" ,5);
    chanGameNumberInput.setEnabled(false);
      listenToPlain = new JCheckBox("Receive plain text.");
      listenToPlain.addItemListener(new ItemListener(){

          public void itemStateChanged(ItemEvent e) {
              String oldChanGameText = chanGameNumberInput.getText().trim();
              String newChanGameText = null;
              if (e.getStateChange()  == ItemEvent.SELECTED){
                  newChanGameText = oldChanGameText.concat(" plain");
                  newChanGameText = newChanGameText.trim();
                  chanGameNumberInput.setText(newChanGameText);
              } else{
                  newChanGameText = oldChanGameText.replace("plain", "");
                  chanGameNumberInput.setText(newChanGameText);
              }
          }
      });
     consoleTypes = getConsoleTypes();

      JLabel newConsoleLabel = new JLabel("Specify console type: ");
     
     newConsolePanel.add(newConsoleLabel);
     newConsolePanel.add(newConsoleSpec);
     newConsolePanel.add(chanGameNumberInput);
      newConsolePanel.add(listenToPlain);
     newConsolePanel.add(newConsoleButton);
     
     splitPane.setTopComponent(newConsolePanel);
     content.add(splitPane, BorderLayout.CENTER);
    consoleContainer.setVisible(true);
  }

    /**
     * Returns a combo box that holds types of consoles.
     * @return combobox with <code>ActionListener</code> attached.
     */

    protected JComboBox getNewConsoleSpec() {
        JComboBox combobox = new JComboBox(consoleTypes);
        combobox.addActionListener( new ActionListener(){
                       public void actionPerformed(ActionEvent e) {
                           int consoleSpec = newConsoleSpec.getSelectedIndex();
                          if (consoleSpec == 1  || consoleSpec == 2){
                              chanGameNumberInput.setEnabled(true);
                          }
                          else{
                              chanGameNumberInput.setText("");
                              chanGameNumberInput.setEnabled(false);
                          }
                       }
                   });
        return combobox;
    }

    /**
     * Creates new console with specified by user receiving type. 
     */

    public void openNewConsole(){
  
      Console newConsole = createConsole();
      
      String consoleName = getConsoleName();
      String consoleType = getConsoleType();
      
      PluginUIContainer newConsoleContainer = createContainer(consoleType, UIProvider.CLOSEABLE_CONTAINER_MODE);
      newConsoleContainer.setTitle(consoleName);

      newConsoleContainer.addPluginUIListener(pul);
      
      containersToConsoles.put(newConsoleContainer, newConsole);
      typesToConsoles.put(consoleType.trim(), newConsole);

      
      Container content = newConsoleContainer.getContentPane();
      content.setLayout(new BorderLayout());
      content.add(newConsole, BorderLayout.CENTER);
      
      newConsoleContainer.setSize(400, 300);
      newConsoleContainer.setVisible(true);

  }

              public void openNewConsole(String text) {
                        Console newConsole = createConsole(text);

      String consoleName = getConsoleName();
      String consoleType = getConsoleType();

      PluginUIContainer newConsoleContainer = createContainer(consoleType, UIProvider.CLOSEABLE_CONTAINER_MODE);
      newConsoleContainer.setTitle(consoleName);

      newConsoleContainer.addPluginUIListener(pul);

      containersToConsoles.put(newConsoleContainer, newConsole);
      typesToConsoles.put(consoleType.trim(), newConsole);


      Container content = newConsoleContainer.getContentPane();
      content.setLayout(new BorderLayout());
      content.add(newConsole, BorderLayout.CENTER);

      newConsoleContainer.setSize(400, 300);
      newConsoleContainer.setVisible(true);
          }

    /**
     * Returns the type of a console to be created.
     * @return
     */

    protected String getConsoleType() {
        return "";  
    }

    /**
     * This method returns the array of string of console types that fills combobox.
     * @return ct Array of console types.
     */
  
  protected String[] getConsoleTypes(){
      String[] ct = {"shout"};
      return ct;
  }

    /**
     * Returns a button that creates new console.
     */

    protected JButton getNewConsoleButton(){
      JButton button = new JButton("N C");
      button.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent evt){
              if (
                      ((String)newConsoleSpec.getSelectedItem()).equals("Channel tells") && !(chanGameNumberInput.getText().matches("\\d{1,3}"))){
                  OptionPanel.error(console, "Channel number error", "Type a number of channel you would like to here, please.");
              }

              if (((String)newConsoleSpec.getSelectedItem()).matches("(Kibitzes)|(Whispers)") && !(chanGameNumberInput.getText().matches("\\d{1,4}"))){
                  OptionPanel.error(console, "Game number error", "Type a game number for which you would like hear " + newConsoleSpec.getSelectedItem().toString().toLowerCase()+ " , please.");
              }

              else{
              if (((String)newConsoleSpec.getSelectedItem()).equals("Private tells")){

                  openNewConsole(chanGameNumberInput.getText());
              }
                  else{
                  System.out.println("EQUALS!!! " + chanGameNumberInput.getText() + "<->" +newConsoleSpec.getSelectedItem());
                openNewConsole();
                }
              }
          }


      }
      );
        return button;
    }

  /**
   * Creates the Console for this ConsoleManager.
   */

  protected Console createConsole(){
    return new Console(getConn(), getPrefs());
  }
  
    /**
     * Creates console with prefix supplier filled with serverCommand string as default.
     * @param serverCommand - the default server command send when enter key is pressed in command line
     * @return Console object with prefix supplier with serverCommand set as default.
     */
  
  protected Console createConsole(String serverCommand){
      return new Console(getConn(), getPrefs(), serverCommand);
  }
  
  
  //TODO review Console class to find out how to filter messages.
  /*protected Console createConsole(){
      return new Console(getConn(), getPrefs(), )
  }*/
  
  
  
  /**
   * Sets the current game lists display style to the specified value. Possible
   * values are <code>EMBEDDED_GAME_LISTS</code>,
   * <code>EXTERNAL_GAME_LISTS</code> and <code>NO_GAME_LISTS</code>. 
   */
  
  public void setGameListsDisplayStyle(int style){
    switch (style){
      case EMBEDDED_GAME_LISTS:
      case EXTERNAL_GAME_LISTS:
      case NO_GAME_LISTS:
        break;
      default:
        throw new IllegalArgumentException("Bad game lists display style value: " + style);
    }

    if (getConn() instanceof GameListConnection){
      GameListListenerManager listenerManager = ((GameListConnection)getConn()).getGameListListenerManager();
  
      if (style == NO_GAME_LISTS)
        listenerManager.removeGameListListener(ConsoleManager.this);
      else if (gameListsDisplayStyle == NO_GAME_LISTS)
        listenerManager.addGameListListener(ConsoleManager.this);
    }
    
    this.gameListsDisplayStyle = style;
  }
  
  
  
  /**
   * Returns the current game lists display style.
   */
  
  public int getGameListsDisplayStyle(){
    return gameListsDisplayStyle;
  }
  
  
  
  /**
   * Sets whether text is copied to the clipboard automatically, upon selection.
   */
  
  public void setCopyOnSelect(boolean isCopyOnSelect){
    getPrefs().setBool("copyOnSelect", isCopyOnSelect);
  }
  
  
  
  /**
   * Returns whether text is copied to the clipboard automatically, upon
   * selection.
   */
  
  public boolean isCopyOnSelect(){
    return getPrefs().getBool("copyOnSelect", true);
  }



  /**
   * Rereads the plugin/user properties and changes the assosiated console
   * manager's settings accordingly. This method should be called when the user
   * changes the preferences.
   */

  public void refreshFromProperties(){
    console.refreshFromProperties();
  }



  /**
   * Registers all the necessary listeners to JinConnection events.
   */

  protected void registerConnListeners(){
    Connection conn = getConn();
    ListenerManager listenerManager = conn.getListenerManager();

    listenerManager.addPlainTextListener(this);
    listenerManager.addChatListener(this);
    listenerManager.addConnectionListener(this);

    if ((conn instanceof GameListConnection) && (getGameListsDisplayStyle() != NO_GAME_LISTS))
      ((GameListConnection)conn).getGameListListenerManager().addGameListListener(this);
  }




  /**
   * Unregisters all the listeners we've registered with the JinConnection.
   */

  protected void unregisterConnListeners(){
    Connection conn = getConn();
    ListenerManager listenerManager = conn.getListenerManager();

    listenerManager.removePlainTextListener(this);
    listenerManager.removeChatListener(this);
    listenerManager.removeConnectionListener(this);

    if ((conn instanceof GameListConnection) && (getGameListsDisplayStyle() != NO_GAME_LISTS))
      ((GameListConnection)conn).getGameListListenerManager().removeGameListListener(this);
  } 



  /**
   * Adds the specified line of text to the console.
   */

  public void addSpecialLine(String line){
    console.addToOutput(line, "special");
  }


  /**
   * Listens to plain text and adds it to the console.
   */

  public void plainTextReceived(PlainTextEvent evt){
    if (isPaused()){
      pausedEventsQueue.add(evt);
      return;
    }
      Iterator idIterator = typesToConsoles.keySet().iterator();
      while (idIterator.hasNext()){

          String typeKey = (String)idIterator.next();
          if (typeKey.contains("plain")){
             ((Console)typesToConsoles.get(typeKey)).addToOutput(evt.getText(), "plain");
          }
      }
      
    console.addToOutput(evt.getText(), "plain");
  }



  /**
   * Listens to ChatEvents and adds appropriate text to the console.
   */

  public void chatMessageReceived(ChatEvent evt){
    if (isPaused()){
      pausedEventsQueue.add(evt);
      return;
    }
    
    String type = evt.getType();
    Object forum = evt.getForum();
    String sender = evt.getSender();
    
    String chatMessageType = type + "." + (forum == null ? "" : forum.toString()) + "." + sender;
    //Console receivingConsole = null;
    String receiverId = type + " " + (forum == null ? "" : forum.toString());
      if (type.equals("say") || type.equals("tell")){
               receiverId = receiverId.concat(sender.toLowerCase());
               System.out.println("ID =" + receiverId);
      }
      Iterator idIterator = typesToConsoles.keySet().iterator();
      while (idIterator.hasNext()){

          String typeKey = (String)idIterator.next();
          //System.out.println(typeKey);
          if (typeKey.contains(receiverId.trim())){
              //System.out.println("KEY = " + receiverId.trim());
             ((Console)typesToConsoles.get(typeKey)).addToOutput(translateChat(evt), chatMessageType);
          }
      }

      /*if (typesToConsoles.containsKey(receiverId.trim())){


      }*/



    console.addToOutput(translateChat(evt), chatMessageType);
    //if (receivingConsole != null){
    //    receivingConsole.addToOutput(translateChat(evt), chatMessageType);
    //}
  }
  
  
  
  /**
   * Sets the pause state of the console manager.
   */
   
  public void setPaused(boolean isPaused){
    this.isPaused = isPaused;
    
    if (!isPaused){
      int size = pausedEventsQueue.size();
      for (int i = 0; i < size; i++){
        try{
          Object evt = pausedEventsQueue.get(i);
          if (evt instanceof PlainTextEvent)
            plainTextReceived((PlainTextEvent)evt);
          else if (evt instanceof ChatEvent)
            chatMessageReceived((ChatEvent)evt);
        } catch (Exception e){e.printStackTrace();}
      }
      pausedEventsQueue.clear();
    }
  }
  
  
  
  /**
   * Returns whether the console manager is currently paused.
   */
   
  public boolean isPaused(){
    return isPaused;
  }




  /**
   * Translates the given ChatEvent into a string to be added to the console's
   * output. This method is intended to be overridden by server specific classes.
   * The default implementation returns a string useful only for debugging.
   */

  protected String translateChat(ChatEvent evt){
    return evt.toString();
  }
  
  
  
  /**
   * Gets called when a connection is attempted.
   */

  public void connectionAttempted(Connection conn, String hostname, int port){
    console.addToOutput("Trying to connect to " + hostname + " on port " + port + "...", "info");
  }



  /**
   * Gets called when the connection to the server is established.
   */

  public void connectionEstablished(Connection conn){
    console.addToOutput("Connected", "info");  
  }



  /**
   * Gets called when the login procedure is done.
   */

  public void loginSucceeded(Connection conn){
    consoleContainer.setTitle("Main Console - " + getConn().getUsername() +
      " on " + getServer().getLongName());
  }



  /**
   * Gets called when the connection to the server is lost.
   */

  public void connectionLost(Connection conn){
    console.addToOutput("WARNING: Disconnected", "info");
  }
  
  
  
  // The rest of ConnectionListener's methods.
  public void connectingFailed(Connection conn, String reason){}
  public void loginFailed(Connection conn, String reason){}



  /**
   * Creates a table to display a game list item for the given GameListEvent.
   */

  protected JTable createGameListTable(GameListEvent evt){
    return new GameListTable(console, evt);
  }

  


  /**
   * Gets called when a game list arrives from the server.
   * Adds a JTable displaying the list to the console.
   */

  public void gameListArrived(GameListEvent evt){
    final JTable table = createGameListTable(evt);
    JTableHeader header = table.getTableHeader();
    Dimension originalPrefSize = header.getPreferredSize();
    // This abomination is needed because Metal L&F has a too small preferred label height on 1.1
    header.setPreferredSize(new Dimension(originalPrefSize.width, Math.max(originalPrefSize.height, 18)));

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // The following block sets the preferred sizes of the columns to the maximum
    // preferred width of the cells in that column.
    TableColumnModel columnModel = table.getColumnModel();
    TableModel model = table.getModel();
    for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++){
      TableColumn column = columnModel.getColumn(columnIndex);
      Component headerRendererComponent = column.getHeaderRenderer().getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, columnIndex);
      int maxWidth = headerRendererComponent.getPreferredSize().width;
      for (int rowIndex=0;rowIndex<model.getRowCount();rowIndex++){
        TableCellRenderer cellRenderer = table.getCellRenderer(rowIndex, columnIndex);
        Object value = model.getValueAt(rowIndex, columnIndex);
        Component rendererComponent = cellRenderer.getTableCellRendererComponent(table, value, false, false, rowIndex, columnIndex);
        int cellWidth = rendererComponent.getPreferredSize().width;
        if (cellWidth>maxWidth)
          maxWidth = cellWidth;
      }
      if (maxWidth>150) // This is probably the "note" column, which is very wide but we don't want it to take all the space
        column.setPreferredWidth(50);
      else
        column.setPreferredWidth(maxWidth);
    }
    
    

    String title = "  "+evt.getListTitle()+".  Displaying items "+evt.getFirstIndex()+"-"+evt.getLastIndex()+" out of "+evt.getItemCount()+"  ";

    // Otherwise, the table header is not created on time for the layout to take account of it
    // and size the scrollpane properly.
    // See bug https://sourceforge.net/tracker/index.php?func=detail&aid=602496&group_id=50386&atid=459537
    scrollPane.setColumnHeaderView(table.getTableHeader());

    if (getGameListsDisplayStyle() == EMBEDDED_GAME_LISTS){
      scrollPane.setBorder(new TitledBorder(title));
      int maxHeight = (console.getOutputArea().height - 40) * 2/3;
      if (scrollPane.getPreferredSize().height > maxHeight)
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, maxHeight));

      // This is stupid, but fixes the bug in the embedded case case described at
      // https://sourceforge.net/tracker/index.php?func=detail&aid=602496&group_id=50386&atid=459537 for JDK1.4
      scrollPane.setPreferredSize(scrollPane.getPreferredSize());

      console.addToOutput(scrollPane);
    }
    else{
      PluginUIContainer container = createContainer(null, UIProvider.CLOSEABLE_CONTAINER_MODE);
      container.setTitle(title);

      Container content = container.getContentPane();
      content.setLayout(new BorderLayout());
      content.add(scrollPane, BorderLayout.CENTER);

      container.setVisible(true);
    }
  }



  /**
   * Saves the current state into the user file.
   */

  public void saveState(){
    if (getConn() instanceof GameListConnection){
      String displayStyleString;
      switch (getGameListsDisplayStyle()){
        case EMBEDDED_GAME_LISTS: displayStyleString = "embedded"; break;
        case EXTERNAL_GAME_LISTS: displayStyleString = "framed"; break;
        case NO_GAME_LISTS: displayStyleString = "none"; break;
        default:
          throw new IllegalStateException("Bad gameListsDisplayStyle value");
      }
      getPrefs().setString("game-list-display-style", displayStyleString);
    }
  }



  /**
   * Returns the string <code>"console"</code>. The scripter plugin has this
   * hardcoded.
   */

  public String getId(){
    return "console";
  }



  /**
   * Returns the string "Main Console".
   */

  public String getName(){
    return "Main Console";
  }



  /**
   * Overrides <code>hasPreverencesUI</code> to return whether the plugin
   * will display a preferences UI (the setting is taken from the
   * <pre>"preferences.show"</pre> property.
   */

  public boolean hasPreferencesUI(){
    return getPrefs().getBool("preferences.show", true);
  }




  /**
   * Return a PreferencesPanel for changing the console manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new ConsolePrefsPanel(this);
  }
  
PluginUIListener pul = new PluginUIListener(){
    public void pluginUIShown(PluginUIEvent evt) {
    }

    public void pluginUIHidden(PluginUIEvent evt) {
        AbstractPluginUIContainer puic = (AbstractPluginUIContainer)evt.getSource();
        String id = puic.getId();
        Plugin plugin = puic.getPlugin();
        
      
        Jin.getInstance().getUIProvider().removePluginContainer(plugin, id, puic);
        
        containersToConsoles.remove(puic);

    }

    public void pluginUIClosing(PluginUIEvent evt) {
        
    }

    public void pluginUIActivated(PluginUIEvent evt) {
    }

    public void pluginUIDeactivated(PluginUIEvent evt) {
    }

};
    /**
     * Method that returns name of console - diplayed in the title of console container.
     * @return s String that will be passed to <code>setTitle()</code> method. 
     */
    protected String getConsoleName() {
        return "New Console";
    }
}
