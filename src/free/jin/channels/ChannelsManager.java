/*
 * ChannelsManager.java
 *
 * Created on 17 marzec 2006, 11:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package free.jin.channels;

import free.freechess.FreechessConnection;

import free.jin.Connection;
import free.jin.Jin;
import free.jin.Preferences;

import free.jin.console.Console;

import free.jin.event.BasicListenerManager;
import free.jin.event.ChannelsEvent;
import free.jin.event.ChannelsListener;
import free.jin.event.ChatEvent;
import free.jin.event.ChatListener;
import free.jin.event.ConnectionListener;

import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;

import free.jin.ui.UIProvider;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;

import java.net.URL;

import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *
 * @author whp
 */

//TODO write javadoc for this class. Make it possible to choose between using JTabbedPane and PluginContainers. But first try to use PluginContainers for consoles.
//TODO make shout tabs aware of channel tabs changes
public class ChannelsManager extends Plugin implements ChannelsListener, ConnectionListener, ChatListener, ChangeListener {
    /**
     * The container for this plugin's ui.
     */
    private PluginUIContainer ui;
    
    /**
     * The button that sends to server command to get channel list for the user.
     */
    private JButton updateButton;
    
    /**
     * JTabbedPane for chatConsoles.
     */
    private JTabbedPane mainPane;
    
    /**
     * Panel with updateButton and updateLabel.
     */
    private JPanel updatePanel;
    
    /**
     * Label that explains what does the updateButton.
     */
    
    //TODO Should not it be done through tooltip?
    private JLabel updateLabel;
    
    /**
     * ConsoleManager preferences.
     */
    private Preferences consolePreferences;
    
    /**
     * Map of chatConsoles. Channels' numbers are the keys.
     */
    private Map chatConsoles;
    
    /**
     * Map of chatConsoles in mainPane. Values are indices of tabs in it.
     */
    private Map chatTabs;
    
    /**
     * Boolean value indicating that channels manager received information about user's channels for the first time.
     */
    private boolean firstTime;
    
    /**
     * Icon for new chat event notfication in tabs.
     */
    private Icon newChatIcon;
    
    /**
     * Icon for notification about direct tells in tabs.
     */
    private Icon directTellIcon;
    private Icon nullIcon;
    
    
    /**
     * Pop menu for each tab.
     */
    
    private PopupMenu channelMenu;
    
    /** Creates a new instance of ChannelsManager */
    public ChannelsManager() {
    }
    
    /**
     * Overriden from free.jin.plugin.Plugin
     * @return plugin's id (Simple string - without any spaces)
     */
    public String getId() {
        return "channels";
    }
    
    /**
     * Returns the name of the plugin. Overriden from free.jin.plugin.Plugin
     * @return plugin's name (normal string)
     */
    public String getName() {
        return "Channels Manager";
    }
    
    /**
     * Starts the plugin.
     */
    public void start() {
        chatConsoles = Collections.synchronizedMap(new TreeMap());
        chatTabs = new HashMap();
        
        ui = getPluginUIContainer();
        
        consolePreferences = getConsolePreferences();
        createIcons();
        getControls();
        //createChannelMenu();
        registerConnListeners();
    }
    
    /**
     * Method that readies all icons need for this plugin.
     */
    private void createIcons() {
        newChatIcon = getIconsEasy("newChatEvent.png");
        directTellIcon = getIconsEasy("directTell.png");
        nullIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
    }
    
    private Icon getIconsEasy(String s) {
        URL iconURL = ChannelsManager.class.getResource(s);
        Icon icon = new ImageIcon(iconURL);
        
        return icon;
    }
    
    private Preferences getConsolePreferences() {
        Plugin consolePlugin = getPlugin("console");
        Preferences preferences = consolePlugin.getPrefs();
        
        return preferences;
    }
    
