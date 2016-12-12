package org.apache.catalina.connector;
static final class Request$3 implements SpecialAttributeAdapter {
    @Override
    public Object get ( final Request request, final String name ) {
        return request.asyncSupported;
    }
    @Override
    public void set ( final Request request, final String name, final Object value ) {
        final Boolean oldValue = request.asyncSupported;
        request.asyncSupported = ( Boolean ) value;
        Request.access$000 ( request, name, value, oldValue );
    }
}
