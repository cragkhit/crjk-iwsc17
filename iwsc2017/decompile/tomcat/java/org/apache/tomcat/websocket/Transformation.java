package org.apache.tomcat.websocket;
import java.util.List;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.websocket.Extension;
public interface Transformation {
    void setNext ( Transformation p0 );
    boolean validateRsvBits ( int p0 );
    Extension getExtensionResponse();
    TransformationResult getMoreData ( byte p0, boolean p1, int p2, ByteBuffer p3 ) throws IOException;
    boolean validateRsv ( int p0, byte p1 );
    List<MessagePart> sendMessagePart ( List<MessagePart> p0 );
    void close();
}
