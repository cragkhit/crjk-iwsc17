package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import org.apache.tomcat.util.res.StringManager;
public class FilterDef implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final StringManager sm =
        StringManager.getManager ( Constants.PACKAGE_NAME );
    private String description = null;
    public String getDescription() {
        return ( this.description );
    }
    public void setDescription ( String description ) {
        this.description = description;
    }
    private String displayName = null;
    public String getDisplayName() {
        return ( this.displayName );
    }
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }
    private transient Filter filter = null;
    public Filter getFilter() {
        return filter;
    }
    public void setFilter ( Filter filter ) {
        this.filter = filter;
    }
    private String filterClass = null;
    public String getFilterClass() {
        return ( this.filterClass );
    }
    public void setFilterClass ( String filterClass ) {
        this.filterClass = filterClass;
    }
    private String filterName = null;
    public String getFilterName() {
        return ( this.filterName );
    }
    public void setFilterName ( String filterName ) {
        if ( filterName == null || filterName.equals ( "" ) ) {
            throw new IllegalArgumentException (
                sm.getString ( "filterDef.invalidFilterName", filterName ) );
        }
        this.filterName = filterName;
    }
    private String largeIcon = null;
    public String getLargeIcon() {
        return ( this.largeIcon );
    }
    public void setLargeIcon ( String largeIcon ) {
        this.largeIcon = largeIcon;
    }
    private final Map<String, String> parameters = new HashMap<>();
    public Map<String, String> getParameterMap() {
        return ( this.parameters );
    }
    private String smallIcon = null;
    public String getSmallIcon() {
        return ( this.smallIcon );
    }
    public void setSmallIcon ( String smallIcon ) {
        this.smallIcon = smallIcon;
    }
    private String asyncSupported = null;
    public String getAsyncSupported() {
        return asyncSupported;
    }
    public void setAsyncSupported ( String asyncSupported ) {
        this.asyncSupported = asyncSupported;
    }
    public void addInitParameter ( String name, String value ) {
        if ( parameters.containsKey ( name ) ) {
            return;
        }
        parameters.put ( name, value );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "FilterDef[" );
        sb.append ( "filterName=" );
        sb.append ( this.filterName );
        sb.append ( ", filterClass=" );
        sb.append ( this.filterClass );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
