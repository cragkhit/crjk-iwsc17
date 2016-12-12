package org.junit.runner;
public static class FilterNotCreatedException extends Exception {
    public FilterNotCreatedException ( final Exception exception ) {
        super ( exception.getMessage(), exception );
    }
}
