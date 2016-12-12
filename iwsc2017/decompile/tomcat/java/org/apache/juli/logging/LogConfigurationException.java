package org.apache.juli.logging;
public class LogConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public LogConfigurationException() {
    }
    public LogConfigurationException ( final String message ) {
        super ( message );
    }
    public LogConfigurationException ( final Throwable cause ) {
        super ( cause );
    }
    public LogConfigurationException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
}
