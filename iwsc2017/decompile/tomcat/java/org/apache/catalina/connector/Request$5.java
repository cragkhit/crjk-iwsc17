package org.apache.catalina.connector;
static final class Request$5 implements SpecialAttributeAdapter {
    @Override
    public Object get ( final Request request, final String name ) {
        if ( request.getCoyoteRequest().getParameters().isParseFailed() ) {
            return Boolean.TRUE;
        }
        return null;
    }
    @Override
    public void set ( final Request request, final String name, final Object value ) {
    }
}
