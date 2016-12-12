package org.apache.catalina.realm;
import org.apache.juli.logging.LogFactory;
import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;
import org.apache.tomcat.util.buf.HexUtils;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKeyFactory;
import org.apache.juli.logging.Log;
public class SecretKeyCredentialHandler extends DigestCredentialHandlerBase {
    private static final Log log;
    public static final String DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final int DEFAULT_KEY_LENGTH = 160;
    public static final int DEFAULT_ITERATIONS = 20000;
    private SecretKeyFactory secretKeyFactory;
    private int keyLength;
    public SecretKeyCredentialHandler() throws NoSuchAlgorithmException {
        this.keyLength = 160;
        this.setAlgorithm ( "PBKDF2WithHmacSHA1" );
    }
    @Override
    public String getAlgorithm() {
        return this.secretKeyFactory.getAlgorithm();
    }
    @Override
    public void setAlgorithm ( final String algorithm ) throws NoSuchAlgorithmException {
        final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance ( algorithm );
        this.secretKeyFactory = secretKeyFactory;
    }
    public int getKeyLength() {
        return this.keyLength;
    }
    public void setKeyLength ( final int keyLength ) {
        this.keyLength = keyLength;
    }
    @Override
    public boolean matches ( final String inputCredentials, final String storedCredentials ) {
        return this.matchesSaltIterationsEncoded ( inputCredentials, storedCredentials );
    }
    @Override
    protected String mutate ( final String inputCredentials, final byte[] salt, final int iterations ) {
        final KeySpec spec = new PBEKeySpec ( inputCredentials.toCharArray(), salt, iterations, this.getKeyLength() );
        try {
            return HexUtils.toHexString ( this.secretKeyFactory.generateSecret ( spec ).getEncoded() );
        } catch ( InvalidKeySpecException e ) {
            SecretKeyCredentialHandler.log.warn ( "pbeCredentialHandler.invalidKeySpec", e );
            return null;
        }
    }
    @Override
    protected int getDefaultIterations() {
        return 20000;
    }
    @Override
    protected Log getLog() {
        return SecretKeyCredentialHandler.log;
    }
    static {
        log = LogFactory.getLog ( SecretKeyCredentialHandler.class );
    }
}
