package javax.servlet;
public interface AsyncContext {
    public static final String ASYNC_REQUEST_URI =
        "javax.servlet.async.request_uri";
    public static final String ASYNC_CONTEXT_PATH  =
        "javax.servlet.async.context_path";
    public static final String ASYNC_PATH_INFO =
        "javax.servlet.async.path_info";
    public static final String ASYNC_SERVLET_PATH =
        "javax.servlet.async.servlet_path";
    public static final String ASYNC_QUERY_STRING =
        "javax.servlet.async.query_string";
    ServletRequest getRequest();
    ServletResponse getResponse();
    boolean hasOriginalRequestAndResponse();
    void dispatch();
    void dispatch ( String path );
    void dispatch ( ServletContext context, String path );
    void complete();
    void start ( Runnable run );
    void addListener ( AsyncListener listener );
    void addListener ( AsyncListener listener, ServletRequest request,
                       ServletResponse response );
    <T extends AsyncListener> T createListener ( Class<T> clazz )
    throws ServletException;
    void setTimeout ( long timeout );
    long getTimeout();
}
