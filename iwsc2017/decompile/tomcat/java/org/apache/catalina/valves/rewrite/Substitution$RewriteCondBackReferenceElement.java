package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
public class RewriteCondBackReferenceElement extends SubstitutionElement {
    public int n;
    @Override
    public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
        return cond.group ( this.n );
    }
}
