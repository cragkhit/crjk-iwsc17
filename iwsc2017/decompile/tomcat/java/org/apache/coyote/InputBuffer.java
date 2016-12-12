package org.apache.coyote;
import java.io.IOException;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
public interface InputBuffer {
    int doRead ( ApplicationBufferHandler p0 ) throws IOException;
}
