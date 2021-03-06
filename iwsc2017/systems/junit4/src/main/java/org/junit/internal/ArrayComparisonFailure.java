package org.junit.internal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
public class ArrayComparisonFailure extends AssertionError {
    private static final long serialVersionUID = 1L;
    private final List<Integer> fIndices = new ArrayList<Integer>();
    private final String fMessage;
    private final AssertionError fCause;
    public ArrayComparisonFailure ( String message, AssertionError cause, int index ) {
        this.fMessage = message;
        this.fCause = cause;
        initCause ( fCause );
        addDimension ( index );
    }
    public void addDimension ( int index ) {
        fIndices.add ( 0, index );
    }
    @Override
    public synchronized Throwable getCause() {
        return super.getCause() == null ? fCause : super.getCause();
    }
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if ( fMessage != null ) {
            sb.append ( fMessage );
        }
        sb.append ( "arrays first differed at element " );
        for ( int each : fIndices ) {
            sb.append ( "[" );
            sb.append ( each );
            sb.append ( "]" );
        }
        sb.append ( "; " );
        sb.append ( getCause().getMessage() );
        return sb.toString();
    }
    @Override
    public String toString() {
        return getMessage();
    }
}
