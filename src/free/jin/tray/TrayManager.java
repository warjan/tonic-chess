/*
 * TrayManager.java
 *
 * Created on 17 maj 2006, 20:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.tray;

import free.jin.Connection;
import free.jin.ConnectionDetails;
import free.jin.Jin;
import free.jin.event.*;
import free.jin.plugin.Plugin;
import free.jin.tray.prefs.TrayPrefsPanel;
import free.jin.ui.MdiUiProvider;
import free.jin.ui.PreferencesPanel;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 *
 * @author whp
 */
public class TrayManager extends Plugin implements ChatListener, GameListener, ConnectionListener  {

    /**
     * The tray representation that will hold application and event icons.
     */
    SystemTray tray;

    /**
     * Application icon. This one is displayed first in the tray.
     */
    TrayIcon trayIcon;

    /**
     * Icon diplayed in the tray when <code>moveMade</code> method is executed.
     */
    TrayIcon movedTrayIcon;

    /**
     * Boolean to make sure that icon may be changed.
     */
    boolean iconChanged;

    /**
     * Application icon.
     */
     private Icon normalIcon;

    /**
     * Move made icon.
     */
    private Icon movedIcon;

    /**
     * Game end icon.
     */

    private Icon gameEndIcon;
    
    /**
     * Game start icon.
     */
    
    private Icon gameStartIcon;
    
    /**
     * Connection lost icon.
     */
    
    private Icon connLostIcon;
    

    
    /**
     * Connection failed icon.
     */
    
    private Icon connFailIcon;
    
    /**
     * Indicates wether tray is displayed with application start.
     */
    
    private boolean firstTime;
    
    /**
     * Popup menu for tray icon.
     */
    
    private JPopupMenu menu;
    
    /**
     * And its items.
     */
    
    private JMenuItem quickQuit;
    private JMenuItem unseek;
    private JMenuItem quickReconnect;
    private JMenuItem minimize;

    private ConnectionDetails lastDetails;
    private ImageIcon nullIcon;

    /** Creates a new instance of TrayManager */
    public TrayManager() {
    }

    /**
     * Overriden from <code>free.jin.Plugin</code>.
     * @return id simple non-whitespace String.
     */

    public String getId() {
        return "tray";
    }

    /**
     * Overriden from <code>free.jin.Plugin</code>.
     * @return name normal String.
     */
    
    public String getName() {
        return "Tray Manager";
    }

    /**
     * Starts the plugin first registering listeners and then creating tray and some icons.
     */

    public void start(){

            if (getPrefs().getBool("display.tray") == true){
                registerListeners();
                initTrayMenu();
                createIcons();
                firstTime = true;
            }



    }

    /**
     * Overrides <code>hasPreverencesUI</code> to return whether the plugin
     * will display a preferences UI (the setting is taken from the
     * <pre>"preferences.show"</pre> property.
     * @return boolean value indicating whether this plugin have ui for setting preferences
     */

    public boolean hasPreferencesUI() {
        return getPrefs().getBool("preferences.show", true);
    }

        public PreferencesPanel getPreferencesUI() {
        return new TrayPrefsPanel(this);
    }
    
    /**
     * Creates icon used later in tray.
     */

    private void createIcons() {

        //creation of normal application icon
        URL normalIconURL = TrayManager.class.getResource("normal.gif");
        normalIcon = new ImageIcon(normalIconURL);
        trayIcon = new TrayIcon(normalIcon); //sets the initial icon for tray
        trayIcon.setIconAutoSize(true); //makes icon autoresizable - otherwise sometimes just a part of icon be visible
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Jin.getInstance().getUIProvider() instanceof MdiUiProvider){
                   ((MdiUiProvider)Jin.getInstance().getUIProvider()).restoreView();

                }
                trayIcon.setIcon(normalIcon);

