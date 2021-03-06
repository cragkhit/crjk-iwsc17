package org.junit.runner;
import java.util.Comparator;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.requests.ClassRequest;
import org.junit.internal.requests.FilterRequest;
import org.junit.internal.requests.SortingRequest;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.model.InitializationError;
public abstract class Request {
    public static Request method ( Class<?> clazz, String methodName ) {
        Description method = Description.createTestDescription ( clazz, methodName );
        return Request.aClass ( clazz ).filterWith ( method );
    }
    public static Request aClass ( Class<?> clazz ) {
        return new ClassRequest ( clazz );
    }
    public static Request classWithoutSuiteMethod ( Class<?> clazz ) {
        return new ClassRequest ( clazz, false );
    }
    public static Request classes ( Computer computer, Class<?>... classes ) {
        try {
            AllDefaultPossibilitiesBuilder builder = new AllDefaultPossibilitiesBuilder();
            Runner suite = computer.getSuite ( builder, classes );
            return runner ( suite );
        } catch ( InitializationError e ) {
            return runner ( new ErrorReportingRunner ( e, classes ) );
        }
    }
    public static Request classes ( Class<?>... classes ) {
        return classes ( JUnitCore.defaultComputer(), classes );
    }
    public static Request errorReport ( Class<?> klass, Throwable cause ) {
        return runner ( new ErrorReportingRunner ( klass, cause ) );
    }
    public static Request runner ( final Runner runner ) {
        return new Request() {
            @Override
            public Runner getRunner() {
                return runner;
            }
        };
    }
    public abstract Runner getRunner();
    public Request filterWith ( Filter filter ) {
        return new FilterRequest ( this, filter );
    }
    public Request filterWith ( final Description desiredDescription ) {
        return filterWith ( Filter.matchMethodDescription ( desiredDescription ) );
    }
    public Request sortWith ( Comparator<Description> comparator ) {
        return new SortingRequest ( this, comparator );
    }
}
