package org.apache.tomcat.util.net;
import java.nio.ByteBuffer;
public interface ApplicationBufferHandler {
    public void setByteBuffer ( ByteBuffer buffer );
    public ByteBuffer getByteBuffer();
    public void expand ( int size );
}
