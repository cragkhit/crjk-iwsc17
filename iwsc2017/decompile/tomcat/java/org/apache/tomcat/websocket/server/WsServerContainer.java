package org.apache.tomcat.websocket.server;
import javax.websocket.Endpoint;
import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import javax.websocket.Encoder;
import javax.websocket.server.ServerEndpoint;
import java.util.Comparator;
import java.util.TreeSet;
import javax.websocket.Decoder;
import java.util.List;
import org.apache.tomcat.websocket.pojo.PojoMethodMapping;
import javax.websocket.DeploymentException;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.websocket.Constants;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.tomcat.websocket.WsSession;
import java.util.Set;
import java.util.SortedSet;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.websocket.CloseReason;
import org.apache.tomcat.util.res.StringManager;
import javax.websocket.server.ServerContainer;
import org.apache.tomcat.websocket.WsWebSocketContainer;
public class WsServerContainer extends WsWebSocketContainer implements ServerContainer {
    private static final StringManager sm;
    private static final CloseReason AUTHENTICATED_HTTP_SESSION_CLOSED;
    private final WsWriteTimeout wsWriteTimeout;
    private final ServletContext servletContext;
    private final Map<String, ServerEndpointConfig> configExactMatchMap;
    private final Map<Integer, SortedSet<TemplatePathMatch>> configTemplateMatchMap;
    private volatile boolean enforceNoAddAfterHandshake;
    private volatile boolean addAllowed;
    private final Map<String, Set<WsSession>> authenticatedSessions;
    private volatile boolean endpointsRegistered;
    WsServerContainer ( final ServletContext servletContext ) {
        this.wsWriteTimeout = new WsWriteTimeout();
        this.configExactMatchMap = new ConcurrentHashMap<String, ServerEndpointConfig>();
        this.configTemplateMatchMap = new ConcurrentHashMap<Integer, SortedSet<TemplatePathMatch>>();
        this.enforceNoAddAfterHandshake = Constants.STRICT_SPEC_COMPLIANCE;
        this.addAllowed = true;
        this.authenticatedSessions = new ConcurrentHashMap<String, Set<WsSession>>();
        this.endpointsRegistered = false;
        this.servletContext = servletContext;
        this.setInstanceManager ( ( InstanceManager ) servletContext.getAttribute ( InstanceManager.class.getName() ) );
        String value = servletContext.getInitParameter ( "org.apache.tomcat.websocket.binaryBufferSize" );
        if ( value != null ) {
            this.setDefaultMaxBinaryMessageBufferSize ( Integer.parseInt ( value ) );
        }
        value = servletContext.getInitParameter ( "org.apache.tomcat.websocket.textBufferSize" );
        if ( value != null ) {
            this.setDefaultMaxTextMessageBufferSize ( Integer.parseInt ( value ) );
        }
        value = servletContext.getInitParameter ( "org.apache.tomcat.websocket.noAddAfterHandshake" );
        if ( value != null ) {
            this.setEnforceNoAddAfterHandshake ( Boolean.parseBoolean ( value ) );
        }
        final FilterRegistration.Dynamic fr = servletContext.addFilter ( "Tomcat WebSocket (JSR356) Filter", ( Filter ) new WsFilter() );
        fr.setAsyncSupported ( true );
        final EnumSet<DispatcherType> types = EnumSet.of ( DispatcherType.REQUEST, DispatcherType.FORWARD );
        fr.addMappingForUrlPatterns ( ( EnumSet ) types, true, new String[] { "/*" } );
    }
    public void addEndpoint ( final ServerEndpointConfig sec ) throws DeploymentException {
        if ( this.enforceNoAddAfterHandshake && !this.addAllowed ) {
            throw new DeploymentException ( WsServerContainer.sm.getString ( "serverContainer.addNotAllowed" ) );
        }
        if ( this.servletContext == null ) {
            throw new DeploymentException ( WsServerContainer.sm.getString ( "serverContainer.servletContextMissing" ) );
        }
        final String path = sec.getPath();
        final PojoMethodMapping methodMapping = new PojoMethodMapping ( sec.getEndpointClass(), sec.getDecoders(), path );
        if ( methodMapping.getOnClose() != null || methodMapping.getOnOpen() != null || methodMapping.getOnError() != null || methodMapping.hasMessageHandlers() ) {
            sec.getUserProperties().put ( "org.apache.tomcat.websocket.pojo.PojoEndpoint.methodMapping", methodMapping );
        }
        final UriTemplate uriTemplate = new UriTemplate ( path );
        if ( uriTemplate.hasParameters() ) {
            final Integer key = uriTemplate.getSegmentCount();
            SortedSet<TemplatePathMatch> templateMatches = this.configTemplateMatchMap.get ( key );
            if ( templateMatches == null ) {
                templateMatches = new TreeSet<TemplatePathMatch> ( TemplatePathMatchComparator.getInstance() );
                this.configTemplateMatchMap.putIfAbsent ( key, templateMatches );
                templateMatches = this.configTemplateMatchMap.get ( key );
            }
            if ( !templateMatches.add ( new TemplatePathMatch ( sec, uriTemplate ) ) ) {
                throw new DeploymentException ( WsServerContainer.sm.getString ( "serverContainer.duplicatePaths", path, sec.getEndpointClass(), sec.getEndpointClass() ) );
            }
        } else {
            final ServerEndpointConfig old = this.configExactMatchMap.put ( path, sec );
            if ( old != null ) {
                throw new DeploymentException ( WsServerContainer.sm.getString ( "serverContainer.duplicatePaths", path, old.getEndpointClass(), sec.getEndpointClass() ) );
            }
        }
        this.endpointsRegistered = true;
    }
    public void addEndpoint ( final Class<?> pojo ) throws DeploymentException {
        final ServerEndpoint annotation = pojo.getAnnotation ( ServerEndpoint.class );
        if ( annotation == null ) {
            throw new DeploymentException ( WsServerContainer.sm.getString ( "serverContainer.missingAnnotation", pojo.getName() ) );
        }
        final String path = annotation.value();
        validateEncoders ( annotation.encoders() );
        final Class<? extends ServerEndpointConfig.Configurator> configuratorClazz = ( Class<? extends ServerEndpointConfig.Configurator> ) annotation.configurator();
        ServerEndpointConfig.Configurator configurator = null;
        if ( !configuratorClazz.equals ( ServerEndpointConfig.Configurator.class ) ) {
            try {
                configurator = annotation.configurator().newInstance();
            } catch ( InstantiationException | IllegalAccessException e ) {
                throw new DeploymentException ( WsServerContainer.sm.getString ( "serverContainer.configuratorFail", annotation.configurator().getName(), pojo.getClass().getName() ), ( Throwable ) e );
            }
        }
        final ServerEndpointConfig sec = ServerEndpointConfig.Builder.create ( ( Class ) pojo, path ).decoders ( ( List ) Arrays.asList ( ( Class[] ) annotation.decoders() ) ).encoders ( ( List ) Arrays.asList ( ( Class[] ) annotation.encoders() ) ).subprotocols ( ( List ) Arrays.asList ( annotation.subprotocols() ) ).configurator ( configurator ).build();
        this.addEndpoint ( sec );
    }
    boolean areEndpointsRegistered() {
        return this.endpointsRegistered;
    }
    public void doUpgrade ( final HttpServletRequest request, final HttpServletResponse response, final ServerEndpointConfig sec, final Map<String, String> pathParams ) throws ServletException, IOException {
        UpgradeUtil.doUpgrade ( this, request, response, sec, pathParams );
    }
    public WsMappingResult findMapping ( final String path ) {
        if ( this.addAllowed ) {
            this.addAllowed = false;
        }
        ServerEndpointConfig sec = this.configExactMatchMap.get ( path );
        if ( sec != null ) {
            return new WsMappingResult ( sec, Collections.emptyMap() );
        }
        UriTemplate pathUriTemplate = null;
        try {
            pathUriTemplate = new UriTemplate ( path );
        } catch ( DeploymentException e ) {
            return null;
        }
        final Integer key = pathUriTemplate.getSegmentCount();
        final SortedSet<TemplatePathMatch> templateMatches = this.configTemplateMatchMap.get ( key );
        if ( templateMatches == null ) {
            return null;
        }
        Map<String, String> pathParams = null;
        for ( final TemplatePathMatch templateMatch : templateMatches ) {
            pathParams = templateMatch.getUriTemplate().match ( pathUriTemplate );
            if ( pathParams != null ) {
                sec = templateMatch.getConfig();
                break;
            }
        }
        if ( sec == null ) {
            return null;
        }
        return new WsMappingResult ( sec, pathParams );
    }
    public boolean isEnforceNoAddAfterHandshake() {
        return this.enforceNoAddAfterHandshake;
    }
    public void setEnforceNoAddAfterHandshake ( final boolean enforceNoAddAfterHandshake ) {
        this.enforceNoAddAfterHandshake = enforceNoAddAfterHandshake;
    }
    protected WsWriteTimeout getTimeout() {
        return this.wsWriteTimeout;
    }
    @Override
    protected void registerSession ( final Endpoint endpoint, final WsSession wsSession ) {
        super.registerSession ( endpoint, wsSession );
        if ( wsSession.isOpen() && wsSession.getUserPrincipal() != null && wsSession.getHttpSessionId() != null ) {
            this.registerAuthenticatedSession ( wsSession, wsSession.getHttpSessionId() );
        }
    }
    @Override
    protected void unregisterSession ( final Endpoint endpoint, final WsSession wsSession ) {
        if ( wsSession.getUserPrincipal() != null && wsSession.getHttpSessionId() != null ) {
            this.unregisterAuthenticatedSession ( wsSession, wsSession.getHttpSessionId() );
        }
        super.unregisterSession ( endpoint, wsSession );
    }
    private void registerAuthenticatedSession ( final WsSession wsSession, final String httpSessionId ) {
        Set<WsSession> wsSessions = this.authenticatedSessions.get ( httpSessionId );
        if ( wsSessions == null ) {
            wsSessions = Collections.newSetFromMap ( new ConcurrentHashMap<WsSession, Boolean>() );
            this.authenticatedSessions.putIfAbsent ( httpSessionId, wsSessions );
            wsSessions = this.authenticatedSessions.get ( httpSessionId );
        }
        wsSessions.add ( wsSession );
    }
    private void unregisterAuthenticatedSession ( final WsSession wsSession, final String httpSessionId ) {
        final Set<WsSession> wsSessions = this.authenticatedSessions.get ( httpSessionId );
        if ( wsSessions != null ) {
            wsSessions.remove ( wsSession );
        }
    }
    public void closeAuthenticatedSession ( final String httpSessionId ) {
        final Set<WsSession> wsSessions = this.authenticatedSessions.remove ( httpSessionId );
        if ( wsSessions != null && !wsSessions.isEmpty() ) {
            for ( final WsSession wsSession : wsSessions ) {
                try {
                    wsSession.close ( WsServerContainer.AUTHENTICATED_HTTP_SESSION_CLOSED );
                } catch ( IOException ex ) {}
            }
        }
    }
    private static void validateEncoders ( final Class<? extends Encoder>[] encoders ) throws DeploymentException {
        for ( final Class<? extends Encoder> encoder : encoders ) {
            try {
                encoder.newInstance();
            } catch ( InstantiationException | IllegalAccessException e ) {
                throw new DeploymentException ( WsServerContainer.sm.getString ( "serverContainer.encoderFail", encoder.getName() ), ( Throwable ) e );
            }
        }
    }
    static {
        sm = StringManager.getManager ( WsServerContainer.class );
        AUTHENTICATED_HTTP_SESSION_CLOSED = new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.VIOLATED_POLICY, "This connection was established under an authenticated HTTP session that has ended." );
    }
    private static class TemplatePathMatch {
        private final ServerEndpointConfig config;
        private final UriTemplate uriTemplate;
        public TemplatePathMatch ( final ServerEndpointConfig config, final UriTemplate uriTemplate ) {
            this.config = config;
            this.uriTemplate = uriTemplate;
        }
        public ServerEndpointConfig getConfig() {
            return this.config;
        }
        public UriTemplate getUriTemplate() {
            return this.uriTemplate;
        }
    }
    private static class TemplatePathMatchComparator implements Comparator<TemplatePathMatch> {
        private static final TemplatePathMatchComparator INSTANCE;
        public static TemplatePathMatchComparator getInstance() {
            return TemplatePathMatchComparator.INSTANCE;
        }
        @Override
        public int compare ( final TemplatePathMatch tpm1, final TemplatePathMatch tpm2 ) {
            return tpm1.getUriTemplate().getNormalizedPath().compareTo ( tpm2.getUriTemplate().getNormalizedPath() );
        }
        static {
            INSTANCE = new TemplatePathMatchComparator();
        }
    }
}
