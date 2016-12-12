package org.apache.catalina.realm;
import org.apache.juli.logging.Log;
import java.security.NoSuchAlgorithmException;
import org.apache.tomcat.util.buf.HexUtils;
import java.security.SecureRandom;
import java.util.Random;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.CredentialHandler;
public abstract class DigestCredentialHandlerBase implements CredentialHandler {
    protected static final StringManager sm;
    public static final int DEFAULT_SALT_LENGTH = 32;
    private int iterations;
    private int saltLength;
    private final Object randomLock;
    private volatile Random random;
    private boolean logInvalidStoredCredentials;
    public DigestCredentialHandlerBase() {
        this.iterations = this.getDefaultIterations();
        this.saltLength = this.getDefaultSaltLength();
        this.randomLock = new Object();
        this.random = null;
        this.logInvalidStoredCredentials = false;
    }
    public int getIterations() {
        return this.iterations;
    }
    public void setIterations ( final int iterations ) {
        this.iterations = iterations;
    }
    public int getSaltLength() {
        return this.saltLength;
    }
    public void setSaltLength ( final int saltLength ) {
        this.saltLength = saltLength;
    }
    public boolean getLogInvalidStoredCredentials() {
        return this.logInvalidStoredCredentials;
    }
    public void setLogInvalidStoredCredentials ( final boolean logInvalidStoredCredentials ) {
        this.logInvalidStoredCredentials = logInvalidStoredCredentials;
    }
    @Override
    public String mutate ( final String userCredential ) {
        byte[] salt = null;
        final int iterations = this.getIterations();
        final int saltLength = this.getSaltLength();
        if ( saltLength == 0 ) {
            salt = new byte[0];
        } else if ( saltLength > 0 ) {
            if ( this.random == null ) {
                synchronized ( this.randomLock ) {
                    if ( this.random == null ) {
                        this.random = new SecureRandom();
                    }
                }
            }
            salt = new byte[saltLength];
            this.random.nextBytes ( salt );
        }
        final String serverCredential = this.mutate ( userCredential, salt, iterations );
        if ( saltLength == 0 && iterations == 1 ) {
            return serverCredential;
        }
        final StringBuilder result = new StringBuilder ( ( saltLength << 1 ) + 10 + serverCredential.length() + 2 );
        result.append ( HexUtils.toHexString ( salt ) );
        result.append ( '$' );
        result.append ( iterations );
        result.append ( '$' );
        result.append ( serverCredential );
        return result.toString();
    }
    protected boolean matchesSaltIterationsEncoded ( final String inputCredentials, final String storedCredentials ) {
        final int sep1 = storedCredentials.indexOf ( 36 );
        final int sep2 = storedCredentials.indexOf ( 36, sep1 + 1 );
        if ( sep1 < 0 || sep2 < 0 ) {
            this.logInvalidStoredCredentials ( storedCredentials );
            return false;
        }
        final String hexSalt = storedCredentials.substring ( 0, sep1 );
        final int iterations = Integer.parseInt ( storedCredentials.substring ( sep1 + 1, sep2 ) );
        final String storedHexEncoded = storedCredentials.substring ( sep2 + 1 );
        byte[] salt;
        try {
            salt = HexUtils.fromHexString ( hexSalt );
        } catch ( IllegalArgumentException iae ) {
            this.logInvalidStoredCredentials ( storedCredentials );
            return false;
        }
        final String inputHexEncoded = this.mutate ( inputCredentials, salt, iterations );
        return storedHexEncoded.equalsIgnoreCase ( inputHexEncoded );
    }
    private void logInvalidStoredCredentials ( final String storedCredentials ) {
        if ( this.logInvalidStoredCredentials ) {
            this.getLog().warn ( DigestCredentialHandlerBase.sm.getString ( "credentialHandler.invalidStoredCredential", storedCredentials ) );
        }
    }
    protected int getDefaultSaltLength() {
        return 32;
    }
    protected abstract String mutate ( final String p0, final byte[] p1, final int p2 );
    public abstract void setAlgorithm ( final String p0 ) throws NoSuchAlgorithmException;
    public abstract String getAlgorithm();
    protected abstract int getDefaultIterations();
    protected abstract Log getLog();
    static {
        sm = StringManager.getManager ( DigestCredentialHandlerBase.class );
    }
}
