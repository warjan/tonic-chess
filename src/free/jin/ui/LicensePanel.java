/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

package free.jin.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import free.jin.Jin;
import free.util.AWTUtilities;
import free.util.IOUtilities;
import free.util.swing.LinkLabel;
import free.util.swing.PlainTextDialog;
import free.util.swing.UrlDisplayingAction;


/**
 * Displays licensing and copyright information about Jin and bundled software.
 */

public class LicensePanel extends DialogPanel{

    /**
     * Translation source.
     */

    private ResourceBundle translation;



  /**
   * The font of the text area displaying the license text.
   */

  private static final Font LICENSE_TEXT_FONT = new Font("Monospaced", Font.PLAIN, 12);



  /**
   * The text of the GPL, loaded lazily.
   */

  private String gplText = null;



  /**
   * The text of the LGPL, loaded lazily.
   */

  private String lgplText = null;



  /**
   * The text of the jregex copyright, loaded lazily.
   */

  private String jregexCopyrightText = null;


  
  /**
   * The text of the jgoodies copyright, loaded lazily.
   */

  private String jgoodiesCopyrightText = null;

  

  /**
   * The text of the BeanShell license/copyright notice, loaded lazily.
   */

  private String beanshellCopyrightText = null;
  


  /**
   * Creates a new <code>LicensePanel</code>.
   */

  public LicensePanel(){
    createUI();
  }



  /**
   * The component to get the focus.
   */

  private Component focusComponent;



  /**
   * A workaround for ESCAPE not working in this dialog, under MS VM at least,
   * because we assign tooltips to some of the labels which makes them grab the
   * focus.
   */

  public void paint(Graphics g){
    super.paint(g);

    if (focusComponent != null){
      focusComponent.requestFocus();
      focusComponent = null;
    }
  }



  /**
   * Displays the panel.
   */

  public void display(){
    super.askResult();
  }



  /**
   * Returns the title of this <code>DialogPanel</code>.
   */

  protected String getTitle(){
    return "Credits and Copyrights in Tonic";
  }



  /**
   * Creates the user interface.
   */

