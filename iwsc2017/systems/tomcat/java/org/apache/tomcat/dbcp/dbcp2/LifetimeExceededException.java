package org.apache.tomcat.dbcp.dbcp2;
class LifetimeExceededException extends Exception {
    private static final long serialVersionUID = -3783783104516492659L;
    public LifetimeExceededException() {
        super();
    }
    public LifetimeExceededException ( final String message ) {
        super ( message );
    }
}