    /**
     * Registers all Listeners for this plugin.
     */
    private void registerConnListeners() {
        Connection conn = getConn();
        BasicListenerManager listenerManager = (BasicListenerManager) conn.getListenerManager();
        
        listenerManager.addChannelsListener(this);
        listenerManager.addConnectionListener(this);
        listenerManager.addChatListener(this);
        mainPane.addChangeListener(this);
    }
    
    /**
     * Gets plugin's ui container according to the current type of DI.
     * @return plugin's ui container
     */
    private PluginUIContainer getPluginUIContainer() {
        PluginUIContainer container = createContainer("channels", UIProvider.HIDEABLE_CONTAINER_MODE);
        
        return container;
    }
    
    /**
     * This method updates the ui for displaying updated channel list.
     * @param remove = indicates wether channel should be removed (true) or added (false)
     * @param evt = strores all needed data about channel that needs to be added or removed
     */
    private synchronized void updateChannelsView(boolean remove, ChannelsEvent evt) {
        ArrayList keys = new ArrayList(chatConsoles.keySet());
        Collections.sort(keys);
        
        Iterator iterator = keys.iterator();
        
        //mainPane.removeAll();
        //mainPane.updateUI();
        //int i = 0;
        
        while (iterator.hasNext()) {
            Integer nextKey = (Integer) iterator.next();
            
            if (chatConsoles.containsKey(new Integer(evt.getChannelNumber())) && remove) {
                if (nextKey.equals(new Integer(evt.getChannelNumber()))) {
                    //System.out.println(">>>NEXT_KEY = " + nextKey + " INDEX = " + i);
                    mainPane.remove((Component) chatConsoles.remove(new Integer(evt.getChannelNumber())));
                    //chatTabs.remove(chatConsoles.get(new Integer(evt.getChannelNumber())));
                    //chatConsoles.remove(new Integer(evt.getChannelNumber()));
                    
                    /*Console oldShoutConsole = (Console) chatConsoles.get(new Integer(500));
                    Console oldCShoutConsole = (Console) chatConsoles.get(new Integer(501));
                    chatTabs.remove(oldShoutConsole);
                    chatTabs.remove(oldCShoutConsole);
                    chatTabs.put(oldShoutConsole, new Integer(chatConsoles.size() - 2));
                    chatTabs.put(oldCShoutConsole, new Integer(chatConsoles.size() - 1));*/
                    //updateMenus();
                    mainPane.updateUI();
                }
            }
            
            if (!chatConsoles.containsKey(new Integer(evt.getChannelNumber())) && !remove) {
                int index;
                
                for (int i1 = keys.size() - 1; i1 >= 0; i1--) {
                    //System.out.println("KEY_VALUE = " + keys.get(i1) + " CHANNEL_NR = " + evt.getChannelNumber());
                    if (evt.getChannelNumber() > ((Integer) keys.get(i1)).intValue()) {
                        index = i1 + 1;
                        
                        Console addConsole = new Console(getConn(), consolePreferences, ("tell " + Integer.toString(evt.getChannelNumber()) + " "));
                        chatConsoles.put(new Integer(evt.getChannelNumber()), addConsole);
                        
                        //chatTabs.put(addConsole, new Integer(index));
                        
                        /*Console oldShoutConsole = (Console) chatConsoles.get(new Integer(500));
                        Console oldCShoutConsole = (Console) chatConsoles.get(new Integer(501));
                        chatTabs.remove(oldShoutConsole);
                        chatTabs.remove(oldCShoutConsole);
                        chatTabs.put(oldShoutConsole, new Integer(chatConsoles.size() - 2));
                        chatTabs.put(oldCShoutConsole, new Integer(chatConsoles.size() - 1));*/
                        
                        //System.out.println("INSERTING TAB AT INDEX: " + index);
                        mainPane.insertTab(Integer.toString(evt.getChannelNumber()), nullIcon, (Console) chatConsoles.get(new Integer(evt.getChannelNumber())), null, index);
                        mainPane.updateUI();
                        break;
                    } else if (i1<1){
                        //for (int i2 = 1; i2 < (keys.size() - 3); i++) {
                        //if (evt.getChannelNumber() < ((Integer) keys.get(i2)).intValue()) {
                        index = 0;
                        
                        Console addConsole = new Console(getConn(), consolePreferences, ("tell " + Integer.toString(evt.getChannelNumber()) + " "));
                        chatConsoles.put(new Integer(evt.getChannelNumber()), addConsole);
                        
                        //chatTabs.put(addConsole, new Integer(index));
                        
                        /*Console oldShoutConsole = (Console) chatConsoles.get(new Integer(500));
                        Console oldCShoutConsole = (Console) chatConsoles.get(new Integer(501));
                        chatTabs.remove(oldShoutConsole);
                        chatTabs.remove(oldCShoutConsole);
                        chatTabs.put(oldShoutConsole, new Integer(chatConsoles.size() - 2));
                        chatTabs.put(oldCShoutConsole, new Integer(chatConsoles.size() - 1));*/
                        
                        mainPane.insertTab(Integer.toString(evt.getChannelNumber()), nullIcon, (Console) chatConsoles.get(new Integer(evt.getChannelNumber())), null, index);
                        mainPane.updateUI();
                        break;
                        //}
                        //}
                    }
                    
                    //mainPane.updateUI();
                    //mainPane.setVisible(true);
                }
            }
            
            
        }
    }
    
