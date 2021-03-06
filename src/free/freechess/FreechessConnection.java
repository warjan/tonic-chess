/**
 * The freechess.org connection library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The freechess.org connection library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The freechess.org connection library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the freechess.org connection library; if not, write to the Free
 * Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.freechess;

import free.util.Connection;

import java.io.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <P>This class implements an easy way to communicate with a freechess.org
 * server. It provides parsing of messages sent by the server and allows
 * receiving notifications of various events in an easy manner.
 * <P>Information usually arrives and is parsed/processed line by line.
 * Usage usually involves overriding one of the many
 * <code>processXXX(<arguments>)</code> methods and handling the arrived
 * information. The boolean value returned by the
 * <code>processXXX(<arguments>)</code> methods determines whether the arrived
 * information has been processed completely and shouldn't be processed any
 * further. Currently, returning <code>false</code> will mean that the line sent
 * by the server will be sent to the <code>processLine(String)</code> method as
 * well, but in the future "further processing" might include other procedures.
 * Since by default, all the <code>processXXX(parsed data)</code> methods return
 * <code>false</code>, the information usually handled by any methods you don't
 * override will end up in <code>processLine(String)</code> which you can easily
 * use for printing the output to the screen or a file.
 */



public class FreechessConnection extends Connection{



  /**
   * A regular expression string matching a FICS username.
   */

  protected static final String USERNAME_REGEX = "[A-z]{3,17}";



  /**
   * A regular expression string for matching FICS titles.
   */

  protected static final String TITLES_REGEX = "\\([A-Z\\*\\(\\)]*\\)";
  
  
  
  /**
   * The stream where we log the commands sent to the server and data arriving from the server.
   */
  
  private final PrintStream logStream;



  /**
   * The current board sending "style".
   */

  private int style = 1;



  /**
   * A BitSet keeping the requested state of ivariables. This may be
   * inconsistent with the server's idea of their state as the message might
   * have yet to arrive.
   *
   * @see #ivarStates
   */

  private final BitSet requestedIvarStates = new BitSet();



  /**
   * A BitSet keeping the (true) state of ivariables. We set this to a non-null
   * value once we send the requestedIvarStates on the login line.
   */

  private BitSet ivarStates = null;



  /**
   * A HashMap of Strings specifying lines that need to be filtered out.
   */

  private final HashMap linesToFilter = new HashMap();



  /**
   * The value we're supposed to assign to the interface variable during login.
   */

  private String interfaceVar = "Java freechess.org library by Alexander Maryanovsky";
  
  /**
   * Boolean that indicates command was sent from one of plugins.
   */
  
  protected boolean fromPlugin;

    //TODO: Make it dependant on user's titles. And other limits as well.
    /**
     * Number that hold the limit of characters that can be send to server. Otherwise server disconnects user.
     * I consider this a bug.
     */
    private int characterLimit = 200;



    /**
   * Creates a new <code>FreechessConnection</code> with the given requested 
   * username, password and optional log stream. Note that the actual
   * username is assigned by the server and is not known until after the
   * login. The echo stream, if not <code>null</code> is used to log any
   * commands we send to the server and information we receive from it.
   */

  public FreechessConnection(String requestedUsername, String password, PrintStream logStream){
    super(requestedUsername, password);
    
    this.logStream = logStream;
    
    this.fromPlugin = false;

    setIvarState(Ivar.NOWRAP, true);
    setIvarState(Ivar.DEFPROMPT, true); // Sets it to the default, which we filter out.
    setIvarState(Ivar.MS, true);
    setIvarState(Ivar.NOHIGHLIGHT, true);
  }




  /**
   * Adds the specified String to the list of lines that will be filtered.
   * The next time this string is received (as a line), it will not be sent to
   * the <code>processLine</code> method. Note that this only works for the
   * first occurrance of the specified string.
   */

  public void filterLine(String line){
    linesToFilter.put(line, line);
  }



  /**
   * Sets the specified ivar on or off. If we're already logged in, a
   * <code>set-2 [DG number] [0/1]</code> string is sent to the server,
   * otherwise the setting is saved, and in the login procedure all 
   * the ivar settings are sent on the login line in the
   * <code>%b0011011011...</code> format. You may call this method to set (or
   * unset) an ivar that has already been set (or unset), which will result in
   * the appropriate message being send again.
   * Note that some ivars are necessary for the correct behaviour of this class,
   * and their states may not be modified (defprompt for example).
   *
   * @param ivar The ivar.
   * @param state Whether set the variable on or off.
   *
   * @return whether the state of the variable was modified successfully. This
   * always returns true when setting an ivar on, and only returns false
   * when trying to set an essential datagram off.
   *
   * @see #isEssentialIvar(Ivar)
   * @see #getIvarState(Ivar)
   */

  public final synchronized boolean setIvarState(Ivar ivar, boolean state){
    if ((state == false) && isEssentialIvar(ivar))
      return false;

    if (state)
      requestedIvarStates.set(ivar.getIndex());
    else
      requestedIvarStates.clear(ivar.getIndex());

    // if this is called after the ivars have been sent on the login line,
    // but before we're fully logged in yet, the settings will be fixed in
    // onLogin(). We don't do it here because it's not a good idea to send
    // anything in the middle of the login procedure.
    if (isLoggedIn()){
      sendCommand("$$iset "+ivar.getName()+" "+(state ? "1" : "0"));
      filterLine(ivar.getName()+" "+(state ? "" : "un")+"set.");
    }

    return true;
  }



  /**
   * Returns the current state of the specified ivar as far as data arriving
   * from the server is concerned. Note that calls to
   * {@link #setIvarState(Ivar, boolean)} will be reflected by this method only
   * when the server echoes that it acknowledged the setting of the ivar.
   */

  public final synchronized boolean getIvarState(Ivar ivar){
    return (ivarStates == null ? requestedIvarStates : ivarStates).get(ivar.getIndex());
  }



  /**
   * Returns the current "requested" state of the specified ivar, the state as
   * it has been requested by the last command sent to the server (regarding
   * this ivar).
   */

  public final synchronized boolean getRequestedIvarState(Ivar ivar){
    return requestedIvarStates.get(ivar.getIndex());
  }



  /**
   * Returns true if the specified ivar is essential and can't be set off.
   * When overriding this method, don't forget to call the superclass' method.
   */

  protected boolean isEssentialIvar(Ivar ivar){
    return ((ivar == Ivar.NOWRAP) ||
            (ivar == Ivar.MS) ||
            (ivar == Ivar.NOHIGHLIGHT));
  }



  /**
   * Sets the style. If the ChessclubConnection is already logged in, then
   * a "set style <style>" command is send immediately, otherwise, the setting
   * is saved and sent immediately after logging in.
   */

  public final synchronized void setStyle(int style){
    this.style = style;

    if (isLoggedIn()){
      sendCommand("$set style "+style);
      filterLine("Style "+style+" set.");
    }
  }



  /**
   * Sets the interface variable to have the given value. This works only if the
   * FreechessConnection is not logged on yet, otherwise, throws an 
   * IllegalArgumentException. The actual interface variable will be set during 
   * the login procedure.
   */

  public final synchronized void setInterface(String interfaceVar){
    if (isLoggedIn())
      throw new IllegalStateException();

    this.interfaceVar = interfaceVar;
  }



  /**
   * Invoked when a connection to the server is established. Sends ivar settings to the server.
   */
  
  protected void handleConnected(){
    sendCommand(createLoginIvarsSettingString(requestedIvarStates));
    ivarStates = (BitSet)requestedIvarStates.clone();
    
    super.handleConnected();
  }
  
  
  
  /**
   * Sends the login information to the server.
   */

  protected void sendLoginSequence(){
    sendCommand(getRequestedUsername());
    if (getPassword() != null)
      sendCommand(getPassword(), false);
  }



  /**
   * Creates the string we send on the login line to set ivars.
   */

  private static String createLoginIvarsSettingString(BitSet ivars){
    StringBuffer buf = new StringBuffer();
    buf.append("%b");

    int largestSetIvarIndex = ivars.size();
    while ((largestSetIvarIndex >= 0) && !ivars.get(largestSetIvarIndex))
      largestSetIvarIndex--;

    for (int i = 0; i <= largestSetIvarIndex; i++)
      buf.append(ivars.get(i) ? "1" : "0");

    return buf.toString();
  }



  /**
   * Sets the various things we need to set on login.
   */

