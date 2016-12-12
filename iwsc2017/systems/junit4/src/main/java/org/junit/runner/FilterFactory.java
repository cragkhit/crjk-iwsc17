package org.junit.runner;

import org.junit.runner.manipulation.Filter;


public interface FilterFactory {

    Filter createFilter ( FilterFactoryParams params ) throws FilterNotCreatedException;


    @SuppressWarnings ( "serial" )
    class FilterNotCreatedException extends Exception {
        public FilterNotCreatedException ( Exception exception ) {
            super ( exception.getMessage(), exception );
        }
    }
}
