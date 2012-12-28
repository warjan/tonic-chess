/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
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

package free.jin.console.fics;

import free.jin.console.Console;
import free.jin.console.ConsoleManager;
import free.jin.event.ChatEvent;
import free.jin.ui.OptionPanel;
import free.jin.ui.PreferencesPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * An extension of the default ConsoleManager for the freechess.org server.
 */

public class FreechessConsoleManager extends ConsoleManager{



  /**
   * Creates a FreechessConsole.
   */

  protected Console createConsole(){
    return new FreechessConsole(getConn(), getPrefs());
  }

    /**
     * Console types for Freechess console.
     * @return ct array of strings - console types
     */
    protected String[] getConsoleTypes(){
      //Typed mapped to strings are:
        //          announcement         qtell       channel-tell        cshout          kibitz          say             ptell           tell            shout         ishout              (qtell.tourney tshout)      whisper
        //indices       0                   1               2               3           4               5                   6               7             8             9                         10                  11              12
      String[] ct = {"Announcements", "Bots shouts", "Channel tells", "Chess shouts", "Kibitzes", "Opponents tells", "Partner tells", "Private tells", "Shouts", "Special shouts (-->)", "Tourney announcments", "Whispers", "Custom console..." };
      return ct;
  }

    protected JButton getNewConsoleButton(){
        JButton button = new JButton("New console");
        button.addActionListener(new NewConsoleListener()
        );
        return button;
    }

    protected JComboBox getNewConsoleSpec(){
        JComboBox combobox = new JComboBox(getConsoleTypes());
        combobox.addActionListener( new ActionListener(){
                       public void actionPerformed(ActionEvent e) {
                           int consoleSpec = newConsoleSpec.getSelectedIndex();
                           if (consoleSpec != 2 && consoleSpec != 4 && consoleSpec != 5 && consoleSpec != 7 && consoleSpec != 11 && consoleSpec != 12)
                           {
                              chanGameNumberInput.setText("");
                              chanGameNumberInput.setEnabled(false);
                          } else {
                              chanGameNumberInput.setEnabled(true);
                          }
                       }
                   });
        return combobox;
    }

    /**
     * Overrides method returning a type of new console.
     */

    @Override
    protected String getConsoleType(){

        String type = "";
        int typeId = newConsoleSpec.getSelectedIndex();
        switch(typeId){
            case 0: type = "announcement"; break;
            case 1: type = "qtell"; break;
            case 2: type = "channel-tell " + chanGameNumberInput.getText(); break;
            case 3: type = "cshout"; break;
            case 4: type = "kibitz " + chanGameNumberInput.getText().toLowerCase(); break;
            case 5: type = "say " + chanGameNumberInput.getText().toLowerCase(); break;
            case 6: type = "ptell"; break;
            case 7: type = "tell " + chanGameNumberInput.getText().toLowerCase(); break;
            case 8: type = "shout"; break;
            case 9: type = "ishout"; break;
            case 10: type = "tshout"; break;
            case 11: type = "whisper " + chanGameNumberInput.getText().toLowerCase(); break;
            case 12: type = chanGameNumberInput.getText(); break;

        }


        return type;
    }
    
    /**
     * Overrides method that returns new console name.
     */

      @Override
      protected String getConsoleName(){
          String name = "";
          int typeId = newConsoleSpec.getSelectedIndex();
          switch(typeId){
              case 0: name = "Announcement"; break;
              case 1: name = "Bots tells"; break;
              case 2: name = "Channel " + chanGameNumberInput.getText() + " tells"; break;
              case 3: name = "Chess shouts"; break;
              case 4: name = "kibitz " + chanGameNumberInput.getText(); break;
              case 5: name = "Oppenent tells from " + chanGameNumberInput.getText(); break;
              case 6: name = "Partner tells"; break;
              case 7: name = "Private tells from " + chanGameNumberInput.getText(); break;
              case 8: name = "Normal shouts"; break;
              case 9: name = "Special shouts(-->)"; break;
              case 10: name = "Tournay shouts"; break;
              case 11: name = "Whispers for game " + chanGameNumberInput.getText(); break;
              case 12: name = "Console for chat message types: " + chanGameNumberInput.getText(); break;

          }


          return name;
      }

