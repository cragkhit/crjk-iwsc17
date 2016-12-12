package org.apache.catalina.valves.rewrite;
import java.util.StringTokenizer;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import javax.servlet.ServletException;
import org.apache.catalina.Pipeline;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.RequestUtil;
import javax.servlet.http.Cookie;
import java.net.URLDecoder;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Iterator;
import org.apache.catalina.Lifecycle;
import java.util.ArrayList;
import java.io.StringReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream;
import java.io.File;
import org.apache.catalina.Host;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.LogFactory;
import java.util.Hashtable;
import java.util.Map;
import org.apache.catalina.util.URLEncoder;
import org.apache.catalina.valves.ValveBase;
public class RewriteValve extends ValveBase {
    static URLEncoder ENCODER;
    protected RewriteRule[] rules;
    protected ThreadLocal<Boolean> invoked;
    protected String resourcePath;
    protected boolean context;
    protected boolean enabled;
    protected Map<String, RewriteMap> maps;
    public RewriteValve() {
        super ( true );
        this.rules = null;
        this.invoked = new ThreadLocal<Boolean>();
        this.resourcePath = "rewrite.config";
        this.context = false;
        this.enabled = true;
        this.maps = new Hashtable<String, RewriteMap>();
    }
    public boolean getEnabled() {
        return this.enabled;
    }
    public void setEnabled ( final boolean enabled ) {
        this.enabled = enabled;
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        this.containerLog = LogFactory.getLog ( this.getContainer().getLogName() + ".rewrite" );
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        InputStream is = null;
        if ( this.getContainer() instanceof Context ) {
            this.context = true;
            is = ( ( Context ) this.getContainer() ).getServletContext().getResourceAsStream ( "/WEB-INF/" + this.resourcePath );
            if ( this.containerLog.isDebugEnabled() ) {
                if ( is == null ) {
                    this.containerLog.debug ( "No configuration resource found: /WEB-INF/" + this.resourcePath );
                } else {
                    this.containerLog.debug ( "Read configuration from: /WEB-INF/" + this.resourcePath );
                }
            }
        } else if ( this.getContainer() instanceof Host ) {
            final String resourceName = this.getHostConfigPath ( this.resourcePath );
            final File file = new File ( this.getConfigBase(), resourceName );
            try {
                if ( !file.exists() ) {
                    is = this.getClass().getClassLoader().getResourceAsStream ( resourceName );
                    if ( is != null && this.containerLog.isDebugEnabled() ) {
                        this.containerLog.debug ( "Read configuration from CL at " + resourceName );
                    }
                } else {
                    if ( this.containerLog.isDebugEnabled() ) {
                        this.containerLog.debug ( "Read configuration from " + file.getAbsolutePath() );
                    }
                    is = new FileInputStream ( file );
                }
                if ( is == null && this.containerLog.isDebugEnabled() ) {
                    this.containerLog.debug ( "No configuration resource found: " + resourceName + " in " + this.getConfigBase() + " or in the classloader" );
                }
            } catch ( Exception e ) {
                this.containerLog.error ( "Error opening configuration", e );
            }
        }
        if ( is == null ) {
            return;
        }
        try ( final InputStreamReader isr = new InputStreamReader ( is, StandardCharsets.UTF_8 );
                    final BufferedReader reader = new BufferedReader ( isr ) ) {
            this.parse ( reader );
        } catch ( IOException ioe ) {
            this.containerLog.error ( "Error closing configuration", ioe );
            try {
                is.close();
            } catch ( IOException e2 ) {
                this.containerLog.error ( "Error closing configuration", e2 );
            }
        } finally {
            try {
                is.close();
            } catch ( IOException e3 ) {
                this.containerLog.error ( "Error closing configuration", e3 );
            }
        }
    }
    public void setConfiguration ( final String configuration ) throws Exception {
        if ( this.containerLog == null ) {
            this.containerLog = LogFactory.getLog ( this.getContainer().getLogName() + ".rewrite" );
        }
        this.maps.clear();
        this.parse ( new BufferedReader ( new StringReader ( configuration ) ) );
    }
    public String getConfiguration() {
        final StringBuffer buffer = new StringBuffer();
        for ( int i = 0; i < this.rules.length; ++i ) {
            for ( int j = 0; j < this.rules[i].getConditions().length; ++j ) {
                buffer.append ( this.rules[i].getConditions() [j].toString() ).append ( "\r\n" );
            }
            buffer.append ( this.rules[i].toString() ).append ( "\r\n" ).append ( "\r\n" );
        }
        return buffer.toString();
    }
    protected void parse ( final BufferedReader reader ) throws LifecycleException {
        final ArrayList<RewriteRule> rules = new ArrayList<RewriteRule>();
        final ArrayList<RewriteCond> conditions = new ArrayList<RewriteCond>();
        Label_0016_Outer:
        while ( true ) {
            while ( true ) {
                try {
                    while ( true ) {
                        final String line = reader.readLine();
                        if ( line == null ) {
                            break;
                        }
                        final Object result = parse ( line );
                        if ( result instanceof RewriteRule ) {
                            final RewriteRule rule = ( RewriteRule ) result;
                            if ( this.containerLog.isDebugEnabled() ) {
                                this.containerLog.debug ( "Add rule with pattern " + rule.getPatternString() + " and substitution " + rule.getSubstitutionString() );
                            }
                            for ( int i = conditions.size() - 1; i > 0; --i ) {
                                if ( conditions.get ( i - 1 ).isOrnext() ) {
                                    conditions.get ( i ).setOrnext ( true );
                                }
                            }
                            for ( int i = 0; i < conditions.size(); ++i ) {
                                if ( this.containerLog.isDebugEnabled() ) {
                                    final RewriteCond cond = conditions.get ( i );
                                    this.containerLog.debug ( "Add condition " + cond.getCondPattern() + " test " + cond.getTestString() + " to rule with pattern " + rule.getPatternString() + " and substitution " + rule.getSubstitutionString() + ( cond.isOrnext() ? " [OR]" : "" ) + ( cond.isNocase() ? " [NC]" : "" ) );
                                }
                                rule.addCondition ( conditions.get ( i ) );
                            }
                            conditions.clear();
                            rules.add ( rule );
                        } else if ( result instanceof RewriteCond ) {
                            conditions.add ( ( RewriteCond ) result );
                        } else {
                            if ( ! ( result instanceof Object[] ) ) {
                                continue Label_0016_Outer;
                            }
                            final String mapName = ( String ) ( ( Object[] ) result ) [0];
                            final RewriteMap map = ( RewriteMap ) ( ( Object[] ) result ) [1];
                            this.maps.put ( mapName, map );
                            if ( ! ( map instanceof Lifecycle ) ) {
                                continue Label_0016_Outer;
                            }
                            ( ( Lifecycle ) map ).start();
                        }
                    }
                    break;
                } catch ( IOException e ) {
                    this.containerLog.error ( "Error reading configuration", e );
                    continue Label_0016_Outer;
                }
                continue;
            }
        }
        this.rules = rules.toArray ( new RewriteRule[0] );
        for ( int j = 0; j < this.rules.length; ++j ) {
            this.rules[j].parse ( this.maps );
        }
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        for ( final RewriteMap map : this.maps.values() ) {
            if ( map instanceof Lifecycle ) {
                ( ( Lifecycle ) map ).stop();
            }
        }
        this.maps.clear();
        this.rules = null;
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        if ( !this.getEnabled() || this.rules == null || this.rules.length == 0 ) {
            this.getNext().invoke ( request, response );
            return;
        }
        if ( Boolean.TRUE.equals ( this.invoked.get() ) ) {
            try {
                this.getNext().invoke ( request, response );
            } finally {
                this.invoked.set ( null );
            }
            return;
        }
        try {
            final Resolver resolver = new ResolverImpl ( request );
            this.invoked.set ( Boolean.TRUE );
            final String uriEncoding = request.getConnector().getURIEncoding();
            final String originalQueryStringEncoded = request.getQueryString();
            final MessageBytes urlMB = this.context ? request.getRequestPathMB() : request.getDecodedRequestURIMB();
            urlMB.toChars();
            CharSequence urlDecoded = urlMB.getCharChunk();
            CharSequence host = request.getServerName();
            boolean rewritten = false;
            boolean done = false;
            boolean qsa = false;
            for ( int i = 0; i < this.rules.length; ++i ) {
                final RewriteRule rule = this.rules[i];
                final CharSequence test = rule.isHost() ? host : urlDecoded;
                final CharSequence newtest = rule.evaluate ( test, resolver );
                if ( newtest != null && !test.equals ( newtest.toString() ) ) {
                    if ( this.containerLog.isDebugEnabled() ) {
                        this.containerLog.debug ( "Rewrote " + ( Object ) test + " as " + ( Object ) newtest + " with rule pattern " + rule.getPatternString() );
                    }
                    if ( rule.isHost() ) {
                        host = newtest;
                    } else {
                        urlDecoded = newtest;
                    }
                    rewritten = true;
                }
                if ( !qsa && newtest != null && rule.isQsappend() ) {
                    qsa = true;
                }
                if ( rule.isForbidden() && newtest != null ) {
                    response.sendError ( 403 );
                    done = true;
                    break;
                }
                if ( rule.isGone() && newtest != null ) {
                    response.sendError ( 410 );
                    done = true;
                    break;
                }
                if ( rule.isRedirect() && newtest != null ) {
                    String urlStringDecoded = urlDecoded.toString();
                    final int index = urlStringDecoded.indexOf ( "?" );
                    String rewrittenQueryStringDecoded;
                    if ( index == -1 ) {
                        rewrittenQueryStringDecoded = null;
                    } else {
                        rewrittenQueryStringDecoded = urlStringDecoded.substring ( index + 1 );
                        urlStringDecoded = urlStringDecoded.substring ( 0, index );
                    }
                    final StringBuffer urlStringEncoded = new StringBuffer ( RewriteValve.ENCODER.encode ( urlStringDecoded, uriEncoding ) );
                    if ( originalQueryStringEncoded != null && originalQueryStringEncoded.length() > 0 ) {
                        if ( rewrittenQueryStringDecoded == null ) {
                            urlStringEncoded.append ( '?' );
                            urlStringEncoded.append ( originalQueryStringEncoded );
                        } else if ( qsa ) {
                            urlStringEncoded.append ( '?' );
                            urlStringEncoded.append ( RewriteValve.ENCODER.encode ( rewrittenQueryStringDecoded, uriEncoding ) );
                            urlStringEncoded.append ( '&' );
                            urlStringEncoded.append ( originalQueryStringEncoded );
                        } else if ( index == urlStringEncoded.length() - 1 ) {
                            urlStringEncoded.deleteCharAt ( index );
                        } else {
                            urlStringEncoded.append ( '?' );
                            urlStringEncoded.append ( RewriteValve.ENCODER.encode ( rewrittenQueryStringDecoded, uriEncoding ) );
                        }
                    } else if ( rewrittenQueryStringDecoded != null ) {
                        urlStringEncoded.append ( '?' );
                        urlStringEncoded.append ( RewriteValve.ENCODER.encode ( rewrittenQueryStringDecoded, uriEncoding ) );
                    }
                    if ( this.context && urlStringEncoded.charAt ( 0 ) == '/' && !UriUtil.hasScheme ( urlStringEncoded ) ) {
                        urlStringEncoded.insert ( 0, request.getContext().getEncodedPath() );
                    }
                    if ( rule.isNoescape() ) {
                        response.sendRedirect ( URLDecoder.decode ( urlStringEncoded.toString(), uriEncoding ) );
                    } else {
                        response.sendRedirect ( urlStringEncoded.toString() );
                    }
                    response.setStatus ( rule.getRedirectCode() );
                    done = true;
                    break;
                }
                if ( rule.isCookie() && newtest != null ) {
                    final Cookie cookie = new Cookie ( rule.getCookieName(), rule.getCookieResult() );
                    cookie.setDomain ( rule.getCookieDomain() );
                    cookie.setMaxAge ( rule.getCookieLifetime() );
                    cookie.setPath ( rule.getCookiePath() );
                    cookie.setSecure ( rule.isCookieSecure() );
                    cookie.setHttpOnly ( rule.isCookieHttpOnly() );
                    response.addCookie ( cookie );
                }
                if ( rule.isEnv() && newtest != null ) {
                    for ( int j = 0; j < rule.getEnvSize(); ++j ) {
                        request.setAttribute ( rule.getEnvName ( j ), rule.getEnvResult ( j ) );
                    }
                }
                if ( rule.isType() && newtest != null ) {
                    request.setContentType ( rule.getTypeValue() );
                }
                if ( rule.isChain() && newtest == null ) {
                    for ( int j = i; j < this.rules.length; ++j ) {
                        if ( !this.rules[j].isChain() ) {
                            i = j;
                            break;
                        }
                    }
                } else {
                    if ( rule.isLast() && newtest != null ) {
                        break;
                    }
                    if ( rule.isNext() && newtest != null ) {
                        i = 0;
                    } else if ( newtest != null ) {
                        i += rule.getSkip();
                    }
                }
            }
            if ( rewritten ) {
                if ( !done ) {
                    String urlStringDecoded2 = urlDecoded.toString();
                    String queryStringDecoded = null;
                    final int queryIndex = urlStringDecoded2.indexOf ( 63 );
                    if ( queryIndex != -1 ) {
                        queryStringDecoded = urlStringDecoded2.substring ( queryIndex + 1 );
                        urlStringDecoded2 = urlStringDecoded2.substring ( 0, queryIndex );
                    }
                    String contextPath = null;
                    if ( this.context ) {
                        contextPath = request.getContextPath();
                    }
                    request.getCoyoteRequest().requestURI().setString ( null );
                    CharChunk chunk = request.getCoyoteRequest().requestURI().getCharChunk();
                    chunk.recycle();
                    if ( this.context ) {
                        chunk.append ( contextPath );
                    }
                    chunk.append ( RewriteValve.ENCODER.encode ( urlStringDecoded2, uriEncoding ) );
                    request.getCoyoteRequest().requestURI().toChars();
                    urlStringDecoded2 = RequestUtil.normalize ( urlStringDecoded2 );
                    request.getCoyoteRequest().decodedURI().setString ( null );
                    chunk = request.getCoyoteRequest().decodedURI().getCharChunk();
                    chunk.recycle();
                    if ( this.context ) {
                        chunk.append ( request.getServletContext().getContextPath() );
                    }
                    chunk.append ( urlStringDecoded2 );
                    request.getCoyoteRequest().decodedURI().toChars();
                    if ( queryStringDecoded != null ) {
                        request.getCoyoteRequest().queryString().setString ( null );
                        chunk = request.getCoyoteRequest().queryString().getCharChunk();
                        chunk.recycle();
                        chunk.append ( RewriteValve.ENCODER.encode ( queryStringDecoded, uriEncoding ) );
                        if ( qsa && originalQueryStringEncoded != null && originalQueryStringEncoded.length() > 0 ) {
                            chunk.append ( '&' );
                            chunk.append ( originalQueryStringEncoded );
                        }
                        if ( !chunk.isNull() ) {
                            request.getCoyoteRequest().queryString().toChars();
                        }
                    }
                    if ( !host.equals ( request.getServerName() ) ) {
                        request.getCoyoteRequest().serverName().setString ( null );
                        chunk = request.getCoyoteRequest().serverName().getCharChunk();
                        chunk.recycle();
                        chunk.append ( host.toString() );
                        request.getCoyoteRequest().serverName().toChars();
                    }
                    request.getMappingData().recycle();
                    try {
                        final Connector connector = request.getConnector();
                        if ( !connector.getProtocolHandler().getAdapter().prepare ( request.getCoyoteRequest(), response.getCoyoteResponse() ) ) {
                            return;
                        }
                        final Pipeline pipeline = connector.getService().getContainer().getPipeline();
                        request.setAsyncSupported ( pipeline.isAsyncSupported() );
                        pipeline.getFirst().invoke ( request, response );
                    } catch ( Exception ex ) {}
                }
            } else {
                this.getNext().invoke ( request, response );
            }
        } finally {
            this.invoked.set ( null );
        }
    }
    protected File getConfigBase() {
        final File configBase = new File ( System.getProperty ( "catalina.base" ), "conf" );
        if ( !configBase.exists() ) {
            return null;
        }
        return configBase;
    }
    protected String getHostConfigPath ( final String resourceName ) {
        final StringBuffer result = new StringBuffer();
        Container container = this.getContainer();
        Container host = null;
        Container engine = null;
        while ( container != null ) {
            if ( container instanceof Host ) {
                host = container;
            }
            if ( container instanceof Engine ) {
                engine = container;
            }
            container = container.getParent();
        }
        if ( engine != null ) {
            result.append ( engine.getName() ).append ( '/' );
        }
        if ( host != null ) {
            result.append ( host.getName() ).append ( '/' );
        }
        result.append ( resourceName );
        return result.toString();
    }
    public static Object parse ( final String line ) {
        final StringTokenizer tokenizer = new StringTokenizer ( line );
        if ( tokenizer.hasMoreTokens() ) {
            final String token = tokenizer.nextToken();
            if ( token.equals ( "RewriteCond" ) ) {
                final RewriteCond condition = new RewriteCond();
                if ( tokenizer.countTokens() < 2 ) {
                    throw new IllegalArgumentException ( "Invalid line: " + line );
                }
                condition.setTestString ( tokenizer.nextToken() );
                condition.setCondPattern ( tokenizer.nextToken() );
                if ( tokenizer.hasMoreTokens() ) {
                    String flags = tokenizer.nextToken();
                    if ( flags.startsWith ( "[" ) && flags.endsWith ( "]" ) ) {
                        flags = flags.substring ( 1, flags.length() - 1 );
                    }
                    final StringTokenizer flagsTokenizer = new StringTokenizer ( flags, "," );
                    while ( flagsTokenizer.hasMoreElements() ) {
                        parseCondFlag ( line, condition, flagsTokenizer.nextToken() );
                    }
                }
                return condition;
            } else if ( token.equals ( "RewriteRule" ) ) {
                final RewriteRule rule = new RewriteRule();
                if ( tokenizer.countTokens() < 2 ) {
                    throw new IllegalArgumentException ( "Invalid line: " + line );
                }
                rule.setPatternString ( tokenizer.nextToken() );
                rule.setSubstitutionString ( tokenizer.nextToken() );
                if ( tokenizer.hasMoreTokens() ) {
                    String flags = tokenizer.nextToken();
                    if ( flags.startsWith ( "[" ) && flags.endsWith ( "]" ) ) {
                        flags = flags.substring ( 1, flags.length() - 1 );
                    }
                    final StringTokenizer flagsTokenizer = new StringTokenizer ( flags, "," );
                    while ( flagsTokenizer.hasMoreElements() ) {
                        parseRuleFlag ( line, rule, flagsTokenizer.nextToken() );
                    }
                }
                return rule;
            } else if ( token.equals ( "RewriteMap" ) ) {
                if ( tokenizer.countTokens() < 2 ) {
                    throw new IllegalArgumentException ( "Invalid line: " + line );
                }
                final String name = tokenizer.nextToken();
                final String rewriteMapClassName = tokenizer.nextToken();
                RewriteMap map = null;
                try {
                    map = ( RewriteMap ) Class.forName ( rewriteMapClassName ).newInstance();
                } catch ( Exception e ) {
                    throw new IllegalArgumentException ( "Invalid map className: " + line );
                }
                if ( tokenizer.hasMoreTokens() ) {
                    map.setParameters ( tokenizer.nextToken() );
                }
                final Object[] result = { name, map };
                return result;
            } else if ( !token.startsWith ( "#" ) ) {
                throw new IllegalArgumentException ( "Invalid line: " + line );
            }
        }
        return null;
    }
    protected static void parseCondFlag ( final String line, final RewriteCond condition, final String flag ) {
        if ( flag.equals ( "NC" ) || flag.equals ( "nocase" ) ) {
            condition.setNocase ( true );
        } else {
            if ( !flag.equals ( "OR" ) && !flag.equals ( "ornext" ) ) {
                throw new IllegalArgumentException ( "Invalid flag in: " + line + " flags: " + flag );
            }
            condition.setOrnext ( true );
        }
    }
    protected static void parseRuleFlag ( final String line, final RewriteRule rule, String flag ) {
        if ( flag.equals ( "B" ) ) {
            rule.setEscapeBackReferences ( true );
        } else if ( flag.equals ( "chain" ) || flag.equals ( "C" ) ) {
            rule.setChain ( true );
        } else if ( flag.startsWith ( "cookie=" ) || flag.startsWith ( "CO=" ) ) {
            rule.setCookie ( true );
            if ( flag.startsWith ( "cookie" ) ) {
                flag = flag.substring ( "cookie=".length() );
            } else if ( flag.startsWith ( "CO=" ) ) {
                flag = flag.substring ( "CO=".length() );
            }
            final StringTokenizer tokenizer = new StringTokenizer ( flag, ":" );
            if ( tokenizer.countTokens() < 2 ) {
                throw new IllegalArgumentException ( "Invalid flag in: " + line );
            }
            rule.setCookieName ( tokenizer.nextToken() );
            rule.setCookieValue ( tokenizer.nextToken() );
            if ( tokenizer.hasMoreTokens() ) {
                rule.setCookieDomain ( tokenizer.nextToken() );
            }
            if ( tokenizer.hasMoreTokens() ) {
                try {
                    rule.setCookieLifetime ( Integer.parseInt ( tokenizer.nextToken() ) );
                } catch ( NumberFormatException e ) {
                    throw new IllegalArgumentException ( "Invalid flag in: " + line, e );
                }
            }
            if ( tokenizer.hasMoreTokens() ) {
                rule.setCookiePath ( tokenizer.nextToken() );
            }
            if ( tokenizer.hasMoreTokens() ) {
                rule.setCookieSecure ( Boolean.parseBoolean ( tokenizer.nextToken() ) );
            }
            if ( tokenizer.hasMoreTokens() ) {
                rule.setCookieHttpOnly ( Boolean.parseBoolean ( tokenizer.nextToken() ) );
            }
        } else if ( flag.startsWith ( "env=" ) || flag.startsWith ( "E=" ) ) {
            rule.setEnv ( true );
            if ( flag.startsWith ( "env=" ) ) {
                flag = flag.substring ( "env=".length() );
            } else if ( flag.startsWith ( "E=" ) ) {
                flag = flag.substring ( "E=".length() );
            }
            final int pos = flag.indexOf ( 58 );
            if ( pos == -1 || pos + 1 == flag.length() ) {
                throw new IllegalArgumentException ( "Invalid flag in: " + line );
            }
            rule.addEnvName ( flag.substring ( 0, pos ) );
            rule.addEnvValue ( flag.substring ( pos + 1 ) );
        } else if ( flag.startsWith ( "forbidden" ) || flag.startsWith ( "F" ) ) {
            rule.setForbidden ( true );
        } else if ( flag.startsWith ( "gone" ) || flag.startsWith ( "G" ) ) {
            rule.setGone ( true );
        } else if ( flag.startsWith ( "host" ) || flag.startsWith ( "H" ) ) {
            rule.setHost ( true );
        } else if ( flag.startsWith ( "last" ) || flag.startsWith ( "L" ) ) {
            rule.setLast ( true );
        } else if ( flag.startsWith ( "nocase" ) || flag.startsWith ( "NC" ) ) {
            rule.setNocase ( true );
        } else if ( flag.startsWith ( "noescape" ) || flag.startsWith ( "NE" ) ) {
            rule.setNoescape ( true );
        } else if ( flag.startsWith ( "next" ) || flag.startsWith ( "N" ) ) {
            rule.setNext ( true );
        } else if ( flag.startsWith ( "qsappend" ) || flag.startsWith ( "QSA" ) ) {
            rule.setQsappend ( true );
        } else if ( flag.startsWith ( "redirect" ) || flag.startsWith ( "R" ) ) {
            if ( flag.startsWith ( "redirect=" ) ) {
                flag = flag.substring ( "redirect=".length() );
                rule.setRedirect ( true );
                rule.setRedirectCode ( Integer.parseInt ( flag ) );
            } else if ( flag.startsWith ( "R=" ) ) {
                flag = flag.substring ( "R=".length() );
                rule.setRedirect ( true );
                rule.setRedirectCode ( Integer.parseInt ( flag ) );
            } else {
                rule.setRedirect ( true );
                rule.setRedirectCode ( 302 );
            }
        } else if ( flag.startsWith ( "skip" ) || flag.startsWith ( "S" ) ) {
            if ( flag.startsWith ( "skip=" ) ) {
                flag = flag.substring ( "skip=".length() );
            } else if ( flag.startsWith ( "S=" ) ) {
                flag = flag.substring ( "S=".length() );
            }
            rule.setSkip ( Integer.parseInt ( flag ) );
        } else {
            if ( !flag.startsWith ( "type" ) && !flag.startsWith ( "T" ) ) {
                throw new IllegalArgumentException ( "Invalid flag in: " + line + " flag: " + flag );
            }
            if ( flag.startsWith ( "type=" ) ) {
                flag = flag.substring ( "type=".length() );
            } else if ( flag.startsWith ( "T=" ) ) {
                flag = flag.substring ( "T=".length() );
            }
            rule.setType ( true );
            rule.setTypeValue ( flag );
        }
    }
    static {
        ( RewriteValve.ENCODER = new URLEncoder() ).addSafeCharacter ( '-' );
        RewriteValve.ENCODER.addSafeCharacter ( '.' );
        RewriteValve.ENCODER.addSafeCharacter ( '_' );
        RewriteValve.ENCODER.addSafeCharacter ( '~' );
        RewriteValve.ENCODER.addSafeCharacter ( '!' );
        RewriteValve.ENCODER.addSafeCharacter ( '$' );
        RewriteValve.ENCODER.addSafeCharacter ( '&' );
        RewriteValve.ENCODER.addSafeCharacter ( '\'' );
        RewriteValve.ENCODER.addSafeCharacter ( '(' );
        RewriteValve.ENCODER.addSafeCharacter ( ')' );
        RewriteValve.ENCODER.addSafeCharacter ( '*' );
        RewriteValve.ENCODER.addSafeCharacter ( '+' );
        RewriteValve.ENCODER.addSafeCharacter ( ',' );
        RewriteValve.ENCODER.addSafeCharacter ( ';' );
        RewriteValve.ENCODER.addSafeCharacter ( '=' );
        RewriteValve.ENCODER.addSafeCharacter ( ':' );
        RewriteValve.ENCODER.addSafeCharacter ( '@' );
        RewriteValve.ENCODER.addSafeCharacter ( '/' );
    }
}
