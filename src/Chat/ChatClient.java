package Chat;

//  ChatClient.java
//
//  Modified 1/30/2000 by Alan Frindell
//  Last modified 2/18/2003 by Ting Zhang 
//  Last modified : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  Chat Client starter application.

//  AWT/Swing


import java.awt.*;
import java.awt.event.*;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;


public class ChatClient {

    public static final int SUCCESS = 0;
    public static final int CONNECTION_REFUSED = 1;
    public static final int BAD_HOST = 2;
    public static final int ERROR = 3;

    private CardLayout _cardLayout;
    private JFrame _appFrame;
    private ChatRoomPanel _chatRoomPanel;

    ObjectOutputStream out = null;
    ObjectInputStream in = null;

    private String loginName;

    private ChatClientThread _thread;
    private Socket _socket = null;


    SecretKey roomKey;

    boolean exists = true;


    private ChatClient() {

        loginName = null;

        try {
            initComponents();
        } catch (Exception e) {
            System.out.println( "ChatClient error: " + e.getMessage() );
            e.printStackTrace();
        }

        _cardLayout.show( _appFrame.getContentPane(), "Login" );

    }

    private void run() {
        _appFrame.pack();
        _appFrame.setVisible( true );

    }


    public static void main(String[] args) {

        ChatClient app = new ChatClient();
        app.run();
    }


