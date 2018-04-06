package Chat;
//
// ChatServerThread.java
// created 02/18/03 by Ting Zhang
// Modified : Priyank K. Patel <pkpatel@cs.stanford.edu>
//

// Java

import java.security.cert.CertificateException;
import java.util.*;
import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ChatServerThread extends Thread {


    private Socket _socket;
    private ChatServer _server;

    ChatServerThread(ChatServer server, Socket socket) {

        super( "ChatServerThread" );
        _server = server;
        _socket = socket;
    }

    public void run() {
        ObjectInputStream in;
        String roomName;
        String roomType;

        try {

            ObjectOutputStream out = new ObjectOutputStream( _socket.getOutputStream() );
            in = new ObjectInputStream( _socket.getInputStream() );


            // [PROCESS]: Generating DH Key Part
            KeyPairGenerator DHKeyPairGenerator = KeyPairGenerator.getInstance( "DiffieHellman" );
            DHKeyPairGenerator.initialize( 512 );
            KeyPair DHKeyPair = DHKeyPairGenerator.generateKeyPair();
            KeyFactory keyFactory = KeyFactory.getInstance( "DiffieHellman" );
            DHPublicKeySpec dhPublicKeySpec = keyFactory.getKeySpec( DHKeyPair.getPublic(), DHPublicKeySpec.class );

            PublicKey DHServerPublicKey = DHKeyPair.getPublic();

            // [PROCESS]: Sending servers packet to client
            ServerPacket serverPacket = new ServerPacket( _server.certificate, dhPublicKeySpec, DHServerPublicKey, _server.RSAPrivateKey );
            out.writeObject( serverPacket );

            // [PROCESS]: Receiving clients packet from client
            ClientPacket clientPacket = (ClientPacket) in.readObject();

            // [PROCESS]: Receiving and verifying clients packet
            if ((clientPacket == null)) {
                System.out.println( "[ERROR]: Receiving clients packet" );
                _socket.close();
                return;
            } else if (!clientPacket.verify()) {
                System.out.println( "[ERROR]: Verifying clients packet failed." );
                _socket.close();
                return;
            }
            // [PROCESS] : Verifying client packet and DH Client Public Key
            clientPacket.getClientCertificate().verify( _server.CAPublicKey );
            PublicKey DHClientPublicKey = clientPacket.getDHClientPart( _server.RSAPrivateKey );

            // [PROCESS] : Calculating shared secret
            KeyAgreement keyAgreement = KeyAgreement.getInstance( "DH" );
            keyAgreement.init( DHKeyPair.getPrivate() );
            keyAgreement.doPhase( DHClientPublicKey, true );
            SecretKey sharedKey = keyAgreement.generateSecret( "AES" );

            // [INFO]: Server key exchange
            System.out.println( "[INFO]: Client key exchange completed." );

            // [PROCESS]:  Initializing symmetric ciphers...
            Cipher encryptCipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            Cipher decryptCipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            byte[] ivParameterBytes = "guaicnjqwvgfashsh".getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec( ivParameterBytes, 0, 16 );
            encryptCipher.init( Cipher.ENCRYPT_MODE, sharedKey, ivParameterSpec );
            decryptCipher.init( Cipher.DECRYPT_MODE, sharedKey, ivParameterSpec );

            // [PROCESS]: Receiving join room request...
            SealedObject roomRequest_1 = (SealedObject) in.readObject();
            roomName = (String) roomRequest_1.getObject( decryptCipher );

            SealedObject roomRequest_2 = (SealedObject) in.readObject();
            roomType = (String) roomRequest_2.getObject( decryptCipher );

            boolean publicRoom = roomType.equalsIgnoreCase( "public" );
            boolean privateRoom = roomType.equalsIgnoreCase( "private" );

            if (publicRoom) {

                ClientRecord clientRecord = new ClientRecord( out );
                HashMap<Integer, ClientRecord> publicRoomClients = _server.clients.get( roomName );
                SecretKey publicRoomKey;

                if (publicRoomClients == null) {

                    HashMap<Integer, ClientRecord> newClientsMap;
                    newClientsMap = new HashMap<>();
                    newClientsMap.put( _server.CLIENT++, clientRecord );
                    _server.clients.put( roomName, newClientsMap );
                    publicRoomKey = _server.roomKeyGenerator.generateKey();
                    _server.roomKeys.put( roomName, publicRoomKey );

                } else {

                    publicRoomClients.put( _server.CLIENT++, clientRecord );
                    publicRoomKey = _server.roomKeys.get( roomName );
                }

                // [PROCESS]:  Sending join room answer...
                out.writeObject( new SealedObject( publicRoomKey, encryptCipher ) );

                System.out.println( "[INFO]: Client joined public room <" + roomName + ">" );

            } else if (privateRoom) {

                if (_server.CLIENT < 1) {
                    ClientRecord clientRecord = new ClientRecord( out );
                    HashMap<Integer, ClientRecord> privateRoomClients = _server.clients.get( roomName );
                    SecretKey privateRoomKey;

                    if (privateRoomClients == null) {

                        HashMap<Integer, ClientRecord> newClientsMap;
                        newClientsMap = new HashMap<>();
                        newClientsMap.put( _server.CLIENT++, clientRecord );
                        _server.clients.put( roomName, newClientsMap );
                        privateRoomKey = _server.roomKeyGenerator.generateKey();
                        _server.roomKeys.put( roomName, privateRoomKey );

                    } else {

                        privateRoomClients.put( _server.CLIENT++, clientRecord );
                        privateRoomKey = _server.roomKeys.get( roomName );
                    }

                    // [PROCESS]:  Sending join room answer...
                    out.writeObject( new SealedObject( privateRoomKey, encryptCipher ) );
                    System.out.println( "[INFO]: Client joined private room <" + roomName + ">" );

                } else {
                    System.out.println( "[INFO]: Client joined room failed. Room  <" + roomName + "> is private." );
                    out.writeObject( new SealedObject( null, encryptCipher ) );
                }

            } else {
                System.out.println( "[INFO]: Client joined room failed. Room  <" + roomName + "> is private." );
                out.writeObject( new SealedObject( null, encryptCipher ) );


            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException
                | NoSuchPaddingException | InvalidKeySpecException | ClassNotFoundException | CertificateException
                | SignatureException | IllegalBlockSizeException | NoSuchProviderException | BadPaddingException e) {

            System.out.println( "[ERROR]: ChatServerThread error: " + e.getMessage() );
            e.printStackTrace();
            return;
        }


        try {

            Message message;
            Map<Integer, ClientRecord> clientsSameRoom = _server.clients.get( roomName );

            while ((message = (Message) in.readObject()) != null) {
                for (ClientRecord c : clientsSameRoom.values()) {
                    try {
                        ObjectOutputStream clientsSameRoomOutputStream = c.getOutputStream();
                        clientsSameRoomOutputStream.writeObject( message );
                    } catch (Exception e) {
                        //System.out.println( "[ERROR]: ChatClientThread error: " + e.getMessage() );
                        //System.exit( 0 );
                    }
                }
            }
            _socket.shutdownInput();
            _socket.shutdownOutput();
            _socket.close();
            System.exit( 1 );


        } catch (Exception e) {
            System.out.println( "[INFO]: Client left room." );
            // e.printStackTrace();
        }

    }
}