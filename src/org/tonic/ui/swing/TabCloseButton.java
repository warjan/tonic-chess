package org.tonic.ui.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Wojtek
 * Date: 07.05.11
 * Time: 00:19
 * Component for closing tabs.
 */
public class TabCloseButton extends JPanel {
    private final JTabbedPane pane;

    public TabCloseButton(JTabbedPane pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.pane = pane;
    }
}
