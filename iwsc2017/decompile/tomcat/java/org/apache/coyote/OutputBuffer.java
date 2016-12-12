package org.apache.coyote;
import java.io.IOException;
import java.nio.ByteBuffer;
public interface OutputBuffer {
    int doWrite ( ByteBuffer p0 ) throws IOException;
    long getBytesWritten();
}