                iconChanged = false;
            }
        });
        
        trayIcon.setToolTip("Tonic - internet chess interface");
        trayIcon.setPopupMenu(menu);
        
        //this is icon for moveMade event
        movedIcon = getIconsEasy("moved.gif");

        //this is the icon for gameEnd event
        gameEndIcon = getIconsEasy("gameend.gif");
        
        //this is the icon for gameStarted event
        gameStartIcon = getIconsEasy("gamestart.gif");
        
        //this is the icon for connectionLost event
        connLostIcon = getIconsEasy("connlost.gif");
        
        //this is the icon for connectionFailed event        
        connFailIcon = getIconsEasy("connfail.gif");

        nullIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        
    }

    private Icon getIconsEasy(String s) {

        
        URL iconURL = TrayManager.class.getResource(s);
        Icon icon = new ImageIcon(iconURL);
        
        return icon;
    }

    /**
     * This method initates the tray menu.
     */

    private void initTrayMenu() {
        menu = new JPopupMenu();
        quickQuit = new JMenuItem("QuickQuit", nullIcon);
        quickQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Jin.getInstance().quit(false);
            }
        });
        
        unseek = new JMenuItem("Cancel seeks", nullIcon);
        unseek.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConn().sendCommand("unseek");
            }
        });
        if (Jin.getInstance().getUIProvider() instanceof MdiUiProvider){
            minimize = new JMenuItem("Minimize", nullIcon);
            minimize.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    ((MdiUiProvider)Jin.getInstance().getUIProvider()).getMainFrame().setState(Frame.ICONIFIED);
                }

            });
            menu.add(minimize);
        }
        else{}
        menu.add(unseek);
        menu.add(quickQuit);

        
        
            
        
    }

    public void chatMessageReceived(ChatEvent evt) {
    }
    
    public void gameStarted(GameStartEvent evt) {
            
        
                trayIcon.setIcon(gameStartIcon);
                iconChanged = false;
        
        
    }

    /**
     * Method called when one of the users makes the move. It changes icon in the tray.
     * @param evt <code>MoveMadeEvent</code>
     */
    public void moveMade(MoveMadeEvent evt) {

       if (!(evt.getMovingPlayersName().equals(Jin.getInstance().getConnManager().getSession().getUser().getUsername()))){
            if (iconChanged == false){
                trayIcon.setIcon(movedIcon);
                iconChanged = true;
            }
       }
    }
    
    public void positionChanged(PositionChangedEvent evt) {
    }
    
    public void takebackOccurred(TakebackEvent evt) {
    }
    
    public void illegalMoveAttempted(IllegalMoveEvent evt) {
    }
    
    public void clockAdjusted(ClockAdjustmentEvent evt) {
    }
    
    public void boardFlipped(BoardFlipEvent evt) {
    }
    
    public void offerUpdated(OfferEvent evt) {
    }
    
    public void gameEnded(GameEndEvent evt) {

        trayIcon.setIcon(gameEndIcon);
        iconChanged = false;

    }
    
    public void connectionAttempted(Connection conn, String hostname, int port) {

    }
    
    public void connectionEstablished(Connection conn) {
        if (firstTime){
            tray = SystemTray.getDefaultSystemTray();
            tray.addTrayIcon(trayIcon);
            iconChanged = false;
            firstTime = false;
           /* if (quickReconnect != null){
                menu.remove(quickReconnect);
                quickReconnect = null;
            }*/
            lastDetails = Jin.getInstance().getConnManager().getSession().getConnDetails();
        }
        else{}
    }
    
    public void connectingFailed(Connection conn, String reason) {
    }
    
    public void loginSucceeded(Connection conn) {
        
    }
    
    public void loginFailed(Connection conn, String reason) {
    }
    
    public void connectionLost(Connection conn) {
        quickReconnect = new JMenuItem("Reconnect");
        quickReconnect.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                Jin.getInstance().getConnManager().closeSession();
                Jin.getInstance().getConnManager().login(lastDetails);

            }
        });
        menu.add(quickReconnect);
        menu.updateUI();

        trayIcon.setIcon(connLostIcon);
    }

    /**
     * Registering all needed listeners.
     */
    private void registerListeners() {
        Connection conn = getConn();
        BasicListenerManager listenerManager = (BasicListenerManager) conn.getListenerManager();
        
        listenerManager.addGameListener(this);
        listenerManager.addConnectionListener(this);
        listenerManager.addChatListener(this);
    }
    public void stop(){
        if (tray != null){
        unregisterListeners();
        removeTrayIcon();
        }
    }

    private void removeTrayIcon() {
        tray.removeTrayIcon(trayIcon);
    }

    private void unregisterListeners() {
    Connection conn = getConn();
    BasicListenerManager listenerManager = (BasicListenerManager)conn.getListenerManager();

    listenerManager.removeGameListener(this);
    listenerManager.removeChatListener(this);
    listenerManager.removeConnectionListener(this);
    }


}








