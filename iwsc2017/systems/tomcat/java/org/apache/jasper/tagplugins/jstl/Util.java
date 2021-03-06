package org.apache.jasper.tagplugins.jstl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import org.apache.jasper.Constants;
public class Util {
    private static final String VALID_SCHEME_CHAR =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";
    public static final String DEFAULT_ENCODING =
        "ISO-8859-1";
    private static final int HIGHEST_SPECIAL = '>';
    private static final char[][] specialCharactersRepresentation =
        new char[HIGHEST_SPECIAL + 1][];
    static {
        specialCharactersRepresentation['&'] = "&amp;".toCharArray();
        specialCharactersRepresentation['<'] = "&lt;".toCharArray();
        specialCharactersRepresentation['>'] = "&gt;".toCharArray();
        specialCharactersRepresentation['"'] = "&#034;".toCharArray();
        specialCharactersRepresentation['\''] = "&#039;".toCharArray();
    }
    public static int getScope ( String scope ) {
        int ret = PageContext.PAGE_SCOPE;
        if ( "request".equalsIgnoreCase ( scope ) ) {
            ret = PageContext.REQUEST_SCOPE;
        } else if ( "session".equalsIgnoreCase ( scope ) ) {
            ret = PageContext.SESSION_SCOPE;
        } else if ( "application".equalsIgnoreCase ( scope ) ) {
            ret = PageContext.APPLICATION_SCOPE;
        }
        return ret;
    }
    public static boolean isAbsoluteUrl ( String url ) {
        if ( url == null ) {
            return false;
        }
        int colonPos = url.indexOf ( ':' );
        if ( colonPos == -1 ) {
            return false;
        }
        for ( int i = 0; i < colonPos; i++ ) {
            if ( VALID_SCHEME_CHAR.indexOf ( url.charAt ( i ) ) == -1 ) {
                return false;
            }
        }
        return true;
    }
    public static String getContentTypeAttribute ( String input, String name ) {
        int begin;
        int end;
        int index = input.toUpperCase ( Locale.ENGLISH ).indexOf ( name.toUpperCase ( Locale.ENGLISH ) );
        if ( index == -1 ) {
            return null;
        }
        index = index + name.length();
        index = input.indexOf ( '=', index );
        if ( index == -1 ) {
            return null;
        }
        index += 1;
        input = input.substring ( index ).trim();
        if ( input.charAt ( 0 ) == '"' ) {
            begin = 1;
            end = input.indexOf ( '"', begin );
            if ( end == -1 ) {
                return null;
            }
        } else {
            begin = 0;
            end = input.indexOf ( ';' );
            if ( end == -1 ) {
                end = input.indexOf ( ' ' );
            }
            if ( end == -1 ) {
                end = input.length();
            }
        }
        return input.substring ( begin, end ).trim();
    }
    public static String stripSession ( String url ) {
        StringBuilder u = new StringBuilder ( url );
        int sessionStart;
        while ( ( sessionStart = u.toString().indexOf ( ";" + Constants.SESSION_PARAMETER_NAME + "=" ) ) != -1 ) {
            int sessionEnd = u.toString().indexOf ( ';', sessionStart + 1 );
            if ( sessionEnd == -1 ) {
                sessionEnd = u.toString().indexOf ( '?', sessionStart + 1 );
            }
            if ( sessionEnd == -1 ) {
                sessionEnd = u.length();
            }
            u.delete ( sessionStart, sessionEnd );
        }
        return u.toString();
    }
    public static String escapeXml ( String buffer ) {
        String result = escapeXml ( buffer.toCharArray(), buffer.length() );
        if ( result == null ) {
            return buffer;
        } else {
            return result;
        }
    }
    @SuppressWarnings ( "null" )
    public static String escapeXml ( char[] arrayBuffer, int length ) {
        int start = 0;
        StringBuilder escapedBuffer = null;
        for ( int i = 0; i < length; i++ ) {
            char c = arrayBuffer[i];
            if ( c <= HIGHEST_SPECIAL ) {
                char[] escaped = specialCharactersRepresentation[c];
                if ( escaped != null ) {
                    if ( start == 0 ) {
                        escapedBuffer = new StringBuilder ( length + 5 );
                    }
                    if ( start < i ) {
                        escapedBuffer.append ( arrayBuffer, start, i - start );
                    }
                    start = i + 1;
                    escapedBuffer.append ( escaped );
                }
            }
        }
        if ( start == 0 ) {
            return null;
        }
        if ( start < length ) {
            escapedBuffer.append ( arrayBuffer, start, length - start );
        }
        return escapedBuffer.toString();
    }
    public static String resolveUrl (
        String url, String context, PageContext pageContext )
    throws JspException {
        if ( isAbsoluteUrl ( url ) ) {
            return url;
        }
        HttpServletRequest request =
            ( HttpServletRequest ) pageContext.getRequest();
        if ( context == null ) {
            if ( url.startsWith ( "/" ) ) {
                return ( request.getContextPath() + url );
            } else {
                return url;
            }
        } else {
            if ( !context.startsWith ( "/" ) || !url.startsWith ( "/" ) ) {
                throw new JspTagException (
                    "In URL tags, when the \"context\" attribute is specified, values of both \"context\" and \"url\" must start with \"/\"." );
            }
            if ( context.equals ( "/" ) ) {
                return url;
            } else {
                return ( context + url );
            }
        }
    }
    public static class ImportResponseWrapper extends HttpServletResponseWrapper {
        private final StringWriter sw = new StringWriter();
        private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        private final ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public void write ( int b ) throws IOException {
                bos.write ( b );
            }
            @Override
            public boolean isReady() {
                return false;
            }
            @Override
            public void setWriteListener ( WriteListener listener ) {
                throw new UnsupportedOperationException();
            }
        };
        private boolean isWriterUsed;
        private boolean isStreamUsed;
        private int status = 200;
        private String charEncoding;
        public ImportResponseWrapper ( HttpServletResponse arg0 ) {
            super ( arg0 );
        }
        @Override
        public PrintWriter getWriter() {
            if ( isStreamUsed )
                throw new IllegalStateException ( "Unexpected internal error during &lt;import&gt: " +
                                                  "Target servlet called getWriter(), then getOutputStream()" );
            isWriterUsed = true;
            return new PrintWriter ( sw );
        }
        @Override
        public ServletOutputStream getOutputStream() {
            if ( isWriterUsed )
                throw new IllegalStateException ( "Unexpected internal error during &lt;import&gt: " +
                                                  "Target servlet called getOutputStream(), then getWriter()" );
            isStreamUsed = true;
            return sos;
        }
        @Override
        public void setContentType ( String x ) {
        }
        @Override
        public void setLocale ( Locale x ) {
        }
        @Override
        public void setStatus ( int status ) {
            this.status = status;
        }
        @Override
        public int getStatus() {
            return status;
        }
        public String getCharEncoding() {
            return this.charEncoding;
        }
        public void setCharEncoding ( String ce ) {
            this.charEncoding = ce;
        }
        public String getString() throws UnsupportedEncodingException {
            if ( isWriterUsed ) {
                return sw.toString();
            } else if ( isStreamUsed ) {
                if ( this.charEncoding != null && !this.charEncoding.equals ( "" ) ) {
                    return bos.toString ( charEncoding );
                } else {
                    return bos.toString ( "ISO-8859-1" );
                }
            } else {
                return "";
            }
        }
    }
}
