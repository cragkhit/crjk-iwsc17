package javax.servlet;
public interface SessionCookieConfig {
    void setName ( String p0 );
    String getName();
    void setDomain ( String p0 );
    String getDomain();
    void setPath ( String p0 );
    String getPath();
    void setComment ( String p0 );
    String getComment();
    void setHttpOnly ( boolean p0 );
    boolean isHttpOnly();
    void setSecure ( boolean p0 );
    boolean isSecure();
    void setMaxAge ( int p0 );
    int getMaxAge();
}
