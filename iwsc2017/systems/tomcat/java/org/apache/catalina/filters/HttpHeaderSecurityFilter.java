package org.apache.catalina.filters;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class HttpHeaderSecurityFilter extends FilterBase {
    private static final Log log = LogFactory.getLog ( HttpHeaderSecurityFilter.class );
    private static final String HSTS_HEADER_NAME = "Strict-Transport-Security";
    private boolean hstsEnabled = true;
    private int hstsMaxAgeSeconds = 0;
    private boolean hstsIncludeSubDomains = false;
    private String hstsHeaderValue;
    private static final String ANTI_CLICK_JACKING_HEADER_NAME = "X-Frame-Options";
    private boolean antiClickJackingEnabled = true;
    private XFrameOption antiClickJackingOption = XFrameOption.DENY;
    private URI antiClickJackingUri;
    private String antiClickJackingHeaderValue;
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME = "X-Content-Type-Options";
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE = "nosniff";
    private boolean blockContentTypeSniffingEnabled = true;
    private static final String XSS_PROTECTION_HEADER_NAME = "X-XSS-Protection";
    private static final String XSS_PROTECTION_HEADER_VALUE = "1; mode=block";
    private boolean xssProtectionEnabled = true;
    @Override
    public void init ( FilterConfig filterConfig ) throws ServletException {
        super.init ( filterConfig );
        StringBuilder hstsValue = new StringBuilder ( "max-age=" );
        hstsValue.append ( hstsMaxAgeSeconds );
        if ( hstsIncludeSubDomains ) {
            hstsValue.append ( ";includeSubDomains" );
        }
        hstsHeaderValue = hstsValue.toString();
        StringBuilder cjValue = new StringBuilder ( antiClickJackingOption.headerValue );
        if ( antiClickJackingOption == XFrameOption.ALLOW_FROM ) {
            cjValue.append ( ' ' );
            cjValue.append ( antiClickJackingUri );
        }
        antiClickJackingHeaderValue = cjValue.toString();
    }
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response,
                           FilterChain chain ) throws IOException, ServletException {
        if ( response instanceof HttpServletResponse ) {
            HttpServletResponse httpResponse = ( HttpServletResponse ) response;
            if ( response.isCommitted() ) {
                throw new ServletException ( sm.getString ( "httpHeaderSecurityFilter.committed" ) );
            }
            if ( hstsEnabled && request.isSecure() ) {
                httpResponse.setHeader ( HSTS_HEADER_NAME, hstsHeaderValue );
            }
            if ( antiClickJackingEnabled ) {
                httpResponse.setHeader ( ANTI_CLICK_JACKING_HEADER_NAME, antiClickJackingHeaderValue );
            }
            if ( blockContentTypeSniffingEnabled ) {
                httpResponse.setHeader ( BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME,
                                         BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE );
            }
            if ( xssProtectionEnabled ) {
                httpResponse.setHeader ( XSS_PROTECTION_HEADER_NAME, XSS_PROTECTION_HEADER_VALUE );
            }
        }
        chain.doFilter ( request, response );
    }
    @Override
    protected Log getLogger() {
        return log;
    }
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
    public boolean isHstsEnabled() {
        return hstsEnabled;
    }
    public void setHstsEnabled ( boolean hstsEnabled ) {
        this.hstsEnabled = hstsEnabled;
    }
    public int getHstsMaxAgeSeconds() {
        return hstsMaxAgeSeconds;
    }
    public void setHstsMaxAgeSeconds ( int hstsMaxAgeSeconds ) {
        if ( hstsMaxAgeSeconds < 0 ) {
            this.hstsMaxAgeSeconds = 0;
        } else {
            this.hstsMaxAgeSeconds = hstsMaxAgeSeconds;
        }
    }
    public boolean isHstsIncludeSubDomains() {
        return hstsIncludeSubDomains;
    }
    public void setHstsIncludeSubDomains ( boolean hstsIncludeSubDomains ) {
        this.hstsIncludeSubDomains = hstsIncludeSubDomains;
    }
    public boolean isAntiClickJackingEnabled() {
        return antiClickJackingEnabled;
    }
    public void setAntiClickJackingEnabled ( boolean antiClickJackingEnabled ) {
        this.antiClickJackingEnabled = antiClickJackingEnabled;
    }
    public String getAntiClickJackingOption() {
        return antiClickJackingOption.toString();
    }
    public void setAntiClickJackingOption ( String antiClickJackingOption ) {
        for ( XFrameOption option : XFrameOption.values() ) {
            if ( option.getHeaderValue().equalsIgnoreCase ( antiClickJackingOption ) ) {
                this.antiClickJackingOption = option;
                return;
            }
        }
        throw new IllegalArgumentException (
            sm.getString ( "httpHeaderSecurityFilter.clickjack.invalid", antiClickJackingOption ) );
    }
    public String getAntiClickJackingUri() {
        return antiClickJackingUri.toString();
    }
    public boolean isBlockContentTypeSniffingEnabled() {
        return blockContentTypeSniffingEnabled;
    }
    public void setBlockContentTypeSniffingEnabled (
        boolean blockContentTypeSniffingEnabled ) {
        this.blockContentTypeSniffingEnabled = blockContentTypeSniffingEnabled;
    }
    public void setAntiClickJackingUri ( String antiClickJackingUri ) {
        URI uri;
        try {
            uri = new URI ( antiClickJackingUri );
        } catch ( URISyntaxException e ) {
            throw new IllegalArgumentException ( e );
        }
        this.antiClickJackingUri = uri;
    }
    public boolean isXssProtectionEnabled() {
        return xssProtectionEnabled;
    }
    public void setXssProtectionEnabled ( boolean xssProtectionEnabled ) {
        this.xssProtectionEnabled = xssProtectionEnabled;
    }
    private static enum XFrameOption {
        DENY ( "DENY" ),
        SAME_ORIGIN ( "SAMEORIGIN" ),
        ALLOW_FROM ( "ALLOW-FROM" );
        private final String headerValue;
        private XFrameOption ( String headerValue ) {
            this.headerValue = headerValue;
        }
        public String getHeaderValue() {
            return headerValue;
        }
    }
}
