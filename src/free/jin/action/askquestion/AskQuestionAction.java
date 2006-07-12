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

package free.jin.action.askquestion;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import free.jin.action.JinAction;
import free.jin.ui.DialogPanel;
import free.workarounds.FixedJTextField;


/**
 * An action which displays a panel where the user can type a question, which
 * is then sent to the server's help channel.
 */

public class AskQuestionAction extends JinAction{
  
  
  
  /**
   * Returns the id of the action - "askquestion".
   */
   
  public String getId(){
    return "askquestion";
  }
  
  
  
  /**
   * Returns the name of the action.
   */
   
  public String getName(){
    return "Ask for Help..."; 
  }
  
  
  
  
  /**
   * Displays a small dialog which lets the user ask a question.
   */
   
  public void go(){
    String question = new QuestionPanel().getQuestion();
    
    if ((question != null) && !"".equals(question.trim()))
      getConn().sendHelpQuestion(question);
  }


  
  
  
  /**
   * A panel which asks the user to type in a question.
   */
   
  private static class QuestionPanel extends DialogPanel{
    
    
    
    /**
     * Creates a new <code>QuestionPanel</code>.
     */
     
    public QuestionPanel(){
      setLayout(new BorderLayout(10, 10));
      
      String [] labelText = new String[]{
        "Type in a question and press the OK button.",
        "Please be patient and watch for a response in the console window."
      };
      JPanel labelsPanel = new JPanel(new GridLayout(labelText.length, 1));
      for (int i = 0; i < labelText.length; i++)
        labelsPanel.add(new JLabel(labelText[i]));
      
      
      final JTextField questionField = new FixedJTextField(30);
      JLabel questionLabel = new JLabel("Question:");
      questionLabel.setDisplayedMnemonic('Q');
      questionLabel.setLabelFor(questionField);
      JPanel textFieldPanel = new JPanel(new BorderLayout(10, 10));
      textFieldPanel.add(questionLabel, BorderLayout.WEST);
      textFieldPanel.add(questionField, BorderLayout.CENTER);
      
      JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JButton okButton = new JButton("OK");
      JButton cancelButton = new JButton("Cancel");
      buttonsPanel.add(okButton);
      buttonsPanel.add(cancelButton);
      
      add(labelsPanel, BorderLayout.NORTH);
      add(textFieldPanel, BorderLayout.CENTER);
      add(buttonsPanel, BorderLayout.SOUTH);
      
      
      setDefaultButton(okButton);
      okButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          close(questionField.getText());
        }
      });
      
      cancelButton.addActionListener(new ClosingListener(null));
    }
    
    
    
    /**
     * Returns the title of the panel.
     */
     
    public String getTitle(){
      return "Ask a Question"; 
    }
    
    
    
    /**
     * Displays the panel to the user and returns the question he typed. Returns
     * <code>null</code> if the user canceled the dialog.
     */
     
    public String getQuestion(){
      return (String)askResult();
    }
    
    
    
  }

  
   
}
