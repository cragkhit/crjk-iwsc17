package org.apache.catalina;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
public interface Valve {
    Valve getNext();
    void setNext ( Valve p0 );
    void backgroundProcess();
    void invoke ( Request p0, Response p1 ) throws IOException, ServletException;
    boolean isAsyncSupported();
}
