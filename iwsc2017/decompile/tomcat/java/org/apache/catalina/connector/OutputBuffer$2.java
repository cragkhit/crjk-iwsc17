package org.apache.catalina.connector;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.tomcat.util.buf.C2BConverter;
import java.security.PrivilegedExceptionAction;
static final class OutputBuffer$2 implements PrivilegedExceptionAction<C2BConverter> {
    final   Charset val$charset;
    @Override
    public C2BConverter run() throws IOException {
        return new C2BConverter ( this.val$charset );
    }
}
