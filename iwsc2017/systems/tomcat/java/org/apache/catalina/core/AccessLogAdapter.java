package org.apache.catalina.core;
import java.util.Arrays;
import java.util.Objects;
import org.apache.catalina.AccessLog;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
public class AccessLogAdapter implements AccessLog {
    private AccessLog[] logs;
    public AccessLogAdapter ( AccessLog log ) {
        Objects.requireNonNull ( log );
        logs = new AccessLog[] { log };
    }
    public void add ( AccessLog log ) {
        Objects.requireNonNull ( log );
        AccessLog newArray[] = Arrays.copyOf ( logs, logs.length + 1 );
        newArray[newArray.length - 1] = log;
        logs = newArray;
    }
    @Override
    public void log ( Request request, Response response, long time ) {
        for ( AccessLog log : logs ) {
            log.log ( request, response, time );
        }
    }
    @Override
    public void setRequestAttributesEnabled ( boolean requestAttributesEnabled ) {
    }
    @Override
    public boolean getRequestAttributesEnabled() {
        return false;
    }
}
