package org.apache.catalina.filters;
@FunctionalInterface
private interface NonceConsumer<T> {
    void setNonce ( T p0, String p1, String p2 );
}