    /*public void setConsoleName(String name){
        this.na
    } */



  /**
   * Overrides <code>chatMessageReceived(ChatEvent)</code> to notify the
   * <code>Console</code> when tells are received.
   */

  public void chatMessageReceived(ChatEvent evt){
    super.chatMessageReceived(evt);

    if (isPaused())
      return;
    
    String type = evt.getType();
    if (type.equals("tell") || type.equals("say") || type.equals("ptell"))
      console.tellReceived(evt.getSender());
  }



  
  /**
   * Returns the string that should be displayed according to the given
   * ChatEvent.
   */

  protected String translateChat(ChatEvent evt){
    String type = evt.getType();
    String sender = evt.getSender();
    String title = evt.getSenderTitle();
    String rating = evt.getSenderRating() == -1 ? "----" : String.valueOf(evt.getSenderRating());
    String message = evt.getMessage();
    Object forum = evt.getForum();

    // Tells
    if (type.equals("tell"))
      return sender + title + " tells you: " + message;
    else if (type.equals("say"))
      return sender + title + " says: " + message;
    else if (type.equals("ptell"))
      return sender + title + " (your partner) tells you: " + message;
    else if (type.equals("qtell"))
      return ":" + message;
    else if (type.equals("qtell.tourney"))
      return ":" + sender + title + "(T" + forum + "): " + message;

    // Channel tells
    else if (type.equals("channel-tell"))
      return sender + title + "("+forum+"): " + message;

    // Kibitzes and whispers
    else if (type.equals("kibitz"))
      return sender + title + "(" + rating + ")[" + forum + "] kibitzes: " + message;
    else if (type.equals("whisper"))
      return sender + title + "(" + rating + ")[" + forum + "] whispers: " + message;

    // Shouts
    else if (type.equals("shout"))
      return sender + title + " shouts: " + message;
    else if (type.equals("ishout"))
      return "--> " + sender + title + " " + message;
    else if (type.equals("tshout"))
      return ":" + sender + title + " t-shouts: " + message;
    else if (type.equals("cshout"))
      return sender + title + " c-shouts: " + message;
    else if (type.equals("announcement"))
      return "    **ANNOUNCEMENT** from " + sender + ": " + message; 

    return evt.toString();
  }

    protected JTextField getChanGameNrInput(){
        JTextField jtf = new JTextField("", 10);
        jtf.addActionListener(new NewConsoleListener());
        return jtf;
    }



  /**
   * Return a PreferencesPanel for changing the console manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new FreechessConsolePrefsPanel(this);
  }


    private class NewConsoleListener implements ActionListener {
        public void actionPerformed(ActionEvent evt){
            String consoleSubtype = chanGameNumberInput.getText();
            if (
                    ((String)newConsoleSpec.getSelectedItem()).equals("Channel tells") && !(consoleSubtype.matches("\\d{1,3}"))){
                OptionPanel.error(console, "Channel number error", "Type a number from 1 to 255, please.");
            }

            if (((String)newConsoleSpec.getSelectedItem()).equals("Specific game") && !(consoleSubtype.matches("\\d{1,4}"))){
                OptionPanel.error(console, "Game number error", "Type a number from 1 to 255, please.");
            }
            else{
            if (((String)newConsoleSpec.getSelectedItem()).equals("Private tells")){

                openNewConsole("tell " + consoleSubtype, null, null);
            }
            /*if (((String)newConsoleSpec.getSelectedItem()).matches("(Kibitzes)|(Whispers)")){

            } */

                else{
                //System.out.println("EQUALS!!! " + chanGameNumberInput.getText() + "<->" +newConsoleSpec.getSelectedItem());
              openNewConsole(null, null, null);
              }
            }
        }
    }
}
