package org.apache.catalina.connector;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.tomcat.util.buf.B2CConverter;
import java.security.PrivilegedExceptionAction;
static final class InputBuffer$1 implements PrivilegedExceptionAction<B2CConverter> {
    final   Charset val$charset;
    @Override
    public B2CConverter run() throws IOException {
        return new B2CConverter ( this.val$charset );
    }
}
