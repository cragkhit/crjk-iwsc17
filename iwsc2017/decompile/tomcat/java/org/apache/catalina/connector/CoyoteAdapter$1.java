package org.apache.catalina.connector;
static final class CoyoteAdapter$1 extends ThreadLocal<String> {
    @Override
    protected String initialValue() {
        return Thread.currentThread().getName();
    }
}
