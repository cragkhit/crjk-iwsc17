package org.junit.validator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import java.util.List;
import org.junit.runners.model.TestClass;
public class PublicClassValidator implements TestClassValidator {
    private static final List<Exception> NO_VALIDATION_ERRORS = emptyList();
    public List<Exception> validateTestClass ( TestClass testClass ) {
        if ( testClass.isPublic() ) {
            return NO_VALIDATION_ERRORS;
        } else {
            return singletonList ( new Exception ( "The class "
                                                   + testClass.getName() + " is not public." ) );
        }
    }
}
