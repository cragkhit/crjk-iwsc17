package org.junit.internal.runners.rules;
import org.junit.ClassRule;
import java.util.List;
import java.lang.annotation.Annotation;
import org.junit.runners.model.FrameworkMember;
private static final class MemberMustBeNonStaticOrAlsoClassRule implements RuleValidator {
    public void validate ( final FrameworkMember<?> member, final Class<? extends Annotation> annotation, final List<Throwable> errors ) {
        final boolean isMethodRuleMember = RuleMemberValidator.access$1200 ( member );
        final boolean isClassRuleAnnotated = member.getAnnotation ( ClassRule.class ) != null;
        if ( member.isStatic() && ( isMethodRuleMember || !isClassRuleAnnotated ) ) {
            String message;
            if ( RuleMemberValidator.access$1200 ( member ) ) {
                message = "must not be static.";
            } else {
                message = "must not be static or it must be annotated with @ClassRule.";
            }
            errors.add ( new ValidationError ( member, annotation, message ) );
        }
    }
}
