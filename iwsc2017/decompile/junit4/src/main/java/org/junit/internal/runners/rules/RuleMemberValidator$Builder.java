package org.junit.internal.runners.rules;
import java.util.ArrayList;
import java.util.List;
import java.lang.annotation.Annotation;
private static class Builder {
    private final Class<? extends Annotation> annotation;
    private boolean methods;
    private final List<RuleValidator> validators;
    private Builder ( final Class<? extends Annotation> annotation ) {
        this.annotation = annotation;
        this.methods = false;
        this.validators = new ArrayList<RuleValidator>();
    }
    Builder forMethods() {
        this.methods = true;
        return this;
    }
    Builder withValidator ( final RuleValidator validator ) {
        this.validators.add ( validator );
        return this;
    }
    RuleMemberValidator build() {
        return new RuleMemberValidator ( this );
    }
}
