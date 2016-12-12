package org.junit.internal.runners.rules;
import java.lang.annotation.Annotation;
import org.junit.runners.model.FrameworkMember;
class ValidationError extends Exception {
    private static final long serialVersionUID = 3176511008672645574L;
    public ValidationError ( final FrameworkMember<?> member, final Class<? extends Annotation> annotation, final String suffix ) {
        super ( String.format ( "The @%s '%s' %s", annotation.getSimpleName(), member.getName(), suffix ) );
    }
}