    private void initComponents() {

        _appFrame = new JFrame( "Chat Room" );
        _cardLayout = new CardLayout();
        _appFrame.getContentPane().setLayout( _cardLayout );
        ChatLoginPanel _chatLoginPanel = new ChatLoginPanel( this );
        _chatRoomPanel = new ChatRoomPanel( this );
        _appFrame.getContentPane().add( _chatLoginPanel, "Login" );
        _appFrame.getContentPane().add( _chatRoomPanel, "ChatRoom" );
        _appFrame.addWindowListener( new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                quit();
            }
        } );
    }

    public void quit() {

        try {
            exists = false;
            _socket.shutdownOutput();
            _socket.close();
            _thread.join();


        } catch (Exception err) {
            System.out.println( "[ERROR]: ChatClient error: " + err.getMessage() );
            err.printStackTrace();
        }
        System.exit( 0 );
    }


    public int connect(String userName,
                       String roomName,
                       String roomType,
                       String keyStoreName, char[] keyStorePassword,
                       String caHost, int caPort,
                       String serverHost, int serverPort) {

        try {

            loginName = userName;


            System.out.println( "\n\n[PROCESS]: Connecting to CA ..." );

            // [PROCESS -1-] : Loading user's keystore...
            System.out.println( "[PROCESS -1-] : Loading user's keystore..." );
            FileInputStream inputStream = new FileInputStream( "/home/seray/ETU/bil448project/src/Chat/keystores/KeyStoreClient" );
            KeyStore userKeyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
            userKeyStore.load( inputStream, keyStorePassword );
            PublicKey CAPublicKey = userKeyStore.getCertificate( "ca" ).getPublicKey();

            // [PROCESS -2-] Check users certificate with username
            System.out.println( "[PROCESS -2-] Checking user's certificate with username..." );
            PrivateKey RSAPrivateKey;
            Certificate userCertificate;
            if (!userKeyStore.isCertificateEntry( loginName )) {

                // [PROCESS -2-] Check users certificate with username, if does not exists
                System.out.println( "[INFO] User's certificate does not exists." );

                // [PROCESS -3 ]: Receive users public key
                RSAPrivateKey = (PrivateKey) userKeyStore.getKey( "client", keyStorePassword );
                userCertificate = userKeyStore.getCertificate( "client" );
                PublicKey RSAPublicKey = userCertificate.getPublicKey();
                System.out.println( "[PROCESS -3 ]: Received user's public key." );

                // [PROCESS -4-]: Connect to CA for registration request
                Socket ca_socket = new Socket( caHost, caPort );
                OutputStream outStream = ca_socket.getOutputStream();
                InputStream inStream = ca_socket.getInputStream();
                out = new ObjectOutputStream( outStream );
                in = new ObjectInputStream( inStream );

                System.out.println( "[PROCESS -4-]: Connected to CA for registration request." );

                // [ -4 ]: send CA username and RSA public key
                out.writeObject( new RegistrationRequest( loginName, RSAPublicKey ) );
                System.out.println( "\t[ -4 ]: sent CA username and RSA public key." );

                try {
                    // [ -4 ]: receive certificate from CA
                    userCertificate = (X509Certificate) in.readObject();
                    System.out.println( "\t[ -4 ]: received certificate from CA." );

                    // [ -4 ]: verify CA
                    userCertificate.verify( CAPublicKey );
                    System.out.println( "\t[ -4 ]: verified CA." );


                } catch (ClassCastException caste) {
                    return CONNECTION_REFUSED;
                } catch (Exception e) {
                    return BAD_HOST;
                }

                out.close();
                in.close();


                // [PROCESS -5-]: Save the certificate in its keystore
                userKeyStore.setCertificateEntry( loginName, userCertificate );
                FileOutputStream keyStoreStream = new FileOutputStream("/home/seray/ETU/bil448project/src/Chat/keystores/KeyStoreClient" );
                userKeyStore.store( keyStoreStream, keyStorePassword );
                System.out.println( "[PROCESS -5-]: Saved the certificate in user's keystore." );

            } else {

                System.out.println( "[INFO] User's certificate exists." );
                // [PROCESS -2-] Check users certificate with username, if  exists
                // [PROCESS -3-]: Received RSA private key and certificate from keystore
                RSAPrivateKey = (PrivateKey) userKeyStore.getKey( "client", keyStorePassword );
                userCertificate = userKeyStore.getCertificate( loginName );
                System.out.println( "[PROCESS -3-]: Received user's RSA private key and certificate from keystore." );

            }

            // [PROCESS]: Connecting to Server ...
            System.out.println( "\n\n[PROCESS]: Connecting to Server ..." );

            _socket = new Socket( serverHost, serverPort );
            OutputStream outStream = _socket.getOutputStream();
            InputStream inStream = _socket.getInputStream();
            out = new ObjectOutputStream( outStream );
            in = new ObjectInputStream( inStream );

            System.out.println( "[INFO]: Connected to server." );

            // [PROCESS -1-]: Receiving server's packet
            System.out.println( "[PROCESS -1-]: Receiving server's packet..." );
            ServerPacket serverPacket = (ServerPacket) in.readObject();

            //-DHPublicKeySpec DHServerPublicKeyPart = serverPacket.getDHServerPart();
            DHPublicKey DHServerPublicKey = serverPacket.getDHServerKey();

            // [ -1- ]: Getting server's certificate
            System.out.println( "\t[-1-]: Getting server's certificate..." );
            Certificate serverCert = serverPacket.getServerCertificate();

            System.out.println( "[PROCESS -2-]: Verifying server's certificate" );
            serverCert.verify( CAPublicKey );
            if (!serverPacket.verify()) {
                return BAD_HOST;
            }

            // [INFO]: Server key exchange
            System.out.println( "[INFO]: Server key exchange completed." );

            // [PROCESS -3-]: Generate DH key part
            System.out.println( "[PROCESS -3-]: Generating DH key part..." );
            KeyPairGenerator keyPairGenerator;
            keyPairGenerator = KeyPairGenerator.getInstance( "DiffieHellman" );
            // parameters

            DHPublicKeySpec DHServerPublicKeyPart = serverPacket.getDHServerPart();
            DHParameterSpec dhParameterSpec = new DHParameterSpec( DHServerPublicKeyPart.getP(), DHServerPublicKeyPart.getG() );
            keyPairGenerator.initialize( dhParameterSpec );
            KeyPair generateKeyPair = keyPairGenerator.generateKeyPair();

            // [PROCESS -4-]: Create and send client packet
            System.out.println( "[PROCESS -4-]: Sending client packet..." );
            Certificate serverCertificate = serverPacket.getServerCertificate();
            ClientPacket clientPacket = new ClientPacket( userCertificate, serverCertificate, generateKeyPair.getPublic(), RSAPrivateKey, serverPacket );
            out.writeObject( clientPacket );

            // [INFO]: Sent client packet.
            System.out.println( "[INFO]: Sent client packet." );

            // [PROCESS -5-]: Calculating shared secret...
            System.out.println( "[PROCESS -5-]: Calculating shared secret..." );
            KeyAgreement keyAgreement = KeyAgreement.getInstance( "DH" );
            keyAgreement.init( generateKeyPair.getPrivate() );
            keyAgreement.doPhase( DHServerPublicKey, true );
            SecretKey secretKey = keyAgreement.generateSecret( "AES" );

            // [INFO]: Key exchange completed.
            System.out.println( "[INFO]: Key exchange completed." );

            // [PROCESS]:  Initializing symmetric ciphers...
            System.out.println( "\n[PROCESS -1-]:  Initializing symmetric ciphers..." );
            Cipher encryptCipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            Cipher decryptCipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            byte[] ivParameterBytes = "guaicnjqwvgfashsh".getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec( ivParameterBytes, 0, 16 );
            encryptCipher.init( Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec );
            decryptCipher.init( Cipher.DECRYPT_MODE, secretKey, ivParameterSpec );

            // [PROCESS]:  Sending join room request...
            System.out.println( "[PROCESS -2-]:  Sending join room request..." );
            out.writeObject( new SealedObject( roomName, encryptCipher ) );
            out.writeObject( new SealedObject( roomType, encryptCipher ) );

            // [INFO]: Join room request sent.
            System.out.println( "[INFO]: Join room request sent." );

            // [PROCESS -1-]:  Receiving joining room requests respond
            System.out.println( "[PROCESS -3-]:  Receiving joining room requests respond..." );
            SealedObject roomRespond = (SealedObject) in.readObject();
            roomKey = (SecretKey) roomRespond.getObject( decryptCipher );

            if (roomKey == null) {
                System.out.println( "[INFO]: Joining room failed." );
                System.out.println( "[EXIT]" );
                return CONNECTION_REFUSED;
            }

            System.out.println( "[INFO]: Joined room." );

            _cardLayout.show( _appFrame.getContentPane(), "ChatRoom" );

            _thread = new ChatClientThread( this );
            _thread.start();
            return SUCCESS;

        } catch (UnknownHostException e) {

            System.err.println( "[ERROR]: Don't know about the serverHost: " + serverHost );

        } catch (IOException e) {

            System.err.println( "[ERROR]: Couldn't get I/O for the connection to the serverHost: " + serverHost );
            System.out.println( "[ERROR]: ChatClient error: " + e.getMessage() );
            e.printStackTrace();


        } catch (AccessControlException e) {

            return BAD_HOST;

        } catch (Exception e) {

            System.out.println( "[ERROR]: ChatClient err: " + e.getMessage() );
            e.printStackTrace();
        }

        return ERROR;

    }


    public void sendMessage(String msg) {
        try {
            msg = loginName + "> " + msg;
            Message message = new Message( msg, roomKey );
            out.writeObject( message );
        } catch (Exception e) {
            System.out.println( "[ERROR]: ChatClient err: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return _socket;
    }

    public JTextArea getOutputArea() {

        return _chatRoomPanel.getOutputArea();
    }


}
