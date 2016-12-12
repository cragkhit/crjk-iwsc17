package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
public class StaticElement extends SubstitutionElement {
    public String value;
    @Override
    public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
        return this.value;
    }
}
