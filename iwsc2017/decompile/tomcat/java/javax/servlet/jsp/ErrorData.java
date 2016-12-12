package javax.servlet.jsp;
public final class ErrorData {
    private final Throwable throwable;
    private final int statusCode;
    private final String uri;
    private final String servletName;
    public ErrorData ( final Throwable throwable, final int statusCode, final String uri, final String servletName ) {
        this.throwable = throwable;
        this.statusCode = statusCode;
        this.uri = uri;
        this.servletName = servletName;
    }
    public Throwable getThrowable() {
        return this.throwable;
    }
    public int getStatusCode() {
        return this.statusCode;
    }
    public String getRequestURI() {
        return this.uri;
    }
    public String getServletName() {
        return this.servletName;
    }
}
