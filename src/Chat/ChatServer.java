package Chat;
//
// ChatServer.java
// Created by Ting on 2/18/2003
// Modified : Priyank K. Patel <pkpatel@cs.stanford.edu>
//

import java.security.cert.Certificate;
import java.util.*;
import java.net.*;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import javax.crypto.*;


public class ChatServer {


    int CLIENT = 0;

    private int port;
    private String hostName = null;

    private ServerSocket serverSocket = null;

    PublicKey CAPublicKey;
    PrivateKey RSAPrivateKey;
    Certificate certificate;

    Map<String, HashMap<Integer, ClientRecord>> clients;
    KeyGenerator roomKeyGenerator;
    Map<String, SecretKey> roomKeys;


    private ChatServer(int port) {

        try {

            this.port = port;
            CLIENT = -1;

            clients = new HashMap<>();
            roomKeys = new HashMap<>();

            serverSocket = null;

            InetAddress inetAddress = InetAddress.getByName( null );
            hostName = inetAddress.getHostName();

            // Getting CAs public key from Servers keystore
            String serverKeyStoreFileName = "keystores/KeyStoreServer";
            FileInputStream inputStream = new FileInputStream("/home/seray/ETU/bil448project/src/Chat/keystores/KeyStoreServer" );
            KeyStore serverKeyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
            char[] serverKeystorePassword = "123456".toCharArray();
            serverKeyStore.load( inputStream, serverKeystorePassword );
            CAPublicKey = serverKeyStore.getCertificate( "ca" ).getPublicKey();

            // Getting Servers certificate and RSA Private key from Servers keystore
            certificate = serverKeyStore.getCertificate( "server" );
            char[] SERVER_KEY_PASSWORD = "123456".toCharArray();
            RSAPrivateKey = (PrivateKey) serverKeyStore.getKey( "server", SERVER_KEY_PASSWORD );

            // Generating room key
            roomKeyGenerator = KeyGenerator.getInstance( "AES" );
            roomKeyGenerator.init( 128 );


        } catch (UnknownHostException e) {

            hostName = "0.0.0.0";

        } catch (IOException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        try {

            int port = Integer.parseInt( "10500" );
            ChatServer server = new ChatServer( port );
            server.run();

        } catch (NumberFormatException e) {

            System.out.println( "[ERROR]: Usage: java ChatServer host portNum" );
            e.printStackTrace();

        } catch (Exception e) {

            System.out.println( "[ERROR]: ChatServer error: " + e.getMessage() );
            e.printStackTrace();
        }
    }


    private void run() {

        try {

            serverSocket = new ServerSocket( port );
            System.out.println( "[INFO]: ChatServer is running on " + hostName + " port " + port );

            while (true) {

                Socket socket = serverSocket.accept();
                ChatServerThread thread = new ChatServerThread( this, socket );
                thread.start();
            }


        } catch (IOException e) {

            System.err.println( "[ERROR]: Could not listen on port: " + port );

        } catch (Exception e) {

            System.out.println( "[ERROR]: ChatServer error: " + e.getMessage() );
            e.printStackTrace();

        }
    }
}