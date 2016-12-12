package org.apache.tomcat.util.buf;
import java.io.IOException;
public interface CharInputChannel {
    int realReadChars() throws IOException;
}