    /**
     * Method overriden from ChannelsListener
     * @param evt
     */
    public void channelRemoved(ChannelsEvent evt) {
        //chatConsoles.remove(new String(Integer.toString(evt.getChannelNumber())));
        updateChannelsView(true, evt);
    }
    
    /**
     * Method overriden from ChannelsListener. Get called when indication that channel was added to user's channel list.
     * @param evt
     */
    public void channelAdded(ChannelsEvent evt) {
        //Console addConsole = new Console(getConn(), consolePreferences);
        //chatConsoles.put(new Integer(evt.getChannelNumber()), addConsole);
        //mainPane.setVisible(false);
        //mainPane.addTab(Integer.toString(evt.getChannelNumber()), (Console)chatConsoles.get(new Integer(evt.getChannelNumber())) );
        //chatConsoles.put(new Integer(evt.getChannelNumber()), new Console(getConn(), consolePreferences) );
        updateChannelsView(false, evt);
    }
    
    /**
     * This method is called when channel list arrives from server. It fills JTabbedPane with tabs of channels and 
     * special tabs for shouts, ishouts and chess shouts.
     */
    public void channelListReceived(ChannelsEvent evt) {
        int[] channels = evt.getChannelsNumbers();
        
       //if (firstTime) {
            
            
            for (int i = 0; i < channels.length; i++) {
                Console chatConsole = new Console(getConn(), consolePreferences, ("tell " + channels[i] + " "));
                chatConsoles.put(new Integer(channels[i]), chatConsole);
                //chatTabs.put(chatConsole, new Integer(i));
                mainPane.addTab(Integer.toString(channels[i]), nullIcon, chatConsole);
                
                
            }
            if (firstTime){
            for (int j = 0; j < 2; j++) {
                if (j == 0) {
                    Console chatConsole = new Console(getConn(), consolePreferences, "shout ");
                    chatConsoles.put(new Integer(500), chatConsole);
                    //chatTabs.put(chatConsole, new Integer(chatConsoles.size() - 1));
                    mainPane.addTab("shouts", nullIcon, chatConsole);
                } else {
                    Console chatConsole = new Console(getConn(), consolePreferences, "cshout ");
                    chatConsoles.put(new Integer(501), chatConsole);
                    //chatTabs.put(chatConsole, new Integer(chatConsoles.size() - 1));
                    mainPane.addTab("cshouts", nullIcon, chatConsole);
                }
            }
            
            
            firstTime = false;
            } else {}
       // } else {
       // }
    }
    
