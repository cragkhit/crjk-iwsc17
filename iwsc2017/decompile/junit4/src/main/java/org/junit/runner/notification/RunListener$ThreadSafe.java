package org.junit.runner.notification;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.Annotation;
@Documented
@Target ( { ElementType.TYPE } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface ThreadSafe {
}
