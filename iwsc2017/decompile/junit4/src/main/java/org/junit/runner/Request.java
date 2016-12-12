package org.junit.runner;
import org.junit.internal.requests.SortingRequest;
import java.util.Comparator;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.model.InitializationError;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runners.model.RunnerBuilder;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.requests.ClassRequest;
public abstract class Request {
    public static Request method ( final Class<?> clazz, final String methodName ) {
        final Description method = Description.createTestDescription ( clazz, methodName );
        return aClass ( clazz ).filterWith ( method );
    }
    public static Request aClass ( final Class<?> clazz ) {
        return new ClassRequest ( clazz );
    }
    public static Request classWithoutSuiteMethod ( final Class<?> clazz ) {
        return new ClassRequest ( clazz, false );
    }
    public static Request classes ( final Computer computer, final Class<?>... classes ) {
        try {
            final AllDefaultPossibilitiesBuilder builder = new AllDefaultPossibilitiesBuilder();
            final Runner suite = computer.getSuite ( builder, classes );
            return runner ( suite );
        } catch ( InitializationError e ) {
            return runner ( new ErrorReportingRunner ( e, classes ) );
        }
    }
    public static Request classes ( final Class<?>... classes ) {
        return classes ( JUnitCore.defaultComputer(), classes );
    }
    public static Request errorReport ( final Class<?> klass, final Throwable cause ) {
        return runner ( new ErrorReportingRunner ( klass, cause ) );
    }
    public static Request runner ( final Runner runner ) {
        return new Request() {
            public Runner getRunner() {
                return runner;
            }
        };
    }
    public abstract Runner getRunner();
    public Request filterWith ( final Filter filter ) {
        return new FilterRequest ( this, filter );
    }
    public Request filterWith ( final Description desiredDescription ) {
        return this.filterWith ( Filter.matchMethodDescription ( desiredDescription ) );
    }
    public Request sortWith ( final Comparator<Description> comparator ) {
        return new SortingRequest ( this, comparator );
    }
}
