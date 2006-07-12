/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003-2005 Alexander Maryanovsky.
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

import free.jin.Jin;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.util.AWTUtilities;
import free.util.RectDouble;
import org.flexdock.docking.DockableFactory;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.perspective.LayoutSequence;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveFactory;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.flexdock.view.Titlebar;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * An DDI (Dockable Document Interface) implementation of <code>UIProvider</code> - each
 * <code>PluginUIContainer</code> is implemented via a
 * <code>Dockable</code>, all of which sit inside a main
 * <code>JFrame</code>. This implementation uses flexdock from flexdock.dev.java.net.
 */

public class DdiUiProvider extends AbstractUiProvider{
    
    private MainDockPane mainFrame;
    private Viewport viewPort;
    private JMenuBar menubar;
    private ActionsMenu actionsmenu;
    private PluginContainersMenu windowsMenu;
    private PrefsMenu preferencesMenu;
    private View viewPUC;
    
    public DdiUiProvider(){
        
    }
    public void start(){
        super.start();
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {

                mainFrame = getMainFrame();
            
                restoreFrameGeometry(Jin.getInstance().getPrefs(), mainFrame, "frame.",
                        new RectDouble(1d/16, 1d/16, 7d/8, 7d/8));
                mainFrame.setTitle(Jin.getInstance().getAppName());
                mainFrame.setIconImage(mainFrame.getToolkit().getImage(Jin.class.getResource("resources/icon.gif")));
                mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                
                menubar = new JMenuBar();
                actionsmenu = new ActionsMenu();
                windowsMenu = new PluginContainersMenu("Windows", 'W');
                preferencesMenu = new PrefsMenu();
                addPluginUIContainerCreationListener(windowsMenu);
                
                
                
                mainFrame.setJMenuBar(menubar);
                
                
                mainFrame.addWindowListener(new WindowAdapter(){
                    public void windowClosing(WindowEvent evt){
                        Jin.getInstance().quit(true);
                    }
                });
                
                //mainFrame.setContentPane(createDockingPane());
                mainFrame.addWindowListener(new WindowAdapter(){
                    public void windowOpened(WindowEvent e){
                        mainFrame.removeWindowListener(this);
                        menubar.add(actionsmenu);
                        menubar.add(windowsMenu);
                        menubar.add(preferencesMenu);
                        Jin.getInstance().getConnManager().start();
                    }
                });
                //mainFrame.pack();
                //mainFrame.setContentPane(createDockingPane());
                mainFrame.setVisible(true);
            }
            
        });
        
    }
    
    public PluginUIContainer createPluginUIContainer(final Plugin plugin, final String id, final int mode) {
  

                AbstractPluginUIContainer container = new DockableUIContainer(plugin, id, mode);


                addPluginContainer(plugin, id, container);

        
        return container;
    }
    
    public void showDialog(DialogPanel dialog, Component parent) {
        Frame parentFrame = mainFrame;
        if (parent != null){
            parentFrame = AWTUtilities.frameForComponent(parent);
        }
        
        dialog.show(new JDialog(parentFrame), parent == null ? parentFrame : parent);
    }
    
    public boolean isUiVisible() {
        return mainFrame.isVisible();
    }
    
    public void stop() {
        saveFrameGeometry(Jin.getInstance().getPrefs(), mainFrame, "frame.");
        mainFrame.dispose();
    }
    
    private MainDockPane getMainFrame() {
        MainDockPane frame = new MainDockPane();
        return frame;
        
        
    }
    
    /*private Container createDockingPane() {
     
     
    }*/
    
    
    
    class DockableUIContainer extends AbstractPluginUIContainer {
        
        String id;
        View viewPUC;
        
        protected DockableUIContainer(Plugin plugin, String id, int mode){
            super(plugin, id, mode);
            this.id = id;
            viewPUC = View.getInstance(id);
            //viewPUC.setTitlebar(new PluginUITitlebar());
            viewPUC.addAction(DockingConstants.CLOSE_ACTION);
            viewPUC.addAction(DockingConstants.PIN_ACTION);
           

            viewPUC.setContentPane(new JPanel());

        }
        protected void disposeImpl() {
        }
        
        protected void loadState() {
        }
        
        protected void saveState() {
        }
        
        protected void insertMenu(JMenu menu, int index) {
        }
        
        protected int getMenuCount() {
            return 0;
        }
        
        protected void setTitleImpl(String title) {
            viewPUC.setTitle(title);
            viewPUC.setTabText(title);
        }
        
        protected void setIconImpl(Image icon) {
        }
        
        public void setSize(int width, int height) {
        }
        
        public Container getContentPane() {
            return viewPUC.getContentPane();
            
        }
        
        
        public boolean isVisible() {
            return viewPUC.isVisible();
        }
        
        public void setActive(boolean active) {
        }
        
        public boolean isActive() {
            return viewPUC.isVisible() && viewPUC.isActive();
        }

        public void show(){
            
            
            //            View view1 = new View("lo", "Low");
            //            JTextField jtf = new JTextField();
            //
            //            jtf.setText("Testing flexdock is not always easy");
            //            view1.getContentPane().add(jtf);
            //
            //            view1.setSize(500,500);
            //            viewPort.dock(view1);
            
            
            //mainFrame.add(viewPUC);
            //mainFrame.getViewport().dock(viewPUC);
            firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_SHOWN));
        }
        
        public void hide(){
            viewPUC.setVisible(false);
            firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_HIDDEN));
        }
        
        public void setVisible(boolean setVisible){
            if (isVisible()){
                return;
            }
            if(setVisible){
                show();
            } else{
                hide();
            }
        }
        
    }
    
    class MainDockPane extends JFrame implements DockingConstants{
        
            Viewport viewport;

            MainDockPane(){
                super();
                configureDocking();
                //this.setLayout(new BorderLayout());
                setContentPane(createContentPane());
                //this.getContentPane().add(createContentPane());


            }

            public Viewport getViewport(){
                return viewport;
            }
            private JPanel createContentPane() {
                JPanel p = new JPanel(new BorderLayout(0, 0));
                p.setBorder(new EmptyBorder(0, 0, 2, 2));

                viewport = new Viewport();
                /*View fake1 = new View("region.east", "East");
                View fake2 = new View("region.west", "West");
                View fake3 = new View("region.north", "North");
                View fake4 = new View("region.center", "Center");
                
                viewport.dock(fake4);
                fake4.dock(fake1, DockingConstants.EAST_REGION);
                fake4.dock(fake2, WEST_REGION);
                fake4.dock(fake3, NORTH_REGION);*/
                                 
               
                viewport.setSingleTabAllowed(true);
                System.out.println(p.getHeight() + " x " + p.getWidth());
                viewport.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
                
                
                Border outerBorder = BorderFactory.createEmptyBorder(0,0,2,2);
                Border innerBorder = new ShadowBorder();
                viewport.setBorderManager(new StandardBorderManager(BorderFactory.createCompoundBorder(outerBorder, innerBorder)));
                //View startPage = createStartPage();
                p.add(viewport, BorderLayout.CENTER);
                
                
                return p;
                }
             
        }
        /*private Container createContentPane(){
            JPanel panel = new JPanel(new BorderLayout(0,0));
            panel.setBorder(new EmptyBorder(5,5,5,5));
         
            viewPort = new Viewport();
            panel.add(viewPort, BorderLayout.CENTER);
            viewPort.setSingleTabAllowed(false);
         
            return panel;
        }*/

        //private View createStartPage() {
        //    return new View("new view", "New view");
        //}

        private void configureDocking() {
            
            DockingManager.setDockableFactory(new PluginUIFactory());
            DockingManager.setFloatingEnabled(true);
            
            
            PerspectiveManager.setFactory(new TonicPerspectiveFactory());
	    PerspectiveManager.setRestoreFloatingOnLoad(true);
	    PerspectiveManager mgr = PerspectiveManager.getInstance();
	    mgr.setCurrentPerspective("default", true);
        }
        
    
    class PluginUIFactory extends DockableFactory.Stub implements DockingConstants{
        public Component getDockableComponent(String dockableId){
            /*View view1 = null;
            View view = null;
            System.out.println(dockableId);
            if(dockableId.equals("console.main")){
                view1 = new View(dockableId, "Main console");
                view1.setContentPane(new JPanel());
                mainFrame.getViewport().dock(view);
                return view1;
            }
            else{
            view = new View(dockableId, dockableId);
            view.setContentPane(new JPanel());
                if (view1 != null){
                    view1.dock(view, EAST_REGION);
                    return view;
                }
            }
            return new View(dockableId, dockableId);*/
            View view = new View(dockableId);
            //view.setTitle(dockableId, true);
            view.setContentPane(new JPanel());
            //Titlebar pluginUITitlebar = new PluginUITitlebar();
            //pluginUITitlebar.add(new JButton("go"));
            //view.setTitlebar(pluginUITitlebar);
            //view.setTitlebar(new Titlebar());
            mainFrame.getViewport().dock(view);

            return view;
        }
        
    }
    
    class TonicPerspectiveFactory implements PerspectiveFactory{

		public Perspective getPerspective(String arg0) {
			Perspective perspective = new Perspective("default", "Default");
                        LayoutSequence sequence = perspective.getInitialSequence();
                        //sequence.add(viewPUC);
                        
			return perspective;
		}
    	
    }

    class PluginUITitlebar extends Titlebar{
        PluginUITitlebar(){
            super();



        }

        /*protected void paintComponent(Graphics g){
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillRect(0,0, this.getWidth(),24);
        }*/

    }
    
    
}




