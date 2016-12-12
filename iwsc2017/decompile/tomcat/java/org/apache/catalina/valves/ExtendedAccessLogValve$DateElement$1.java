package org.apache.catalina.valves;
static final class ExtendedAccessLogValve$DateElement$1 extends ThreadLocal<ElementTimestampStruct> {
    @Override
    protected ElementTimestampStruct initialValue() {
        return new ElementTimestampStruct ( "yyyy-MM-dd" );
    }
}
