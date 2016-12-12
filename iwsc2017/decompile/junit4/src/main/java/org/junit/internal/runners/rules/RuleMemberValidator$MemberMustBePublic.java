package org.junit.internal.runners.rules;
import java.util.List;
import java.lang.annotation.Annotation;
import org.junit.runners.model.FrameworkMember;
private static final class MemberMustBePublic implements RuleValidator {
    public void validate ( final FrameworkMember<?> member, final Class<? extends Annotation> annotation, final List<Throwable> errors ) {
        if ( !member.isPublic() ) {
            errors.add ( new ValidationError ( member, annotation, "must be public." ) );
        }
    }
}
