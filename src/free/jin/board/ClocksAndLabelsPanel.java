package free.jin.board;

import free.jin.BadChangesException;
import free.jin.ui.PreferencesPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: wojtek
 * Date: 30.12.12
 * Time: 02:36
 * To change this template use File | Settings | File Templates.
 */
public class ClocksAndLabelsPanel extends PreferencesPanel {

    private final BoardManager boardManager;
    private final JCheckBox hideOpponentsRatingCheckBox;
    private final JCheckBox hideOpponentsNameCheckBox;


    public ClocksAndLabelsPanel(BoardManager boardManager) {
        this.boardManager = boardManager;
        hideOpponentsRatingCheckBox = new JCheckBox("Hide opponent's rating?", boardManager.isHidingOpponentsRating());
        hideOpponentsRatingCheckBox.addActionListener(new PrefsActionListener());

        hideOpponentsNameCheckBox = new JCheckBox("Hide oppenent's name?", boardManager.isHidingOpponentsName());
        hideOpponentsNameCheckBox.addActionListener(new PrefsActionListener());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(hideOpponentsRatingCheckBox);
        add(hideOpponentsNameCheckBox);

    }

    @Override
    public void applyChanges() throws BadChangesException {
        boardManager.setHidingOpponentsRating(hideOpponentsRatingCheckBox.isSelected());
        boardManager.setHidingOpponentsName(hideOpponentsNameCheckBox.isSelected());
    }

    private class PrefsActionListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                fireStateChanged();
            }
        }
}
