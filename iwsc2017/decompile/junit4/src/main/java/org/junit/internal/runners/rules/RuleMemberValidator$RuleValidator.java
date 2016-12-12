package org.junit.internal.runners.rules;
import java.util.List;
import java.lang.annotation.Annotation;
import org.junit.runners.model.FrameworkMember;
interface RuleValidator {
    void validate ( FrameworkMember<?> p0, Class<? extends Annotation> p1, List<Throwable> p2 );
}