    private void getControls() {
        mainPane = new JTabbedPane();
        
        /*updatePanel = new JPanel();
        updatePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        updateLabel = new JLabel("Click Update button to get channel list.");
         
        updateButton = new JButton("Update channel list");
        updateButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ChannelsConnection channelsConn = (ChannelsConnection) getConn();
                channelsConn.updateChannelsList();
                if (listArrived){
                System.out.println(channelButtonSize.getHeight() + channelButtonSize.getWidth());
                }
            }
        });*/
        ui.getContentPane().setLayout(new BorderLayout());
        /*updatePanel.add(updateLabel);
        updatePanel.add(updateButton);*/
        
        //ui.getContentPane().add(updatePanel, BorderLayout.SOUTH);
        ui.setTitle(getName());
        ui.getContentPane().add(mainPane, BorderLayout.CENTER);
    }
    
    /**
     * This method get called when channel tell arrives from the server.
     * @param evt
     */
    public void chatMessageReceived(ChatEvent evt) {
        String chatType = evt.getType();
        //System.out.println(chatType);
        //System.out.println(chatType.matches("(shout)|(ishout)|(cshout)"));
        
        Object channelNumber = evt.getForum();
        
        if ((channelNumber != null && chatType.equals("channel-tell")) || chatType.matches("(shout)|(cshout)|(ishout)")) {
            String channelName = null;
            
            if (channelNumber != null) {
                channelName = channelNumber.toString();
            }
            
            //System.out.println("channelNumber = " + channelName);
            String message = evt.getMessage();
            
            //System.out.println("channelMessage = " + message);
            Console receivingConsole;
            
            if (chatType.equals("shout") || chatType.equals("ishout")) {
                receivingConsole = (Console) chatConsoles.get(new Integer(500));
            } else if (chatType.equals("cshout")) {
                receivingConsole = (Console) chatConsoles.get(new Integer(501));
            } else {
                receivingConsole = (Console) chatConsoles.get(new Integer(Integer.parseInt(channelName)));
            }
            
            Console selectedConsole = (Console) mainPane.getSelectedComponent();
            
            if (!receivingConsole.equals(selectedConsole)) {
                Integer index = null;
                //Integer index = (Integer) chatTabs.get(receivingConsole);
                if (chatType.matches("(shout)|(ishout)") == true) {
                    //System.out.println("CHAT TYPE = " + chatType);
                    index = new Integer((mainPane.getTabCount()-2));
                    
                }
                else if (chatType.equals("cshout") == true){
                    index = new Integer((mainPane.getTabCount()-1));
                    
                } else{
                    for (int k = 0; k < mainPane.getTabCount(); k++){
                        if ((mainPane.getTitleAt(k)).equals(channelNumber.toString())){
                            index = new Integer(k);
                            break;
                        }
                        
                    }
                }
                    //System.out.println("INDEX = " + index);
                    /*mainPane.setTitleAt(index.intValue(),  "*"+channelName);*/
                    //mainPane.setBackgroundAt(index.intValue(), UIManager.getDefaults().getColor("TabRenderer.selectedActivatedBackgroud"));
                    //mainPane.setBackgroundAt(index.intValue(), new Color(rnd.nextInt(128) + 127, rnd.nextInt(128), rnd.nextInt(128)));
                    // if (index != null){
                    if (message.indexOf(Jin.getInstance().getConnManager().getSession().getUser().getUsername()) == -1) {
                        mainPane.setIconAt(index.intValue(), newChatIcon);
                    } else {
                        mainPane.setIconAt(index.intValue(), directTellIcon);
                    }
                    // }
                }
            
            
            String type = evt.getType();
            Object forum = evt.getForum();
            String sender = evt.getSender();
            String chatMessageType = type + "." + ((forum == null) ? "" : forum.toString()) + "." + sender;
            //System.out.println(">>>TEST" + receivingConsole.toString() + sender + chatMessageType);
            receivingConsole.addToOutput(translateChat(evt), chatMessageType);
        }
    }
    
