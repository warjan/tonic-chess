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

package free.jin.console.fics;

import free.jin.Connection;
import free.jin.Preferences;
import free.jin.console.Console;
import free.jin.console.ConsoleTextField;
import free.jin.console.ConsoleTextPane;


/**
 * An extension of free.jin.console.Console which adds some FICS specific
 * features.
 */

public class FreechessConsole extends Console{



  /**
   * Creates a new FreechessConsole with the specified connection and
   * preferences.
   */

  public FreechessConsole(Connection conn, Preferences prefs){
    super(conn, prefs);
  }



  /**
   * Creates and returns a FreechessConsoleTextField.
   */

  protected ConsoleTextField createInputComponent(){
    return new FreechessConsoleTextField(this);
  }




  /**
   * Overrides <code>Console.createOutputComponent()</code> since we need a
   * special <code>ConsoleTextPane</code> for FICS.
   */

  protected ConsoleTextPane createOutputComponent(){
    return new FreechessConsoleTextPane(this);
  }


}
