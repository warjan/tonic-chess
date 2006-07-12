/*
 * Main.java
 *
 * Created on 5 luty 2006, 15:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.action.showratinggraph;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import free.jin.Jin;
/**
 *
 * @author whp
 */
public class RatingGraphFrame extends JFrame{
     
    private final JFrame frame;
    private final JPanel panel;
    private final JLabel label;
    //private final Image image;
    private URL url= null;
    private final ImageIcon image;
    String username = Jin.getInstance().getConnManager().getSession().getUser().getUsername();
    
    
    /** Creates a new instance of Main */
    public RatingGraphFrame() {
        frame = new JFrame("Test frame");
        panel = new JPanel();
        label = new JLabel();
        url = getUrl();
        image = new ImageIcon(url);
        label.setIcon(image);
        panel.add(label);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

    }
    
    private URL getUrl(){
    
        try{
            url = new URL("http://62.216.12.238/maciejg/png/" + username + ".png");
        }
        catch  (MalformedURLException e){
            System.err.println(e.getMessage());
        }
        return url;
    }
    
    

    
}
