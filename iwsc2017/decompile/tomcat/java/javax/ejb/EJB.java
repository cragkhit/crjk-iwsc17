package javax.ejb;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
@Target ( { ElementType.METHOD, ElementType.TYPE, ElementType.FIELD } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface EJB {
String name() default "";
String description() default "";
Class beanInterface() default Object.class;
String beanName() default "";
String mappedName() default "";
}
