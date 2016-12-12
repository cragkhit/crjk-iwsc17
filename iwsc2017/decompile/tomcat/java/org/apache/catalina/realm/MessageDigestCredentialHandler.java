package org.apache.catalina.realm;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.HexUtils;
import java.util.Arrays;
import org.apache.tomcat.util.codec.binary.Base64;
import java.security.NoSuchAlgorithmException;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import java.io.UnsupportedEncodingException;
import org.apache.tomcat.util.buf.B2CConverter;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import org.apache.juli.logging.Log;
public class MessageDigestCredentialHandler extends DigestCredentialHandlerBase {
    private static final Log log;
    public static final int DEFAULT_ITERATIONS = 1;
    private Charset encoding;
    private String algorithm;
    public MessageDigestCredentialHandler() {
        this.encoding = StandardCharsets.UTF_8;
        this.algorithm = null;
    }
    public String getEncoding() {
        return this.encoding.name();
    }
    public void setEncoding ( final String encodingName ) {
        if ( encodingName == null ) {
            this.encoding = StandardCharsets.UTF_8;
        } else {
            try {
                this.encoding = B2CConverter.getCharset ( encodingName );
            } catch ( UnsupportedEncodingException e ) {
                MessageDigestCredentialHandler.log.warn ( MessageDigestCredentialHandler.sm.getString ( "mdCredentialHandler.unknownEncoding", encodingName, this.encoding.name() ) );
            }
        }
    }
    @Override
    public String getAlgorithm() {
        return this.algorithm;
    }
    @Override
    public void setAlgorithm ( final String algorithm ) throws NoSuchAlgorithmException {
        ConcurrentMessageDigest.init ( algorithm );
        this.algorithm = algorithm;
    }
    @Override
    public boolean matches ( final String inputCredentials, final String storedCredentials ) {
        if ( inputCredentials == null || storedCredentials == null ) {
            return false;
        }
        if ( this.getAlgorithm() == null ) {
            return storedCredentials.equals ( inputCredentials );
        }
        if ( storedCredentials.startsWith ( "{MD5}" ) || storedCredentials.startsWith ( "{SHA}" ) ) {
            final String serverDigest = storedCredentials.substring ( 5 );
            final String userDigest = Base64.encodeBase64String ( ConcurrentMessageDigest.digest ( this.getAlgorithm(), new byte[][] { inputCredentials.getBytes ( StandardCharsets.ISO_8859_1 ) } ) );
            return userDigest.equals ( serverDigest );
        }
        if ( storedCredentials.startsWith ( "{SSHA}" ) ) {
            final String serverDigestPlusSalt = storedCredentials.substring ( 6 );
            final byte[] serverDigestPlusSaltBytes = Base64.decodeBase64 ( serverDigestPlusSalt );
            final int saltPos = 20;
            final byte[] serverDigestBytes = new byte[20];
            System.arraycopy ( serverDigestPlusSaltBytes, 0, serverDigestBytes, 0, 20 );
            final int saltLength = serverDigestPlusSaltBytes.length - 20;
            final byte[] serverSaltBytes = new byte[saltLength];
            System.arraycopy ( serverDigestPlusSaltBytes, 20, serverSaltBytes, 0, saltLength );
            final byte[] userDigestBytes = ConcurrentMessageDigest.digest ( this.getAlgorithm(), new byte[][] { inputCredentials.getBytes ( StandardCharsets.ISO_8859_1 ), serverSaltBytes } );
            return Arrays.equals ( userDigestBytes, serverDigestBytes );
        }
        if ( storedCredentials.indexOf ( 36 ) > -1 ) {
            return this.matchesSaltIterationsEncoded ( inputCredentials, storedCredentials );
        }
        final String userDigest2 = this.mutate ( inputCredentials, null, 1 );
        return storedCredentials.equalsIgnoreCase ( userDigest2 );
    }
    @Override
    protected String mutate ( final String inputCredentials, final byte[] salt, final int iterations ) {
        if ( this.algorithm == null ) {
            return inputCredentials;
        }
        byte[] userDigest;
        if ( salt == null ) {
            userDigest = ConcurrentMessageDigest.digest ( this.algorithm, iterations, new byte[][] { inputCredentials.getBytes ( this.encoding ) } );
        } else {
            userDigest = ConcurrentMessageDigest.digest ( this.algorithm, iterations, new byte[][] { salt, inputCredentials.getBytes ( this.encoding ) } );
        }
        return HexUtils.toHexString ( userDigest );
    }
    @Override
    protected int getDefaultIterations() {
        return 1;
    }
    @Override
    protected Log getLog() {
        return MessageDigestCredentialHandler.log;
    }
    static {
        log = LogFactory.getLog ( MessageDigestCredentialHandler.class );
    }
}
