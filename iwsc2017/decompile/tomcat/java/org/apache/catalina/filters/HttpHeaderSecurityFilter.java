package org.apache.catalina.filters;
import org.apache.juli.logging.LogFactory;
import java.net.URISyntaxException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletException;
import javax.servlet.FilterConfig;
import java.net.URI;
import org.apache.juli.logging.Log;
public class HttpHeaderSecurityFilter extends FilterBase {
    private static final Log log;
    private static final String HSTS_HEADER_NAME = "Strict-Transport-Security";
    private boolean hstsEnabled;
    private int hstsMaxAgeSeconds;
    private boolean hstsIncludeSubDomains;
    private String hstsHeaderValue;
    private static final String ANTI_CLICK_JACKING_HEADER_NAME = "X-Frame-Options";
    private boolean antiClickJackingEnabled;
    private XFrameOption antiClickJackingOption;
    private URI antiClickJackingUri;
    private String antiClickJackingHeaderValue;
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME = "X-Content-Type-Options";
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE = "nosniff";
    private boolean blockContentTypeSniffingEnabled;
    private static final String XSS_PROTECTION_HEADER_NAME = "X-XSS-Protection";
    private static final String XSS_PROTECTION_HEADER_VALUE = "1; mode=block";
    private boolean xssProtectionEnabled;
    public HttpHeaderSecurityFilter() {
        this.hstsEnabled = true;
        this.hstsMaxAgeSeconds = 0;
        this.hstsIncludeSubDomains = false;
        this.antiClickJackingEnabled = true;
        this.antiClickJackingOption = XFrameOption.DENY;
        this.blockContentTypeSniffingEnabled = true;
        this.xssProtectionEnabled = true;
    }
    @Override
    public void init ( final FilterConfig filterConfig ) throws ServletException {
        super.init ( filterConfig );
        final StringBuilder hstsValue = new StringBuilder ( "max-age=" );
        hstsValue.append ( this.hstsMaxAgeSeconds );
        if ( this.hstsIncludeSubDomains ) {
            hstsValue.append ( ";includeSubDomains" );
        }
        this.hstsHeaderValue = hstsValue.toString();
        final StringBuilder cjValue = new StringBuilder ( this.antiClickJackingOption.headerValue );
        if ( this.antiClickJackingOption == XFrameOption.ALLOW_FROM ) {
            cjValue.append ( ' ' );
            cjValue.append ( this.antiClickJackingUri );
        }
        this.antiClickJackingHeaderValue = cjValue.toString();
    }
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( response instanceof HttpServletResponse ) {
            final HttpServletResponse httpResponse = ( HttpServletResponse ) response;
            if ( response.isCommitted() ) {
                throw new ServletException ( HttpHeaderSecurityFilter.sm.getString ( "httpHeaderSecurityFilter.committed" ) );
            }
            if ( this.hstsEnabled && request.isSecure() ) {
                httpResponse.setHeader ( "Strict-Transport-Security", this.hstsHeaderValue );
            }
            if ( this.antiClickJackingEnabled ) {
                httpResponse.setHeader ( "X-Frame-Options", this.antiClickJackingHeaderValue );
            }
            if ( this.blockContentTypeSniffingEnabled ) {
                httpResponse.setHeader ( "X-Content-Type-Options", "nosniff" );
            }
            if ( this.xssProtectionEnabled ) {
                httpResponse.setHeader ( "X-XSS-Protection", "1; mode=block" );
            }
        }
        chain.doFilter ( request, response );
    }
    @Override
    protected Log getLogger() {
        return HttpHeaderSecurityFilter.log;
    }
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
    public boolean isHstsEnabled() {
        return this.hstsEnabled;
    }
    public void setHstsEnabled ( final boolean hstsEnabled ) {
        this.hstsEnabled = hstsEnabled;
    }
    public int getHstsMaxAgeSeconds() {
        return this.hstsMaxAgeSeconds;
    }
    public void setHstsMaxAgeSeconds ( final int hstsMaxAgeSeconds ) {
        if ( hstsMaxAgeSeconds < 0 ) {
            this.hstsMaxAgeSeconds = 0;
        } else {
            this.hstsMaxAgeSeconds = hstsMaxAgeSeconds;
        }
    }
    public boolean isHstsIncludeSubDomains() {
        return this.hstsIncludeSubDomains;
    }
    public void setHstsIncludeSubDomains ( final boolean hstsIncludeSubDomains ) {
        this.hstsIncludeSubDomains = hstsIncludeSubDomains;
    }
    public boolean isAntiClickJackingEnabled() {
        return this.antiClickJackingEnabled;
    }
    public void setAntiClickJackingEnabled ( final boolean antiClickJackingEnabled ) {
        this.antiClickJackingEnabled = antiClickJackingEnabled;
    }
    public String getAntiClickJackingOption() {
        return this.antiClickJackingOption.toString();
    }
    public void setAntiClickJackingOption ( final String antiClickJackingOption ) {
        for ( final XFrameOption option : XFrameOption.values() ) {
            if ( option.getHeaderValue().equalsIgnoreCase ( antiClickJackingOption ) ) {
                this.antiClickJackingOption = option;
                return;
            }
        }
        throw new IllegalArgumentException ( HttpHeaderSecurityFilter.sm.getString ( "httpHeaderSecurityFilter.clickjack.invalid", antiClickJackingOption ) );
    }
    public String getAntiClickJackingUri() {
        return this.antiClickJackingUri.toString();
    }
    public boolean isBlockContentTypeSniffingEnabled() {
        return this.blockContentTypeSniffingEnabled;
    }
    public void setBlockContentTypeSniffingEnabled ( final boolean blockContentTypeSniffingEnabled ) {
        this.blockContentTypeSniffingEnabled = blockContentTypeSniffingEnabled;
    }
    public void setAntiClickJackingUri ( final String antiClickJackingUri ) {
        URI uri;
        try {
            uri = new URI ( antiClickJackingUri );
        } catch ( URISyntaxException e ) {
            throw new IllegalArgumentException ( e );
        }
        this.antiClickJackingUri = uri;
    }
    public boolean isXssProtectionEnabled() {
        return this.xssProtectionEnabled;
    }
    public void setXssProtectionEnabled ( final boolean xssProtectionEnabled ) {
        this.xssProtectionEnabled = xssProtectionEnabled;
    }
    static {
        log = LogFactory.getLog ( HttpHeaderSecurityFilter.class );
    }
    private enum XFrameOption {
        DENY ( "DENY" ),
        SAME_ORIGIN ( "SAMEORIGIN" ),
        ALLOW_FROM ( "ALLOW-FROM" );
        private final String headerValue;
        private XFrameOption ( final String headerValue ) {
            this.headerValue = headerValue;
        }
        public String getHeaderValue() {
            return this.headerValue;
        }
    }
}
