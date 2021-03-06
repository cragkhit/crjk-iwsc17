package org.apache.catalina.realm;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
public class MessageDigestCredentialHandler extends DigestCredentialHandlerBase {
    private static final Log log = LogFactory.getLog ( MessageDigestCredentialHandler.class );
    public static final int DEFAULT_ITERATIONS = 1;
    private Charset encoding = StandardCharsets.UTF_8;
    private String algorithm = null;
    public String getEncoding() {
        return encoding.name();
    }
    public void setEncoding ( String encodingName ) {
        if ( encodingName == null ) {
            encoding = StandardCharsets.UTF_8;
        } else {
            try {
                this.encoding = B2CConverter.getCharset ( encodingName );
            } catch ( UnsupportedEncodingException e ) {
                log.warn ( sm.getString ( "mdCredentialHandler.unknownEncoding",
                                          encodingName, encoding.name() ) );
            }
        }
    }
    @Override
    public String getAlgorithm() {
        return algorithm;
    }
    @Override
    public void setAlgorithm ( String algorithm ) throws NoSuchAlgorithmException {
        ConcurrentMessageDigest.init ( algorithm );
        this.algorithm = algorithm;
    }
    @Override
    public boolean matches ( String inputCredentials, String storedCredentials ) {
        if ( inputCredentials == null || storedCredentials == null ) {
            return false;
        }
        if ( getAlgorithm() == null ) {
            return storedCredentials.equals ( inputCredentials );
        } else {
            if ( storedCredentials.startsWith ( "{MD5}" ) ||
                    storedCredentials.startsWith ( "{SHA}" ) ) {
                String serverDigest = storedCredentials.substring ( 5 );
                String userDigest = Base64.encodeBase64String ( ConcurrentMessageDigest.digest (
                                        getAlgorithm(), inputCredentials.getBytes ( StandardCharsets.ISO_8859_1 ) ) );
                return userDigest.equals ( serverDigest );
            } else if ( storedCredentials.startsWith ( "{SSHA}" ) ) {
                String serverDigestPlusSalt = storedCredentials.substring ( 6 );
                byte[] serverDigestPlusSaltBytes =
                    Base64.decodeBase64 ( serverDigestPlusSalt );
                final int saltPos = 20;
                byte[] serverDigestBytes = new byte[saltPos];
                System.arraycopy ( serverDigestPlusSaltBytes, 0,
                                   serverDigestBytes, 0, saltPos );
                final int saltLength = serverDigestPlusSaltBytes.length - saltPos;
                byte[] serverSaltBytes = new byte[saltLength];
                System.arraycopy ( serverDigestPlusSaltBytes, saltPos,
                                   serverSaltBytes, 0, saltLength );
                byte[] userDigestBytes = ConcurrentMessageDigest.digest ( getAlgorithm(),
                                         inputCredentials.getBytes ( StandardCharsets.ISO_8859_1 ),
                                         serverSaltBytes );
                return Arrays.equals ( userDigestBytes, serverDigestBytes );
            } else if ( storedCredentials.indexOf ( '$' ) > -1 ) {
                return matchesSaltIterationsEncoded ( inputCredentials, storedCredentials );
            } else {
                String userDigest = mutate ( inputCredentials, null, 1 );
                return storedCredentials.equalsIgnoreCase ( userDigest );
            }
        }
    }
    @Override
    protected String mutate ( String inputCredentials, byte[] salt, int iterations ) {
        if ( algorithm == null ) {
            return inputCredentials;
        } else {
            byte[] userDigest;
            if ( salt == null ) {
                userDigest = ConcurrentMessageDigest.digest ( algorithm, iterations,
                             inputCredentials.getBytes ( encoding ) );
            } else {
                userDigest = ConcurrentMessageDigest.digest ( algorithm, iterations,
                             salt, inputCredentials.getBytes ( encoding ) );
            }
            return HexUtils.toHexString ( userDigest );
        }
    }
    @Override
    protected int getDefaultIterations() {
        return DEFAULT_ITERATIONS;
    }
    @Override
    protected Log getLog() {
        return log;
    }
}
