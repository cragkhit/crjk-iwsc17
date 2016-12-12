package org.apache.catalina.connector;
import java.security.PrivilegedAction;
private final class SetContentTypePrivilegedAction implements PrivilegedAction<Void> {
    private final String contentType;
    public SetContentTypePrivilegedAction ( final String contentType ) {
        this.contentType = contentType;
    }
    @Override
    public Void run() {
        ResponseFacade.this.response.setContentType ( this.contentType );
        return null;
    }
}
