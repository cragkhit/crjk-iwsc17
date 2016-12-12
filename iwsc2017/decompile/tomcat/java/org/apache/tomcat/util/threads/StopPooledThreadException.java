package org.apache.tomcat.util.threads;
public class StopPooledThreadException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public StopPooledThreadException ( final String msg ) {
        super ( msg );
    }
}
