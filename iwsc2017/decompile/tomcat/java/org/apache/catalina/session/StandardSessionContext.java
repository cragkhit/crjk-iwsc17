package org.apache.catalina.session;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpSessionContext;
@Deprecated
final class StandardSessionContext implements HttpSessionContext {
    private static final List<String> emptyString;
    @Deprecated
    public Enumeration<String> getIds() {
        return Collections.enumeration ( StandardSessionContext.emptyString );
    }
    @Deprecated
    public HttpSession getSession ( final String id ) {
        return null;
    }
    static {
        emptyString = Collections.emptyList();
    }
}
