package Chat;

//
//  CertificateAuthority.java
//
//  Written by : Priyank Patel <pkpatel@cs.stanford.edu>
//

//  AWT/Swing

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;


public class CertificateAuthority {

    public static final int SUCCESS = 0;
    public static final int KEYSTORE_FILE_NOT_FOUND = 1;
    public static final int ERROR = 4;

    private CertificateAuthorityLoginPanel _panel;
    private CertificateAuthorityActivityPanel _activityPanel;
    private CardLayout _layout;
    private JFrame _appFrame;

    private int portNumber;

    String keystoreFileName;
    char[] privateKeyPassword;
    KeyStore keyStore;
    KeyPair keyPair;


    private CertificateAuthority() {

        _panel = null;
        _activityPanel = null;
        _layout = null;
        _appFrame = null;

        try {
            initialize();
        } catch (Exception e) {
            System.out.println( "CA error: " + e.getMessage() );
            e.printStackTrace();
        }

        _layout.show( _appFrame.getContentPane(), "CAPanel" );

    }

    private void initialize() {

        _appFrame = new JFrame( "Certificate Authority" );
        _layout = new CardLayout();

        _appFrame.getContentPane().setLayout( _layout );
        _panel = new CertificateAuthorityLoginPanel( this );
        _appFrame.getContentPane().add( _panel, "CAPanel" );

        _activityPanel = new CertificateAuthorityActivityPanel( this );
        _appFrame.getContentPane().add( _activityPanel, "ActivityPanel" );

        _appFrame.addWindowListener( new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                quit();
            }
        } );
    }

    private void run() {
        _appFrame.pack();
        _appFrame.setVisible( true );
    }

    public void quit() {

        try {
            System.out.println( "[INFO]: Quit called." );
        } catch (Exception err) {
            System.out.println( "[ERROR]: CertificateAuthority error: " + err.getMessage() );
            err.printStackTrace();
        }

        System.exit( 0 );
    }

    public int startup(String _ksFileName,
                       char[] _privateKeyPass,
                       int _caPort) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {


        portNumber = _caPort;

        keystoreFileName = _ksFileName;
        privateKeyPassword = _privateKeyPass;

        // [PROCESS -1-]: Loading CAs keystore
        FileInputStream keyStoreStream = new FileInputStream( "/home/seray/ETU/bil448project/src/Chat/keystores/KeyStoreCA");
        keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
        keyStore.load( keyStoreStream, privateKeyPassword );

        // [-1]: Get CAs certificate and keypair from CAs keystore
        PrivateKey privateKey = (PrivateKey) keyStore.getKey( "ca", privateKeyPassword );
        Certificate certificate = keyStore.getCertificate( "ca" );
        keyPair = new KeyPair( certificate.getPublicKey(), privateKey );

        _layout.show( _appFrame.getContentPane(), "ActivityPanel" );

        CertificateAuthorityThread _thread = new CertificateAuthorityThread( this );
        _thread.start();
        return CertificateAuthority.SUCCESS;

    }

    public int getPortNumber() {

        return portNumber;
    }

    public JTextArea getOutputArea() {

        return _activityPanel.getOutputArea();
    }


    public static void main(String[] args) {

        CertificateAuthority ca = new CertificateAuthority();
        ca.run();
    }
}
