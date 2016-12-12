package org.apache.tomcat.util.net;
import java.nio.ByteBuffer;
public interface ApplicationBufferHandler {
    void setByteBuffer ( ByteBuffer p0 );
    ByteBuffer getByteBuffer();
    void expand ( int p0 );
}
