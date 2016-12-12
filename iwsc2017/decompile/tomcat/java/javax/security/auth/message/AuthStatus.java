package javax.security.auth.message;
public class AuthStatus {
    public static final AuthStatus SUCCESS;
    public static final AuthStatus FAILURE;
    public static final AuthStatus SEND_SUCCESS;
    public static final AuthStatus SEND_FAILURE;
    public static final AuthStatus SEND_CONTINUE;
    private final String name;
    private AuthStatus ( final String name ) {
        this.name = name;
    }
    @Override
    public String toString() {
        return this.name;
    }
    static {
        SUCCESS = new AuthStatus ( "SUCCESS" );
        FAILURE = new AuthStatus ( "FAILURE" );
        SEND_SUCCESS = new AuthStatus ( "SEND_SUCCESS" );
        SEND_FAILURE = new AuthStatus ( "SEND_FAILURE" );
        SEND_CONTINUE = new AuthStatus ( "SEND_CONTINUE" );
    }
}
