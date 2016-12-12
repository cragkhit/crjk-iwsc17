package javax.servlet;
public class UnavailableException extends ServletException {
    private static final long serialVersionUID = 1L;
    private final Servlet servlet;
    private final boolean permanent;
    private final int seconds;
    public UnavailableException ( final Servlet servlet, final String msg ) {
        super ( msg );
        this.servlet = servlet;
        this.permanent = true;
        this.seconds = 0;
    }
    public UnavailableException ( final int seconds, final Servlet servlet, final String msg ) {
        super ( msg );
        this.servlet = servlet;
        if ( seconds <= 0 ) {
            this.seconds = -1;
        } else {
            this.seconds = seconds;
        }
        this.permanent = false;
    }
    public UnavailableException ( final String msg ) {
        super ( msg );
        this.seconds = 0;
        this.servlet = null;
        this.permanent = true;
    }
    public UnavailableException ( final String msg, final int seconds ) {
        super ( msg );
        if ( seconds <= 0 ) {
            this.seconds = -1;
        } else {
            this.seconds = seconds;
        }
        this.servlet = null;
        this.permanent = false;
    }
    public boolean isPermanent() {
        return this.permanent;
    }
    @Deprecated
    public Servlet getServlet() {
        return this.servlet;
    }
    public int getUnavailableSeconds() {
        return this.permanent ? -1 : this.seconds;
    }
}
