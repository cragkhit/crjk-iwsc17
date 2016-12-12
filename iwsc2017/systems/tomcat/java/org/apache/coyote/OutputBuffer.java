package org.apache.coyote;
import java.io.IOException;
import java.nio.ByteBuffer;
public interface OutputBuffer {
    public int doWrite ( ByteBuffer chunk ) throws IOException;
    public long getBytesWritten();
}
