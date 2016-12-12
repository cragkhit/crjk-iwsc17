package org.apache.tomcat.util.buf;
import java.nio.ByteBuffer;
import java.io.IOException;
public interface ByteOutputChannel {
    void realWriteBytes ( byte[] p0, int p1, int p2 ) throws IOException;
    void realWriteBytes ( ByteBuffer p0 ) throws IOException;
}
