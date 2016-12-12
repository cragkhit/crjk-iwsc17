package org.apache.tomcat.util.net.jsse;
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
import java.security.cert.CertificateException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import org.apache.tomcat.util.codec.binary.Base64;
import java.util.Iterator;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.security.GeneralSecurityException;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
class PEMFile {
    private static final StringManager sm;
    private String filename;
    private List<X509Certificate> certificates;
    private PrivateKey privateKey;
    public List<X509Certificate> getCertificates() {
        return this.certificates;
    }
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }
    public PEMFile ( final String filename ) throws IOException, GeneralSecurityException {
        this ( filename, null );
    }
    public PEMFile ( final String filename, final String password ) throws IOException, GeneralSecurityException {
        this.certificates = new ArrayList<X509Certificate>();
        this.filename = filename;
        final List<Part> parts = new ArrayList<Part>();
        try ( final BufferedReader in = new BufferedReader ( new FileReader ( filename ) ) ) {
            Part part = null;
            String line;
            while ( ( line = in.readLine() ) != null ) {
                if ( line.startsWith ( "-----BEGIN " ) ) {
                    part = new Part();
                    part.type = line.substring ( "-----BEGIN ".length(), line.length() - 5 ).trim();
                } else if ( line.startsWith ( "-----END " ) ) {
                    parts.add ( part );
                    part = null;
                } else {
                    if ( part == null || line.contains ( ":" ) || line.startsWith ( " " ) ) {
                        continue;
                    }
                    final StringBuilder sb = new StringBuilder();
                    final Part part3 = part;
                    part3.content = sb.append ( part3.content ).append ( line ).toString();
                }
            }
        }
        for ( final Part part2 : parts ) {
            final String type = part2.type;
            switch ( type ) {
            case "PRIVATE KEY": {
                this.privateKey = part2.toPrivateKey ( null );
                continue;
            }
            case "ENCRYPTED PRIVATE KEY": {
                this.privateKey = part2.toPrivateKey ( password );
                continue;
            }
            case "CERTIFICATE":
            case "X509 CERTIFICATE": {
                this.certificates.add ( part2.toCertificate() );
                continue;
            }
            }
        }
    }
    static {
        sm = StringManager.getManager ( PEMFile.class );
    }
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
            final InvalidKeyException exception = new InvalidKeyException ( PEMFile.sm.getString ( "jsse.pemParseError", PEMFile.this.filename ) );
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
}
