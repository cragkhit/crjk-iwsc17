package org.apache.tomcat.util.buf;
import java.io.IOException;
public interface ByteInputChannel {
    int realReadBytes() throws IOException;
}
