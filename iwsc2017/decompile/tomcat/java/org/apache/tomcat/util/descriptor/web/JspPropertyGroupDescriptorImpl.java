package org.apache.tomcat.util.descriptor.web;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
public class JspPropertyGroupDescriptorImpl implements JspPropertyGroupDescriptor {
    private final JspPropertyGroup jspPropertyGroup;
    public JspPropertyGroupDescriptorImpl ( final JspPropertyGroup jspPropertyGroup ) {
        this.jspPropertyGroup = jspPropertyGroup;
    }
    public String getBuffer() {
        return this.jspPropertyGroup.getBuffer();
    }
    public String getDefaultContentType() {
        return this.jspPropertyGroup.getDefaultContentType();
    }
    public String getDeferredSyntaxAllowedAsLiteral() {
        String result = null;
        if ( this.jspPropertyGroup.getDeferredSyntax() != null ) {
            result = this.jspPropertyGroup.getDeferredSyntax().toString();
        }
        return result;
    }
    public String getElIgnored() {
        String result = null;
        if ( this.jspPropertyGroup.getElIgnored() != null ) {
            result = this.jspPropertyGroup.getElIgnored().toString();
        }
        return result;
    }
    public String getErrorOnUndeclaredNamespace() {
        String result = null;
        if ( this.jspPropertyGroup.getErrorOnUndeclaredNamespace() != null ) {
            result = this.jspPropertyGroup.getErrorOnUndeclaredNamespace().toString();
        }
        return result;
    }
    public Collection<String> getIncludeCodas() {
        return new ArrayList<String> ( this.jspPropertyGroup.getIncludeCodas() );
    }
    public Collection<String> getIncludePreludes() {
        return new ArrayList<String> ( this.jspPropertyGroup.getIncludePreludes() );
    }
    public String getIsXml() {
        String result = null;
        if ( this.jspPropertyGroup.getIsXml() != null ) {
            result = this.jspPropertyGroup.getIsXml().toString();
        }
        return result;
    }
    public String getPageEncoding() {
        return this.jspPropertyGroup.getPageEncoding();
    }
    public String getScriptingInvalid() {
        String result = null;
        if ( this.jspPropertyGroup.getScriptingInvalid() != null ) {
            result = this.jspPropertyGroup.getScriptingInvalid().toString();
        }
        return result;
    }
    public String getTrimDirectiveWhitespaces() {
        String result = null;
        if ( this.jspPropertyGroup.getTrimWhitespace() != null ) {
            result = this.jspPropertyGroup.getTrimWhitespace().toString();
        }
        return result;
    }
    public Collection<String> getUrlPatterns() {
        return new ArrayList<String> ( this.jspPropertyGroup.getUrlPatterns() );
    }
}
