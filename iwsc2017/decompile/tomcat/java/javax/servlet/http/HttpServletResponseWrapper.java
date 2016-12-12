package javax.servlet.http;
import java.util.Collection;
import java.io.IOException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
public class HttpServletResponseWrapper extends ServletResponseWrapper implements HttpServletResponse {
    public HttpServletResponseWrapper ( final HttpServletResponse response ) {
        super ( response );
    }
    private HttpServletResponse _getHttpServletResponse() {
        return ( HttpServletResponse ) super.getResponse();
    }
    @Override
    public void addCookie ( final Cookie cookie ) {
        this._getHttpServletResponse().addCookie ( cookie );
    }
    @Override
    public boolean containsHeader ( final String name ) {
        return this._getHttpServletResponse().containsHeader ( name );
    }
    @Override
    public String encodeURL ( final String url ) {
        return this._getHttpServletResponse().encodeURL ( url );
    }
    @Override
    public String encodeRedirectURL ( final String url ) {
        return this._getHttpServletResponse().encodeRedirectURL ( url );
    }
    @Deprecated
    @Override
    public String encodeUrl ( final String url ) {
        return this._getHttpServletResponse().encodeUrl ( url );
    }
    @Deprecated
    @Override
    public String encodeRedirectUrl ( final String url ) {
        return this._getHttpServletResponse().encodeRedirectUrl ( url );
    }
    @Override
    public void sendError ( final int sc, final String msg ) throws IOException {
        this._getHttpServletResponse().sendError ( sc, msg );
    }
    @Override
    public void sendError ( final int sc ) throws IOException {
        this._getHttpServletResponse().sendError ( sc );
    }
    @Override
    public void sendRedirect ( final String location ) throws IOException {
        this._getHttpServletResponse().sendRedirect ( location );
    }
    @Override
    public void setDateHeader ( final String name, final long date ) {
        this._getHttpServletResponse().setDateHeader ( name, date );
    }
    @Override
    public void addDateHeader ( final String name, final long date ) {
        this._getHttpServletResponse().addDateHeader ( name, date );
    }
    @Override
    public void setHeader ( final String name, final String value ) {
        this._getHttpServletResponse().setHeader ( name, value );
    }
    @Override
    public void addHeader ( final String name, final String value ) {
        this._getHttpServletResponse().addHeader ( name, value );
    }
    @Override
    public void setIntHeader ( final String name, final int value ) {
        this._getHttpServletResponse().setIntHeader ( name, value );
    }
    @Override
    public void addIntHeader ( final String name, final int value ) {
        this._getHttpServletResponse().addIntHeader ( name, value );
    }
    @Override
    public void setStatus ( final int sc ) {
        this._getHttpServletResponse().setStatus ( sc );
    }
    @Deprecated
    @Override
    public void setStatus ( final int sc, final String sm ) {
        this._getHttpServletResponse().setStatus ( sc, sm );
    }
    @Override
    public int getStatus() {
        return this._getHttpServletResponse().getStatus();
    }
    @Override
    public String getHeader ( final String name ) {
        return this._getHttpServletResponse().getHeader ( name );
    }
    @Override
    public Collection<String> getHeaders ( final String name ) {
        return this._getHttpServletResponse().getHeaders ( name );
    }
    @Override
    public Collection<String> getHeaderNames() {
        return this._getHttpServletResponse().getHeaderNames();
    }
}
