package org.apache.jasper.tagplugins.jstl;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.PrintWriter;
import javax.servlet.WriteListener;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.apache.jasper.Constants;
import java.util.Locale;
public class Util {
    private static final String VALID_SCHEME_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final int HIGHEST_SPECIAL = 62;
    private static final char[][] specialCharactersRepresentation;
    public static int getScope ( final String scope ) {
        int ret = 1;
        if ( "request".equalsIgnoreCase ( scope ) ) {
            ret = 2;
        } else if ( "session".equalsIgnoreCase ( scope ) ) {
            ret = 3;
        } else if ( "application".equalsIgnoreCase ( scope ) ) {
            ret = 4;
        }
        return ret;
    }
    public static boolean isAbsoluteUrl ( final String url ) {
        if ( url == null ) {
            return false;
        }
        final int colonPos = url.indexOf ( 58 );
        if ( colonPos == -1 ) {
            return false;
        }
        for ( int i = 0; i < colonPos; ++i ) {
            if ( "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-".indexOf ( url.charAt ( i ) ) == -1 ) {
                return false;
            }
        }
        return true;
    }
    public static String getContentTypeAttribute ( String input, final String name ) {
        int index = input.toUpperCase ( Locale.ENGLISH ).indexOf ( name.toUpperCase ( Locale.ENGLISH ) );
        if ( index == -1 ) {
            return null;
        }
        index += name.length();
        index = input.indexOf ( 61, index );
        if ( index == -1 ) {
            return null;
        }
        ++index;
        input = input.substring ( index ).trim();
        int begin;
        int end;
        if ( input.charAt ( 0 ) == '\"' ) {
            begin = 1;
            end = input.indexOf ( 34, begin );
            if ( end == -1 ) {
                return null;
            }
        } else {
            begin = 0;
            end = input.indexOf ( 59 );
            if ( end == -1 ) {
                end = input.indexOf ( 32 );
            }
            if ( end == -1 ) {
                end = input.length();
            }
        }
        return input.substring ( begin, end ).trim();
    }
    public static String stripSession ( final String url ) {
        final StringBuilder u = new StringBuilder ( url );
        int sessionStart;
        while ( ( sessionStart = u.toString().indexOf ( ";" + Constants.SESSION_PARAMETER_NAME + "=" ) ) != -1 ) {
            int sessionEnd = u.toString().indexOf ( 59, sessionStart + 1 );
            if ( sessionEnd == -1 ) {
                sessionEnd = u.toString().indexOf ( 63, sessionStart + 1 );
            }
            if ( sessionEnd == -1 ) {
                sessionEnd = u.length();
            }
            u.delete ( sessionStart, sessionEnd );
        }
        return u.toString();
    }
    public static String escapeXml ( final String buffer ) {
        final String result = escapeXml ( buffer.toCharArray(), buffer.length() );
        if ( result == null ) {
            return buffer;
        }
        return result;
    }
    public static String escapeXml ( final char[] arrayBuffer, final int length ) {
        int start = 0;
        StringBuilder escapedBuffer = null;
        for ( int i = 0; i < length; ++i ) {
            final char c = arrayBuffer[i];
            if ( c <= '>' ) {
                final char[] escaped = Util.specialCharactersRepresentation[c];
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
    public static String resolveUrl ( final String url, final String context, final PageContext pageContext ) throws JspException {
        if ( isAbsoluteUrl ( url ) ) {
            return url;
        }
        final HttpServletRequest request = ( HttpServletRequest ) pageContext.getRequest();
        if ( context == null ) {
            if ( url.startsWith ( "/" ) ) {
                return request.getContextPath() + url;
            }
            return url;
        } else {
            if ( !context.startsWith ( "/" ) || !url.startsWith ( "/" ) ) {
                throw new JspTagException ( "In URL tags, when the \"context\" attribute is specified, values of both \"context\" and \"url\" must start with \"/\"." );
            }
            if ( context.equals ( "/" ) ) {
                return url;
            }
            return context + url;
        }
    }
    static {
        ( specialCharactersRepresentation = new char[63][] ) [38] = "&amp;".toCharArray();
        Util.specialCharactersRepresentation[60] = "&lt;".toCharArray();
        Util.specialCharactersRepresentation[62] = "&gt;".toCharArray();
        Util.specialCharactersRepresentation[34] = "&#034;".toCharArray();
        Util.specialCharactersRepresentation[39] = "&#039;".toCharArray();
    }
    public static class ImportResponseWrapper extends HttpServletResponseWrapper {
        private final StringWriter sw;
        private final ByteArrayOutputStream bos;
        private final ServletOutputStream sos;
        private boolean isWriterUsed;
        private boolean isStreamUsed;
        private int status;
        private String charEncoding;
        public ImportResponseWrapper ( final HttpServletResponse arg0 ) {
            super ( arg0 );
            this.sw = new StringWriter();
            this.bos = new ByteArrayOutputStream();
            this.sos = new ServletOutputStream() {
                public void write ( final int b ) throws IOException {
                    ImportResponseWrapper.this.bos.write ( b );
                }
                public boolean isReady() {
                    return false;
                }
                public void setWriteListener ( final WriteListener listener ) {
                    throw new UnsupportedOperationException();
                }
            };
            this.status = 200;
        }
        public PrintWriter getWriter() {
            if ( this.isStreamUsed ) {
                throw new IllegalStateException ( "Unexpected internal error during &lt;import&gt: Target servlet called getWriter(), then getOutputStream()" );
            }
            this.isWriterUsed = true;
            return new PrintWriter ( this.sw );
        }
        public ServletOutputStream getOutputStream() {
            if ( this.isWriterUsed ) {
                throw new IllegalStateException ( "Unexpected internal error during &lt;import&gt: Target servlet called getOutputStream(), then getWriter()" );
            }
            this.isStreamUsed = true;
            return this.sos;
        }
        public void setContentType ( final String x ) {
        }
        public void setLocale ( final Locale x ) {
        }
        public void setStatus ( final int status ) {
            this.status = status;
        }
        public int getStatus() {
            return this.status;
        }
        public String getCharEncoding() {
            return this.charEncoding;
        }
        public void setCharEncoding ( final String ce ) {
            this.charEncoding = ce;
        }
        public String getString() throws UnsupportedEncodingException {
            if ( this.isWriterUsed ) {
                return this.sw.toString();
            }
            if ( !this.isStreamUsed ) {
                return "";
            }
            if ( this.charEncoding != null && !this.charEncoding.equals ( "" ) ) {
                return this.bos.toString ( this.charEncoding );
            }
            return this.bos.toString ( "ISO-8859-1" );
        }
    }
}
