package org.junit.validator;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import org.junit.runners.model.TestClass;
import org.junit.runners.model.Annotatable;
private abstract static class AnnotatableValidator<T extends Annotatable> {
    private static final AnnotationValidatorFactory ANNOTATION_VALIDATOR_FACTORY;
    abstract Iterable<T> getAnnotatablesForTestClass ( final TestClass p0 );
    abstract List<Exception> validateAnnotatable ( final AnnotationValidator p0, final T p1 );
    public List<Exception> validateTestClass ( final TestClass testClass ) {
        final List<Exception> validationErrors = new ArrayList<Exception>();
        for ( final T annotatable : this.getAnnotatablesForTestClass ( testClass ) ) {
            final List<Exception> additionalErrors = this.validateAnnotatable ( annotatable );
            validationErrors.addAll ( additionalErrors );
        }
        return validationErrors;
    }
    private List<Exception> validateAnnotatable ( final T annotatable ) {
        final List<Exception> validationErrors = new ArrayList<Exception>();
        for ( final Annotation annotation : annotatable.getAnnotations() ) {
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            final ValidateWith validateWith = annotationType.getAnnotation ( ValidateWith.class );
            if ( validateWith != null ) {
                final AnnotationValidator annotationValidator = AnnotatableValidator.ANNOTATION_VALIDATOR_FACTORY.createAnnotationValidator ( validateWith );
                final List<Exception> errors = this.validateAnnotatable ( annotationValidator, annotatable );
                validationErrors.addAll ( errors );
            }
        }
        return validationErrors;
    }
    static {
        ANNOTATION_VALIDATOR_FACTORY = new AnnotationValidatorFactory();
    }
}
