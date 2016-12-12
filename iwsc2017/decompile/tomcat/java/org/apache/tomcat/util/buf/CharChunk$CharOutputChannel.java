package org.apache.tomcat.util.buf;
import java.io.IOException;
public interface CharOutputChannel {
    void realWriteChars ( char[] p0, int p1, int p2 ) throws IOException;
}
