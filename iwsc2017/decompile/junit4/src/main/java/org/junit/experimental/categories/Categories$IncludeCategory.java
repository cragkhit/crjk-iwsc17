package org.junit.experimental.categories;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
@Retention ( RetentionPolicy.RUNTIME )
public @interface IncludeCategory {
Class<?>[] value() default {};
boolean matchAny() default true;
}