  protected void handleLoginSucceeded(){
    super.handleLoginSucceeded();

    // Apply any ivar changes which might have occurred when we were waiting
    // for login.
    synchronized(this){

      // Workaround: the server won't send us the full seek list if we set seekinfo
      // on the login line.
      if (ivarStates.get(Ivar.SEEKINFO.getIndex())){
        sendCommand("$$iset seekinfo 1");
        filterLine("seekinfo set.");
      }


      for (int i = 0; i < requestedIvarStates.size(); i++){
        boolean state = requestedIvarStates.get(i);
        Ivar ivar = Ivar.getByIndex(i);
        if (state != ivarStates.get(i)){
          sendCommand("$$iset "+ivar.getName()+" "+(state ? "1" : "0"));
          filterLine(ivar.getName()+" "+(state ? "" : "un")+"set.");
        }
      }

      sendCommand("$set style "+style);
      filterLine("Style "+style+" set.");

      sendCommand("$set interface "+interfaceVar);

      sendCommand("$set ptime 0");
      filterLine("Your prompt will now not show the time.");
    }
  }


  
  /**
   * Sends the specified command to the server.
   */
   
  public void sendCommand(String command){
    sendCommand(command, true);
      fromPlugin = false;

  }
  
  
  
  /**
   * Sends the given command to the server, optionally echoing it to System.out.
   */

  public synchronized void sendCommand(String command, boolean echo){
    if (!isConnected())
      throw new IllegalStateException("Not connected");

    if (echo)
      System.out.println("SENDING COMMAND: " + command);

    if (command.length() > this.characterLimit && isLoggedIn()){
        System.out.println("Too many character in your command!");    
    }else{

    try{
      OutputStream out = getOutputStream();
        for (int i = 0; i < command.length(); i++){
            char ch = command.charAt(i);
            if (ch > 127){
                switch(ch){
                    case '\u017A': command = command.replace(ch, '\u007A'); break;
                    case '\u017C': command = command.replace(ch, '\u007A'); break;
                    case '\u017E': command = command.replace(ch, '\u007A'); break;
                    case '\u00F2': command = command.replace(ch, '\u006F'); break;
                    case '\u00F3': command = command.replace(ch, '\u006F'); break;
                    case '\u00F4': command = command.replace(ch, '\u006F'); break;
                    case '\u00F5': command = command.replace(ch, '\u006F'); break;
                    case '\u00F6': command = command.replace(ch, '\u006F'); break;
                    case '\u00F8': command = command.replace(ch, '\u006F'); break;
                    case '\u014D': command = command.replace(ch, '\u006F'); break;
                    case '\u014F': command = command.replace(ch, '\u006F'); break;
                    case '\u0151': command = command.replace(ch, '\u006F'); break;
                    case '\u00DD': command = command.replace(ch, '\u0059'); break;
                    case '\u0176': command = command.replace(ch, '\u0059'); break;
                    case '\u0178': command = command.replace(ch, '\u0059'); break;
                    case '\u015A': command = command.replace(ch, '\u0053'); break;
                    case '\u015C': command = command.replace(ch, '\u0053'); break;
                    case '\u015E': command = command.replace(ch, '\u0053'); break;
                    case '\u0160': command = command.replace(ch, '\u0053'); break;
                    case '\u0154': command = command.replace(ch, '\u0052'); break;
                    case '\u0156': command = command.replace(ch, '\u0052'); break;
                    case '\u0158': command = command.replace(ch, '\u0052'); break;
                    case '\u0174': command = command.replace(ch, '\u0057'); break;
                    case '\u00D9': command = command.replace(ch, '\u0055'); break;
                    case '\u00DA': command = command.replace(ch, '\u0055'); break;
                    case '\u00DB': command = command.replace(ch, '\u0055'); break;
                    case '\u00DC': command = command.replace(ch, '\u0055'); break;
                    case '\u0168': command = command.replace(ch, '\u0055'); break;
                    case '\u016A': command = command.replace(ch, '\u0055'); break;
                    case '\u016C': command = command.replace(ch, '\u0055'); break;
                    case '\u016E': command = command.replace(ch, '\u0055'); break;
                    case '\u0170': command = command.replace(ch, '\u0055'); break;
                    case '\u0172': command = command.replace(ch, '\u0055'); break;
                    case '\u00DE': command = command.replace(ch, '\u0054'); break;
                    case '\u0162': command = command.replace(ch, '\u0054'); break;
                    case '\u0164': command = command.replace(ch, '\u0054'); break;
                    case '\u0166': command = command.replace(ch, '\u0054'); break;
                    case '\u011D': command = command.replace(ch, '\u0067'); break;
                    case '\u011F': command = command.replace(ch, '\u0067'); break;
                    case '\u0121': command = command.replace(ch, '\u0067'); break;
                    case '\u0123': command = command.replace(ch, '\u0067'); break;
                    case '\u010F': command = command.replace(ch, '\u0064'); break;
                    case '\u0111': command = command.replace(ch, '\u0064'); break;
                    case '\u0131': command = command.replace(ch, '\u0064'); break;
                    case '\u00E8': command = command.replace(ch, '\u0065'); break;
                    case '\u00E9': command = command.replace(ch, '\u0065'); break;
                    case '\u00EA': command = command.replace(ch, '\u0065'); break;
                    case '\u00EB': command = command.replace(ch, '\u0065'); break;
                    case '\u0113': command = command.replace(ch, '\u0065'); break;
                    case '\u0115': command = command.replace(ch, '\u0065'); break;
                    case '\u0117': command = command.replace(ch, '\u0065'); break;
                    case '\u0119': command = command.replace(ch, '\u0065'); break;
                    case '\u011B': command = command.replace(ch, '\u0065'); break;
                    case '\u00E7': command = command.replace(ch, '\u0063'); break;
                    case '\u0107': command = command.replace(ch, '\u0063'); break;
                    case '\u0109': command = command.replace(ch, '\u0063'); break;
                    case '\u010B': command = command.replace(ch, '\u0063'); break;
                    case '\u010D': command = command.replace(ch, '\u0063'); break;
                    case '\u00E0': command = command.replace(ch, '\u0061'); break;
                    case '\u00E1': command = command.replace(ch, '\u0061'); break;
                    case '\u00E2': command = command.replace(ch, '\u0061'); break;
                    case '\u00E3': command = command.replace(ch, '\u0061'); break;
                    case '\u00E4': command = command.replace(ch, '\u0061'); break;
                    case '\u00E5': command = command.replace(ch, '\u0061'); break;
                    case '\u0101': command = command.replace(ch, '\u0061'); break;
                    case '\u0103': command = command.replace(ch, '\u0061'); break;
                    case '\u0105': command = command.replace(ch, '\u0061'); break;
                    case '\u0134': command = command.replace(ch, '\u004A'); break;
                    case '\u0136': command = command.replace(ch, '\u004B'); break;
                    case '\u0139': command = command.replace(ch, '\u004C'); break;
                    case '\u013B': command = command.replace(ch, '\u004C'); break;
                    case '\u013D': command = command.replace(ch, '\u004C'); break;
                    case '\u013F': command = command.replace(ch, '\u004C'); break;
                    case '\u0141': command = command.replace(ch, '\u004C'); break;
                    case '\u00D1': command = command.replace(ch, '\u004E'); break;
                    case '\u0143': command = command.replace(ch, '\u004E'); break;
                    case '\u0145': command = command.replace(ch, '\u004E'); break;
                    case '\u0147': command = command.replace(ch, '\u004E'); break;
                    case '\u00D2': command = command.replace(ch, '\u004F'); break;
                    case '\u00D3': command = command.replace(ch, '\u004F'); break;
                    case '\u00D4': command = command.replace(ch, '\u004F'); break;
                    case '\u00D5': command = command.replace(ch, '\u004F'); break;
                    case '\u00D6': command = command.replace(ch, '\u004F'); break;
                    case '\u00D8': command = command.replace(ch, '\u004F'); break;
                    case '\u014C': command = command.replace(ch, '\u004F'); break;
                    case '\u014E': command = command.replace(ch, '\u004F'); break;
                    case '\u0150': command = command.replace(ch, '\u004F'); break;
                    case '\u00EC': command = command.replace(ch, '\u0069'); break;
                    case '\u00ED': command = command.replace(ch, '\u0069'); break;
                    case '\u00EE': command = command.replace(ch, '\u0069'); break;
                    case '\u00EF': command = command.replace(ch, '\u0069'); break;
                    case '\u0129': command = command.replace(ch, '\u0069'); break;
                    case '\u012B': command = command.replace(ch, '\u0069'); break;
                    case '\u012D': command = command.replace(ch, '\u0069'); break;
                    case '\u012F': command = command.replace(ch, '\u0069'); break;
                    case '\u0124': command = command.replace(ch, '\u0048'); break;
                    case '\u0126': command = command.replace(ch, '\u0048'); break;
                    case '\u00CC': command = command.replace(ch, '\u0049'); break;
                    case '\u00CD': command = command.replace(ch, '\u0049'); break;
                    case '\u00CE': command = command.replace(ch, '\u0049'); break;
                    case '\u00CF': command = command.replace(ch, '\u0049'); break;
                    case '\u0128': command = command.replace(ch, '\u0049'); break;
                    case '\u012A': command = command.replace(ch, '\u0049'); break;
                    case '\u012C': command = command.replace(ch, '\u0049'); break;
                    case '\u012E': command = command.replace(ch, '\u0049'); break;
                    case '\u0130': command = command.replace(ch, '\u0049'); break;
                    case '\u00F1': command = command.replace(ch, '\u006E'); break;
                    case '\u0144': command = command.replace(ch, '\u006E'); break;
                    case '\u0146': command = command.replace(ch, '\u006E'); break;
                    case '\u0148': command = command.replace(ch, '\u006E'); break;
                    case '\u0149': command = command.replace(ch, '\u006E'); break;
                    case '\u0137': command = command.replace(ch, '\u006B'); break;
                    case '\u013A': command = command.replace(ch, '\u006C'); break;
                    case '\u013C': command = command.replace(ch, '\u006C'); break;
                    case '\u013E': command = command.replace(ch, '\u006C'); break;
                    case '\u0140': command = command.replace(ch, '\u006C'); break;
                    case '\u0142': command = command.replace(ch, '\u006C'); break;
                    case '\u017F': command = command.replace(ch, '\u006C'); break;
                    case '\u0135': command = command.replace(ch, '\u006A'); break;
                    case '\u00C0': command = command.replace(ch, '\u0041'); break;
                    case '\u00C1': command = command.replace(ch, '\u0041'); break;
                    case '\u00C2': command = command.replace(ch, '\u0041'); break;
                    case '\u00C3': command = command.replace(ch, '\u0041'); break;
                    case '\u00C4': command = command.replace(ch, '\u0041'); break;
                    case '\u00C5': command = command.replace(ch, '\u0041'); break;
                    case '\u00C6': command = command.replace(ch, '\u0041'); break;
                    case '\u0100': command = command.replace(ch, '\u0041'); break;
                    case '\u0102': command = command.replace(ch, '\u0041'); break;
                    case '\u0104': command = command.replace(ch, '\u0041'); break;
                    case '\u00C7': command = command.replace(ch, '\u0043'); break;
                    case '\u0106': command = command.replace(ch, '\u0043'); break;
                    case '\u0108': command = command.replace(ch, '\u0043'); break;
                    case '\u010A': command = command.replace(ch, '\u0043'); break;
                    case '\u010C': command = command.replace(ch, '\u0043'); break;
                    case '\u010E': command = command.replace(ch, '\u0044'); break;
                    case '\u0110': command = command.replace(ch, '\u0044'); break;
                    case '\u00C8': command = command.replace(ch, '\u0045'); break;
                    case '\u00C9': command = command.replace(ch, '\u0045'); break;
                    case '\u00CA': command = command.replace(ch, '\u0045'); break;
                    case '\u00CB': command = command.replace(ch, '\u0045'); break;
                    case '\u00D0': command = command.replace(ch, '\u0045'); break;
                    case '\u0112': command = command.replace(ch, '\u0045'); break;
                    case '\u0114': command = command.replace(ch, '\u0045'); break;
                    case '\u0116': command = command.replace(ch, '\u0045'); break;
                    case '\u0118': command = command.replace(ch, '\u0045'); break;
                    case '\u011A': command = command.replace(ch, '\u0045'); break;
                    case '\u014A': command = command.replace(ch, '\u0045'); break;
                    case '\u011C': command = command.replace(ch, '\u0047'); break;
                    case '\u011E': command = command.replace(ch, '\u0047'); break;
                    case '\u0120': command = command.replace(ch, '\u0047'); break;
                    case '\u0122': command = command.replace(ch, '\u0047'); break;
                    case '\u00FE': command = command.replace(ch, '\u0074'); break;
                    case '\u0163': command = command.replace(ch, '\u0074'); break;
                    case '\u0165': command = command.replace(ch, '\u0074'); break;
                    case '\u0167': command = command.replace(ch, '\u0074'); break;
                    case '\u00DF': command = command.replace(ch, '\u0073'); break;
                    case '\u015B': command = command.replace(ch, '\u0073'); break;
                    case '\u015D': command = command.replace(ch, '\u0073'); break;
                    case '\u015F': command = command.replace(ch, '\u0073'); break;
                    case '\u0161': command = command.replace(ch, '\u0073'); break;
                    case '\u0155': command = command.replace(ch, '\u0072'); break;
                    case '\u0157': command = command.replace(ch, '\u0072'); break;
                    case '\u0159': command = command.replace(ch, '\u0072'); break;
                    case '\u00F9': command = command.replace(ch, '\u0075'); break;
                    case '\u00FA': command = command.replace(ch, '\u0075'); break;
                    case '\u00FB': command = command.replace(ch, '\u0075'); break;
                    case '\u00FC': command = command.replace(ch, '\u0075'); break;
                    case '\u0169': command = command.replace(ch, '\u0075'); break;
                    case '\u016B': command = command.replace(ch, '\u0075'); break;
                    case '\u016D': command = command.replace(ch, '\u0075'); break;
                    case '\u016F': command = command.replace(ch, '\u0075'); break;
                    case '\u0171': command = command.replace(ch, '\u0075'); break;
                    case '\u0173': command = command.replace(ch, '\u0075'); break;
                    case '\u0125': command = command.replace(ch, '\u0068'); break;
                    case '\u0127': command = command.replace(ch, '\u0068'); break;
                    case '\u0175': command = command.replace(ch, '\u0077'); break;
                    case '\u00FD': command = command.replace(ch, '\u0079'); break;
                    case '\u00FF': command = command.replace(ch, '\u0079'); break;
                    case '\u0177': command = command.replace(ch, '\u0079'); break;
                    case '\u0179': command = command.replace(ch, '\u005A'); break;
                    case '\u017B': command = command.replace(ch, '\u005A'); break;
                    case '\u017D': command = command.replace(ch, '\u005A'); break;
                    
                }

            }else {}

         }
        System.out.println(command);

      byte[] commandsBytes = command.getBytes("US-ASCII");
      //out.write(command.getBytes());
        out.write(commandsBytes);
      out.write('\n');
      out.flush();
    } catch (IOException e){
        connectionInterrupted(e);
      }
    }
  }
  
