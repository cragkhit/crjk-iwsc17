package org.junit.validator;
import org.junit.runners.model.Annotatable;
import java.util.List;
import org.junit.runners.model.TestClass;
import org.junit.runners.model.FrameworkField;
private static class FieldValidator extends AnnotatableValidator<FrameworkField> {
    Iterable<FrameworkField> getAnnotatablesForTestClass ( final TestClass testClass ) {
        return testClass.getAnnotatedFields();
    }
    List<Exception> validateAnnotatable ( final AnnotationValidator validator, final FrameworkField field ) {
        return validator.validateAnnotatedField ( field );
    }
}
