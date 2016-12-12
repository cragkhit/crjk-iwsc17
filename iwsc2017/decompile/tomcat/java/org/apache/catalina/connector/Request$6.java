package org.apache.catalina.connector;
static final class Request$6 implements SpecialAttributeAdapter {
    @Override
    public Object get ( final Request request, final String name ) {
        return request.getCoyoteRequest().getParameters().getParseFailedReason();
    }
    @Override
    public void set ( final Request request, final String name, final Object value ) {
    }
}
