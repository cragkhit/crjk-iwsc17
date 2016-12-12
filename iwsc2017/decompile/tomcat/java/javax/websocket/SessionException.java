package javax.websocket;
public class SessionException extends Exception {
    private static final long serialVersionUID = 1L;
    private final Session session;
    public SessionException ( final String message, final Throwable cause, final Session session ) {
        super ( message, cause );
        this.session = session;
    }
    public Session getSession() {
        return this.session;
    }
}
