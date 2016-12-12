package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
protected static class RequestParameterElement implements AccessLogElement {
    private final String parameter;
    public RequestParameterElement ( final String parameter ) {
        this.parameter = parameter;
    }
    private String urlEncode ( final String value ) {
        if ( null == value || value.length() == 0 ) {
            return null;
        }
        try {
            return URLEncoder.encode ( value, "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            return null;
        }
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        buf.append ( ExtendedAccessLogValve.wrap ( this.urlEncode ( request.getParameter ( this.parameter ) ) ) );
    }
}
