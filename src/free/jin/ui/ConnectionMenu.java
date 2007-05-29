package free.jin.ui;

import free.jin.*;
import free.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
   * The connection menu.
 */

class ConnectionMenu extends JMenu implements ActionListener, SessionListener {



  /**
   * The maximum accounts on the recently used accounts list.
   */

  private static final int MAX_RECENT_LIST = 5;



  /**
   * The "New Connection..." menu item.
   */

  private final JMenuItem newConnection;



  /**
   * The "Close Connection" menu item.
   */

  private final JMenuItem closeConnection;



  /**
   * The "Exit" menu item.
   */

  private final JMenuItem exit;



  /**
   * A ArrayList holding the history of recently used accounts, in descending
   * order (last used first).
   */

  private final ArrayList recentAccounts;



  /**
   * The index of the separator following the close connection menu item.
   */

  private final int separatorIndex;



  /**
   * Creates a new <code>ConnectionMenu</code>.
   */

  public ConnectionMenu(){
    super("Connection");
    setMnemonic('C');

    add(newConnection = new JMenuItem("New Connection...", 'N'));
    // If you use 'c' as the mnemonic here, alt+c won't work for the menu,
    // see http://developer.java.sun.com/developer/bugParade/bugs/4213634.html
    add(closeConnection = new JMenuItem("Close Connection", 'l'));
    separatorIndex = getItemCount();

    addSeparator();
    add(exit = new JMenuItem("Exit", 'x'));

    exit.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    newConnection.addActionListener(this);
    closeConnection.addActionListener(this);
    exit.addActionListener(this);

    recentAccounts = loadRecentAccounts();
    updateRecentAccountsMenuItems();

    setConnected(false, null);
  }



  /**
   * Registers us as session listener.
   */

  public void addNotify(){
    super.addNotify();

    Jin.getInstance().getConnManager().addSessionListener(this);
    Session session = Jin.getInstance().getConnManager().getSession();
    setConnected(session != null, session);
  }



  /**
   * Unregisters us as a session listener.
   */

  public void removeNotify(){
    super.removeNotify();

    Jin.getInstance().getConnManager().removeSessionListener(this);
  }



  /**
   * SessionListener implementation. Simply delegates to
   * <code>setConnected</code>.
   */

  public void sessionEstablished(SessionEvent evt){
    setConnected(true, evt.getSession());
  }



  /**
   * SessionListener implementation. Simple delegates to
   * <code>setConnected</code>.
   */

  public void sessionClosed(SessionEvent evt){
    setConnected(false, evt.getSession());
  }



  /**
   * This method is called to notify the menu of the current state of the
   * connection - whether we are currently connected to the server or not.
   */

  public void setConnected(boolean connected, Session session){
    newConnection.setEnabled(!connected);
    closeConnection.setEnabled(connected);

    if (!connected && (session != null)){
      User user = session.getUser();
      if (Jin.getInstance().isKnownUser(user) && !user.isGuest()){
        recentAccounts.remove(user);
        recentAccounts.add(0, user);
        updateRecentAccountsMenuItems();
        saveRecentAccounts(recentAccounts);
      }
    }

    setRecentAccountsMenuItemsEnabled(!connected);
  }



  /**
   * <code>ActionListener</code> implementation. Listens to activation of the
   * various menu items and performs the desired operation.
   */

  public void actionPerformed(ActionEvent evt){
    ConnectionManager connManager = Jin.getInstance().getConnManager();
    Object source = evt.getSource();
    if (source == newConnection){
      connManager.displayNewConnUI();
    }
    else if (source == closeConnection){
      Object result = OptionPanel.OK;
      Session session = Jin.getInstance().getConnManager().getSession();
      if ((session != null) && session.isConnected()){
        result = OptionPanel.confirm("Close Session?",
          "Disconnect from the server and close the session?", OptionPanel.OK);
      }

      if (result == OptionPanel.OK)
        connManager.closeSession();
    }
    else if (source == exit){
      Jin.getInstance().quit(true);
    }
    else{ // One of the recent account menu items
      int index = Utilities.indexOf(getMenuComponents(), source);
      User user = (User)recentAccounts.get(index - separatorIndex - 1);
      connManager.displayNewConnUI(user);
    }
  }



  /**
   * Brings the recent history menu item list up-to-date.
   */

  private void updateRecentAccountsMenuItems(){
    // Remove all existing history menu items
    while (getItem(separatorIndex + 1) != exit)
      remove(separatorIndex + 1);

    // Add them again
    for (int i = 1; i <= recentAccounts.size(); i++){
      User user = (User)recentAccounts.get(i - 1);
      String label = i + " " + user.getUsername() + " at " + user.getServer().getShortName();
      JMenuItem menuItem = new JMenuItem(label);
      if (i <= 8)
        menuItem.setMnemonic(Character.forDigit(i, 10));
      menuItem.addActionListener(this);

      insert(menuItem, separatorIndex + i);
    }

    if (recentAccounts.size() != 0)
      insertSeparator(separatorIndex + recentAccounts.size() + 1);
  }



  /**
   * Enables or disables the recent accounts menu items.
   */

  private void setRecentAccountsMenuItemsEnabled(boolean isEnabled){
    for (int i = 1; i <= recentAccounts.size(); i++)
      getItem(separatorIndex + i).setEnabled(isEnabled);
  }



  /**
   * Loads the recently used accounts list into a ArrayList and returns it.
   */

  private ArrayList loadRecentAccounts(){
    ArrayList accounts = new ArrayList(MAX_RECENT_LIST);
    Preferences prefs = Jin.getInstance().getPrefs();

    int count = prefs.getInt("accounts.recent.count", 0);
    for (int i = 0; i < count; i++){
      String username = prefs.getString("accounts.recent." + i + ".username");
      String serverId = prefs.getString("accounts.recent." + i + ".serverId");

      Server server = Jin.getInstance().getServerById(serverId);
      if (server == null)
        continue;

      User user = Jin.getInstance().getUser(server, username);
      if (user == null)
        continue;

      accounts.add(user);
      if (accounts.size() == MAX_RECENT_LIST)
        break;
    }

    return accounts;
  }



  /**
   * Saves the recent account list into user preferences.
   */

  private void saveRecentAccounts(ArrayList accounts){
    Preferences prefs = Jin.getInstance().getPrefs();

    int count = accounts.size();
    prefs.setInt("accounts.recent.count", count);

    for (int i = 0; i < count; i++){
      User user = (User)accounts.get(i);

      String username = user.getUsername();
      String serverId = user.getServer().getId();

      prefs.setString("accounts.recent." + i + ".username", username);
      prefs.setString("accounts.recent." + i + ".serverId", serverId);
    }
  }


}
