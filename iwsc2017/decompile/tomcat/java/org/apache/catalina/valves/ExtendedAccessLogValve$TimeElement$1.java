package org.apache.catalina.valves;
static final class ExtendedAccessLogValve$TimeElement$1 extends ThreadLocal<ElementTimestampStruct> {
    @Override
    protected ElementTimestampStruct initialValue() {
        return new ElementTimestampStruct ( "HH:mm:ss" );
    }
}
