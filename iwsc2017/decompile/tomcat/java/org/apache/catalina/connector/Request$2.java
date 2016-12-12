package org.apache.catalina.connector;
static final class Request$2 implements SpecialAttributeAdapter {
    @Override
    public Object get ( final Request request, final String name ) {
        return ( request.requestDispatcherPath == null ) ? request.getRequestPathMB().toString() : request.requestDispatcherPath.toString();
    }
    @Override
    public void set ( final Request request, final String name, final Object value ) {
        request.requestDispatcherPath = value;
    }
}
