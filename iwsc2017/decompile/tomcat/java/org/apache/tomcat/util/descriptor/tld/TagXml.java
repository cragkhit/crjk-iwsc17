package org.apache.tomcat.util.descriptor.tld;
import java.util.ArrayList;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import java.util.List;
public class TagXml {
    private String name;
    private String tagClass;
    private String teiClass;
    private String bodyContent;
    private String displayName;
    private String smallIcon;
    private String largeIcon;
    private String info;
    private boolean dynamicAttributes;
    private final List<TagAttributeInfo> attributes;
    private final List<TagVariableInfo> variables;
    public TagXml() {
        this.bodyContent = "JSP";
        this.attributes = new ArrayList<TagAttributeInfo>();
        this.variables = new ArrayList<TagVariableInfo>();
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public String getTagClass() {
        return this.tagClass;
    }
    public void setTagClass ( final String tagClass ) {
        this.tagClass = tagClass;
    }
    public String getTeiClass() {
        return this.teiClass;
    }
    public void setTeiClass ( final String teiClass ) {
        this.teiClass = teiClass;
    }
    public String getBodyContent() {
        return this.bodyContent;
    }
    public void setBodyContent ( final String bodyContent ) {
        this.bodyContent = bodyContent;
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
    public boolean hasDynamicAttributes() {
        return this.dynamicAttributes;
    }
    public void setDynamicAttributes ( final boolean dynamicAttributes ) {
        this.dynamicAttributes = dynamicAttributes;
    }
    public List<TagAttributeInfo> getAttributes() {
        return this.attributes;
    }
    public List<TagVariableInfo> getVariables() {
        return this.variables;
    }
}
