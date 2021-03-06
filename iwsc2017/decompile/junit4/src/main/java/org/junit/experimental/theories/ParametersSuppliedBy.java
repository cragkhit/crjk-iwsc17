package org.junit.experimental.theories;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
@Retention ( RetentionPolicy.RUNTIME )
@Target ( { ElementType.ANNOTATION_TYPE, ElementType.PARAMETER } )
public @interface ParametersSuppliedBy {
    Class<? extends ParameterSupplier> value();
}
