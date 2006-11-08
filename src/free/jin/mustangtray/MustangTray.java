/*
 * TrayManager.java
 *
 * Created on 17 maj 2006, 20:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.mustangtray;

import free.jin.Connection;
import free.jin.ConnectionDetails;
import free.jin.Jin;
import free.jin.mustangtray.prefs.MustangTrayPrefsPanel;
import free.jin.event.*;
import free.jin.plugin.Plugin;
import free.jin.ui.MdiUiProvider;
import free.jin.ui.PreferencesPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

/**
 * This class is just a tray manager for Java 6 implementation.
 * @author whp
 */
public class MustangTray extends Plugin implements ChatListener, GameListener, ConnectionListener  {

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
     private Image normalIcon;

    /**
     * Move made icon.
     */
    private Image movedIcon;

    /**
     * Game end icon.
     */

    private Image gameEndIcon;
    
    /**
     * Game start icon.
     */
    
    private Image gameStartIcon;
    
    /**
     * Connection lost icon.
     */
    
    private Image connLostIcon;
    

    
    /**
     * Connection failed icon.
     */
    
    private Image connFailIcon;
    
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

    private ConnectionDetails lastDetails;

    /** Creates a new instance of TrayManager */
    public MustangTray() {
    }

    /**
     * Overriden from <code>free.jin.Plugin</code>.
     * @return id simple non-whitespace String.
     */
    @Override
    public String getId() {
        return "mustangtray";
    }

    /**
     * Overriden from <code>free.jin.Plugin</code>.
     * @return name normal String.
     */
    @Override
    public String getName() {
        return "Mustang Tray Manager";
    }

    /**
     * Starts the plugin first registering listeners and then creating tray and some icons.
     */
    @Override
    public void start(){
        if (SystemTray.isSupported() && getPrefs().getBool("display.tray") == true){
            registerListeners();
            initTrayMenu();
            createIcons();
            firstTime = true;
        }
        else{
            System.out.println("***WARNING*** SystemTray is unsupported here!");
        }

    }


    /**
     * Returns whether this plugin has a preferences panel it wants to be
     * displayed to the user. The default implementation returns
     * <code>false</code>.
     */

    @Override
    public boolean hasPreferencesUI() {
        return getPrefs().getBool("preferences.show", true); 
    }


    /**
     * Return a PreferencesPanel for changing the plugin's preferences. This
     * method will never be called if <code>hasPreferencesUI</code> returns false.
     * The default implementation throws an <code>IllegalStateException</code>,
     * since it's not supposed to be called.
     */

    @Override
    public PreferencesPanel getPreferencesUI() {
        return new MustangTrayPrefsPanel(this);
    }

    /**
     * Creates icon used later in tray.
     */

    private void createIcons() {

        //creation of normal application icon
        URL normalIconURL = MustangTray.class.getResource("normal.gif");
        try {
            normalIcon = ImageIO.read(normalIconURL);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        trayIcon = new TrayIcon(normalIcon, "Tonic - graphical chess interface"); //sets the initial icon for tray
        trayIcon.setImageAutoSize(true); //makes icon autoresizable - otherwise sometimes just a part of icon be visible
        
        //Learnt thanks to: http://weblogs.java.net/blog/ixmal/archive/2006/05/using_jpopupmen.html article
        trayIcon.addMouseListener(new MouseAdapter() {
        public void mouseReleased(MouseEvent e) {
            

                menu.setLocation(e.getX(), e.getY());
                menu.setInvoker(menu);
                menu.setVisible(true);
            
        }
    });
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Jin.getInstance().getUIProvider() instanceof MdiUiProvider){
                   ((MdiUiProvider)Jin.getInstance().getUIProvider()).restoreView();

                }
                trayIcon.setImage(normalIcon);

                iconChanged = false;
            }
        });
        


        
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
        
        
    }

    private Image getIconsEasy(String s) {

        
        URL imageURL = MustangTray.class.getResource(s);
        //Image image = null;

        Image image = Toolkit.getDefaultToolkit().getImage(imageURL);

        
        return image;
    }

    /**
     * This method initates the tray menu.
     */

    private void initTrayMenu() {
        menu = new JPopupMenu();
        quickQuit = new JMenuItem("QuickQuit");
        quickQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Jin.getInstance().quit(false);
            }
        });
        
        unseek = new JMenuItem("Cancel seeks");
        unseek.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConn().sendCommand("unseek");
            }
        });
        menu.add(unseek);
        menu.add(quickQuit);

        
        
            
        
    }

    public void chatMessageReceived(ChatEvent evt) {
    }
    
    public void gameStarted(GameStartEvent evt) {
            
        
                trayIcon.setImage(gameStartIcon);
                iconChanged = false;
        
        
    }

    /**
     * Method called when one of the users makes the move. It changes icon in the tray.
     * @param evt <code>MoveMadeEvent</code>
     */
    public void moveMade(MoveMadeEvent evt) {

       if (!(evt.getMovingPlayersName().equals(Jin.getInstance().getConnManager().getSession().getUser().getUsername()))){
            if (iconChanged == false){
                trayIcon.setImage(movedIcon);
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

        trayIcon.setImage(gameEndIcon);
        iconChanged = false;

    }
    
    public void connectionAttempted(Connection conn, String hostname, int port) {

    }
    
    public void connectionEstablished(Connection conn) {
        if (firstTime){
            tray = SystemTray.getSystemTray();
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
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

        trayIcon.setImage(connLostIcon);
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
        unregisterListeners();
        removeTrayIcon();
    }

    private void removeTrayIcon() {
        tray.remove(trayIcon);
    }

    private void unregisterListeners() {
    Connection conn = getConn();
    BasicListenerManager listenerManager = (BasicListenerManager)conn.getListenerManager();

    listenerManager.removeGameListener(this);
    listenerManager.removeChatListener(this);
    listenerManager.removeConnectionListener(this);
    }


}








