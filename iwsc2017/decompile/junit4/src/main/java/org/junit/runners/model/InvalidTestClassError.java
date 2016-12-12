package org.junit.runners.model;
import java.util.Iterator;
import java.util.List;
public class InvalidTestClassError extends InitializationError {
    private static final long serialVersionUID = 1L;
    private final String message;
    public InvalidTestClassError ( final Class<?> offendingTestClass, final List<Throwable> validationErrors ) {
        super ( validationErrors );
        this.message = createMessage ( offendingTestClass, validationErrors );
    }
    private static String createMessage ( final Class<?> testClass, final List<Throwable> validationErrors ) {
        final StringBuilder sb = new StringBuilder();
        sb.append ( String.format ( "Invalid test class '%s':", testClass.getName() ) );
        int i = 1;
        for ( final Throwable error : validationErrors ) {
            sb.append ( "\n  " + i++ + ". " + error.getMessage() );
        }
        return sb.toString();
    }
    public String getMessage() {
        return this.message;
    }
}
