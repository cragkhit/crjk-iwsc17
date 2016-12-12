package org.apache.tomcat.util.buf;
import java.io.CharConversionException;
private static class DecodeException extends CharConversionException {
    private static final long serialVersionUID = 1L;
    public DecodeException ( final String s ) {
        super ( s );
    }
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
