package Chat;
//
//  X509CertificateGenerator.java
//
//  Modified by : Priyank Patel <pkpatel@cs.stanford.edu>
//                added the policies for the chat rooms A and B
//  Modified by :Murat Ak, Dec 2011
//                  Changed to java.security.cert

import sun.security.x509.*;

import java.io.IOException;
import java.util.*;
import java.security.*;
import java.math.BigInteger;
import java.security.cert.*;

public class X509CertificateGenerator {

    public static X509Certificate generateCertificate(
            String dname,
            PrivateKey signingKey,
            PublicKey key,
            int days,
            String algorithm) throws GeneralSecurityException, IOException {

        Date from = new Date();
        Date to = new Date( from.getTime() + days * 86400000l );
        CertificateValidity validity = new CertificateValidity( from, to );

        BigInteger serialNumber = new BigInteger( 64, new SecureRandom() );

        X500Name owner = new X500Name( dname );
        AlgorithmId algorithmId = new AlgorithmId( AlgorithmId.md5WithRSAEncryption_oid );

        X509CertInfo info = new X509CertInfo();

        info.set( X509CertInfo.VALIDITY, validity );
        info.set( X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber( serialNumber ) );
        info.set( X509CertInfo.SUBJECT, owner );
        info.set( X509CertInfo.ISSUER, owner );
        info.set( X509CertInfo.KEY, new CertificateX509Key( key ) );
        info.set( X509CertInfo.VERSION, new CertificateVersion( CertificateVersion.V3 ) );
        info.set( X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId( algorithmId ) );

        X509CertImpl cert = new X509CertImpl( info );
        cert.sign( signingKey, algorithm );

        algorithmId = (AlgorithmId) cert.get( X509CertImpl.SIG_ALG );
        info.set( CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithmId );
        cert = new X509CertImpl( info );
        cert.sign( signingKey, algorithm );
        return cert;
    }
}
