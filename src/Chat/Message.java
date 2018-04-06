package Chat;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Message implements Serializable {

    private SealedObject sealedMessage;
    private byte[] MAC;

    Message(String message, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, InvalidAlgorithmParameterException {


        Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
        byte[] ivBytes = "guaicnjqwvgfashsh".getBytes();

        // [PROCESS]: Encrypting message...
        cipher.init( Cipher.ENCRYPT_MODE, key, new IvParameterSpec( ivBytes, 0, 16 ) );
        this.sealedMessage = new SealedObject( message, cipher );

        // [PROCESS]: Calculating MAC...
        Mac mac = Mac.getInstance( "HmacSHA256" );
        mac.init( key );
        byte[] macByte = message.getBytes( "UTF-8" );
        this.MAC = mac.doFinal( macByte );
    }

    public String verifyAndGetMessage(SecretKey key) {
        try {

            Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            byte[] ivBytes = "guaicnjqwvgfashsh".getBytes();

            // [PROCESS]: Decrypting message...
            cipher.init( Cipher.DECRYPT_MODE, key, new IvParameterSpec( ivBytes, 0, 16 ) );
            String sealedMessageObject = (String) this.sealedMessage.getObject( cipher );


            // [PROCESS]: Calculating MAC...
            Mac mac = Mac.getInstance( "HmacSHA256" );
            mac.init( key );

            byte[] macByte = sealedMessageObject.getBytes( "UTF-8" );
            byte[] MACOther = mac.doFinal( macByte );

            // [PROCESS]: Verifying MAC...
            if (Arrays.equals( this.MAC, MACOther )) {

                return sealedMessageObject;

            } else {

                System.out.println( "[ERROR]: MAC verification failed." );

                return null;
            }
        } catch (Exception e) {

            System.out.println( "[ERROR]: Message error: " + e.getMessage() );
            e.printStackTrace();

            return null;

        }
    }
}
