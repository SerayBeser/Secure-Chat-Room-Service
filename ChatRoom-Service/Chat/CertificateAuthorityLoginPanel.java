package Chat;
//
//  CertificateAuthorityPanel.java
//
//  Written by : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  GUI class for the Certificate Authority Initialization.
//

import java.awt.*;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.swing.*;

class CertificateAuthorityLoginPanel extends JPanel {

    private JPasswordField _privateKeyPassField;
    private JTextField _portField;
    private JTextField _keystoreFileNameField;
    private JLabel _errorLabel;
    private CertificateAuthority _ca;

    CertificateAuthorityLoginPanel(CertificateAuthority ca) {
        _ca = ca;

        try {
            componentInit();
        } catch (Exception e) {
            System.out.println( "[ERROR]: CertificateAuthorityPanel error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void componentInit() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout( gridBag );

        addLabel( gridBag, "Certificate Server Startup Panel", SwingConstants.CENTER, 0, 2 );
        addLabel( gridBag, "KeyStore File Name: ", SwingConstants.LEFT, 1, 1 );
        addLabel( gridBag, "KeyStore (Private Key) Password: ", SwingConstants.LEFT, 2, 1 );
        addLabel( gridBag, "Port Number: ", SwingConstants.LEFT, 5, 1 );


        _keystoreFileNameField = new JTextField();
        addField( gridBag, _keystoreFileNameField, 1 );
        _keystoreFileNameField.setText( "keystores/KeyStoreCA" );

        _privateKeyPassField = new JPasswordField();
        _privateKeyPassField.setEchoChar( '*' );
        addField( gridBag, _privateKeyPassField, 2 );
        _privateKeyPassField.setText( "123456" );

        _portField = new JTextField();
        addField( gridBag, _portField, 5 );
        _portField.setText( "6666" );

        _errorLabel = addLabel( gridBag, " ", SwingConstants.CENTER, 6, 2 );

        _errorLabel.setForeground( Color.red );

        JButton _startupButton = new JButton( "Startup" );
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 2;
        gridBag.setConstraints( _startupButton, c );
        add( _startupButton );

        _startupButton.addActionListener( e -> {
            try {
                startup();
            } catch (UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e1) {
                e1.printStackTrace();
            }
        } );
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

    private void startup() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

        System.out.println( "[INFO]: Called startup." );

        int _caPort;

        String _keystoreFileName = _keystoreFileNameField.getText();
        char[] _privateKeyPass = _privateKeyPassField.getPassword();

        if (_privateKeyPass.length == 0
                || _portField.getText().equals( "" )
                || _keystoreFileName.equals( "" )
                ) {

            _errorLabel.setText( "Missing required field." );

            return;

        } else {

            _errorLabel.setText( " " );

        }

        try {

            _caPort = Integer.parseInt( _portField.getText() );

        } catch (NumberFormatException nfExp) {

            _errorLabel.setText( "Port field is not numeric." );

            return;
        }

        System.out.println( "[PROCESS]: Certificate Authority is starting up ..." );

        switch (_ca.startup( _keystoreFileName,
                _privateKeyPass,
                _caPort )) {

            case CertificateAuthority.SUCCESS:
                _errorLabel.setText( " " );
                break;
            case CertificateAuthority.KEYSTORE_FILE_NOT_FOUND:
                _errorLabel.setText( "[ERROR]: KeyStore file not found!" );
                break;
            case CertificateAuthority.ERROR:
                _errorLabel.setText( "[ERROR]: Unknown Error!" );
                break;
        }

        System.out.println( "[INFO]: Certificate Authority startup complete." );
    }
}
