package javax.security.auth.message;
public interface ProtectionPolicy {
    public static final String AUTHENTICATE_SENDER = "#authenticateSender";
    public static final String AUTHENTICATE_CONTENT = "#authenticateContent";
    public static final String AUTHENTICATE_RECIPIENT = "#authenticateRecipient";
    String getID();
}
