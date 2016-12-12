package org.junit;
import org.junit.runners.MethodSorters;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
@Retention ( RetentionPolicy.RUNTIME )
@Target ( { ElementType.TYPE } )
public @interface FixMethodOrder {
MethodSorters value() default MethodSorters.DEFAULT;
}
