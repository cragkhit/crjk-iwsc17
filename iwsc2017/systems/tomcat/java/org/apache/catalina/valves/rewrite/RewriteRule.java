package org.apache.catalina.valves.rewrite;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RewriteRule {
    protected RewriteCond[] conditions = new RewriteCond[0];
    protected ThreadLocal<Pattern> pattern = new ThreadLocal<>();
    protected Substitution substitution = null;
    protected String patternString = null;
    protected String substitutionString = null;
    public void parse ( Map<String, RewriteMap> maps ) {
        if ( !"-".equals ( substitutionString ) ) {
            substitution = new Substitution();
            substitution.setSub ( substitutionString );
            substitution.parse ( maps );
            substitution.setEscapeBackReferences ( isEscapeBackReferences() );
        }
        int flags = 0;
        if ( isNocase() ) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        Pattern.compile ( patternString, flags );
        for ( int i = 0; i < conditions.length; i++ ) {
            conditions[i].parse ( maps );
        }
        if ( isEnv() ) {
            for ( int i = 0; i < envValue.size(); i++ ) {
                Substitution newEnvSubstitution = new Substitution();
                newEnvSubstitution.setSub ( envValue.get ( i ) );
                newEnvSubstitution.parse ( maps );
                envSubstitution.add ( newEnvSubstitution );
                envResult.add ( new ThreadLocal<String>() );
            }
        }
        if ( isCookie() ) {
            cookieSubstitution = new Substitution();
            cookieSubstitution.setSub ( cookieValue );
            cookieSubstitution.parse ( maps );
        }
    }
    public void addCondition ( RewriteCond condition ) {
        RewriteCond[] conditions = new RewriteCond[this.conditions.length + 1];
        for ( int i = 0; i < this.conditions.length; i++ ) {
            conditions[i] = this.conditions[i];
        }
        conditions[this.conditions.length] = condition;
        this.conditions = conditions;
    }
    public CharSequence evaluate ( CharSequence url, Resolver resolver ) {
        Pattern pattern = this.pattern.get();
        if ( pattern == null ) {
            int flags = 0;
            if ( isNocase() ) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            pattern = Pattern.compile ( patternString, flags );
            this.pattern.set ( pattern );
        }
        Matcher matcher = pattern.matcher ( url );
        if ( !matcher.matches() ) {
            return null;
        }
        boolean done = false;
        boolean rewrite = true;
        Matcher lastMatcher = null;
        int pos = 0;
        while ( !done ) {
            if ( pos < conditions.length ) {
                rewrite = conditions[pos].evaluate ( matcher, lastMatcher, resolver );
                if ( rewrite ) {
                    Matcher lastMatcher2 = conditions[pos].getMatcher();
                    if ( lastMatcher2 != null ) {
                        lastMatcher = lastMatcher2;
                    }
                    while ( pos < conditions.length && conditions[pos].isOrnext() ) {
                        pos++;
                    }
                } else if ( !conditions[pos].isOrnext() ) {
                    done = true;
                }
                pos++;
            } else {
                done = true;
            }
        }
        if ( rewrite ) {
            if ( isEnv() ) {
                for ( int i = 0; i < envSubstitution.size(); i++ ) {
                    envResult.get ( i ).set ( envSubstitution.get ( i ).evaluate ( matcher, lastMatcher, resolver ) );
                }
            }
            if ( isCookie() ) {
                cookieResult.set ( cookieSubstitution.evaluate ( matcher, lastMatcher, resolver ) );
            }
            if ( substitution != null ) {
                return substitution.evaluate ( matcher, lastMatcher, resolver );
            } else {
                return url;
            }
        } else {
            return null;
        }
    }
    @Override
    public String toString() {
        return "RewriteRule " + patternString + " " + substitutionString;
    }
    private boolean escapeBackReferences = false;
    protected boolean chain = false;
    protected boolean cookie = false;
    protected String cookieName = null;
    protected String cookieValue = null;
    protected String cookieDomain = null;
    protected int cookieLifetime = -1;
    protected String cookiePath = null;
    protected boolean cookieSecure = false;
    protected boolean cookieHttpOnly = false;
    protected Substitution cookieSubstitution = null;
    protected ThreadLocal<String> cookieResult = new ThreadLocal<>();
    protected boolean env = false;
    protected ArrayList<String> envName = new ArrayList<>();
    protected ArrayList<String> envValue = new ArrayList<>();
    protected ArrayList<Substitution> envSubstitution = new ArrayList<>();
    protected ArrayList<ThreadLocal<String>> envResult = new ArrayList<>();
    protected boolean forbidden = false;
    protected boolean gone = false;
    protected boolean host = false;
    protected boolean last = false;
    protected boolean next = false;
    protected boolean nocase = false;
    protected boolean noescape = false;
    protected boolean nosubreq = false;
    protected boolean qsappend = false;
    protected boolean redirect = false;
    protected int redirectCode = 0;
    protected int skip = 0;
    protected boolean type = false;
    protected String typeValue = null;
    public boolean isEscapeBackReferences() {
        return escapeBackReferences;
    }
    public void setEscapeBackReferences ( boolean escapeBackReferences ) {
        this.escapeBackReferences = escapeBackReferences;
    }
    public boolean isChain() {
        return chain;
    }
    public void setChain ( boolean chain ) {
        this.chain = chain;
    }
    public RewriteCond[] getConditions() {
        return conditions;
    }
    public void setConditions ( RewriteCond[] conditions ) {
        this.conditions = conditions;
    }
    public boolean isCookie() {
        return cookie;
    }
    public void setCookie ( boolean cookie ) {
        this.cookie = cookie;
    }
    public String getCookieName() {
        return cookieName;
    }
    public void setCookieName ( String cookieName ) {
        this.cookieName = cookieName;
    }
    public String getCookieValue() {
        return cookieValue;
    }
    public void setCookieValue ( String cookieValue ) {
        this.cookieValue = cookieValue;
    }
    public String getCookieResult() {
        return cookieResult.get();
    }
    public boolean isEnv() {
        return env;
    }
    public int getEnvSize() {
        return envName.size();
    }
    public void setEnv ( boolean env ) {
        this.env = env;
    }
    public String getEnvName ( int i ) {
        return envName.get ( i );
    }
    public void addEnvName ( String envName ) {
        this.envName.add ( envName );
    }
    public String getEnvValue ( int i ) {
        return envValue.get ( i );
    }
    public void addEnvValue ( String envValue ) {
        this.envValue.add ( envValue );
    }
    public String getEnvResult ( int i ) {
        return envResult.get ( i ).get();
    }
    public boolean isForbidden() {
        return forbidden;
    }
    public void setForbidden ( boolean forbidden ) {
        this.forbidden = forbidden;
    }
    public boolean isGone() {
        return gone;
    }
    public void setGone ( boolean gone ) {
        this.gone = gone;
    }
    public boolean isLast() {
        return last;
    }
    public void setLast ( boolean last ) {
        this.last = last;
    }
    public boolean isNext() {
        return next;
    }
    public void setNext ( boolean next ) {
        this.next = next;
    }
    public boolean isNocase() {
        return nocase;
    }
    public void setNocase ( boolean nocase ) {
        this.nocase = nocase;
    }
    public boolean isNoescape() {
        return noescape;
    }
    public void setNoescape ( boolean noescape ) {
        this.noescape = noescape;
    }
    public boolean isNosubreq() {
        return nosubreq;
    }
    public void setNosubreq ( boolean nosubreq ) {
        this.nosubreq = nosubreq;
    }
    public boolean isQsappend() {
        return qsappend;
    }
    public void setQsappend ( boolean qsappend ) {
        this.qsappend = qsappend;
    }
    public boolean isRedirect() {
        return redirect;
    }
    public void setRedirect ( boolean redirect ) {
        this.redirect = redirect;
    }
    public int getRedirectCode() {
        return redirectCode;
    }
    public void setRedirectCode ( int redirectCode ) {
        this.redirectCode = redirectCode;
    }
    public int getSkip() {
        return skip;
    }
    public void setSkip ( int skip ) {
        this.skip = skip;
    }
    public Substitution getSubstitution() {
        return substitution;
    }
    public void setSubstitution ( Substitution substitution ) {
        this.substitution = substitution;
    }
    public boolean isType() {
        return type;
    }
    public void setType ( boolean type ) {
        this.type = type;
    }
    public String getTypeValue() {
        return typeValue;
    }
    public void setTypeValue ( String typeValue ) {
        this.typeValue = typeValue;
    }
    public String getPatternString() {
        return patternString;
    }
    public void setPatternString ( String patternString ) {
        this.patternString = patternString;
    }
    public String getSubstitutionString() {
        return substitutionString;
    }
    public void setSubstitutionString ( String substitutionString ) {
        this.substitutionString = substitutionString;
    }
    public boolean isHost() {
        return host;
    }
    public void setHost ( boolean host ) {
        this.host = host;
    }
    public String getCookieDomain() {
        return cookieDomain;
    }
    public void setCookieDomain ( String cookieDomain ) {
        this.cookieDomain = cookieDomain;
    }
    public int getCookieLifetime() {
        return cookieLifetime;
    }
    public void setCookieLifetime ( int cookieLifetime ) {
        this.cookieLifetime = cookieLifetime;
    }
    public String getCookiePath() {
        return cookiePath;
    }
    public void setCookiePath ( String cookiePath ) {
        this.cookiePath = cookiePath;
    }
    public boolean isCookieSecure() {
        return cookieSecure;
    }
    public void setCookieSecure ( boolean cookieSecure ) {
        this.cookieSecure = cookieSecure;
    }
    public boolean isCookieHttpOnly() {
        return cookieHttpOnly;
    }
    public void setCookieHttpOnly ( boolean cookieHttpOnly ) {
        this.cookieHttpOnly = cookieHttpOnly;
    }
}
