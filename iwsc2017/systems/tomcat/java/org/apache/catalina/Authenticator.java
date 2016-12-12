package org.apache.catalina;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
public interface Authenticator {
    public boolean authenticate ( Request request, HttpServletResponse response )
    throws IOException;
    public void login ( String userName, String password, Request request )
    throws ServletException;
    public void logout ( Request request );
}
