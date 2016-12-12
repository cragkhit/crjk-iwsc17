package org.apache.coyote.http2;
import java.io.IOException;
import org.apache.coyote.Request;
import java.security.PrivilegedExceptionAction;
static final class Stream$1 implements PrivilegedExceptionAction<Void> {
    final   Http2UpgradeHandler val$handler;
    final   Request val$request;
    final   Stream val$stream;
    @Override
    public Void run() throws IOException {
        this.val$handler.push ( this.val$request, this.val$stream );
        return null;
    }
}
