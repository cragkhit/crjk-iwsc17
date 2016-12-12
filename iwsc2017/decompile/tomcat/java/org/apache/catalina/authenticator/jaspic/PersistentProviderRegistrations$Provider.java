package org.apache.catalina.authenticator.jaspic;
import java.util.HashMap;
import java.util.Map;
public static class Provider {
    private String className;
    private String layer;
    private String appContext;
    private String description;
    private final Map<String, String> properties;
    public Provider() {
        this.properties = new HashMap<String, String>();
    }
    public String getClassName() {
        return this.className;
    }
    public void setClassName ( final String className ) {
        this.className = className;
    }
    public String getLayer() {
        return this.layer;
    }
    public void setLayer ( final String layer ) {
        this.layer = layer;
    }
    public String getAppContext() {
        return this.appContext;
    }
    public void setAppContext ( final String appContext ) {
        this.appContext = appContext;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String description ) {
        this.description = description;
    }
    public void addProperty ( final Property property ) {
        this.properties.put ( property.getName(), property.getValue() );
    }
    void addProperty ( final String name, final String value ) {
        this.properties.put ( name, value );
    }
    public Map<String, String> getProperties() {
        return this.properties;
    }
}
