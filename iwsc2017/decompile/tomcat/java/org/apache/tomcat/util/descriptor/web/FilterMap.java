package org.apache.tomcat.util.descriptor.web;
import java.util.ArrayList;
import javax.servlet.DispatcherType;
import java.util.Locale;
import org.apache.tomcat.util.buf.UDecoder;
import java.io.Serializable;
public class FilterMap extends XmlEncodingBase implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int ERROR = 1;
    public static final int FORWARD = 2;
    public static final int INCLUDE = 4;
    public static final int REQUEST = 8;
    public static final int ASYNC = 16;
    private static final int NOT_SET = 0;
    private int dispatcherMapping;
    private String filterName;
    private String[] servletNames;
    private boolean matchAllUrlPatterns;
    private boolean matchAllServletNames;
    private String[] urlPatterns;
    public FilterMap() {
        this.dispatcherMapping = 0;
        this.filterName = null;
        this.servletNames = new String[0];
        this.matchAllUrlPatterns = false;
        this.matchAllServletNames = false;
        this.urlPatterns = new String[0];
    }
    public String getFilterName() {
        return this.filterName;
    }
    public void setFilterName ( final String filterName ) {
        this.filterName = filterName;
    }
    public String[] getServletNames() {
        if ( this.matchAllServletNames ) {
            return new String[0];
        }
        return this.servletNames;
    }
    public void addServletName ( final String servletName ) {
        if ( "*".equals ( servletName ) ) {
            this.matchAllServletNames = true;
        } else {
            final String[] results = new String[this.servletNames.length + 1];
            System.arraycopy ( this.servletNames, 0, results, 0, this.servletNames.length );
            results[this.servletNames.length] = servletName;
            this.servletNames = results;
        }
    }
    public boolean getMatchAllUrlPatterns() {
        return this.matchAllUrlPatterns;
    }
    public boolean getMatchAllServletNames() {
        return this.matchAllServletNames;
    }
    public String[] getURLPatterns() {
        if ( this.matchAllUrlPatterns ) {
            return new String[0];
        }
        return this.urlPatterns;
    }
    public void addURLPattern ( final String urlPattern ) {
        this.addURLPatternDecoded ( UDecoder.URLDecode ( urlPattern, this.getEncoding() ) );
    }
    public void addURLPatternDecoded ( final String urlPattern ) {
        if ( "*".equals ( urlPattern ) ) {
            this.matchAllUrlPatterns = true;
        } else {
            final String[] results = new String[this.urlPatterns.length + 1];
            System.arraycopy ( this.urlPatterns, 0, results, 0, this.urlPatterns.length );
            results[this.urlPatterns.length] = UDecoder.URLDecode ( urlPattern );
            this.urlPatterns = results;
        }
    }
    public void setDispatcher ( final String dispatcherString ) {
        final String dispatcher = dispatcherString.toUpperCase ( Locale.ENGLISH );
        if ( dispatcher.equals ( DispatcherType.FORWARD.name() ) ) {
            this.dispatcherMapping |= 0x2;
        } else if ( dispatcher.equals ( DispatcherType.INCLUDE.name() ) ) {
            this.dispatcherMapping |= 0x4;
        } else if ( dispatcher.equals ( DispatcherType.REQUEST.name() ) ) {
            this.dispatcherMapping |= 0x8;
        } else if ( dispatcher.equals ( DispatcherType.ERROR.name() ) ) {
            this.dispatcherMapping |= 0x1;
        } else if ( dispatcher.equals ( DispatcherType.ASYNC.name() ) ) {
            this.dispatcherMapping |= 0x10;
        }
    }
    public int getDispatcherMapping() {
        if ( this.dispatcherMapping == 0 ) {
            return 8;
        }
        return this.dispatcherMapping;
    }
    public String[] getDispatcherNames() {
        final ArrayList<String> result = new ArrayList<String>();
        if ( ( this.dispatcherMapping & 0x2 ) > 0 ) {
            result.add ( DispatcherType.FORWARD.name() );
        }
        if ( ( this.dispatcherMapping & 0x4 ) > 0 ) {
            result.add ( DispatcherType.INCLUDE.name() );
        }
        if ( ( this.dispatcherMapping & 0x8 ) > 0 ) {
            result.add ( DispatcherType.REQUEST.name() );
        }
        if ( ( this.dispatcherMapping & 0x1 ) > 0 ) {
            result.add ( DispatcherType.ERROR.name() );
        }
        if ( ( this.dispatcherMapping & 0x10 ) > 0 ) {
            result.add ( DispatcherType.ASYNC.name() );
        }
        return result.toArray ( new String[result.size()] );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "FilterMap[" );
        sb.append ( "filterName=" );
        sb.append ( this.filterName );
        for ( int i = 0; i < this.servletNames.length; ++i ) {
            sb.append ( ", servletName=" );
            sb.append ( this.servletNames[i] );
        }
        for ( int i = 0; i < this.urlPatterns.length; ++i ) {
            sb.append ( ", urlPattern=" );
            sb.append ( this.urlPatterns[i] );
        }
        sb.append ( "]" );
        return sb.toString();
    }
}
