package org.apache.catalina.filters;
@FunctionalInterface
private interface NonceSupplier<T, R> {
    R getNonce ( T p0, String p1 );
}
