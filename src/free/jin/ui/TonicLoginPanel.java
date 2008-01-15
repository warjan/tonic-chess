package free.jin.ui;

import free.jin.ConnectionDetails;
import free.jin.Server;

import javax.swing.*;

/**
 * New login class for login panel in Tonic.
 */
public class TonicLoginPanel extends DialogPanel {
    /**
     * Combo box containing servers configurations names.
     */
    JComboBox serversBox;

    /**
     * Labels for handle and password text fields.
     */
    JLabel handleLabel, passwordLabel;

    /**
     * Text field into which user types his handle.
     */
    JTextField handleField;

    /**
     * Password field into which user types his handle.
     */
    JPasswordField passwordField;

    /**
     * Checkbox for storing password.
     */

    JCheckBox passwordStore;

    /**
     * Checkbox for autologin.
     */

    JCheckBox autologin;

    /**
     * Button that inities connection to server.
     */
    JButton loginButton;

    /**
     * Model that holds servers configurations.
     */
    private ComboBoxModel serversBoxModel;

    /**
     * Creates new <code>TonicLoginPanel</code> with specified connection details.
     * @param details
     */

    public TonicLoginPanel(ConnectionDetails details){
        this(details.getServer(), details);
    }

    /**
     * Creates new <code>TonicLoginPanel</code> with specified server.
     * @param server
     */
    public TonicLoginPanel(Server server){
        this(server, null);
    }

    /**
     * Creates new <code>TonicLoginPanel</code> with specified server and connection details.
     * This is the ultimate constructor of this class.
     * @param server
     * @param details
     */
    public TonicLoginPanel(Server server, ConnectionDetails details) {

        createUI();

    }

    /**
     * Creates the components of this panel
     */
    private void createUI() {
        handleLabel = new JLabel("Handle:");
        passwordLabel = new JLabel("Password:");
        handleField = new JTextField();
        passwordField = new JPasswordField();
        passwordStore = new JCheckBox("Store password");
        autologin = new JCheckBox("Autologin");

        serversBox = new JComboBox(serversBoxModel);

    }

    /**
     * Returns the title of the panel. This method allows subclasses to specify
     * the title of the dialog (or whatever other container is used).
     */

    protected String getTitle() {
        return "Login to server";
    }

    /**
     * Main method for testing the class.
     * @param args
     */

    public static void main(String ... args){

    }
}
