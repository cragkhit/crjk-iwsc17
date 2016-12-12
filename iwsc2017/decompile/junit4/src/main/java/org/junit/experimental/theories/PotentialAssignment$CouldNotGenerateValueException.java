package org.junit.experimental.theories;
public static class CouldNotGenerateValueException extends Exception {
    private static final long serialVersionUID = 1L;
    public CouldNotGenerateValueException() {
    }
    public CouldNotGenerateValueException ( final Throwable e ) {
        super ( e );
    }
}
