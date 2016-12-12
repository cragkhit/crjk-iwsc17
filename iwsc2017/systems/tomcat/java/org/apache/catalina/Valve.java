package org.apache.catalina;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
public interface Valve {
    public Valve getNext();
    public void setNext ( Valve valve );
    public void backgroundProcess();
    public void invoke ( Request request, Response response )
    throws IOException, ServletException;
    public boolean isAsyncSupported();
}
