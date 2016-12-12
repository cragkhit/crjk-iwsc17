package org.apache.tomcat.util.descriptor.web;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import java.io.Serializable;
public class ServletDef implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final StringManager sm;
    private String description;
    private String displayName;
    private String smallIcon;
    private String largeIcon;
    private String servletName;
    private String servletClass;
    private String jspFile;
    private final Map<String, String> parameters;
    private Integer loadOnStartup;
    private String runAs;
    private final Set<SecurityRoleRef> securityRoleRefs;
    private MultipartDef multipartDef;
    private Boolean asyncSupported;
    private Boolean enabled;
    private boolean overridable;
    public ServletDef() {
        this.description = null;
        this.displayName = null;
        this.smallIcon = null;
        this.largeIcon = null;
        this.servletName = null;
        this.servletClass = null;
        this.jspFile = null;
        this.parameters = new HashMap<String, String>();
        this.loadOnStartup = null;
        this.runAs = null;
        this.securityRoleRefs = new HashSet<SecurityRoleRef>();
        this.multipartDef = null;
        this.asyncSupported = null;
        this.enabled = null;
        this.overridable = false;
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
    public String getSmallIcon() {
        return this.smallIcon;
    }
    public void setSmallIcon ( final String smallIcon ) {
        this.smallIcon = smallIcon;
    }
    public String getLargeIcon() {
        return this.largeIcon;
    }
    public void setLargeIcon ( final String largeIcon ) {
        this.largeIcon = largeIcon;
    }
    public String getServletName() {
        return this.servletName;
    }
    public void setServletName ( final String servletName ) {
        if ( servletName == null || servletName.equals ( "" ) ) {
            throw new IllegalArgumentException ( ServletDef.sm.getString ( "servletDef.invalidServletName", servletName ) );
        }
        this.servletName = servletName;
    }
    public String getServletClass() {
        return this.servletClass;
    }
    public void setServletClass ( final String servletClass ) {
        this.servletClass = servletClass;
    }
    public String getJspFile() {
        return this.jspFile;
    }
    public void setJspFile ( final String jspFile ) {
        this.jspFile = jspFile;
    }
    public Map<String, String> getParameterMap() {
        return this.parameters;
    }
    public void addInitParameter ( final String name, final String value ) {
        if ( this.parameters.containsKey ( name ) ) {
            return;
        }
        this.parameters.put ( name, value );
    }
    public Integer getLoadOnStartup() {
        return this.loadOnStartup;
    }
    public void setLoadOnStartup ( final String loadOnStartup ) {
        this.loadOnStartup = Integer.valueOf ( loadOnStartup );
    }
    public String getRunAs() {
        return this.runAs;
    }
    public void setRunAs ( final String runAs ) {
        this.runAs = runAs;
    }
    public Set<SecurityRoleRef> getSecurityRoleRefs() {
        return this.securityRoleRefs;
    }
    public void addSecurityRoleRef ( final SecurityRoleRef securityRoleRef ) {
        this.securityRoleRefs.add ( securityRoleRef );
    }
    public MultipartDef getMultipartDef() {
        return this.multipartDef;
    }
    public void setMultipartDef ( final MultipartDef multipartDef ) {
        this.multipartDef = multipartDef;
    }
    public Boolean getAsyncSupported() {
        return this.asyncSupported;
    }
    public void setAsyncSupported ( final String asyncSupported ) {
        this.asyncSupported = Boolean.valueOf ( asyncSupported );
    }
    public Boolean getEnabled() {
        return this.enabled;
    }
    public void setEnabled ( final String enabled ) {
        this.enabled = Boolean.valueOf ( enabled );
    }
    public boolean isOverridable() {
        return this.overridable;
    }
    public void setOverridable ( final boolean overridable ) {
        this.overridable = overridable;
    }
    static {
        sm = StringManager.getManager ( Constants.PACKAGE_NAME );
    }
}
