package org.apache.tomcat.util.descriptor.tld;
public class TagFileXml {
    private String name;
    private String path;
    private String displayName;
    private String smallIcon;
    private String largeIcon;
    private String info;
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public String getPath() {
        return this.path;
    }
    public void setPath ( final String path ) {
        this.path = path;
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
    public String getInfo() {
        return this.info;
    }
    public void setInfo ( final String info ) {
        this.info = info;
    }
}
