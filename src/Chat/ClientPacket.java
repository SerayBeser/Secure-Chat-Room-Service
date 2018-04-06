package Chat;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


public class ClientPacket implements Serializable {

    /**
     * Client generates an object of ClientPacket class, then sends this packet to the server for Diffie-Helman key exchange.
     */
    private Certificate clientCertificate;
    private SignedObject signedObject;
    private byte[] DHClientPart;


    ClientPacket(Certificate clientCertificate, Certificate serverCertificate, PublicKey DHClientPublicKey,
                 PrivateKey signingKey, ServerPacket serverPacket) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        this.clientCertificate = clientCertificate;

        Signature signature = Signature.getInstance( "SHA256withRSA" );
        this.signedObject = new SignedObject( serverPacket, signingKey, signature );
        Cipher cipher = Cipher.getInstance( "RSA/ECB/PKCS1Padding" );
        cipher.init( Cipher.ENCRYPT_MODE, serverCertificate );
        this.DHClientPart = cipher.doFinal( DHClientPublicKey.getEncoded() );
    }

    public Certificate getClientCertificate() {

        // return clients certificate
        return this.clientCertificate;
    }

    public PublicKey getDHClientPart(PrivateKey privateKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, ClassNotFoundException, BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException {

        // Server get clients DH part to calculate shared secret key
        Cipher cipher = Cipher.getInstance( "RSA/ECB/PKCS1Padding" );
        cipher.init( Cipher.DECRYPT_MODE, privateKey );
        KeyFactory keyFactory = KeyFactory.getInstance( "DH" );
        byte[] bytes = cipher.doFinal( this.DHClientPart );
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec( bytes );

        return keyFactory.generatePublic( x509EncodedKeySpec );
    }

    public boolean verify() {

        try {

            // Server verify the signature with clients certificate
            PublicKey verificationKey = this.clientCertificate.getPublicKey();
            Signature signature = Signature.getInstance( "SHA256withRSA" );

            return this.signedObject.verify( verificationKey, signature );


        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {

            System.out.println( "[ERROR]: ClientPacket error: " + e.getMessage() );
            e.printStackTrace();
        }

        return false;
    }


}
