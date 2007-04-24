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
import java.util.Map;
import java.util.HashMap;


/**
 * An DDI (Dockable Document Interface) implementation of <code>UIProvider</code> - each
 * <code>PluginUIContainer</code> is implemented via a
 * <code>Dockable</code>, all of which sit inside a main
 * <code>JFrame</code>. This implementation uses flexdock from flexdock.dev.java.net.
 */

public class DdiUiProvider extends AbstractUiProvider{
    
    private MainDockPane mainFrame;
    private Viewport viewport;
    private JMenuBar menubar;
    private ActionsMenu actionsmenu;
    private PluginContainersMenu windowsMenu;
    private PrefsMenu preferencesMenu;
    private View viewPUC;
    private Map<String, View> viewsMap;
    private boolean consoleReady;

    public DdiUiProvider(){
        
    }
    public void start(){
        super.start();
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {

                viewsMap = new HashMap<String, View>(8);
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
                menubar.add(new ConnectionMenu());
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

        
        protected DockableUIContainer(Plugin plugin, String id, int mode){
            super(plugin, id, mode);
            this.id = id;

            System.out.println("NEW VIEW ID = " + id);
     
            viewPUC = View.getInstance(id);
            System.out.println("NEW VIEW INFO = " + viewPUC);

            //viewPUC.setTitlebar(new PluginUITitlebar());
//            View.getInstance(id).addAction(CLOSE_ACTION);
//            View.getInstance(id).addAction(PIN_ACTION);
           

            //View.getInstance(id).setContentPane(new JPanel());
             if (id.equals("console.main")){
                consoleReady = true;
                }
            //viewsMap.put(id, viewPUC);

        }
        protected void disposeImpl() {
            DockingManager.close(View.getInstance(id));
            viewsMap.remove(id);
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
            /*viewsMap.get(id).setTitle(title);
            viewsMap.get(id).setTabText(title);*/
            View.getInstance(id).setTitle(title);
            View.getInstance(id).setTabText(title);
        }
        
        protected void setIconImpl(Image icon) {
        }
        
        public void setSize(int width, int height) {
            View.getInstance(id).setPreferredSize(new Dimension(width, height));
            //viewsMap.get(id).setPreferredSize(new Dimension(width, height));
        }
        
        public Container getContentPane() {
           return View.getInstance(id).getContentPane();
            //return viewsMap.get(id).getContentPane();
        }
        
        
        public boolean isVisible() {
           // return viewsMap.get(id).isVisible();
            return View.getInstance(id).isVisible();
        }
        
        public void setActive(boolean active) {
            //viewsMap.get(id).setActive(active);
            View.getInstance(id).setActive(active);
        }
        
        public boolean isActive() {
            //return viewsMap.get(id).isVisible() && viewsMap.get(id).isActive();
            return View.getInstance(id).isVisible() && View.getInstance(id).isActive();
        }

        public void show(){
            firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_SHOWN));
        }
        
        public void hide(){
            //viewsMap.get(id).setVisible(false);
            View.getInstance(id).setVisible(false);
            DockingManager.close(View.getInstance(id));
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
        


            MainDockPane(){
                super();
                configureDocking();
                setContentPane(createContentPane());
            }

            public Viewport getViewport(){
                return viewport;
            }
            private JPanel createContentPane() {
                JPanel p = new JPanel(new BorderLayout(0, 0));
                p.setBorder(new EmptyBorder(0, 0, 2, 2));

                viewport = new Viewport();

               
                viewport.setSingleTabAllowed(true);
                System.out.println(p.getHeight() + " x " + p.getWidth());
                viewport.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
                
                
                Border outerBorder = BorderFactory.createEmptyBorder(0,0,2,2);
                Border innerBorder = new ShadowBorder();
                viewport.setBorderManager(new StandardBorderManager(BorderFactory.createCompoundBorder(outerBorder, innerBorder)));

                p.add(viewport, BorderLayout.CENTER);
                
                
                return p;
             }
        }

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

            View view;
//            if (View.getInstance(dockableId) != null){
//               view = View.getInstance(dockableId);
//           } else{
                view = new View(dockableId);
                view.setContentPane(new JPanel());
                view.addAction(CLOSE_ACTION);
                view.addAction(PIN_ACTION);
//            }

            if (!consoleReady){
                mainFrame.getViewport().dock(view);
            }else{
                //viewsMap.get("console.main").dock(view);
                View.getInstance("console.main").dock(view);
                System.out.println("MAIN CONSOLE TITLE = " + View.getInstance("console.main").getTitle());
                //viewsMap.get("console.main").setTabText(viewsMap.get("console.main").getTitle());

                
                System.out.println("CONSOLE VIEW INSTANCE = " + View.getInstance("console.main"));
                System.out.println("CONSOLE VIEW TAB TEXT = " + View.getInstance("console.main").getTabText());
            }

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

    class PluginUITitlebar extends Titlebar {
        PluginUITitlebar(){
            super();



        }



        public Dimension getPreferredSize(){
            return new Dimension(100, 24);
        }

    }
    
    
}




