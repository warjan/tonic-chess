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

package free.jin.action.flipboard;


import free.jin.action.JinAction;
import java.util.Locale;

import java.util.ResourceBundle;


/**
 * An action which shows server specific help to the user.
 */

public class FlipBoard extends JinAction{


    private ResourceBundle translation;



  /**
   * Causes the board  to be flipped.
   */

  public void go(){
    getConn().sendCommand("flip");
    getConn().sendCommand("refresh");
  }

    public String getId() {
        return "flipboard";
    }

  /**
   * Returns the name of the action.
   */

 public String getName(){
        translation = ResourceBundle.getBundle("free.jin.action.flipboard.flipboard");
        String s = translation.getString("name");
       // s = translate(s);
        return s;



}

}