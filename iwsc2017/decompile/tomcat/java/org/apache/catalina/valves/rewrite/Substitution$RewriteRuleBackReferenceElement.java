package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
public class RewriteRuleBackReferenceElement extends SubstitutionElement {
    public int n;
    @Override
    public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
        if ( Substitution.access$000 ( Substitution.this ) ) {
            return RewriteValve.ENCODER.encode ( rule.group ( this.n ), resolver.getUriEncoding() );
        }
        return rule.group ( this.n );
    }
}
