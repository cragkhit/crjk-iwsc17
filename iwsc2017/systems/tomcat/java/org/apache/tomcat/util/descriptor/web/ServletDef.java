package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tomcat.util.res.StringManager;
public class ServletDef implements Serializable {
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
    private String smallIcon = null;
    public String getSmallIcon() {
        return ( this.smallIcon );
    }
    public void setSmallIcon ( String smallIcon ) {
        this.smallIcon = smallIcon;
    }
    private String largeIcon = null;
    public String getLargeIcon() {
        return ( this.largeIcon );
    }
    public void setLargeIcon ( String largeIcon ) {
        this.largeIcon = largeIcon;
    }
    private String servletName = null;
    public String getServletName() {
        return ( this.servletName );
    }
    public void setServletName ( String servletName ) {
        if ( servletName == null || servletName.equals ( "" ) ) {
            throw new IllegalArgumentException (
                sm.getString ( "servletDef.invalidServletName", servletName ) );
        }
        this.servletName = servletName;
    }
    private String servletClass = null;
    public String getServletClass() {
        return ( this.servletClass );
    }
    public void setServletClass ( String servletClass ) {
        this.servletClass = servletClass;
    }
    private String jspFile = null;
    public String getJspFile() {
        return ( this.jspFile );
    }
    public void setJspFile ( String jspFile ) {
        this.jspFile = jspFile;
    }
    private final Map<String, String> parameters = new HashMap<>();
    public Map<String, String> getParameterMap() {
        return ( this.parameters );
    }
    public void addInitParameter ( String name, String value ) {
        if ( parameters.containsKey ( name ) ) {
            return;
        }
        parameters.put ( name, value );
    }
    private Integer loadOnStartup = null;
    public Integer getLoadOnStartup() {
        return ( this.loadOnStartup );
    }
    public void setLoadOnStartup ( String loadOnStartup ) {
        this.loadOnStartup = Integer.valueOf ( loadOnStartup );
    }
    private String runAs = null;
    public String getRunAs() {
        return ( this.runAs );
    }
    public void setRunAs ( String runAs ) {
        this.runAs = runAs;
    }
    private final Set<SecurityRoleRef> securityRoleRefs = new HashSet<>();
    public Set<SecurityRoleRef> getSecurityRoleRefs() {
        return ( this.securityRoleRefs );
    }
    public void addSecurityRoleRef ( SecurityRoleRef securityRoleRef ) {
        securityRoleRefs.add ( securityRoleRef );
    }
    private MultipartDef multipartDef = null;
    public MultipartDef getMultipartDef() {
        return this.multipartDef;
    }
    public void setMultipartDef ( MultipartDef multipartDef ) {
        this.multipartDef = multipartDef;
    }
    private Boolean asyncSupported = null;
    public Boolean getAsyncSupported() {
        return this.asyncSupported;
    }
    public void setAsyncSupported ( String asyncSupported ) {
        this.asyncSupported = Boolean.valueOf ( asyncSupported );
    }
    private Boolean enabled = null;
    public Boolean getEnabled() {
        return this.enabled;
    }
    public void setEnabled ( String enabled ) {
        this.enabled = Boolean.valueOf ( enabled );
    }
    private boolean overridable = false;
    public boolean isOverridable() {
        return overridable;
    }
    public void setOverridable ( boolean overridable ) {
        this.overridable = overridable;
    }
}
