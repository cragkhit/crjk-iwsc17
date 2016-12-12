package javax.servlet;
public class UnavailableException extends ServletException {
    private static final long serialVersionUID = 1L;
    private final Servlet servlet;
    private final boolean permanent;
    private final int seconds;
    @Deprecated
    public UnavailableException ( Servlet servlet, String msg ) {
        super ( msg );
        this.servlet = servlet;
        permanent = true;
        this.seconds = 0;
    }
    @Deprecated
    public UnavailableException ( int seconds, Servlet servlet, String msg ) {
        super ( msg );
        this.servlet = servlet;
        if ( seconds <= 0 ) {
            this.seconds = -1;
        } else {
            this.seconds = seconds;
        }
        permanent = false;
    }
    public UnavailableException ( String msg ) {
        super ( msg );
        seconds = 0;
        servlet = null;
        permanent = true;
    }
    public UnavailableException ( String msg, int seconds ) {
        super ( msg );
        if ( seconds <= 0 ) {
            this.seconds = -1;
        } else {
            this.seconds = seconds;
        }
        servlet = null;
        permanent = false;
    }
    public boolean isPermanent() {
        return permanent;
    }
    @Deprecated
    public Servlet getServlet() {
        return servlet;
    }
    public int getUnavailableSeconds() {
        return permanent ? -1 : seconds;
    }
}
