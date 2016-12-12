package org.apache.tomcat.util.descriptor.web;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import org.apache.tomcat.util.res.StringManager;
import java.io.Serializable;
public class FilterDef implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final StringManager sm;
    private String description;
    private String displayName;
    private transient Filter filter;
    private String filterClass;
    private String filterName;
    private String largeIcon;
    private final Map<String, String> parameters;
    private String smallIcon;
    private String asyncSupported;
    public FilterDef() {
        this.description = null;
        this.displayName = null;
        this.filter = null;
        this.filterClass = null;
        this.filterName = null;
        this.largeIcon = null;
        this.parameters = new HashMap<String, String>();
        this.smallIcon = null;
        this.asyncSupported = null;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String description ) {
        this.description = description;
    }
    public String getDisplayName() {
        return this.displayName;
    }
    public void setDisplayName ( final String displayName ) {
        this.displayName = displayName;
    }
    public Filter getFilter() {
        return this.filter;
    }
    public void setFilter ( final Filter filter ) {
        this.filter = filter;
    }
    public String getFilterClass() {
        return this.filterClass;
    }
    public void setFilterClass ( final String filterClass ) {
        this.filterClass = filterClass;
    }
    public String getFilterName() {
        return this.filterName;
    }
    public void setFilterName ( final String filterName ) {
        if ( filterName == null || filterName.equals ( "" ) ) {
            throw new IllegalArgumentException ( FilterDef.sm.getString ( "filterDef.invalidFilterName", filterName ) );
        }
        this.filterName = filterName;
    }
    public String getLargeIcon() {
        return this.largeIcon;
    }
    public void setLargeIcon ( final String largeIcon ) {
        this.largeIcon = largeIcon;
    }
    public Map<String, String> getParameterMap() {
        return this.parameters;
    }
    public String getSmallIcon() {
        return this.smallIcon;
    }
    public void setSmallIcon ( final String smallIcon ) {
        this.smallIcon = smallIcon;
    }
    public String getAsyncSupported() {
        return this.asyncSupported;
    }
    public void setAsyncSupported ( final String asyncSupported ) {
        this.asyncSupported = asyncSupported;
    }
    public void addInitParameter ( final String name, final String value ) {
        if ( this.parameters.containsKey ( name ) ) {
            return;
        }
        this.parameters.put ( name, value );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "FilterDef[" );
        sb.append ( "filterName=" );
        sb.append ( this.filterName );
        sb.append ( ", filterClass=" );
        sb.append ( this.filterClass );
        sb.append ( "]" );
        return sb.toString();
    }
    static {
        sm = StringManager.getManager ( Constants.PACKAGE_NAME );
    }
}
