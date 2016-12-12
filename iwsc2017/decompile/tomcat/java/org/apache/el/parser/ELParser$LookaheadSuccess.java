package org.apache.el.parser;
private static final class LookaheadSuccess extends Error {
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
