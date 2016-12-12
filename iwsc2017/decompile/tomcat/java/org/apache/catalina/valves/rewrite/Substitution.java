package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Map;
public class Substitution {
    protected SubstitutionElement[] elements;
    protected String sub;
    private boolean escapeBackReferences;
    public Substitution() {
        this.elements = null;
        this.sub = null;
    }
    public String getSub() {
        return this.sub;
    }
    public void setSub ( final String sub ) {
        this.sub = sub;
    }
    void setEscapeBackReferences ( final boolean escapeBackReferences ) {
        this.escapeBackReferences = escapeBackReferences;
    }
    public void parse ( final Map<String, RewriteMap> maps ) {
        final ArrayList<SubstitutionElement> elements = new ArrayList<SubstitutionElement>();
        int pos = 0;
        int percentPos = 0;
        int dollarPos = 0;
        int backslashPos = 0;
        while ( pos < this.sub.length() ) {
            percentPos = this.sub.indexOf ( 37, pos );
            dollarPos = this.sub.indexOf ( 36, pos );
            backslashPos = this.sub.indexOf ( 92, pos );
            if ( percentPos == -1 && dollarPos == -1 && backslashPos == -1 ) {
                final StaticElement newElement = new StaticElement();
                newElement.value = this.sub.substring ( pos, this.sub.length() );
                pos = this.sub.length();
                elements.add ( newElement );
            } else if ( this.isFirstPos ( backslashPos, dollarPos, percentPos ) ) {
                if ( backslashPos + 1 == this.sub.length() ) {
                    throw new IllegalArgumentException ( this.sub );
                }
                final StaticElement newElement = new StaticElement();
                newElement.value = this.sub.substring ( pos, backslashPos ) + this.sub.substring ( backslashPos + 1, backslashPos + 2 );
                pos = backslashPos + 2;
                elements.add ( newElement );
            } else if ( this.isFirstPos ( dollarPos, percentPos ) ) {
                if ( dollarPos + 1 == this.sub.length() ) {
                    throw new IllegalArgumentException ( this.sub );
                }
                if ( pos < dollarPos ) {
                    final StaticElement newElement = new StaticElement();
                    newElement.value = this.sub.substring ( pos, dollarPos );
                    pos = dollarPos;
                    elements.add ( newElement );
                }
                if ( Character.isDigit ( this.sub.charAt ( dollarPos + 1 ) ) ) {
                    final RewriteRuleBackReferenceElement newElement2 = new RewriteRuleBackReferenceElement();
                    newElement2.n = Character.digit ( this.sub.charAt ( dollarPos + 1 ), 10 );
                    pos = dollarPos + 2;
                    elements.add ( newElement2 );
                } else {
                    if ( this.sub.charAt ( dollarPos + 1 ) != '{' ) {
                        throw new IllegalArgumentException ( this.sub + ": missing digit or curly brace." );
                    }
                    final MapElement newElement3 = new MapElement();
                    final int open = this.sub.indexOf ( 123, dollarPos );
                    final int colon = this.sub.indexOf ( 58, dollarPos );
                    final int def = this.sub.indexOf ( 124, dollarPos );
                    final int close = this.sub.indexOf ( 125, dollarPos );
                    if ( -1 >= open || open >= colon || colon >= close ) {
                        throw new IllegalArgumentException ( this.sub );
                    }
                    newElement3.map = maps.get ( this.sub.substring ( open + 1, colon ) );
                    if ( newElement3.map == null ) {
                        throw new IllegalArgumentException ( this.sub + ": No map: " + this.sub.substring ( open + 1, colon ) );
                    }
                    if ( def > -1 ) {
                        if ( colon >= def || def >= close ) {
                            throw new IllegalArgumentException ( this.sub );
                        }
                        newElement3.key = this.sub.substring ( colon + 1, def );
                        newElement3.defaultValue = this.sub.substring ( def + 1, close );
                    } else {
                        newElement3.key = this.sub.substring ( colon + 1, close );
                    }
                    if ( newElement3.key.startsWith ( "$" ) ) {
                        newElement3.n = Integer.parseInt ( newElement3.key.substring ( 1 ) );
                    }
                    pos = close + 1;
                    elements.add ( newElement3 );
                }
            } else {
                if ( percentPos + 1 == this.sub.length() ) {
                    throw new IllegalArgumentException ( this.sub );
                }
                if ( pos < percentPos ) {
                    final StaticElement newElement = new StaticElement();
                    newElement.value = this.sub.substring ( pos, percentPos );
                    pos = percentPos;
                    elements.add ( newElement );
                }
                if ( Character.isDigit ( this.sub.charAt ( percentPos + 1 ) ) ) {
                    final RewriteCondBackReferenceElement newElement4 = new RewriteCondBackReferenceElement();
                    newElement4.n = Character.digit ( this.sub.charAt ( percentPos + 1 ), 10 );
                    pos = percentPos + 2;
                    elements.add ( newElement4 );
                } else {
                    if ( this.sub.charAt ( percentPos + 1 ) != '{' ) {
                        throw new IllegalArgumentException ( this.sub + ": missing digit or curly brace." );
                    }
                    SubstitutionElement newElement5 = null;
                    final int open = this.sub.indexOf ( 123, percentPos );
                    final int colon = this.sub.indexOf ( 58, percentPos );
                    final int close2 = this.sub.indexOf ( 125, percentPos );
                    if ( -1 >= open || open >= close2 ) {
                        throw new IllegalArgumentException ( this.sub );
                    }
                    if ( colon > -1 && open < colon && colon < close2 ) {
                        final String type = this.sub.substring ( open + 1, colon );
                        if ( type.equals ( "ENV" ) ) {
                            newElement5 = new ServerVariableEnvElement();
                            ( ( ServerVariableEnvElement ) newElement5 ).key = this.sub.substring ( colon + 1, close2 );
                        } else if ( type.equals ( "SSL" ) ) {
                            newElement5 = new ServerVariableSslElement();
                            ( ( ServerVariableSslElement ) newElement5 ).key = this.sub.substring ( colon + 1, close2 );
                        } else {
                            if ( !type.equals ( "HTTP" ) ) {
                                throw new IllegalArgumentException ( this.sub + ": Bad type: " + type );
                            }
                            newElement5 = new ServerVariableHttpElement();
                            ( ( ServerVariableHttpElement ) newElement5 ).key = this.sub.substring ( colon + 1, close2 );
                        }
                    } else {
                        newElement5 = new ServerVariableElement();
                        ( ( ServerVariableElement ) newElement5 ).key = this.sub.substring ( open + 1, close2 );
                    }
                    pos = close2 + 1;
                    elements.add ( newElement5 );
                }
            }
        }
        this.elements = elements.toArray ( new SubstitutionElement[0] );
    }
    public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
        final StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < this.elements.length; ++i ) {
            buf.append ( this.elements[i].evaluate ( rule, cond, resolver ) );
        }
        return buf.toString();
    }
    private boolean isFirstPos ( final int testPos, final int... others ) {
        if ( testPos < 0 ) {
            return false;
        }
        for ( final int other : others ) {
            if ( other >= 0 && other < testPos ) {
                return false;
            }
        }
        return true;
    }
    public abstract class SubstitutionElement {
        public abstract String evaluate ( final Matcher p0, final Matcher p1, final Resolver p2 );
    }
    public class StaticElement extends SubstitutionElement {
        public String value;
        @Override
        public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
            return this.value;
        }
    }
    public class RewriteRuleBackReferenceElement extends SubstitutionElement {
        public int n;
        @Override
        public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
            if ( Substitution.this.escapeBackReferences ) {
                return RewriteValve.ENCODER.encode ( rule.group ( this.n ), resolver.getUriEncoding() );
            }
            return rule.group ( this.n );
        }
    }
    public class RewriteCondBackReferenceElement extends SubstitutionElement {
        public int n;
        @Override
        public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
            return cond.group ( this.n );
        }
    }
    public class ServerVariableElement extends SubstitutionElement {
        public String key;
        @Override
        public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
            return resolver.resolve ( this.key );
        }
    }
    public class ServerVariableEnvElement extends SubstitutionElement {
        public String key;
        @Override
        public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
            return resolver.resolveEnv ( this.key );
        }
    }
    public class ServerVariableSslElement extends SubstitutionElement {
        public String key;
        @Override
        public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
            return resolver.resolveSsl ( this.key );
        }
    }
    public class ServerVariableHttpElement extends SubstitutionElement {
        public String key;
        @Override
        public String evaluate ( final Matcher rule, final Matcher cond, final Resolver resolver ) {
            return resolver.resolveHttp ( this.key );
        }
    }
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
}