  /**
   * Sends the given command to the server and makes sure it will not go to the console.
   */
  
  public void sendCommFromPlugin(String command){

      sendCommand(command, true);
      this.fromPlugin = true;
  }
  
  //TODO: do we need a getter for fromPlugin boolean?




  /**
   * This method is called when a line of text that isn't identified as some
   * known type of information arrives from the server.
   */

  protected void processLine(String line){}




  /**
   * This method is called to process disconnection from the server.
   */

  protected void processDisconnection(){}
  
  
  
  /**
   * Overrides {@link Connection#createInputStream(InputStream)} to wrap the
   * specified <code>InputStream</code> in a <code>BufferedInputStream</code>
   * and a <code>PushbackInputStream</code>.
   */
  
  protected InputStream createInputStream(InputStream in){
    return new PushbackInputStream(new BufferedInputStream(in));
  }



  /**
   * Reads a single line from the server.
   */
  
  protected Object readMessage(InputStream inputStream) throws IOException{
    PushbackInputStream pin = (PushbackInputStream)inputStream;
    StringBuffer buf = new StringBuffer();
    
    boolean lineStartsWithPrompt = false;
    while (true){
      int b = pin.read();
      
      if (b < 0){
        if (buf.length() == 0) // Clean disconnection
          return null;
        break;
      }
            
      // End of line
      if (b == '\n'){
        // FICS uses \n\r for an end-of-line marker!?
        // Eat the following '\r', if there is one
        b = pin.read();
        if ((b > 0) && (b != '\r'))
          pin.unread(b);
        
        // Ignore all-prompt lines
        if (lineStartsWithPrompt && (buf.length() == 0)){
          lineStartsWithPrompt = false;
          continue;
        }
        else
          break;
      }
      
      buf.append((char)b);
      
      // Filter out the prompt
      if (buf.toString().equals("fics% ")){
        buf.setLength(0);
        lineStartsWithPrompt = true;
      }
    }
    
    return buf.toString();
  }
  
  
  
  /**
   * The method is responsible for determining the type of the
   * information, parsing it and sending it for further processing.
   */

