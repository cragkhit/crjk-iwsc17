package javax.xml.ws;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
@Target ( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface WebServiceRef {
String name() default "";
Class type() default Object.class;
Class value() default Object.class;
String wsdlLocation() default "";
String mappedName() default "";
}
