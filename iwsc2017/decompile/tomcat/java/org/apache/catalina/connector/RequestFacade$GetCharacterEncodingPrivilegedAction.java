package org.apache.catalina.connector;
import java.security.PrivilegedAction;
private final class GetCharacterEncodingPrivilegedAction implements PrivilegedAction<String> {
    @Override
    public String run() {
        return RequestFacade.this.request.getCharacterEncoding();
    }
}
