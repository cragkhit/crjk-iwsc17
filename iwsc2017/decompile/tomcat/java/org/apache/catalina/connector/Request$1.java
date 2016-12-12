package org.apache.catalina.connector;
import javax.servlet.DispatcherType;
static final class Request$1 implements SpecialAttributeAdapter {
    @Override
    public Object get ( final Request request, final String name ) {
        return ( request.internalDispatcherType == null ) ? DispatcherType.REQUEST : request.internalDispatcherType;
    }
    @Override
    public void set ( final Request request, final String name, final Object value ) {
        request.internalDispatcherType = ( DispatcherType ) value;
    }
}