  protected void handleMessage(Object lineObj){
    String line = (String)lineObj;
    
    if (logStream != null)
      logStream.println(line);
    
    if (handleGameInfo(line))
      return;
    if (handleExaminedGameInfo(line))
        return;
    if (handleStyle12(line))
      return;
    if (handleDeltaBoard(line))
      return;
    if (handleSeeksCleared(line))
      return;
    if (handleSeekAdded(line))
      return;
    if (handleSeeksRemoved(line))
      return;
    if (handleBughouseHoldings(line))
      return;
    if (handleGameEnd(line))
      return;
    if (handleStoppedObserving(line))
      return;
    if (handleStoppedExamining(line))
      return;
    if (handleEnteredBSetupMode(line))
      return;
    if (handleExitedBSetupMode(line))
      return;
    if (handleIllegalMove(line))
      return;
    if (handleChannelTell(line))
      return;
    if (handleLogin(line))
      return;
    if (handleIvarStateChanged(line))
      return;
    if (handlePersonalTell(line))
      return;
    if (handleSayTell(line))
      return;
    if (handlePTell(line))
      return;
    if (handleShout(line))
      return;
    if (handleIShout(line))
      return;
    if (handleTShout(line))
      return;
    if (handleCShout(line))
      return;
    if (handleAnnouncement(line))
      return;
    if (handleKibitz(line))
      return;
    if (handleWhisper(line))
      return;
    if (handleQTell(line))
      return;
    if (handleOffer(line))
      return;
    if (handleOfferRemoved(line))
      return;
    if (handlePlayerOffered(line))                    //
      return;                                         // We have to handle these
    if (handlePlayerDeclined(line))                   // "manually", since the  
      return;                                         // server currently does  
    if (handlePlayerWithdrew(line))                   // not inform us of offers
      return;                                         // in games we're         
    if (handlePlayerCounteredTakebackOffer(line))     // observing.             
      return;                                         // 
    if (handleSimulCurrentBoardChanged(line))
      return;
    if (handlePrimaryGameChanged(line))
      return;
    if (handleUserChannelListNext(line))
        return;
    if (handleChannelListArrives(line))
        return;
    if (handleChannelListChanged(line))
        return;
    if (handleMessagesInfo(line))
        return;
    if (handleMessages(line))
        return;

    if (linesToFilter.remove(line) == null)
      processLine(line);
  }



  /**
   * The regular expression matching lines which are notifications of an the
   * state of an ivar changing.
   */

  private static final Pattern IVAR_SET_REGEX = Pattern.compile("^(\\w+) (un)?set.$");



  /**
   * Called to determine whether the specified line is a notification that the
   * state of some ivar has changed.
   */

  private boolean handleIvarStateChanged(String line){
    if (line.indexOf("set") == -1)
      return false;
    
    Matcher matcher = IVAR_SET_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String ivarName = matcher.group(1);
    boolean state = (matcher.group(2) == null) || (matcher.group(2).length() == 0);

    Ivar ivar = Ivar.getByName(ivarName);
    if (ivar == null) // It's a notification that something has been set, but not a known ivar
      return false;
    
    ivarStates.set(ivar.getIndex());
    
    return processIvarStateChanged(ivar, state);
  }
  
  
  
  /**
   * Gets called when the server notifies us of a change in the state of some
   * ivar.
   */
   
  protected boolean processIvarStateChanged(Ivar ivar, boolean state){return false;}




  /**
   * The regular expression matching login confirmation lines.
   * Example: "**** Starting FICS session as AlexTheGreat ****"
   */

  private static final Pattern LOGIN_REGEX =
    Pattern.compile("^\\*\\*\\*\\* Starting FICS session as ("+USERNAME_REGEX+")("+TITLES_REGEX+")? \\*\\*\\*\\*");



  /**
   * The regular expression matching login failure due to wrong password lines. 
   */

  private static final Pattern WRONG_PASSWORD_REGEX = 
    Pattern.compile("^\\*\\*\\*\\* Invalid password! \\*\\*\\*\\*");




  /**
   * Called to determine if the given line of text is a login confirming line
   * and to handle that information if it is. Returns <code>true</code> if the
   * given line is a login confirming line, otherwise, returns
   * <code>false</code>.
   */

  private boolean handleLogin(String line){
    Matcher matcher = LOGIN_REGEX.matcher(line);
    if ((!isLoggedIn()) && matcher.matches()){
      loginSucceeded(matcher.group(1));

        String usersServerRole = matcher.group(2);
        if (usersServerRole == null){
            this.characterLimit = 400;
        }else if (usersServerRole.equals("(U)")){
            this.characterLimit = 200;
        }else if (usersServerRole.equals("(*)") || usersServerRole.equals("(SR)")){
            this.characterLimit = 1024;
        }else{
            this.characterLimit = 400;
        }
        System.out.println("Server character limit = " + this.characterLimit);

      processLine(line);

      return true;
    }
    else if ((!isLoggedIn()) && WRONG_PASSWORD_REGEX.matcher(line).matches())
      loginFailed("Invalid password");

    return false;
  }




  /**
   * The regular expression matching personal tells.
   */

