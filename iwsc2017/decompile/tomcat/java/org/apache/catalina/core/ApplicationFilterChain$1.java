package org.apache.catalina.core;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.security.PrivilegedExceptionAction;
class ApplicationFilterChain$1 implements PrivilegedExceptionAction<Void> {
    final   ServletRequest val$req;
    final   ServletResponse val$res;
    @Override
    public Void run() throws ServletException, IOException {
        ApplicationFilterChain.access$000 ( ApplicationFilterChain.this, this.val$req, this.val$res );
        return null;
    }
}
