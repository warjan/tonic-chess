/*
 * ClosealbeTabbedPane.java
 *
 * Created on 14 lipiec 2006, 14:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.tonic.ui.swing;

import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import org.tonic.ui.swing.CloseTabAction;

/**
 *
 * @author whp
 */
 public class CloseableTabbedPane extends JTabbedPane {
    
    private CloseTabAction closeTabAction = null;
    private JPopupMenu popup = null;
    private boolean hasPopup = false;
    
    public CloseableTabbedPane() {
        super();
        init();
    }
    
    public CloseableTabbedPane(int arg0) {
        super(arg0);
        init();
    }
    
    public CloseableTabbedPane(int arg0, int arg1) {
        super(arg0, arg1);
        init();
    }
    
    private void init() {
        setUI(new CloseableTabbedPaneUI());
        closeTabAction = new CloseTabAction() {
            public void act(CloseableTabbedPane closableTabbedPane, int tabIndex) {
                closableTabbedPane.removeTabAt(tabIndex);
            }
        };
    }
    
    public JPopupMenu getPopup(){
        return popup;
    }
    
    public void setPopup(final JPopupMenu popup){
        this.popup = popup;
        this.hasPopup = true;
    }
    
    public boolean getPopupExists(){
        return hasPopup;
    }
    
    public CloseTabAction getCloseTabAction() {
        return closeTabAction;
    }
    
    public void setCloseTabAction(CloseTabAction action) {
        closeTabAction = action;
    }
}
