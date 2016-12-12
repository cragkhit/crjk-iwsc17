package org.junit.runners.model;
import java.util.List;
public class InvalidTestClassError extends InitializationError {
    private static final long serialVersionUID = 1L;
    private final String message;
    public InvalidTestClassError ( Class<?> offendingTestClass, List<Throwable> validationErrors ) {
        super ( validationErrors );
        this.message = createMessage ( offendingTestClass, validationErrors );
    }
    private static String createMessage ( Class<?> testClass, List<Throwable> validationErrors ) {
        StringBuilder sb = new StringBuilder();
        sb.append ( String.format ( "Invalid test class '%s':", testClass.getName() ) );
        int i = 1;
        for ( Throwable error : validationErrors ) {
            sb.append ( "\n  " + ( i++ ) + ". " + error.getMessage() );
        }
        return sb.toString();
    }
    @Override
    public String getMessage() {
        return message;
    }
}
