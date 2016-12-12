package org.apache.catalina.connector;
static final class Request$7 implements SpecialAttributeAdapter {
    @Override
    public Object get ( final Request request, final String name ) {
        return request.getConnector().getProtocolHandler().isSendfileSupported() && request.getCoyoteRequest().getSendfile();
    }
    @Override
    public void set ( final Request request, final String name, final Object value ) {
    }
}