    /**
     * Gets called when a connection attempt is made.
     */
    public void connectionAttempted(Connection conn, String hostname, int port) {
    }
    
    /**
     * Gets called when the connection to the server is established.
     */
    public void connectionEstablished(Connection conn) {
    }
    
    /**
     * Gets called when the connection attempt failed.
     */
    public void connectingFailed(Connection conn, String reason) {
    }
    
    /**
     * Gets called when the login procedure is successful. Here it sends inch <user_name> command to the server.
     */
    public void loginSucceeded(Connection conn) {
    	((FreechessConnection) getConn()).sendCommFromPlugin("=ch");
    	//getConn().sendCommand("inch " + getConn().getUsername());
        firstTime = true;
        //
    }
    
    /**
     * Gets called when the login procedure fails. Note that <code>reason</code> may be null.
     */
    public void loginFailed(Connection conn, String reason) {
    }
    
    /**
     * Gets called when the connection to the server is lost.
     */
    public void connectionLost(Connection conn) {
    	mainPane.removeAll();
    	ui.dispose();
    	chatConsoles.clear();
    	firstTime = false;
    }
    
    protected String translateChat(ChatEvent evt) {
        String type = evt.getType();
        String sender = evt.getSender();
        String title = evt.getSenderTitle();
        String rating = (evt.getSenderRating() == -1) ? "----" : String.valueOf(evt.getSenderRating());
        String message = evt.getMessage();
        Object forum = evt.getForum();
        
        // Tells
        if (type.equals("tell")) {
            return sender + title + " tells you: " + message;
        } else if (type.equals("say")) {
            return sender + title + " says: " + message;
        } else if (type.equals("ptell")) {
            return sender + title + " (your partner) tells you: " + message;
        } else if (type.equals("qtell")) {
            return ":" + message;
        } else if (type.equals("qtell.tourney")) {
            return ":" + sender + title + "(T" + forum + "): " + message;
        }
        // Channel tells
        else if (type.equals("channel-tell")) {
            return sender + title + "(" + forum + "): " + message;
        }
        // Kibitzes and whispers
        else if (type.equals("kibitz")) {
            return sender + title + "(" + rating + ")[" + forum + "] kibitzes: " + message;
        } else if (type.equals("whisper")) {
            return sender + title + "(" + rating + ")[" + forum + "] whispers: " + message;
        }
        // Shouts
        else if (type.equals("shout")) {
            return sender + title + " shouts: " + message;
        } else if (type.equals("ishout")) {
            return "--> " + sender + title + " " + message;
        } else if (type.equals("tshout")) {
            return ":" + sender + title + " t-shouts: " + message;
        } else if (type.equals("cshout")) {
            return sender + title + " c-shouts: " + message;
        } else if (type.equals("announcement")) {
            return "    **ANNOUNCEMENT** from " + sender + ": " + message;
        }
        
        return evt.toString();
    }
    
    /**
     * Removes icon from tab when it is selected.
     *
     * @param e ChangeEvent
     */
    public void stateChanged(ChangeEvent e) {
        //mainPane.setBackgroundAt(mainPane.getSelectedIndex(), UIManager.getDefaults().getColor("control"));
        mainPane.setIconAt(mainPane.getSelectedIndex(), nullIcon);
        
        //String title = mainPane.getTitleAt(mainPane.getSelectedIndex()).replace("*", "");
        //mainPane.setTitleAt(mainPane.getSelectedIndex(), title );
    }

    /*private void updateMenus() {
        for (int i = 0; i < mainPane.getTabCount(); i++){
            mainPane.getComponentAt(i).addMouseListener(new MouseAdapter(){
                
            });
        }
    }

    private void createChannelMenu() {
        channelMenu = new PopupMenu();
        JMenuItem remove = new JMenuItem("Remove channel");
        remove.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
             
            }
            
        });
    }*/
}
