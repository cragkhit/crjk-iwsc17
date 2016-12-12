package org.apache.catalina;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
public interface Authenticator {
    boolean authenticate ( Request p0, HttpServletResponse p1 ) throws IOException;
    void login ( String p0, String p1, Request p2 ) throws ServletException;
    void logout ( Request p0 );
}
