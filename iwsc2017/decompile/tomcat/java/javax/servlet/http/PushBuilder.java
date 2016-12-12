package javax.servlet.http;
import java.util.Set;
public interface PushBuilder {
    PushBuilder method ( String p0 );
    PushBuilder queryString ( String p0 );
    PushBuilder sessionId ( String p0 );
    PushBuilder conditional ( boolean p0 );
    PushBuilder setHeader ( String p0, String p1 );
    PushBuilder addHeader ( String p0, String p1 );
    PushBuilder removeHeader ( String p0 );
    PushBuilder path ( String p0 );
    PushBuilder etag ( String p0 );
    PushBuilder lastModified ( String p0 );
    boolean push();
    String getMethod();
    String getQueryString();
    String getSessionId();
    boolean isConditional();
    Set<String> getHeaderNames();
    String getHeader ( String p0 );
    String getPath();
    String getEtag();
    String getLastModified();
}
