package Chat;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHPublicKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;


public class ServerPacket implements Serializable {

    /**
     * Server generates an object of ServerPacket class, and sends this packet to the client  for Diffie-Helman key exchange.
     */
    private Certificate serverCertificate;
    private SignedObject signedDHServerPart;
    private SignedObject signedDHServerKey;

    ServerPacket(Certificate serverCertificate,
                 DHPublicKeySpec DHServerPublicKeySpec,
                 PublicKey DHServerPublicKey,
                 PrivateKey signingKey) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException {
        this.serverCertificate = serverCertificate;
        Signature signature = Signature.getInstance( "SHA256withRSA" );
        SerializableDHPublicKey serializableDHPublicKey = new SerializableDHPublicKey( DHServerPublicKeySpec );
        this.signedDHServerPart = new SignedObject( serializableDHPublicKey, signingKey, signature );
        this.signedDHServerKey = new SignedObject( DHServerPublicKey, signingKey, signature );

    }

    public DHPublicKeySpec getDHServerPart() throws IOException, ClassNotFoundException {

        //  Client get servers DH part to calculate shared secret key
        SerializableDHPublicKey serializableKey = (SerializableDHPublicKey) this.signedDHServerPart.getObject();
        return serializableKey.getDHPublicKeySpec();
    }

    public DHPublicKey getDHServerKey() throws IOException, ClassNotFoundException {

        // return signed DH server public key
        return (DHPublicKey) this.signedDHServerKey.getObject();
    }

    public boolean verify() {

        try {

            //  Client verify the signature with servers certificate
            PublicKey verificationKey = this.serverCertificate.getPublicKey();
            Signature signature = Signature.getInstance( "SHA256withRSA" );
            boolean verifiedPart = this.signedDHServerPart.verify( verificationKey, signature );
            boolean verifiedKey = this.signedDHServerKey.verify( verificationKey, signature );
            return verifiedPart && verifiedKey;

        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {

            System.out.println( "[ERROR]: ServerPacket error: " + e.getMessage() );
            e.printStackTrace();
        }

        return false;
    }

    public Certificate getServerCertificate() {

        // return servers certificate
        return this.serverCertificate;
    }

    static class SerializableDHPublicKey implements Serializable {
        BigInteger G;
        BigInteger P;
        BigInteger Y;

        SerializableDHPublicKey(DHPublicKeySpec dhPublicKeySpec) {
            this.G = dhPublicKeySpec.getG();
            this.P = dhPublicKeySpec.getP();
            this.Y = dhPublicKeySpec.getY();
        }

        DHPublicKeySpec getDHPublicKeySpec() {

            return new DHPublicKeySpec( this.Y, this.P, this.G );
        }
    }
}
