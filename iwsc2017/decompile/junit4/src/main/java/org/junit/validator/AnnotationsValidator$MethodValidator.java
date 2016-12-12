package org.junit.validator;
import org.junit.runners.model.Annotatable;
import java.util.List;
import org.junit.runners.model.TestClass;
import org.junit.runners.model.FrameworkMethod;
private static class MethodValidator extends AnnotatableValidator<FrameworkMethod> {
    Iterable<FrameworkMethod> getAnnotatablesForTestClass ( final TestClass testClass ) {
        return testClass.getAnnotatedMethods();
    }
    List<Exception> validateAnnotatable ( final AnnotationValidator validator, final FrameworkMethod method ) {
        return validator.validateAnnotatedMethod ( method );
    }
}
