package org.apache.catalina.connector;
import java.util.Map;
import java.security.PrivilegedAction;
private final class GetParameterMapPrivilegedAction implements PrivilegedAction<Map<String, String[]>> {
    @Override
    public Map<String, String[]> run() {
        return RequestFacade.this.request.getParameterMap();
    }
}
