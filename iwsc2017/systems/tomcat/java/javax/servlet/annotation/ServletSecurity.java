package javax.servlet.annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Inherited
@Target ( ElementType.TYPE )
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface ServletSecurity {
    enum EmptyRoleSemantic {
        PERMIT,
        DENY
    }
    enum TransportGuarantee {
        NONE,
        CONFIDENTIAL
    }
HttpConstraint value() default @HttpConstraint;
HttpMethodConstraint[] httpMethodConstraints() default {};
}
