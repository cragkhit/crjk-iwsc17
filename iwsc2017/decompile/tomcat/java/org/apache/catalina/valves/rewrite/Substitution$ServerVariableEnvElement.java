package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
public class ServerVariableEnvElement extends SubstitutionElement {
    public String key;
    @Override
    public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
        return resolver.resolveEnv ( this.key );
    }
}
