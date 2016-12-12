package org.apache.tomcat.websocket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import javax.websocket.Extension;
public interface Transformation {
    void setNext ( Transformation t );
    boolean validateRsvBits ( int i );
    Extension getExtensionResponse();
    TransformationResult getMoreData ( byte opCode, boolean fin, int rsv, ByteBuffer dest ) throws IOException;
    boolean validateRsv ( int rsv, byte opCode );
    List<MessagePart> sendMessagePart ( List<MessagePart> messageParts );
    void close();
}
