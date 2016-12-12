package org.junit.runner;
public static class CommandLineParserError extends Exception {
    private static final long serialVersionUID = 1L;
    public CommandLineParserError ( final String message ) {
        super ( message );
    }
}
