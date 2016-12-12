package org.apache.catalina;
public final class LifecycleException extends Exception {
    private static final long serialVersionUID = 1L;
    public LifecycleException() {
    }
    public LifecycleException ( final String message ) {
        super ( message );
    }
    public LifecycleException ( final Throwable throwable ) {
        super ( throwable );
    }
    public LifecycleException ( final String message, final Throwable throwable ) {
        super ( message, throwable );
    }
}
