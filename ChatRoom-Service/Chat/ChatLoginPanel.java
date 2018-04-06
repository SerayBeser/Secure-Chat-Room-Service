package Chat;
//  ChatLoginPanel.java
//
//  Last modified 1/30/2000 by Alan Frindell
//  Last modified : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  GUI class for the login panel.
//
//  You should not have to modify this class.

import java.awt.*;
import javax.swing.*;

class ChatLoginPanel extends JPanel {

    private JTextField _loginNameField;
    private JTextField _roomNameField;
    private JTextField _roomTypeField;
    private JTextField _serverHostField;
    private JTextField _serverPortField;
    private JTextField _caHostField;
    private JTextField _caPortField;
    private JTextField _keyStoreNameField;
    private JPasswordField _keyStorePasswordField;
    private JLabel _errorLabel;
    private ChatClient _client;

    ChatLoginPanel(ChatClient client) {

        _client = client;

        try {

            componentInit();

        } catch (Exception e) {

            System.out.println( "[ERROR]: ChatLoginPanel error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void componentInit() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout( gridBag );

        addLabel( gridBag, "Welcome to Chat", SwingConstants.CENTER,
                0, 2 );
        addLabel( gridBag, "Username: ", SwingConstants.LEFT, 1, 1 );
        addLabel( gridBag, "Room Name: ", SwingConstants.LEFT, 2, 1 );
        addLabel( gridBag, "Room Type: ", SwingConstants.LEFT, 3, 1 );
        addLabel( gridBag, "KeyStore File Name: ", SwingConstants.LEFT, 4, 1 );
        addLabel( gridBag, "KeyStore Password: ", SwingConstants.LEFT, 5, 1 );
        addLabel( gridBag, "Server Host Name: ", SwingConstants.LEFT, 6, 1 );
        addLabel( gridBag, "Server Port: ", SwingConstants.LEFT, 7, 1 );
        addLabel( gridBag, "CA Host Name: ", SwingConstants.LEFT, 8, 1 );
        addLabel( gridBag, "CA Port: ", SwingConstants.LEFT, 9, 1 );

        _loginNameField = new JTextField();
        addField( gridBag, _loginNameField, 1 );

        _roomNameField = new JTextField();
        addField( gridBag, _roomNameField, 2 );

        _roomTypeField = new JTextField();
        addField( gridBag, _roomTypeField, 3 );

        _keyStoreNameField = new JTextField();
        addField( gridBag, _keyStoreNameField, 4 );
        _keyStorePasswordField = new JPasswordField();
        _keyStorePasswordField.setEchoChar( '*' );
        addField( gridBag, _keyStorePasswordField, 5 );

        _serverHostField = new JTextField();
        addField( gridBag, _serverHostField, 6 );
        _serverPortField = new JTextField();
        addField( gridBag, _serverPortField, 7 );

        _caHostField = new JTextField();
        addField( gridBag, _caHostField, 8 );
        _caPortField = new JTextField();
        addField( gridBag, _caPortField, 9 );

        _errorLabel = addLabel( gridBag, " ", SwingConstants.CENTER,
                10, 2 );

        _loginNameField.setText( "seraybeser" );
        _roomNameField.setText( "roomA" );
        _roomTypeField.setText( "public" );
        _keyStoreNameField.setText( "keystores/KeyStoreClient" );
        _keyStorePasswordField.setText( "123456" );
        _caHostField.setText( "localhost" );
        _caPortField.setText( "6666" );
        _serverHostField.setText( "localhost" );
        _serverPortField.setText( "10500" );

        _errorLabel.setForeground( Color.red );

        JButton _connectButton = new JButton( "Connect" );
        c.gridx = 1;
        c.gridy = 12;
        c.gridwidth = 2;
        gridBag.setConstraints( _connectButton, c );
        add( _connectButton );

        _connectButton.addActionListener( e -> connect() );
    }

    private JLabel addLabel(GridBagLayout gridBag, String labelStr, int align,
                            int y, int width) {
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel( labelStr );
        if (align == SwingConstants.LEFT) {
            c.anchor = GridBagConstraints.WEST;
        } else {
            c.insets = new Insets( 10, 0, 10, 0 );
        }
        c.gridx = 1;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = 1;
        gridBag.setConstraints( label, c );
        add( label );

        return label;
    }

    private void addField(GridBagLayout gridBag, JTextField field, int y) {
        GridBagConstraints c = new GridBagConstraints();
        field.setPreferredSize( new Dimension( 156,
                field.getMinimumSize().height ) );
        c.gridx = 2;
        c.gridy = y;
        c.gridwidth = 1;
        c.gridheight = 1;
        gridBag.setConstraints( field, c );
        add( field );
    }

    private void connect() {

        int serverPort;
        int caPort;

        String loginName = _loginNameField.getText();
        String roomName = _roomNameField.getText();
        String roomType = _roomTypeField.getText();

        String keyStoreName = _keyStoreNameField.getText();
        char[] keyStorePassword = _keyStorePasswordField.getPassword();

        String serverHost = _serverHostField.getText();
        String caHost = _caHostField.getText();

        if (loginName.equals( "" )
                || roomName.equals( "" )
                || roomType.equals( "" )
                || keyStoreName.equals( "" )
                || keyStorePassword.length == 0
                || serverHost.equals( "" )
                || _serverPortField.getText().equals( "" )
                || caHost.equals( "" )
                || _caPortField.getText().equals( "" )) {

            _errorLabel.setText( "Missing required field." );

            return;

        } else {

            _errorLabel.setText( " " );

        }

        try {

            serverPort = Integer.parseInt( _serverPortField.getText() );
            caPort = Integer.parseInt( _caPortField.getText() );

        } catch (NumberFormatException nfExp) {

            _errorLabel.setText( "Port field is not numeric." );

            return;
        }


        switch (_client.connect( loginName,
                roomName,
                roomType,
                keyStoreName,
                keyStorePassword,
                caHost,
                caPort,
                serverHost,
                serverPort )) {

            case ChatClient.SUCCESS:
                _errorLabel.setText( " " );
                break;
            case ChatClient.CONNECTION_REFUSED:
            case ChatClient.BAD_HOST:
                _errorLabel.setText( "Connection Refused!" );
                break;
            case ChatClient.ERROR:
                _errorLabel.setText( "ERROR!  Stop That!" );
                break;

        }


    }
}
