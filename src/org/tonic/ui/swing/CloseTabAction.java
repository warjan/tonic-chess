/*
 * CloseTabAction.java
 *
 * Created on 14 lipiec 2006, 14:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.tonic.ui.swing;

/**
 *
 * @author whp
 */
public interface CloseTabAction {
    public void act(CloseableTabbedPane closableTabbedPane, int tabIndex);
}
