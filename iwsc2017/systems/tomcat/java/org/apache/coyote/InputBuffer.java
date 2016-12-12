package org.apache.coyote;
import java.io.IOException;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
public interface InputBuffer {
    public int doRead ( ApplicationBufferHandler handler ) throws IOException;
}