  private void createUI(){
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    ActionListener gplActionListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (gplText == null){
          try{
            gplText = IOUtilities.loadText(Jin.class.getResource("legal/gpl.txt"));
          } catch (IOException e){
              e.printStackTrace();
              return;
            }
        }

        PlainTextDialog textDialog = new PlainTextDialog(LicensePanel.this, "The GNU General Public License", gplText);
        textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
        AWTUtilities.centerWindow(textDialog, getParent());
        textDialog.setVisible(true);
      }
    };

    ActionListener lgplActionListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (lgplText == null){
          try{
            lgplText = IOUtilities.loadText(Jin.class.getResource("legal/lgpl.txt"));
          } catch (IOException e){
              e.printStackTrace();
              return;
            }
        }

        PlainTextDialog textDialog = 
          new PlainTextDialog(LicensePanel.this, "The GNU Lesser General Public License", lgplText);
        textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
        AWTUtilities.centerWindow(textDialog, getParent());
        textDialog.setVisible(true);
      }
    };

    
    ActionListener jgoodiesActionListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (jgoodiesCopyrightText == null){
          try{
            jgoodiesCopyrightText = IOUtilities.loadText(Jin.class.getResource("legal/jgoodies.txt"));
          } catch (IOException e){
              e.printStackTrace();
              return;
            }
        }

        PlainTextDialog textDialog = new PlainTextDialog(LicensePanel.this, "The JGoodies License (BSD)", jgoodiesCopyrightText);
        textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
        AWTUtilities.centerWindow(textDialog, getParent());
        textDialog.setVisible(true);
      }
    };
    

    ActionListener beanshellActionListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (beanshellCopyrightText == null){
          try{
            beanshellCopyrightText = IOUtilities.loadText(Jin.class.getResource("legal/beanshell.txt"));
          } catch (IOException e){
              e.printStackTrace();
              return;
            }
        }

        PlainTextDialog textDialog = new PlainTextDialog(LicensePanel.this, "The BeanShell License", beanshellCopyrightText);
        textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
        AWTUtilities.centerWindow(textDialog, getParent());
        textDialog.setVisible(true);
      }
    };



    JPanel jinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    jinPanel.add(new JLabel("<html>Tonic is distributed under the&nbsp</html>"));
    LinkLabel jinGPLLabel = new LinkLabel("GNU General Public License");
    jinGPLLabel.addActionListener(gplActionListener);
    jinPanel.add(jinGPLLabel);
    jinPanel.add(new JLabel("<html>.</html>"));
    add(jinPanel);
    add(Box.createVerticalStrut(5));

    JPanel jinWebsitePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    jinWebsitePanel.add(new JLabel("<html>Tonic is based on Jin. More information about Jin is available at&nbsp</html>"));
    LinkLabel jinWebsiteLabel = new LinkLabel("the Jin website");
    jinWebsiteLabel.setToolTipText("http://www.jinchess.com");
    jinWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.jinchess.com"));
    jinWebsitePanel.add(jinWebsiteLabel);
    jinWebsitePanel.add(new JLabel("<html>.</html>"));
    add(jinWebsitePanel);

    add(Box.createVerticalStrut(10));
    add(new JSeparator());
    add(Box.createVerticalStrut(10));


    add(Box.createVerticalStrut(5));

    try{
      // Throws ClassNotFoundException if not found
      Class.forName("bsh.EvalError"); 
      
      JPanel beanshellPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      beanshellPanel.add(new JLabel("<html>Tonic uses the&nbsp</html>"));
      LinkLabel beanshellWebsiteLabel = new LinkLabel("BeanShell embeddable script interpreter");
      beanshellWebsiteLabel.setToolTipText("http://www.beanshell.org");
      beanshellWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.beanshell.org"));
      beanshellPanel.add(beanshellWebsiteLabel);
      beanshellPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
      LinkLabel beanshellLicenseLabel = new LinkLabel("LGPL and Sun licenses");
      beanshellLicenseLabel.addActionListener(beanshellActionListener);
      beanshellPanel.add(beanshellLicenseLabel);
      beanshellPanel.add(new JLabel("<html>.</html>"));
      add(beanshellPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e){}

    try{
      if (new File("./sox").exists()){
        JPanel soxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        soxPanel.add(new JLabel("<html>Tonic uses the&nbsp</html>"));
        LinkLabel soxWebsiteLabel = new LinkLabel("SoX - Sound eXchange commandline utility");
        soxWebsiteLabel.setToolTipText("http://sox.sourceforge.net");
        soxWebsiteLabel.addActionListener(new UrlDisplayingAction("http://sox.sourceforge.net"));
        soxPanel.add(soxWebsiteLabel);
        soxPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
        LinkLabel soxLicenseLabel = new LinkLabel("GNU Lesser General Public License");
        soxLicenseLabel.addActionListener(lgplActionListener);
        soxPanel.add(soxLicenseLabel);
        soxPanel.add(new JLabel("<html>.</html>"));
        add(soxPanel);
        add(Box.createVerticalStrut(5));
      }
    } catch (SecurityException e){}
    
    JPanel xboardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    xboardPanel.add(new JLabel("<html>Tonic contains a piece set from&nbsp</html>"));
    LinkLabel xboardWebsiteLabel = new LinkLabel("xboard/winboard");
    xboardWebsiteLabel.setToolTipText("http://www.tim-mann.org/xboard.html");
    xboardWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.tim-mann.org/xboard.html"));
    xboardPanel.add(xboardWebsiteLabel);
    xboardPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
    LinkLabel xboardLicenseLabel = new LinkLabel("GNU General Public License");
    xboardLicenseLabel.addActionListener(gplActionListener);
    xboardPanel.add(xboardLicenseLabel);
    xboardPanel.add(new JLabel("<html>.</html>"));
    add(xboardPanel);
    add(Box.createVerticalStrut(5));

    JPanel eboardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    eboardPanel.add(new JLabel("<html>Tonic includes a piece set from&nbsp</html>"));
    LinkLabel eboardWebsiteLabel = new LinkLabel("eboard");
    eboardWebsiteLabel.setToolTipText("http://eboard.sourceforge.net/");
    eboardWebsiteLabel.addActionListener(new UrlDisplayingAction("http://eboard.sourceforge.net/"));
    eboardPanel.add(eboardWebsiteLabel);
    eboardPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
    LinkLabel eboardLicenseLabel = new LinkLabel("GNU General Public License");
    eboardLicenseLabel.addActionListener(gplActionListener);
    eboardPanel.add(eboardLicenseLabel);
    eboardPanel.add(new JLabel("<html>.</html>"));
    add(eboardPanel);
    add(Box.createVerticalStrut(5));

    JPanel blitzinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    blitzinPanel.add(new JLabel("<html>Tonic includes piece sets, boards and sounds owned by the &nbsp</html>"));
    LinkLabel blitzinWebsiteLabel = new LinkLabel("Internet Chess Club");
    blitzinWebsiteLabel.setToolTipText("http://www.chessclub.com");
    blitzinWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.chessclub.com"));
    blitzinPanel.add(blitzinWebsiteLabel);
    blitzinPanel.add(new JLabel("<html>, used with permission.</html>"));
    add(blitzinPanel);
    add(Box.createVerticalStrut(5));

    try{
      // Throws ClassNotFoundException if not found
      Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
      
      JPanel kunststoffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      kunststoffPanel.add(new JLabel("<html>Tonic is distributed with the&nbsp</html>"));
      LinkLabel kunststoffWebsiteLabel = new LinkLabel("Kunststoff Look and Feel");
      kunststoffWebsiteLabel.setToolTipText("http://www.incors.org/");
      kunststoffWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.incors.org/"));
      kunststoffPanel.add(kunststoffWebsiteLabel);
      kunststoffPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
      LinkLabel kunststoffLicenseLabel = new LinkLabel("GNU Lesser General Public License");
      kunststoffLicenseLabel.addActionListener(lgplActionListener);
      kunststoffPanel.add(kunststoffLicenseLabel);
      kunststoffPanel.add(new JLabel("<html>.</html>"));
      add(kunststoffPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e){}

    try{
      // Throws ClassNotFoundException if not found
      Class.forName("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
      
      JPanel metouialfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      metouialfPanel.add(new JLabel("<html>Tonic is distributed with the&nbsp</html>"));
      LinkLabel metouialfWebsiteLabel = new LinkLabel("Metouia Look and Feel");
      metouialfWebsiteLabel.setToolTipText("http://mlf.sourceforge.net/");
      metouialfWebsiteLabel.addActionListener(
        new UrlDisplayingAction("http://mlf.sourceforge.net/"));
      metouialfPanel.add(metouialfWebsiteLabel);
      metouialfPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
      LinkLabel metouialfLicenseLabel = new LinkLabel("GNU Lesser General Public License");
      metouialfLicenseLabel.addActionListener(lgplActionListener);
      metouialfPanel.add(metouialfLicenseLabel);
      metouialfPanel.add(new JLabel("<html>.</html>"));
      add(metouialfPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e){}
    
    try{
      // Throws ClassNotFoundException if not found
      Class.forName("com.birosoft.liquid.LiquidLookAndFeel");
      
      JPanel liquidlfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      liquidlfPanel.add(new JLabel("<html>Tonic is distributed with the&nbsp</html>"));
      LinkLabel liquidlfWebsiteLabel = new LinkLabel("Liquid Look and Feel");
      liquidlfWebsiteLabel.setToolTipText("http://liquidlnf.sourceforge.net/");
      liquidlfWebsiteLabel.addActionListener(
        new UrlDisplayingAction("http://liquidlnf.sourceforge.net/"));
      liquidlfPanel.add(liquidlfWebsiteLabel);
      liquidlfPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
      LinkLabel liquidlfLicenseLabel = new LinkLabel("GNU Lesser General Public License");
      liquidlfLicenseLabel.addActionListener(lgplActionListener);
      liquidlfPanel.add(liquidlfLicenseLabel);
      liquidlfPanel.add(new JLabel("<html>.</html>"));
      add(liquidlfPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e){}
    
    try{
      // Throws ClassNotFoundException if not found
      Class.forName("com.jgoodies.plaf.plastic.PlasticLookAndFeel");
      
      JPanel jgoodieslfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      jgoodieslfPanel.add(new JLabel("<html>Tonic is distributed with the&nbsp</html>"));
      LinkLabel jgoodieslfWebsiteLabel = new LinkLabel("JGoodies Looks");
      jgoodieslfWebsiteLabel.setToolTipText("http://jgoodies.dev.java.net");
      jgoodieslfWebsiteLabel.addActionListener(
        new UrlDisplayingAction("http://jgoodies.dev.java.net"));
      jgoodieslfPanel.add(jgoodieslfWebsiteLabel);
      jgoodieslfPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
      LinkLabel jgoodieslfLicenseLabel = new LinkLabel("BSD License");
      jgoodieslfLicenseLabel.addActionListener(jgoodiesActionListener);
      jgoodieslfPanel.add(jgoodieslfLicenseLabel);
      jgoodieslfPanel.add(new JLabel("<html>.</html>"));
      add(jgoodieslfPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e){}
    
    /*JPanel denisDesLauriersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    denisDesLauriersPanel.add(new JLabel("<html>The Tonic logo was designed by&nbsp</html>"));
    LinkLabel denisDesLauriersWebsiteLabel = new LinkLabel("Denis DesLauriers");
    denisDesLauriersWebsiteLabel.setToolTipText("http://www.chess-art.com");
    denisDesLauriersWebsiteLabel.addActionListener(
      new UrlDisplayingAction("http://www.chess-art.com"));
    denisDesLauriersPanel.add(denisDesLauriersWebsiteLabel);
    denisDesLauriersPanel.add(new JLabel("<html>, licensed under the&nbsp</html>"));
    LinkLabel logoLicenseLabel = new LinkLabel("GNU General Public License");
    logoLicenseLabel.addActionListener(gplActionListener);
    denisDesLauriersPanel.add(logoLicenseLabel);
    denisDesLauriersPanel.add(new JLabel("<html>.</html>"));
    add(denisDesLauriersPanel);*/
    

    add(Box.createVerticalStrut(30));

    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ClosingListener(null));
    okButton.setAlignmentX(CENTER_ALIGNMENT);
    add(okButton);

    setDefaultButton(okButton);

    focusComponent = okButton;
  }



}
