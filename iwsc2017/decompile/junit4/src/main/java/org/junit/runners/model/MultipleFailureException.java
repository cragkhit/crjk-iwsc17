package org.junit.runners.model;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.internal.Throwables;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
public class MultipleFailureException extends Exception {
    private static final long serialVersionUID = 1L;
    private final List<Throwable> fErrors;
    public MultipleFailureException ( final List<Throwable> errors ) {
        if ( errors.isEmpty() ) {
            throw new IllegalArgumentException ( "List of Throwables must not be empty" );
        }
        this.fErrors = new ArrayList<Throwable> ( errors );
    }
    public List<Throwable> getFailures() {
        return Collections.unmodifiableList ( ( List<? extends Throwable> ) this.fErrors );
    }
    public String getMessage() {
        final StringBuilder sb = new StringBuilder ( String.format ( "There were %d errors:", this.fErrors.size() ) );
        for ( final Throwable e : this.fErrors ) {
            sb.append ( String.format ( "\n  %s(%s)", e.getClass().getName(), e.getMessage() ) );
        }
        return sb.toString();
    }
    public void printStackTrace() {
        for ( final Throwable e : this.fErrors ) {
            e.printStackTrace();
        }
    }
    public void printStackTrace ( final PrintStream s ) {
        for ( final Throwable e : this.fErrors ) {
            e.printStackTrace ( s );
        }
    }
    public void printStackTrace ( final PrintWriter s ) {
        for ( final Throwable e : this.fErrors ) {
            e.printStackTrace ( s );
        }
    }
    public static void assertEmpty ( final List<Throwable> errors ) throws Exception {
        if ( errors.isEmpty() ) {
            return;
        }
        if ( errors.size() == 1 ) {
            throw Throwables.rethrowAsException ( errors.get ( 0 ) );
        }
        throw new org.junit.internal.runners.model.MultipleFailureException ( errors );
    }
}
