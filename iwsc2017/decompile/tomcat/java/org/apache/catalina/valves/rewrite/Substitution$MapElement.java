package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
public class MapElement extends SubstitutionElement {
    public RewriteMap map;
    public String key;
    public String defaultValue;
    public int n;
    public MapElement() {
        this.map = null;
        this.defaultValue = null;
    }
    @Override
    public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
        String result = this.map.lookup ( rule.group ( this.n ) );
        if ( result == null ) {
            result = this.defaultValue;
        }
        return result;
    }
}
