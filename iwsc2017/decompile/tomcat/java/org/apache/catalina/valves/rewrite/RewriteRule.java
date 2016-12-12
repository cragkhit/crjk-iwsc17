package org.apache.catalina.valves.rewrite;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Pattern;
public class RewriteRule {
    protected RewriteCond[] conditions;
    protected ThreadLocal<Pattern> pattern;
    protected Substitution substitution;
    protected String patternString;
    protected String substitutionString;
    private boolean escapeBackReferences;
    protected boolean chain;
    protected boolean cookie;
    protected String cookieName;
    protected String cookieValue;
    protected String cookieDomain;
    protected int cookieLifetime;
    protected String cookiePath;
    protected boolean cookieSecure;
    protected boolean cookieHttpOnly;
    protected Substitution cookieSubstitution;
    protected ThreadLocal<String> cookieResult;
    protected boolean env;
    protected ArrayList<String> envName;
    protected ArrayList<String> envValue;
    protected ArrayList<Substitution> envSubstitution;
    protected ArrayList<ThreadLocal<String>> envResult;
    protected boolean forbidden;
    protected boolean gone;
    protected boolean host;
    protected boolean last;
    protected boolean next;
    protected boolean nocase;
    protected boolean noescape;
    protected boolean nosubreq;
    protected boolean qsappend;
    protected boolean redirect;
    protected int redirectCode;
    protected int skip;
    protected boolean type;
    protected String typeValue;
    public RewriteRule() {
        this.conditions = new RewriteCond[0];
        this.pattern = new ThreadLocal<Pattern>();
        this.substitution = null;
        this.patternString = null;
        this.substitutionString = null;
        this.escapeBackReferences = false;
        this.chain = false;
        this.cookie = false;
        this.cookieName = null;
        this.cookieValue = null;
        this.cookieDomain = null;
        this.cookieLifetime = -1;
        this.cookiePath = null;
        this.cookieSecure = false;
        this.cookieHttpOnly = false;
        this.cookieSubstitution = null;
        this.cookieResult = new ThreadLocal<String>();
        this.env = false;
        this.envName = new ArrayList<String>();
        this.envValue = new ArrayList<String>();
        this.envSubstitution = new ArrayList<Substitution>();
        this.envResult = new ArrayList<ThreadLocal<String>>();
        this.forbidden = false;
        this.gone = false;
        this.host = false;
        this.last = false;
        this.next = false;
        this.nocase = false;
        this.noescape = false;
        this.nosubreq = false;
        this.qsappend = false;
        this.redirect = false;
        this.redirectCode = 0;
        this.skip = 0;
        this.type = false;
        this.typeValue = null;
    }
    public void parse ( final Map<String, RewriteMap> maps ) {
        if ( !"-".equals ( this.substitutionString ) ) {
            ( this.substitution = new Substitution() ).setSub ( this.substitutionString );
            this.substitution.parse ( maps );
            this.substitution.setEscapeBackReferences ( this.isEscapeBackReferences() );
        }
        int flags = 0;
        if ( this.isNocase() ) {
            flags |= 0x2;
        }
        Pattern.compile ( this.patternString, flags );
        for ( int i = 0; i < this.conditions.length; ++i ) {
            this.conditions[i].parse ( maps );
        }
        if ( this.isEnv() ) {
            for ( int i = 0; i < this.envValue.size(); ++i ) {
                final Substitution newEnvSubstitution = new Substitution();
                newEnvSubstitution.setSub ( this.envValue.get ( i ) );
                newEnvSubstitution.parse ( maps );
                this.envSubstitution.add ( newEnvSubstitution );
                this.envResult.add ( new ThreadLocal<String>() );
            }
        }
        if ( this.isCookie() ) {
            ( this.cookieSubstitution = new Substitution() ).setSub ( this.cookieValue );
            this.cookieSubstitution.parse ( maps );
        }
    }
    public void addCondition ( final RewriteCond condition ) {
        final RewriteCond[] conditions = new RewriteCond[this.conditions.length + 1];
        for ( int i = 0; i < this.conditions.length; ++i ) {
            conditions[i] = this.conditions[i];
        }
        conditions[this.conditions.length] = condition;
        this.conditions = conditions;
    }
    public CharSequence evaluate ( final CharSequence url, final Resolver resolver ) {
        Pattern pattern = this.pattern.get();
        if ( pattern == null ) {
            int flags = 0;
            if ( this.isNocase() ) {
                flags |= 0x2;
            }
            pattern = Pattern.compile ( this.patternString, flags );
            this.pattern.set ( pattern );
        }
        final Matcher matcher = pattern.matcher ( url );
        if ( !matcher.matches() ) {
            return null;
        }
        boolean done = false;
        boolean rewrite = true;
        Matcher lastMatcher = null;
        int pos = 0;
        while ( !done ) {
            if ( pos < this.conditions.length ) {
                rewrite = this.conditions[pos].evaluate ( matcher, lastMatcher, resolver );
                if ( rewrite ) {
                    final Matcher lastMatcher2 = this.conditions[pos].getMatcher();
                    if ( lastMatcher2 != null ) {
                        lastMatcher = lastMatcher2;
                    }
                    while ( pos < this.conditions.length && this.conditions[pos].isOrnext() ) {
                        ++pos;
                    }
                } else if ( !this.conditions[pos].isOrnext() ) {
                    done = true;
                }
                ++pos;
            } else {
                done = true;
            }
        }
        if ( !rewrite ) {
            return null;
        }
        if ( this.isEnv() ) {
            for ( int i = 0; i < this.envSubstitution.size(); ++i ) {
                this.envResult.get ( i ).set ( this.envSubstitution.get ( i ).evaluate ( matcher, lastMatcher, resolver ) );
            }
        }
        if ( this.isCookie() ) {
            this.cookieResult.set ( this.cookieSubstitution.evaluate ( matcher, lastMatcher, resolver ) );
        }
        if ( this.substitution != null ) {
            return this.substitution.evaluate ( matcher, lastMatcher, resolver );
        }
        return url;
    }
    @Override
    public String toString() {
        return "RewriteRule " + this.patternString + " " + this.substitutionString;
    }
    public boolean isEscapeBackReferences() {
        return this.escapeBackReferences;
    }
    public void setEscapeBackReferences ( final boolean escapeBackReferences ) {
        this.escapeBackReferences = escapeBackReferences;
    }
    public boolean isChain() {
        return this.chain;
    }
    public void setChain ( final boolean chain ) {
        this.chain = chain;
    }
    public RewriteCond[] getConditions() {
        return this.conditions;
    }
    public void setConditions ( final RewriteCond[] conditions ) {
        this.conditions = conditions;
    }
    public boolean isCookie() {
        return this.cookie;
    }
    public void setCookie ( final boolean cookie ) {
        this.cookie = cookie;
    }
    public String getCookieName() {
        return this.cookieName;
    }
    public void setCookieName ( final String cookieName ) {
        this.cookieName = cookieName;
    }
    public String getCookieValue() {
        return this.cookieValue;
    }
    public void setCookieValue ( final String cookieValue ) {
        this.cookieValue = cookieValue;
    }
    public String getCookieResult() {
        return this.cookieResult.get();
    }
    public boolean isEnv() {
        return this.env;
    }
    public int getEnvSize() {
        return this.envName.size();
    }
    public void setEnv ( final boolean env ) {
        this.env = env;
    }
    public String getEnvName ( final int i ) {
        return this.envName.get ( i );
    }
    public void addEnvName ( final String envName ) {
        this.envName.add ( envName );
    }
    public String getEnvValue ( final int i ) {
        return this.envValue.get ( i );
    }
    public void addEnvValue ( final String envValue ) {
        this.envValue.add ( envValue );
    }
    public String getEnvResult ( final int i ) {
        return this.envResult.get ( i ).get();
    }
    public boolean isForbidden() {
        return this.forbidden;
    }
    public void setForbidden ( final boolean forbidden ) {
        this.forbidden = forbidden;
    }
    public boolean isGone() {
        return this.gone;
    }
    public void setGone ( final boolean gone ) {
        this.gone = gone;
    }
    public boolean isLast() {
        return this.last;
    }
    public void setLast ( final boolean last ) {
        this.last = last;
    }
    public boolean isNext() {
        return this.next;
    }
    public void setNext ( final boolean next ) {
        this.next = next;
    }
    public boolean isNocase() {
        return this.nocase;
    }
    public void setNocase ( final boolean nocase ) {
        this.nocase = nocase;
    }
    public boolean isNoescape() {
        return this.noescape;
    }
    public void setNoescape ( final boolean noescape ) {
        this.noescape = noescape;
    }
    public boolean isNosubreq() {
        return this.nosubreq;
    }
    public void setNosubreq ( final boolean nosubreq ) {
        this.nosubreq = nosubreq;
    }
    public boolean isQsappend() {
        return this.qsappend;
    }
    public void setQsappend ( final boolean qsappend ) {
        this.qsappend = qsappend;
    }
    public boolean isRedirect() {
        return this.redirect;
    }
    public void setRedirect ( final boolean redirect ) {
        this.redirect = redirect;
    }
    public int getRedirectCode() {
        return this.redirectCode;
    }
    public void setRedirectCode ( final int redirectCode ) {
        this.redirectCode = redirectCode;
    }
    public int getSkip() {
        return this.skip;
    }
    public void setSkip ( final int skip ) {
        this.skip = skip;
    }
    public Substitution getSubstitution() {
        return this.substitution;
    }
    public void setSubstitution ( final Substitution substitution ) {
        this.substitution = substitution;
    }
    public boolean isType() {
        return this.type;
    }
    public void setType ( final boolean type ) {
        this.type = type;
    }
    public String getTypeValue() {
        return this.typeValue;
    }
    public void setTypeValue ( final String typeValue ) {
        this.typeValue = typeValue;
    }
    public String getPatternString() {
        return this.patternString;
    }
    public void setPatternString ( final String patternString ) {
        this.patternString = patternString;
    }
    public String getSubstitutionString() {
        return this.substitutionString;
    }
    public void setSubstitutionString ( final String substitutionString ) {
        this.substitutionString = substitutionString;
    }
    public boolean isHost() {
        return this.host;
    }
    public void setHost ( final boolean host ) {
        this.host = host;
    }
    public String getCookieDomain() {
        return this.cookieDomain;
    }
    public void setCookieDomain ( final String cookieDomain ) {
        this.cookieDomain = cookieDomain;
    }
    public int getCookieLifetime() {
        return this.cookieLifetime;
    }
    public void setCookieLifetime ( final int cookieLifetime ) {
        this.cookieLifetime = cookieLifetime;
    }
    public String getCookiePath() {
        return this.cookiePath;
    }
    public void setCookiePath ( final String cookiePath ) {
        this.cookiePath = cookiePath;
    }
    public boolean isCookieSecure() {
        return this.cookieSecure;
    }
    public void setCookieSecure ( final boolean cookieSecure ) {
        this.cookieSecure = cookieSecure;
    }
    public boolean isCookieHttpOnly() {
        return this.cookieHttpOnly;
    }
    public void setCookieHttpOnly ( final boolean cookieHttpOnly ) {
        this.cookieHttpOnly = cookieHttpOnly;
    }
}
