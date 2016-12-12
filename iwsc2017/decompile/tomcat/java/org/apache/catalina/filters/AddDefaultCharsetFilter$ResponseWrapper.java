package org.apache.catalina.filters;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
public static class ResponseWrapper extends HttpServletResponseWrapper {
    private String encoding;
    public ResponseWrapper ( final HttpServletResponse response, final String encoding ) {
        super ( response );
        this.encoding = encoding;
    }
    public void setContentType ( final String ct ) {
        if ( ct != null && ct.startsWith ( "text/" ) ) {
            if ( ct.indexOf ( "charset=" ) < 0 ) {
                super.setContentType ( ct + ";charset=" + this.encoding );
            } else {
                super.setContentType ( ct );
                this.encoding = this.getCharacterEncoding();
            }
        } else {
            super.setContentType ( ct );
        }
    }
    public void setCharacterEncoding ( final String charset ) {
        super.setCharacterEncoding ( charset );
        this.encoding = charset;
    }
}
