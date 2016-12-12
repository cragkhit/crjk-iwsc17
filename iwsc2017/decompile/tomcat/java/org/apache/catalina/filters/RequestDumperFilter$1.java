package org.apache.catalina.filters;
static final class RequestDumperFilter$1 extends ThreadLocal<Timestamp> {
    @Override
    protected Timestamp initialValue() {
        return new Timestamp();
    }
}
