package javax.servlet.http;
import java.util.Set;
public interface PushBuilder {
    PushBuilder method ( String method );
    PushBuilder queryString ( String queryString );
    PushBuilder sessionId ( String sessionId );
    PushBuilder conditional ( boolean conditional );
    PushBuilder setHeader ( String name, String value );
    PushBuilder addHeader ( String name, String value );
    PushBuilder removeHeader ( String name );
    PushBuilder path ( String path );
    PushBuilder etag ( String etag );
    PushBuilder lastModified ( String lastModified );
    boolean push();
    String getMethod();
    String getQueryString();
    String getSessionId();
    boolean isConditional();
    Set<String> getHeaderNames();
    String getHeader ( String name );
    String getPath();
    String getEtag();
    String getLastModified();
}
