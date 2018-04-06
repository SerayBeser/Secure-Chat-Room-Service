package Chat;
//
//  CertificateAuthorityThread.java
//
//  Written by : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  Accepts connection requests and processes them

import java.net.*;
import java.io.*;
import javax.swing.JTextArea;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class CertificateAuthorityThread extends Thread {

    private CertificateAuthority CA;
    private ServerSocket serverSocket;
    private int portNum;
    private String hostName;
    private JTextArea outputArea;

    CertificateAuthorityThread(CertificateAuthority ca) {

        super( "CertificateAuthorityThread" );
        CA = ca;
        portNum = ca.getPortNumber();
        outputArea = ca.getOutputArea();
        serverSocket = null;

        try {

            InetAddress serverAddress = InetAddress.getByName( null );
            hostName = serverAddress.getHostName();

        } catch (UnknownHostException e) {
            hostName = "0.0.0.0";
        }
    }


    //  Accept connections and service them one at a time
    public void run() {

        try {

            serverSocket = new ServerSocket( portNum );

            outputArea.append( "CA waiting on " + hostName + " port " + portNum );

            while (true) try {
                Socket socket = serverSocket.accept();

                OutputStream outStream = socket.getOutputStream();
                InputStream inStream = socket.getInputStream();
                ObjectOutputStream out = new ObjectOutputStream( outStream );
                ObjectInputStream in = new ObjectInputStream( inStream );

                // [PROCESS -1- ]:  Receiving registration request
                RegistrationRequest requestPacket = (RegistrationRequest) in.readObject();

                // [PROCESS -2- ]:  Check existing of registration request packet
                if (requestPacket != null) {

                    // [PROCESS -2- ]:  Check existing of registration request packet, if request packet exists
                    outputArea.append( "\n[REQUEST]:  Registration request for user < " + requestPacket.username + ">" );

                    // [PROCESS -3- ]:  Check registration request packet's certificate in CA's keystore
                    Certificate existingCertificate = CA.keyStore.getCertificate( requestPacket.username );
                    X509Certificate certificate = null;

                    // [PROCESS -4- ]:  If does not exists a certificate, generate a new certificate for that user
                    if (existingCertificate == null) {
                        certificate = X509CertificateGenerator.generateCertificate( "CN=" + requestPacket.username + ", O=TOBB ETU, C=TR",
                                CA.keyPair.getPrivate(), requestPacket.publicKey, 90, "SHA256withRSA" );

                        // [ -4 ] save the certificate in CA's keystore for remember that user
                        CA.keyStore.setCertificateEntry( requestPacket.username, certificate );
                        FileOutputStream keyStoreStream = new FileOutputStream("/home/seray/ETU/bil448project/src/Chat/keystores/KeyStoreCA" );
                        CA.keyStore.store( keyStoreStream, CA.privateKeyPassword );

                        outputArea.append( "\n[DONE]: Registration completed." );
                    }

                    // [ -4- ] send the certificate to the user
                    out.writeObject( certificate );
                }
            } catch (Exception e) {

                System.out.println( "\n [ERROR]: Connection error: " + e.getMessage() );
                e.printStackTrace();
            }
        } catch (Exception e)

        {
            System.out.println( "[ERROR]: CA thread error: " + e.getMessage() );
            e.printStackTrace();
        }

    }
}
