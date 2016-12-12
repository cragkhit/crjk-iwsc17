package org.junit.runners.model;
import java.lang.annotation.Annotation;
public interface Annotatable {
    Annotation[] getAnnotations();
    <T extends Annotation> T getAnnotation ( Class<T> annotationType );
}
