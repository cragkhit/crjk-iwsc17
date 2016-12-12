package org.apache.catalina;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
public interface AsyncDispatcher {
    public void dispatch ( ServletRequest request, ServletResponse response )
    throws ServletException, IOException;
}
