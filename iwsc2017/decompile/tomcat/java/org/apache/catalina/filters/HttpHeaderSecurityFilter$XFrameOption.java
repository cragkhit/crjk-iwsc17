package org.apache.catalina.filters;
private enum XFrameOption {
    DENY ( "DENY" ),
    SAME_ORIGIN ( "SAMEORIGIN" ),
    ALLOW_FROM ( "ALLOW-FROM" );
    private final String headerValue;
    private XFrameOption ( final String headerValue ) {
        this.headerValue = headerValue;
    }
    public String getHeaderValue() {
        return this.headerValue;
    }
}
