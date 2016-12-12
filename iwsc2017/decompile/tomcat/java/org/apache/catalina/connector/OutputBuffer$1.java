package org.apache.catalina.connector;
import java.io.IOException;
import org.apache.tomcat.util.buf.B2CConverter;
import java.nio.charset.Charset;
import java.security.PrivilegedExceptionAction;
static final class OutputBuffer$1 implements PrivilegedExceptionAction<Charset> {
    final   String val$encoding;
    @Override
    public Charset run() throws IOException {
        return B2CConverter.getCharset ( this.val$encoding );
    }
}
