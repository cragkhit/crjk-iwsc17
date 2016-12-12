package org.apache.catalina.realm;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import org.apache.catalina.CredentialHandler;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.res.StringManager;
public abstract class DigestCredentialHandlerBase implements CredentialHandler {
    protected static final StringManager sm =
        StringManager.getManager ( DigestCredentialHandlerBase.class );
    public static final int DEFAULT_SALT_LENGTH = 32;
    private int iterations = getDefaultIterations();
    private int saltLength = getDefaultSaltLength();
    private final Object randomLock = new Object();
    private volatile Random random = null;
    private boolean logInvalidStoredCredentials = false;
    public int getIterations() {
        return iterations;
    }
    public void setIterations ( int iterations ) {
        this.iterations = iterations;
    }
    public int getSaltLength() {
        return saltLength;
    }
    public void setSaltLength ( int saltLength ) {
        this.saltLength = saltLength;
    }
    public boolean getLogInvalidStoredCredentials() {
        return logInvalidStoredCredentials;
    }
    public void setLogInvalidStoredCredentials ( boolean logInvalidStoredCredentials ) {
        this.logInvalidStoredCredentials = logInvalidStoredCredentials;
    }
    @Override
    public String mutate ( String userCredential ) {
        byte[] salt = null;
        int iterations = getIterations();
        int saltLength = getSaltLength();
        if ( saltLength == 0 ) {
            salt = new byte[0];
        } else if ( saltLength > 0 ) {
            if ( random == null ) {
                synchronized ( randomLock ) {
                    if ( random == null ) {
                        random = new SecureRandom();
                    }
                }
            }
            salt = new byte[saltLength];
            random.nextBytes ( salt );
        }
        String serverCredential = mutate ( userCredential, salt, iterations );
        if ( saltLength == 0 && iterations == 1 ) {
            return serverCredential;
        } else {
            StringBuilder result =
                new StringBuilder ( ( saltLength << 1 ) + 10 + serverCredential.length() + 2 );
            result.append ( HexUtils.toHexString ( salt ) );
            result.append ( '$' );
            result.append ( iterations );
            result.append ( '$' );
            result.append ( serverCredential );
            return result.toString();
        }
    }
    protected boolean matchesSaltIterationsEncoded ( String inputCredentials,
            String storedCredentials ) {
        int sep1 = storedCredentials.indexOf ( '$' );
        int sep2 = storedCredentials.indexOf ( '$', sep1 + 1 );
        if ( sep1 < 0 || sep2 < 0 ) {
            logInvalidStoredCredentials ( storedCredentials );
            return false;
        }
        String hexSalt = storedCredentials.substring ( 0,  sep1 );
        int iterations = Integer.parseInt ( storedCredentials.substring ( sep1 + 1, sep2 ) );
        String storedHexEncoded = storedCredentials.substring ( sep2 + 1 );
        byte[] salt;
        try {
            salt = HexUtils.fromHexString ( hexSalt );
        } catch ( IllegalArgumentException iae ) {
            logInvalidStoredCredentials ( storedCredentials );
            return false;
        }
        String inputHexEncoded = mutate ( inputCredentials, salt, iterations );
        return storedHexEncoded.equalsIgnoreCase ( inputHexEncoded );
    }
    private void logInvalidStoredCredentials ( String storedCredentials ) {
        if ( logInvalidStoredCredentials ) {
            getLog().warn ( sm.getString ( "credentialHandler.invalidStoredCredential",
                                           storedCredentials ) );
        }
    }
    protected int getDefaultSaltLength() {
        return DEFAULT_SALT_LENGTH;
    }
    protected abstract String mutate ( String inputCredentials, byte[] salt, int iterations );
    public abstract void setAlgorithm ( String algorithm ) throws NoSuchAlgorithmException;
    public abstract String getAlgorithm();
    protected abstract int getDefaultIterations();
    protected abstract Log getLog();
}
