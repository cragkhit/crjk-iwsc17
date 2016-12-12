package org.apache.tomcat.websocket.server;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class WsFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    private WsServerContainer sc;
    @Override
    public void init() throws ServletException {
        sc = ( WsServerContainer ) getServletContext().getAttribute (
                 Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE );
    }
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response,
                           FilterChain chain ) throws IOException, ServletException {
        if ( !sc.areEndpointsRegistered() ||
                !UpgradeUtil.isWebSocketUpgradeRequest ( request, response ) ) {
            chain.doFilter ( request, response );
            return;
        }
        HttpServletRequest req = ( HttpServletRequest ) request;
        HttpServletResponse resp = ( HttpServletResponse ) response;
        String path;
        String pathInfo = req.getPathInfo();
        if ( pathInfo == null ) {
            path = req.getServletPath();
        } else {
            path = req.getServletPath() + pathInfo;
        }
        WsMappingResult mappingResult = sc.findMapping ( path );
        if ( mappingResult == null ) {
            chain.doFilter ( request, response );
            return;
        }
        UpgradeUtil.doUpgrade ( sc, req, resp, mappingResult.getConfig(),
                                mappingResult.getPathParams() );
    }
}
