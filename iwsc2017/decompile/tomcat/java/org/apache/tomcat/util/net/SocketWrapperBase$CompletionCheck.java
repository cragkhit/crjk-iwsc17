package org.apache.tomcat.util.net;
import java.nio.ByteBuffer;
public interface CompletionCheck {
    CompletionHandlerCall callHandler ( CompletionState p0, ByteBuffer[] p1, int p2, int p3 );
}
