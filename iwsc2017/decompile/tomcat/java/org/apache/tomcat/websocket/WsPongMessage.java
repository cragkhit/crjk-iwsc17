package org.apache.tomcat.websocket;
import java.nio.ByteBuffer;
import javax.websocket.PongMessage;
public class WsPongMessage implements PongMessage {
    private final ByteBuffer applicationData;
    public WsPongMessage ( final ByteBuffer applicationData ) {
        final byte[] dst = new byte[applicationData.limit()];
        applicationData.get ( dst );
        this.applicationData = ByteBuffer.wrap ( dst );
    }
    public ByteBuffer getApplicationData() {
        return this.applicationData;
    }
}
