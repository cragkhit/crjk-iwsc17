package javax.servlet;
public interface AsyncContext {
    public static final String ASYNC_REQUEST_URI = "javax.servlet.async.request_uri";
    public static final String ASYNC_CONTEXT_PATH = "javax.servlet.async.context_path";
    public static final String ASYNC_PATH_INFO = "javax.servlet.async.path_info";
    public static final String ASYNC_SERVLET_PATH = "javax.servlet.async.servlet_path";
    public static final String ASYNC_QUERY_STRING = "javax.servlet.async.query_string";
    ServletRequest getRequest();
    ServletResponse getResponse();
    boolean hasOriginalRequestAndResponse();
    void dispatch();
    void dispatch ( String p0 );
    void dispatch ( ServletContext p0, String p1 );
    void complete();
    void start ( Runnable p0 );
    void addListener ( AsyncListener p0 );
    void addListener ( AsyncListener p0, ServletRequest p1, ServletResponse p2 );
    <T extends AsyncListener> T createListener ( Class<T> p0 ) throws ServletException;
    void setTimeout ( long p0 );
    long getTimeout();
}
