package org.apache.tomcat.util.net.jsse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import java.security.spec.InvalidKeySpecException;
import java.security.KeyFactory;
import java.security.InvalidKeyException;
import java.security.Key;
import javax.crypto.Cipher;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.EncryptedPrivateKeyInfo;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.tomcat.util.codec.binary.Base64;
private class Part {
    public static final String BEGIN_BOUNDARY = "-----BEGIN ";
    public static final String END_BOUNDARY = "-----END ";
    public String type;
    public String content;
    private Part() {
        this.content = "";
    }
    private byte[] decode() {
        return Base64.decodeBase64 ( this.content );
    }
    public X509Certificate toCertificate() throws CertificateException {
        final CertificateFactory factory = CertificateFactory.getInstance ( "X.509" );
        return ( X509Certificate ) factory.generateCertificate ( new ByteArrayInputStream ( this.decode() ) );
    }
    public PrivateKey toPrivateKey ( final String password ) throws GeneralSecurityException, IOException {
        KeySpec keySpec;
        if ( password == null ) {
            keySpec = new PKCS8EncodedKeySpec ( this.decode() );
        } else {
            final EncryptedPrivateKeyInfo privateKeyInfo = new EncryptedPrivateKeyInfo ( this.decode() );
            final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance ( privateKeyInfo.getAlgName() );
            final SecretKey secretKey = secretKeyFactory.generateSecret ( new PBEKeySpec ( password.toCharArray() ) );
            final Cipher cipher = Cipher.getInstance ( privateKeyInfo.getAlgName() );
            cipher.init ( 2, secretKey, privateKeyInfo.getAlgParameters() );
            keySpec = privateKeyInfo.getKeySpec ( cipher );
        }
        final InvalidKeyException exception = new InvalidKeyException ( PEMFile.access$200().getString ( "jsse.pemParseError", PEMFile.access$100 ( PEMFile.this ) ) );
        final String[] array = { "RSA", "DSA", "EC" };
        final int length = array.length;
        int i = 0;
        while ( i < length ) {
            final String algorithm = array[i];
            try {
                return KeyFactory.getInstance ( algorithm ).generatePrivate ( keySpec );
            } catch ( InvalidKeySpecException e ) {
                exception.addSuppressed ( e );
                ++i;
                continue;
            }
            break;
        }
        throw exception;
    }
}
