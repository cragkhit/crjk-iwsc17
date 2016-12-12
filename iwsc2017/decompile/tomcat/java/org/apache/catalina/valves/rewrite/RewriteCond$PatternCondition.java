package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public static class PatternCondition extends Condition {
    public Pattern pattern;
    public Matcher matcher;
    public PatternCondition() {
        this.matcher = null;
    }
    @Override
    public boolean evaluate ( final String value, final Resolver resolver ) {
        final Matcher m = this.pattern.matcher ( value );
        if ( m.matches() ) {
            this.matcher = m;
            return true;
        }
        return false;
    }
}
