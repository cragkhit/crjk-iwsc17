package org.apache.catalina.core;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Arrays;
import java.util.Objects;
import org.apache.catalina.AccessLog;
public class AccessLogAdapter implements AccessLog {
    private AccessLog[] logs;
    public AccessLogAdapter ( final AccessLog log ) {
        Objects.requireNonNull ( log );
        this.logs = new AccessLog[] { log };
    }
    public void add ( final AccessLog log ) {
        Objects.requireNonNull ( log );
        final AccessLog[] newArray = Arrays.copyOf ( this.logs, this.logs.length + 1 );
        newArray[newArray.length - 1] = log;
        this.logs = newArray;
    }
    @Override
    public void log ( final Request request, final Response response, final long time ) {
        for ( final AccessLog log : this.logs ) {
            log.log ( request, response, time );
        }
    }
    @Override
    public void setRequestAttributesEnabled ( final boolean requestAttributesEnabled ) {
    }
    @Override
    public boolean getRequestAttributesEnabled() {
        return false;
    }
}