  private static final Pattern PERSONAL_TELL_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")? tells you: (.*)");




  /**
   * Called to determine whether the given line of text is a personal tell and
   * to further process it if it is.
   */

  private boolean handlePersonalTell(String line){
    if (line.indexOf("tells you: ") == -1)
      return false;
    
    Matcher matcher = PERSONAL_TELL_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processPersonalTell(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a personal tell is received. 
   */

  protected boolean processPersonalTell(String username, String titles, String message){return false;}




  /**
   * The regular expression matching "say" tells.
   */

  private static final Pattern SAY_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")?(\\[(\\d+)\\])? says: (.*)");




  /**
   * Called to determine whether the given line of text is a "say" tell and
   * to further process it if it is.
   */

  private boolean handleSayTell(String line){
    if (line.indexOf("says: ") == -1)
      return false;
    
    Matcher matcher = SAY_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String gameNumberString = matcher.group(4);
    String message = matcher.group(5);

    int gameNumber = gameNumberString == null ? -1 : Integer.parseInt(gameNumberString);

    if (!processSayTell(username, titles, gameNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "say" tell is received. The
   * <code>gameNumber</code> argument will contain -1 if the game number was not
   * specified.
   */

  protected boolean processSayTell(String username, String titles, int gameNumber, String message){return false;}




  /**
   * The regular expression matching "ptell" tells.
   */

  private static final Pattern PTELL_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")? \\(your partner\\) tells you: (.*)");




  /**
   * Called to determine whether the given line of text is a "ptell" tell and
   * to further process it if it is.
   */

  private boolean handlePTell(String line){
    if (line.indexOf("(your partner) tells you: ") == -1)
      return false;
    
    Matcher matcher = PTELL_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processPTell(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "ptell" tell is received. 
   */

  protected boolean processPTell(String username, String titles, String message){return false;}




  /**
   * The regular expression matching channel tells.
   */

  private static final Pattern CHANNEL_TELL_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")?\\((\\d+)\\): (.*)");




  /**
   * Called to determine whether the given line of text is a channel tell and
   * to further process it if it is.
   */

  private boolean handleChannelTell(String line){
    if (line.indexOf("): ") == -1)
      return false;
    
    Matcher matcher = CHANNEL_TELL_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String channelNumberString = matcher.group(3);
    String message = matcher.group(4);

    int channelNumber = Integer.parseInt(channelNumberString);

    if (!processChannelTell(username, titles, channelNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a channel tell is received. 
   */

  protected boolean processChannelTell(String username, String titles, int channelNumber, String message){return false;}




  /**
   * The regular expression matching kibitzes.
   */

  private static final Pattern KIBITZ_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")?\\( {0,3}([\\-0-9]+)\\)\\[(\\d+)\\] kibitzes: (.*)");




  /**
   * Called to determine whether the given line of text is a kibitz and
   * to further process it if it is.
   */

  private boolean handleKibitz(String line){
    if (line.indexOf("kibitzes: ") == -1)
      return false;
    
    Matcher matcher = KIBITZ_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String ratingString = matcher.group(3);
    String gameNumberString = matcher.group(4);
    String message = matcher.group(5);

    int rating = (ratingString != null) && !ratingString.equals("----") ?
      Integer.parseInt(ratingString) : -1;
    int gameNumber = Integer.parseInt(gameNumberString);

    if (!processKibitz(username, titles, rating, gameNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a kibitz is received. The rating argument
   * contains -1 if the player is unrated or otherwise doesn't have a rating.
   */

  protected boolean processKibitz(String username, String titles, int rating, int gameNumber, String message){return false;}





  /**
   * The regular expression matching whispers.
   */

  private static final Pattern WHISPER_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")?\\( {0,3}([\\-0-9]+)\\)\\[(\\d+)\\] whispers: (.*)");




  /**
   * Called to determine whether the given line of text is a whisper and
   * to further process it if it is.
   */

  private boolean handleWhisper(String line){
    if (line.indexOf("whispers: ") == -1)
      return false;
    
    Matcher matcher = WHISPER_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String ratingString = matcher.group(3);
    String gameNumberString = matcher.group(4);
    String message = matcher.group(5);

    int rating = (ratingString != null) && !ratingString.equals("----") ? Integer.parseInt(ratingString) : -1;
    int gameNumber = Integer.parseInt(gameNumberString);

    if (!processWhisper(username, titles, rating, gameNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a whisper is received. The rating argument
   * contains -1 if the player is unrated or otherwise doesn't have a rating.
   */

  protected boolean processWhisper(String username, String titles, int rating, int gameNumber, String message){return false;}




  /**
   * The regular expression matching qtells.
   */

  private static final Pattern QTELL_REGEX = Pattern.compile("^:(.*)");




  /**
   * Called to determine whether the given line of text is a qtell and
   * to further process it if it is.
   */

  private boolean handleQTell(String line){
    if (!line.startsWith(":"))
      return false;
    
    Matcher matcher = QTELL_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String message = matcher.group(1);

    if (!processQTell(message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a qtell is received.
   */

  protected boolean processQTell(String message){return false;}




  
  /**
   * The regular expression matching shouts.
   */

  private static final Pattern SHOUT_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")? shouts: (.*)");




  /**
   * Called to determine whether the given line of text is a shout and
   * to further process it if it is.
   */

  private boolean handleShout(String line){
    if (line.indexOf("shouts: ") == -1)
      return false;
    
    Matcher matcher = SHOUT_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a shout is received. 
   */

  protected boolean processShout(String username, String titles, String message){return false;}




  /**
   * The regular expression matching "ishouts".
   */

  private static final Pattern ISHOUT_REGEX = 
    Pattern.compile("^--> ("+USERNAME_REGEX+")("+TITLES_REGEX+")? ?(.*)");




  /**
   * Called to determine whether the given line of text is an "ishout" and
   * to further process it if it is.
   */

  private boolean handleIShout(String line){
    if (!line.startsWith("--> "))
      return false;
    
    Matcher matcher = ISHOUT_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processIShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when an "ishout" is received. 
   */

  protected boolean processIShout(String username, String titles, String message){return false;}





  /**
   * The regular expression matching "tshouts".
   */

  private static final Pattern TSHOUT_REGEX = 
    Pattern.compile("^:("+USERNAME_REGEX+")("+TITLES_REGEX+")? t-shouts: (.*)");




  /**
   * Called to determine whether the given line of text is a "tshout" and
   * to further process it if it is.
   */

  private boolean handleTShout(String line){
    if (line.indexOf("t-shouts: ") == -1)
      return false;
    
    Matcher matcher = TSHOUT_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processTShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "tshout" is received. 
   */

  protected boolean processTShout(String username, String titles, String message){return false;}





  /**
   * The regular expression matching "cshouts".
   */

  private static final Pattern CSHOUT_REGEX = 
    Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")? c-shouts: (.*)");




  /**
   * Called to determine whether the given line of text is a "cshout" and
   * to further process it if it is.
   */

  private boolean handleCShout(String line){
    if (line.indexOf("c-shouts: ") == -1)
      return false;
    
    Matcher matcher = CSHOUT_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processCShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "cshout" is received. 
   */

  protected boolean processCShout(String username, String titles, String message){return false;}




  /**
   * The regular expression matching announcements.
   */

  private static final Pattern ANNOUNCEMENT_REGEX = 
    Pattern.compile("^    \\*\\*ANNOUNCEMENT\\*\\* from ("+USERNAME_REGEX+"): (.*)");




  /**
   * Called to determine whether the given line of text is an announcement and
   * to further process it if it is.
   */

  private boolean handleAnnouncement(String line){
    if (!line.startsWith("    **ANNOUNCEMENT** from "))
      return false;
    
    Matcher matcher = ANNOUNCEMENT_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String username = matcher.group(1);
    String message = matcher.group(2);

    if (!processAnnouncement(username, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when an announcement is received. 
   */

  protected boolean processAnnouncement(String username, String message){return false;}




  /**
   * Called to determine whether the given line of text is a gameinfo line and
   * to further process it if it is.
   */

  private boolean handleGameInfo(String line){
    if (!line.startsWith("<g1> "))
      return false;
    
    GameInfoStruct data = GameInfoStruct.parseGameInfoLine(line);

    if (!processGameInfo(data))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a gameinfo line is received. To turn gameinfo
   * lines on, use <code>sendCommand("$iset gameinfo 1")</code>
   */

  protected boolean processGameInfo(GameInfoStruct data){return false;}

    /**
     * Regular expression indicating that give line of text is examined game info.
     */
    private static final Pattern EXAMINED_GAME_INFO = Pattern.compile( "^\\s{0,2}(\\d{1,3}) (\\(Exam\\.)\\s{1,4}\\d{1,4} (\\w*)\\s*(\\d{1,4}) (\\w*) {0,}\\) \\[\\s{0,1}(\\w{2,3})\\s*(\\d*)\\s*(\\d*)\\] (\\w)\\:\\s*(\\d*)");

    /**
     * Called to determine whether the given line of text is a examined game info and to further
     * process it if it is.
     */

    protected boolean handleExaminedGameInfo(String line){
        Matcher matcher = EXAMINED_GAME_INFO.matcher(line);
        if (matcher.matches() == false){
            return false;
        }

        int examinedGameNr = Integer.parseInt(matcher.group(1));
        String examinedGameCategory = matcher.group(6);

        if (processExaminedGameInfo(examinedGameNr, examinedGameCategory) == false){
            return false;
        }
        return false;
    }

    /**
     * Processes info from line containing information about examined game.
     * @param gameNr
     * @param gameCategory
     * @return
     */

    protected boolean processExaminedGameInfo(int gameNr, String gameCategory){return false;}



  
  /**
   * Called to determine whether the given line of text is a style12 line and
   * to further process it if it is.
   */

  private boolean handleStyle12(String line){
    if (!line.startsWith("<12> "))
      return false;

    Style12Struct data = Style12Struct.parseStyle12Line(line);

    if (!processStyle12(data))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a style12 line is received. To turn on style 12,
   * use <code>setStyle(12)</code>.
   */

  protected boolean processStyle12(Style12Struct data){return false;}




  /**
   * Called to determine whether the given line of text is a delta board line
   * and to further process it if it is.
   */

  private boolean handleDeltaBoard(String line){
    if (!line.startsWith("<d1> "))
      return false;

    DeltaBoardStruct data = DeltaBoardStruct.parseDeltaBoardLine(line);

    if (!processDeltaBoard(data))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a delta board line is received. To turn delta
   * board on, use <code>sendCommand("$iset compressmove 1")</code>. Note,
   * however, that it will disable the sending of a full board (like a style12
   * board) in some cases.
   */

  protected boolean processDeltaBoard(DeltaBoardStruct data){return false;}

    /**
     * Pattern for handling bughouse holdings.
     */

    private static final Pattern BUGHOUSE_HOLDINGS = Pattern.compile("^<b1> game (\\d{1,4}) white \\[(\\w{0,12})\\] black \\[(\\w{0,12})\\].*");


  /**
   * Called to determine whether the given line of text is a bughouse holdings
   * line and to further process it if it is.
   */

  private boolean handleBughouseHoldings(String line){
    if (!line.startsWith("<b1> "))
      return false;

      Matcher matcher = BUGHOUSE_HOLDINGS.matcher(line);
      if (!matcher.matches()){
          return false;
      }


      int gameNumber = Integer.parseInt(matcher.group(1));
      String whiteAvailablePieces = matcher.group(2);
      String blackAvailablePieces = matcher.group(3);

      System.out.println("BUGHOUSE INFO = " + gameNumber + " " + whiteAvailablePieces + " " + blackAvailablePieces);

       //This makes this connection to process this line further only when some piece is added to pieces pool.

        if (!processBughouseHoldings(gameNumber, whiteAvailablePieces, blackAvailablePieces)){

          processLine(line);
        
       }
    
    return true;
  }

    protected boolean processBughouseHoldings(int gameNumber, String whiteAvailablePieces, String blackAvailablePieces) {

        return false;

    }


    /**
   * The regular expression matching game end lines, like the following:<br>
   * {Game 6 (Strakh vs. Svag) Strakh forfeits on time} 0-1.
   */

  private static final Pattern GAME_END_REGEX = 
    Pattern.compile("^\\{Game (\\d+) \\(("+USERNAME_REGEX+") vs\\. ("+USERNAME_REGEX+")\\) ([^\\}]+)\\} (.*)");




  /**
   * Called to determine whether the given line of text is a game end line
   * and to further process it if it is.
   */

  private boolean handleGameEnd(String line){
    if (!line.startsWith("{Game "))
      return false;
      
    Matcher matcher = GAME_END_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    int gameNumber = Integer.parseInt(matcher.group(1));
    String whiteName = matcher.group(2);
    String blackName = matcher.group(3);
    String reason = matcher.group(4);
    String result = matcher.group(5);

    if (!processGameEnd(gameNumber, whiteName, blackName, reason, result))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a game end line is received.
   */

  protected boolean processGameEnd(int gameNumber, String whiteName, String blackName, String reason, String result){return false;}




  /**
   * The regular expression matching lines specifying that we've stopped
   * observing a game, like the following:<br>
   * Removing game 7 from observation list.
   */

  private static final Pattern STOPPED_OBSERVING_REGEX = 
    Pattern.compile("^Removing game (\\d+) from observation list\\.$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that we've stopped observing a game and to further process it if it is.
   */

  private boolean handleStoppedObserving(String line){
    if (!line.startsWith("Removing game "))
      return false;
      
    Matcher matcher = STOPPED_OBSERVING_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    int gameNumber = Integer.parseInt(matcher.group(1));

    if (!processStoppedObserving(gameNumber))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that we've stopped observing a
   * game is received.
   */

  protected boolean processStoppedObserving(int gameNumber){return false;}




  /**
   * The regular expression matching lines specifying that we've stopped
   * examining a game, like the following:<br>
   * You are no longer examining game 114.
   */

  private static final Pattern STOPPED_EXAMINING_REGEX = 
    Pattern.compile("^You are no longer examining game (\\d+)\\.$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that we've stopped examining a game and to further process it if it is.
   */

  private boolean handleStoppedExamining(String line){
    if (!line.startsWith("You are no longer examining game "))
      return false;
    
    Matcher matcher = STOPPED_EXAMINING_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    int gameNumber = Integer.parseInt(matcher.group(1));

    if (!processStoppedExamining(gameNumber))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that we've stopped examining a
   * game is received.
   */

  protected boolean processStoppedExamining(int gameNumber){return false;}



  
  /**
   * Called to determine whether the specified line of text specifies entering
   * bsetup mode.
   */

  private boolean handleEnteredBSetupMode(String line){
    if (!line.equals("Entering setup mode."))
      return false;

    if (!processBSetupMode(true))
      processLine(line);

    return true;
  }



  /**
   * Called to determine whether the specified line of text specifies exiting
   * bsetup mode.
   */

  private boolean handleExitedBSetupMode(String line){
    if (!line.equals("Game is validated - entering examine mode."))
      return false;

    if (!processBSetupMode(false))
      processLine(line);

    return true;
  }



  /**
   * This method is called whenever the user enters or exits bsetup mode.
   * The boolean argument is <code>true</code> if we've entered bsetup mode,
   * <code>false</code> when exited.
   */

  protected boolean processBSetupMode(boolean entered){return false;}



  
  /**
   * The regular expression matching lines specifying that an illegal move has
   * been attempted. There are two types I know currently, but there may be
   * more, so I'm allowing anything that starts with the expected pattern. The
   * two I know are:
   * 1. Illegal move (e2e4).
   * 2. Illegal move (e2e4). You must capture.
   */

  private static final Pattern ILLEGAL_MOVE_REGEX = 
    Pattern.compile("^Illegal move \\((.*)\\)\\.(.*)");

    
    
    
  /**
   * The regular expression matching lines specifying that the user attempted to
   * make a move when it is not his turn.
   */

  private static final Pattern NOT_YOUR_TURN_REGEX =
    Pattern.compile("^(It is not your move\\.)$");
    
    
    
  /**
   * The regular expression matching lines specifying that the user attempted to
   * make a move while the game is paused.
   */
  
  private static final Pattern MOVED_WHEN_GAME_PAUSED = 
    Pattern.compile("^(The clock is paused, use \"unpause\" to resume\\.)$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that an illegal move has been attempted and to further process it if it is.
   */

  private boolean handleIllegalMove(String line){
    if (!(line.startsWith("Illegal move ") ||
          line.equals("It is not your move.") ||
          line.equals("The clock is paused, use \"unpause\" to resume.")))
      return false;
    
    Matcher illegalMoveMatcher = ILLEGAL_MOVE_REGEX.matcher(line);
    Matcher notYourTurnMatcher = NOT_YOUR_TURN_REGEX.matcher(line);
    Matcher movedWhenGamePausedMatcher = MOVED_WHEN_GAME_PAUSED.matcher(line);

    if (illegalMoveMatcher.matches()){
      String moveString = illegalMoveMatcher.group(1);
      String reason = illegalMoveMatcher.group(2);

      if (!processIllegalMove(moveString, reason))
        processLine(line);

      return true;
    }
    else if (notYourTurnMatcher.matches()){
      String moveString = null; // sigh
      String reason = notYourTurnMatcher.group(1);

      if (!processIllegalMove(moveString, reason))
        processLine(line);

      return true;
    }
    else if (movedWhenGamePausedMatcher.matches()){
      String moveString = null;
      String reason = movedWhenGamePausedMatcher.group(1);
      
      if (!processIllegalMove(moveString, reason))
        processLine(line);
      
      return true;
    }

    return false;
  }




  /**
   * This method is called when a line specifying that an illegal move has been
   * attempted is received. <code>moveString</code> is the move string that was
   * sent to the server, if the server bothers to tells us what it was
   * (otherwise, it is null). <code>reason</code> specifies the reason the move
   * is illegal. This, too, may be null.
   */

  protected boolean processIllegalMove(String moveString, String reason){return false;}




  /**
   * Called to determine whether the given line of text is a line specifying
   * that all seeks have been cleared.
   */

  private boolean handleSeeksCleared(String line){
    if (!line.equals("<sc>"))
      return false;

    if (!processSeeksCleared())
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that all seeks have been
   * cleared is received.
   */

  protected boolean processSeeksCleared(){return false;}




  /**
   * Called to determine whether the given line of text is a line specifying
   * that a new seek has been added.
   */

  private boolean handleSeekAdded(String line){
    if (!(line.startsWith("<s> ") || line.startsWith("<sn> ")))
      return false;
    
    SeekInfoStruct seekInfo = SeekInfoStruct.parseSeekInfoLine(line);

    if (!processSeekAdded(seekInfo))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that a new seek has been added
   * is received.
   */

  protected boolean processSeekAdded(SeekInfoStruct seekInfo){return false;}




  /**
   * Called to determine whether the given line of text is a line specifying
   * that seeks have been removed.
   */

  private boolean handleSeeksRemoved(String line){
    if (!line.startsWith("<sr> "))
      return false;
    
    StringTokenizer tokenizer = new StringTokenizer(line, " ");
    tokenizer.nextToken(); // Skip the "<sr>"

    int [] removedSeeks = new int[tokenizer.countTokens()];
    for (int i = 0; i < removedSeeks.length; i++)
      removedSeeks[i] = Integer.parseInt(tokenizer.nextToken());

    if (!processSeeksRemoved(removedSeeks))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that seeks have been removed
   * is received. The array specifies the numbers of the removed seeks.
   */

  protected boolean processSeeksRemoved(int [] removedSeeks){return false;}




  /**
   * The regular expression we use to parse offers as they are specified in
   * <pt> and <pf> lines (see "help iv_pendinfo").
   */

  private static final Pattern OFFER_PARSER = 
    Pattern.compile("^(\\d+) w=("+USERNAME_REGEX+") t=(\\S+) p=(.*)");

  
  
  /**
   * The regular expression matching lines specifying that an offer has been
   * made to the user.
   */

  private static final Pattern OFFER_REGEX = Pattern.compile("^<p([tf])> (.*)");



  /**
   * Called to determine whether the specified line is a line informing us that
   * an offer has been made (either to or by the user).
   */

  private boolean handleOffer(String line){
    if (!(line.startsWith("<pt> ") || line.startsWith("<pf> ")))
      return false;
    
    Matcher matcher = OFFER_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    boolean toUser = "f".equals(matcher.group(1));

    Matcher parser = OFFER_PARSER.matcher(matcher.group(2));
    if (!parser.matches()) // Really weird
      return false;

    int offerIndex = Integer.parseInt(parser.group(1));
    String username = parser.group(2);
    String offerType = parser.group(3);
    String offerParams = parser.group(4);

    if (!processOffer(toUser, offerType, offerIndex, username, offerParams))
      processLine(line);

    return true;
  }



	/**
	 * Gets called when an offer is made. The <code>toUser</code> argument
   * specified whether the offer was made to or by the user. Possible
	 * offer types are (according to DAV) match, bughouse (I think that's
	 * when your partner is challenged), simul, draw, abort, adjourn,
	 * seal (currently unused), takeback, switch, pause, unpause, partner.
	 */

	protected boolean processOffer(boolean toUser, String offerType, int offerIndex,
		String oppName, String offerParams){

    if ("match".equals(offerType)){
      return processMatchOffered(toUser, offerIndex, oppName, offerParams);
      // Maybe I should parse the offer parameters here, but I'm not sure about
      // the format and I don't need them right now.
    }
    else if ("takeback".equals(offerType)){
      // Use a tokenizer just in case new fields are added
      StringTokenizer tokenizer = new StringTokenizer(offerParams, " ");
      int plies = Integer.parseInt(tokenizer.nextToken());
      return processTakebackOffered(toUser, offerIndex, oppName, plies);
    }
    else if ("draw".equals(offerType)){
      return processDrawOffered(toUser, offerIndex, oppName);
    }
    else if ("abort".equals(offerType)){
      return processAbortOffered(toUser, offerIndex, oppName);
    }
    else if ("adjourn".equals(offerType)){
      return processAdjournOffered(toUser, offerIndex, oppName);
    }
    else // Unknown type
      return false;
	}
	




  /**
   * Gets called when a match offer is made. The <code>toUser</code> argument
   * specified whether the offer was made to or by the user. The
   * <code>matchDetails</code> argument contains unparsed details about the type
   * of the match offered.
   */

  protected boolean processMatchOffered(boolean toUser, int offerIndex, String oppName,
      String matchDetails){
    return false;
  }

 


  /**
   * Gets called when a takeback offer is made. The <code>toUser</code> argument
   * specified whether the offer was made to or by the user.
   * <code>takebackCount</code> is plies offered to take back.
   */

  protected boolean processTakebackOffered(boolean toUser, int offerIndex, String oppName,
      int takebackCount){
    return false;
  }



  /**
   * Gets called when a draw offer is made. The <code>toUser</code> argument
   * specified whether the offer was made to or by the user.
   */

  protected boolean processDrawOffered(boolean toUser, int offerIndex, String oppName){
    return false;
  }
  


  /**
   * Gets called when an abort offer is made. The <code>toUser</code> argument
   * specified whether the offer was made to or by the user.
   */

  protected boolean processAbortOffered(boolean toUser, int offerIndex, String oppName){
    return false;
  }



  /**
   * Gets called when an adjourn offer is made. The <code>toUser</code> argument
   * specified whether the offer was made to or by the user.
   */

  protected boolean processAdjournOffered(boolean toUser, int offerIndex, String oppName){
    return false;
  }



  
  /**
   * Gets called to determine whether the specified line is a line informing us
   * that the specified offer has been removed (accepted, declined, withdrawn,
   * game ended or anything else).
   */

  private boolean handleOfferRemoved(String line){
    if (!line.startsWith("<pr> "))
      return false;
      
    int offerIndex = Integer.parseInt(line.substring("<pr> ".length()));

    if (!processOfferRemoved(offerIndex))
      processLine(line);

    return true;
  }



  /**
   * Gets called when an offer has been removed (accepted, declined, withdrawn,
   * game ended or anything else).
   */

  protected boolean processOfferRemoved(int offerIndex){
    return false;
  }




  /**
   * The regular expression matching lines specifying that a player (in a game
   * we're observing) made a draw offer.
   */

  private static final Pattern PLAYER_OFFERED_DRAW_REGEX = 
    Pattern.compile("^Game (\\d+): ("+USERNAME_REGEX+") offers a draw\\.$");
  


  /**
   * The regular expression matching lines specifying that a player offered to
   * abort the game.
   */

  private static final Pattern PLAYER_OFFERED_ABORT_REGEX = 
    Pattern.compile("^Game (\\d+): ("+USERNAME_REGEX+") requests to abort the game\\.$");



  /**
   * The regular expression matching lines specifying that a player offered to
   * adjourn the game.
   */

  private static final Pattern PLAYER_OFFERED_ADJOURN_REGEX = 
    Pattern.compile("^Game (\\d+): ("+USERNAME_REGEX+") requests to adjourn the game\\.$");



  /**
   * The regular expression matching lines specifying that a player offered to
   * takeback.
   */

  private static final Pattern PLAYER_OFFERED_TAKEBACK_REGEX = 
    Pattern.compile("^Game (\\d+): ("+USERNAME_REGEX+") requests to take back (\\d+) half move\\(s\\)\\.$");



  /**
   * Handles lines specifying that a player (in a game we're observing) made an
   * offer.
   */

  private boolean handlePlayerOffered(String line){
    if (!line.startsWith("Game "))
      return false;
    
    String offer;

    Matcher matcher;
    if ((matcher = PLAYER_OFFERED_DRAW_REGEX.matcher(line)).matches())
      offer = "draw";
    else if ((matcher = PLAYER_OFFERED_ADJOURN_REGEX.matcher(line)).matches())
      offer = "adjourn";
    else if ((matcher = PLAYER_OFFERED_ABORT_REGEX.matcher(line)).matches())
      offer = "abort";
    else if ((matcher = PLAYER_OFFERED_TAKEBACK_REGEX.matcher(line)).matches())
      offer = "takeback";
    else
      return false;

    int gameNum = Integer.parseInt(matcher.group(1));
    String playerName = matcher.group(2);

    if ("takeback".equals(offer)){
      int takebackCount = Integer.parseInt(matcher.group(3));
      if (!processPlayerOfferedTakeback(gameNum, playerName, takebackCount))
        processLine(line);
    }
    else if (!processPlayerOffered(gameNum, playerName, offer))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that an offer has been made by
   * one of the players in the specified game. Possible values for offer are
   * "draw", "abort" and "adjourn", but clients should not break if a different
   * value is received.
   */

  protected boolean processPlayerOffered(int gameNum, String playerName, String offer){
    return false;
  }



  /**
   * This method is called when a line specifying that a takeback has been
   * offered by one of the players in the specified game.
   */

  protected boolean processPlayerOfferedTakeback(int gameNum, String playerName, int takebackCount){
    return false;
  }




  /**
   * The regular expression matching lines specifying that a player declined an
   * offer.
   */

  private static final Pattern PLAYER_DECLINED_REGEX = 
    Pattern.compile("^Game (\\d+): ("+USERNAME_REGEX+") declines the (\\w+) request\\.$");



  /**
   * Called to determine whether the given line of text is a line specifying
   * that a player declined his opponent's offer.
   */

  private boolean handlePlayerDeclined(String line){
    if ((!line.startsWith("Game ")) || (line.indexOf("declines the ") == -1))
      return false;
      
    Matcher matcher = PLAYER_DECLINED_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    int gameNum = Integer.parseInt(matcher.group(1));
    String playerName = matcher.group(2);
    String offer = matcher.group(3);

    if (!processPlayerDeclined(gameNum, playerName, offer))
      processLine(line);

    return true;
  }



  /**
   * Gets called when a player (in an observed game) declines an offer. Possible
   * values for offer are "draw", "abort", "adjourn" and "takeback" but clients
   * should not break if a different value is received.
   */

  protected boolean processPlayerDeclined(int gameNum, String playerName, String offer){
    return false;
  }




  /**
   * The regular expression matching lines specifying that a player withdrew his
   * offer.
   */

  private static final Pattern PLAYER_WITHDREW_REGEX = 
    Pattern.compile("^Game (\\d+): ("+USERNAME_REGEX+") withdraws the (\\w+) request\\.$");



  /**
   * Called to determine whether the given line of text is a line specifying
   * that a player declined his opponent's offer.
   */

  private boolean handlePlayerWithdrew(String line){
    if ((!line.startsWith("Game ")) || (line.indexOf("withdraws the ") == -1))
      return false;
    
    Matcher matcher = PLAYER_WITHDREW_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    int gameNum = Integer.parseInt(matcher.group(1));
    String playerName = matcher.group(2);
    String offer = matcher.group(3);

    if (!processPlayerWithdrew(gameNum, playerName, offer))
      processLine(line);

    return true;
  }



  /**
   * Gets called when a player (in an observed game) withdraws an offer.
   * Possible values for offer are "draw", "abort", "adjourn" and "takeback" but
   * clients should not break if a different value is received.
   */

  protected boolean processPlayerWithdrew(int gameNum, String playerName, String offer){
    return false;
  }




  /**
   * The regular expression matching lines specifying that a player (in a game
   * we're observing) counter-offered a takeback offer by his opponent with a
   * different amount of plies to take back.
   */

  private static final Pattern PLAYER_COUNTER_TAKEBACK_OFFER_REGEX =
    Pattern.compile("^Game (\\d+): ("+USERNAME_REGEX+") proposes a different number \\((\\d+)\\) of half-move\\(s\\) to take back\\.$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that a player (in a game we're observing) counter-offered a takeback offer
   * by his opponent with a different amount of plies to take back.
   */

  private boolean handlePlayerCounteredTakebackOffer(String line){
    if ((!line.startsWith("Game ")) || (line.indexOf("proposes a different number ") == -1))
      return false;
    
    Matcher matcher = PLAYER_COUNTER_TAKEBACK_OFFER_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    int gameNum = Integer.parseInt(matcher.group(1));
    String playerName = matcher.group(2);
    int takebackCount = Integer.parseInt(matcher.group(3));

    if (!processPlayerCounteredTakebackOffer(gameNum, playerName, takebackCount))
      processLine(line);

    return true;
  }



  /**
   * This method is called when a player (in a game we're observing)
   * counter-offers a takeback offer by his opponent with a different amount of
   * plies to take back.
   */

  protected boolean processPlayerCounteredTakebackOffer(int gameNum, String playerName,
      int takebackCount){
    return false;
  }



  /**
   * The regular expression matching lines notifying the user which board he's
   * at, in a simul.
   */

  private static final Pattern AT_BOARD_REGEX = 
    Pattern.compile("^You are now at ("+USERNAME_REGEX+")'s board \\(game (\\d+)\\)\\.$");



  /**
   * Handles lines notifying us that the board we're at (in a simul) has
   * changed.
   */

  private boolean handleSimulCurrentBoardChanged(String line){
    if (!line.startsWith("You are now at "))
      return false;
      
    Matcher matcher = AT_BOARD_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    String oppName = matcher.group(1);
    int gameNumber = Integer.parseInt(matcher.group(2));

    if (!processSimulCurrentBoardChanged(gameNumber, oppName))
      processLine(line);

    return true;
  }




  /**
   * Gets called when a line notifying us that the current board in a simul
   * we're giving has changed.
   */

  protected boolean processSimulCurrentBoardChanged(int gameNumber, String oppName){
    return false;
  }



  /**
   * The regular expression matching lines notifying us that the primary
   * observed game has changed.
   */

  private static final java.util.regex.Pattern PRIMARY_GAME_CHANGED_REGEX =
    java.util.regex.Pattern.compile("^Your primary game is now game (\\d+)\\.$");




  /**
   * Handles lines notifying us that the primary observed game has changed.
   */

  private boolean handlePrimaryGameChanged(String line){
    if (!line.startsWith("Your primary game is now game "))
      return false;
    
    java.util.regex.Matcher matcher = PRIMARY_GAME_CHANGED_REGEX.matcher(line);
    if (!matcher.matches())
      return false;

    int gameNumber = Integer.parseInt(matcher.group(1));

    if (!processPrimaryGameChanged(gameNumber))
      processLine(line);

    return true;
  }



  /**
   * Gets called when a line notifying us that the primary observes game has
   * changed arrives.
   */

  protected boolean processPrimaryGameChanged(int gameNumber){
    return true;
  }

  /**
   * The regular expression matching lines that contain channels list state
   * changes. Adding and removing channels that is.
   */

  private static final Pattern USER_CHANNELS_LIST_CHANGES = Pattern.compile("^\\[([0-9]{1,3})\\]\\s(\\w*).*");

  /**
   * Method that handles lines informing about changes with channel list.
   */

  private boolean handleChannelListChanged(String line){
        Matcher matcher = USER_CHANNELS_LIST_CHANGES.matcher(line);
       

        if (!matcher.matches()){
            return false;
         }

         int channelNumber = Integer.parseInt(matcher.group(1));
         //System.out.println("NUMBER = " + channelNumber);
         String change = matcher.group(2);
         //System.out.println("CHANGE = " + change);
         //System.out.println(">>> CHANGE LINE RECEIVED FROM SERVER!!!");
         if (!processChannelListChanged(change, channelNumber)){
            processLine(line);
         }

         return true;
  }

  /**
   * Gets called when a line notifying about channel list change arrives from the
   * server.
   */

    protected boolean processChannelListChanged(String change, int channelNumber) {
        return false;
    }

    /**
     * Regular exrpession recognizing line before user's channel list.
     */

    private final java.util.regex.Pattern USER_CHANNEL_LIST_IS_NEXT = java.util.regex.Pattern.compile( "(" + USERNAME_REGEX + ")"  + " is in the following channels:");
    private final java.util.regex.Pattern USER_CHANNEL_LIST_IS_NEXT2 = java.util.regex.Pattern.compile("^(--) channel list: \\d+ channels (--)$");

    /**
     * Boolean that helps to determine if channel numbers contained in handled
     * line belong to user.
     */

    //private boolean userChannelListNext = false;

    /**
     * Method handling line that predates channel list.
     */

    private boolean handleUserChannelListNext(String line){
        java.util.regex.Matcher matcher = USER_CHANNEL_LIST_IS_NEXT.matcher(line);
        java.util.regex.Matcher matcher2 = USER_CHANNEL_LIST_IS_NEXT2.matcher(line);
        String userName = "";
        if (!matcher.matches() && !matcher2.matches()){
            return false;
        }
        if (matcher.matches()){
            userName = matcher.group(1);
        }
        //userChannelListNext = true;

        if (matcher2.matches()){
            userName = "ok";
            //System.out.println(userName);
        }
        
        if (!processUserChannelListNext(userName)){
            processLine(line);
        }
        
        

        return true;
    }

    /**
     * Method that is invoked when line arrives from server stating that next line
     * will contain user's channel list.
     */

    protected boolean processUserChannelListNext(String userName) {
        return false;
    }
    /**
     * The regular expression that gets called when user's channel list arrives
     * from server.
     */

    //private static final java.util.regex.Pattern USERS_CHANNEL_LIST_ARRIVES = java.util.regex.Pattern.compile("^(\\b[0-9]{1,3}\\b)((\\s){3,4}(\\b[0-9]{1,3}\\b)){1,19}");
    private static final java.util.regex.Pattern USERS_CHANNEL_LIST_ARRIVES = java.util.regex.Pattern.compile("^(\\d{1,3})[0-9\\s]*");
    /**
     * Method that handles lines containing channel list.
     */
    private boolean handleChannelListArrives(String line){
        java.util.regex.Matcher matcher = USERS_CHANNEL_LIST_ARRIVES.matcher(line);

        if (!matcher.matches()){
            return false;
        }

       // if (userChannelListNext == false){
        //    return false;
        //}
        //System.out.println(">>>CHANNEL LIST ARRIVED!!!");
        String channelsNumbers = matcher.group(0);
       

        if (!processChannelListArrives(channelsNumbers)){
            processLine(line);
        }

        return true;
    }

    /**
     * Method called when user's channel list arrives from server.
     */

    protected boolean processChannelListArrives(String channelsNumbers) {
        if (!this.fromPlugin){
            return false;
        }
        else {
            return true;
        }
    }
    
    /**
     * Regular expression for getting info about user's messages.
     */

      private static final Pattern USERS_MESSAGES_INFO = Pattern.compile("^You have (\\d+) (message |messages )\\((\\d+) unread\\)\\.$");
      
    /**
     * Method that handles line containing information about messages.
     */
    
    private boolean handleMessagesInfo(String line) {
        int unreadMessages;
        int allMessages;
        Matcher matcher = USERS_MESSAGES_INFO.matcher(line);
        
        if (!matcher.matches()){
            return false;
        }
        
        allMessages = Integer.parseInt(matcher.group(1));
        unreadMessages = Integer.parseInt(matcher.group(3));
        
        if (!processMessagesInfo(allMessages, unreadMessages)){
            processLine(line);
        }
        
        return true;
    }
    
    /**
     * Method called when information about messages arrives from server.
     */
    
    protected boolean processMessagesInfo(int allMessages, int unreadMessages){
        return false;
    
    }
    
    /**
     * Regular expression for recognizing messages from users.
     */
    //^(\d+)\. (\w+) at (\w+ \w+\s+\d{1,2}\, \d{1,2}\:\d{1,2} \w{1,3} \d{4})\:.+
    private static final Pattern MESSAGE_RECEIVED = Pattern.compile("^(\\d+)\\. " + "("+ USERNAME_REGEX +")" + " at (\\w{3} \\w{3}\\s+\\d{1,2}\\, \\d{1,2}\\:\\d{1,2} \\w{1,3} \\d{4})\\:(.+)");
    
    
    /**
     * Method that handles line containing message from users.
     */

    private boolean handleMessages(String line) {
        int number;
        String user;
        String date;
        String content;
        Matcher matcher = MESSAGE_RECEIVED.matcher(line);
        
        if (!matcher.matches()){
            return false;
        }
        
        number = Integer.parseInt(matcher.group(1));
        user = matcher.group(2);
        date = matcher.group(3);
        content = matcher.group(4);
        
        if (!processMessages(number, user, date, content)){
            processLine(line);
        }
        
        
        return true;
    }

    protected boolean processMessages(int number, String user, String date, String content) {
        return false;
    }
    
    private static final Pattern RECEIVED_MESSAGE = Pattern.compile("");
    private boolean handleMessageReceived(String line){return true;}
}

