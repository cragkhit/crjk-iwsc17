package org.junit.validator;
import org.junit.runners.model.Annotatable;
import java.util.List;
import java.util.Collections;
import org.junit.runners.model.TestClass;
private static class ClassValidator extends AnnotatableValidator<TestClass> {
    Iterable<TestClass> getAnnotatablesForTestClass ( final TestClass testClass ) {
        return Collections.singletonList ( testClass );
    }
    List<Exception> validateAnnotatable ( final AnnotationValidator validator, final TestClass testClass ) {
        return validator.validateAnnotatedClass ( testClass );
    }
}
