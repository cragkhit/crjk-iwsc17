package org.junit.internal.runners.rules;
import java.lang.reflect.Modifier;
import java.util.List;
import java.lang.annotation.Annotation;
import org.junit.runners.model.FrameworkMember;
private static final class DeclaringClassMustBePublic implements RuleValidator {
    public void validate ( final FrameworkMember<?> member, final Class<? extends Annotation> annotation, final List<Throwable> errors ) {
        if ( !this.isDeclaringClassPublic ( member ) ) {
            errors.add ( new ValidationError ( member, annotation, "must be declared in a public class." ) );
        }
    }
    private boolean isDeclaringClassPublic ( final FrameworkMember<?> member ) {
        return Modifier.isPublic ( member.getDeclaringClass().getModifiers() );
    }
}
