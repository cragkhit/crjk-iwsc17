package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import javax.servlet.DispatcherType;
import org.apache.tomcat.util.buf.UDecoder;
public class FilterMap extends XmlEncodingBase implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int ERROR = 1;
    public static final int FORWARD = 2;
    public static final int INCLUDE = 4;
    public static final int REQUEST = 8;
    public static final int ASYNC = 16;
    private static final int NOT_SET = 0;
    private int dispatcherMapping = NOT_SET;
    private String filterName = null;
    public String getFilterName() {
        return ( this.filterName );
    }
    public void setFilterName ( String filterName ) {
        this.filterName = filterName;
    }
    private String[] servletNames = new String[0];
    public String[] getServletNames() {
        if ( matchAllServletNames ) {
            return new String[] {};
        } else {
            return ( this.servletNames );
        }
    }
    public void addServletName ( String servletName ) {
        if ( "*".equals ( servletName ) ) {
            this.matchAllServletNames = true;
        } else {
            String[] results = new String[servletNames.length + 1];
            System.arraycopy ( servletNames, 0, results, 0, servletNames.length );
            results[servletNames.length] = servletName;
            servletNames = results;
        }
    }
    private boolean matchAllUrlPatterns = false;
    public boolean getMatchAllUrlPatterns() {
        return matchAllUrlPatterns;
    }
    private boolean matchAllServletNames = false;
    public boolean getMatchAllServletNames() {
        return matchAllServletNames;
    }
    private String[] urlPatterns = new String[0];
    public String[] getURLPatterns() {
        if ( matchAllUrlPatterns ) {
            return new String[] {};
        } else {
            return ( this.urlPatterns );
        }
    }
    public void addURLPattern ( String urlPattern ) {
        addURLPatternDecoded ( UDecoder.URLDecode ( urlPattern, getEncoding() ) );
    }
    public void addURLPatternDecoded ( String urlPattern ) {
        if ( "*".equals ( urlPattern ) ) {
            this.matchAllUrlPatterns = true;
        } else {
            String[] results = new String[urlPatterns.length + 1];
            System.arraycopy ( urlPatterns, 0, results, 0, urlPatterns.length );
            results[urlPatterns.length] = UDecoder.URLDecode ( urlPattern );
            urlPatterns = results;
        }
    }
    public void setDispatcher ( String dispatcherString ) {
        String dispatcher = dispatcherString.toUpperCase ( Locale.ENGLISH );
        if ( dispatcher.equals ( DispatcherType.FORWARD.name() ) ) {
            dispatcherMapping |= FORWARD;
        } else if ( dispatcher.equals ( DispatcherType.INCLUDE.name() ) ) {
            dispatcherMapping |= INCLUDE;
        } else if ( dispatcher.equals ( DispatcherType.REQUEST.name() ) ) {
            dispatcherMapping |= REQUEST;
        }  else if ( dispatcher.equals ( DispatcherType.ERROR.name() ) ) {
            dispatcherMapping |= ERROR;
        }  else if ( dispatcher.equals ( DispatcherType.ASYNC.name() ) ) {
            dispatcherMapping |= ASYNC;
        }
    }
    public int getDispatcherMapping() {
        if ( dispatcherMapping == NOT_SET ) {
            return REQUEST;
        }
        return dispatcherMapping;
    }
    public String[] getDispatcherNames() {
        ArrayList<String> result = new ArrayList<>();
        if ( ( dispatcherMapping & FORWARD ) > 0 ) {
            result.add ( DispatcherType.FORWARD.name() );
        }
        if ( ( dispatcherMapping & INCLUDE ) > 0 ) {
            result.add ( DispatcherType.INCLUDE.name() );
        }
        if ( ( dispatcherMapping & REQUEST ) > 0 ) {
            result.add ( DispatcherType.REQUEST.name() );
        }
        if ( ( dispatcherMapping & ERROR ) > 0 ) {
            result.add ( DispatcherType.ERROR.name() );
        }
        if ( ( dispatcherMapping & ASYNC ) > 0 ) {
            result.add ( DispatcherType.ASYNC.name() );
        }
        return result.toArray ( new String[result.size()] );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "FilterMap[" );
        sb.append ( "filterName=" );
        sb.append ( this.filterName );
        for ( int i = 0; i < servletNames.length; i++ ) {
            sb.append ( ", servletName=" );
            sb.append ( servletNames[i] );
        }
        for ( int i = 0; i < urlPatterns.length; i++ ) {
            sb.append ( ", urlPattern=" );
            sb.append ( urlPatterns[i] );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
