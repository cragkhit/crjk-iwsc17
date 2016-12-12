package org.apache.catalina.connector;
import java.io.IOException;
import org.apache.tomcat.util.buf.CharChunk;
import java.security.PrivilegedExceptionAction;
class Response$3 implements PrivilegedExceptionAction<CharChunk> {
    final   String val$frelativePath;
    final   int val$fend;
    @Override
    public CharChunk run() throws IOException {
        return Response.this.urlEncoder.encodeURL ( this.val$frelativePath, 0, this.val$fend );
    }
}
