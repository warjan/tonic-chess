/*
 * ShowRatingGraph.java
 *
 * Created on 5 luty 2006, 22:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package free.jin.action.showratinggraph;

import free.jin.action.JinAction;

/**
 *
 * @author whp
 *  Action that will show his rating graph to user. 
 */


public class ShowRatingGraph extends JinAction {
    
    /** Creates a new instance of ShowRatingGraph */
    public ShowRatingGraph() {
    }

    public void go() {
        RatingGraphFrame graph = new RatingGraphFrame();
    }

    public String getName() {
        return "Show rating graph";
    }

    public String getId() {
        return "showratinggraph";
    }
        public String getBundlePath() {
        return "";
    }
    
}
