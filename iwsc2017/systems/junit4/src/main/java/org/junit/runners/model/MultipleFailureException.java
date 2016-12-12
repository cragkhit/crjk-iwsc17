package org.junit.runners.model;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.internal.Throwables;
public class MultipleFailureException extends Exception {
    private static final long serialVersionUID = 1L;
    private final List<Throwable> fErrors;
    public MultipleFailureException ( List<Throwable> errors ) {
        if ( errors.isEmpty() ) {
            throw new IllegalArgumentException (
                "List of Throwables must not be empty" );
        }
        this.fErrors = new ArrayList<Throwable> ( errors );
    }
    public List<Throwable> getFailures() {
        return Collections.unmodifiableList ( fErrors );
    }
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder (
            String.format ( "There were %d errors:", fErrors.size() ) );
        for ( Throwable e : fErrors ) {
            sb.append ( String.format ( "\n  %s(%s)", e.getClass().getName(), e.getMessage() ) );
        }
        return sb.toString();
    }
    @Override
    public void printStackTrace() {
        for ( Throwable e : fErrors ) {
            e.printStackTrace();
        }
    }
    @Override
    public void printStackTrace ( PrintStream s ) {
        for ( Throwable e : fErrors ) {
            e.printStackTrace ( s );
        }
    }
    @Override
    public void printStackTrace ( PrintWriter s ) {
        for ( Throwable e : fErrors ) {
            e.printStackTrace ( s );
        }
    }
    @SuppressWarnings ( "deprecation" )
    public static void assertEmpty ( List<Throwable> errors ) throws Exception {
        if ( errors.isEmpty() ) {
            return;
        }
        if ( errors.size() == 1 ) {
            throw Throwables.rethrowAsException ( errors.get ( 0 ) );
        }
        throw new org.junit.internal.runners.model.MultipleFailureException ( errors );
    }
}
