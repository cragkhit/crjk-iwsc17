package org.apache.catalina.core;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.catalina.AccessLog;
protected static final class NoopAccessLog implements AccessLog {
    @Override
    public void log ( final Request request, final Response response, final long time ) {
    }
    @Override
    public void setRequestAttributesEnabled ( final boolean requestAttributesEnabled ) {
    }
    @Override
    public boolean getRequestAttributesEnabled() {
        return false;
    }
}
