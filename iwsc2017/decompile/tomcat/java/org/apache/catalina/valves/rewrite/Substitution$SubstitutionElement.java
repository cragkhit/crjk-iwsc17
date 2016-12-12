package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
public abstract class SubstitutionElement {
    public abstract String evaluate ( final Matcher p0, final Matcher p1, final Resolver p2 );
}
