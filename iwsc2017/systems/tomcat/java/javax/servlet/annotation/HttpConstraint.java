package javax.servlet.annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface HttpConstraint {
EmptyRoleSemantic value() default EmptyRoleSemantic.PERMIT;
TransportGuarantee transportGuarantee() default TransportGuarantee.NONE;
String[] rolesAllowed() default {};
}
