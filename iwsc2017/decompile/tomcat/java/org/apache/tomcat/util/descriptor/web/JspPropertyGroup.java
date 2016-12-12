package org.apache.tomcat.util.descriptor.web;
import java.util.Set;
import org.apache.tomcat.util.buf.UDecoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collection;
public class JspPropertyGroup extends XmlEncodingBase {
    private Boolean deferredSyntax;
    private Boolean elIgnored;
    private final Collection<String> includeCodas;
    private final Collection<String> includePreludes;
    private Boolean isXml;
    private String pageEncoding;
    private Boolean scriptingInvalid;
    private Boolean trimWhitespace;
    private LinkedHashSet<String> urlPattern;
    private String defaultContentType;
    private String buffer;
    private Boolean errorOnUndeclaredNamespace;
    public JspPropertyGroup() {
        this.deferredSyntax = null;
        this.elIgnored = null;
        this.includeCodas = new ArrayList<String>();
        this.includePreludes = new ArrayList<String>();
        this.isXml = null;
        this.pageEncoding = null;
        this.scriptingInvalid = null;
        this.trimWhitespace = null;
        this.urlPattern = new LinkedHashSet<String>();
        this.defaultContentType = null;
        this.buffer = null;
        this.errorOnUndeclaredNamespace = null;
    }
    public void setDeferredSyntax ( final String deferredSyntax ) {
        this.deferredSyntax = Boolean.valueOf ( deferredSyntax );
    }
    public Boolean getDeferredSyntax() {
        return this.deferredSyntax;
    }
    public void setElIgnored ( final String elIgnored ) {
        this.elIgnored = Boolean.valueOf ( elIgnored );
    }
    public Boolean getElIgnored() {
        return this.elIgnored;
    }
    public void addIncludeCoda ( final String includeCoda ) {
        this.includeCodas.add ( includeCoda );
    }
    public Collection<String> getIncludeCodas() {
        return this.includeCodas;
    }
    public void addIncludePrelude ( final String includePrelude ) {
        this.includePreludes.add ( includePrelude );
    }
    public Collection<String> getIncludePreludes() {
        return this.includePreludes;
    }
    public void setIsXml ( final String isXml ) {
        this.isXml = Boolean.valueOf ( isXml );
    }
    public Boolean getIsXml() {
        return this.isXml;
    }
    public void setPageEncoding ( final String pageEncoding ) {
        this.pageEncoding = pageEncoding;
    }
    public String getPageEncoding() {
        return this.pageEncoding;
    }
    public void setScriptingInvalid ( final String scriptingInvalid ) {
        this.scriptingInvalid = Boolean.valueOf ( scriptingInvalid );
    }
    public Boolean getScriptingInvalid() {
        return this.scriptingInvalid;
    }
    public void setTrimWhitespace ( final String trimWhitespace ) {
        this.trimWhitespace = Boolean.valueOf ( trimWhitespace );
    }
    public Boolean getTrimWhitespace() {
        return this.trimWhitespace;
    }
    public void addUrlPattern ( final String urlPattern ) {
        this.addUrlPatternDecoded ( UDecoder.URLDecode ( urlPattern, this.getEncoding() ) );
    }
    public void addUrlPatternDecoded ( final String urlPattern ) {
        this.urlPattern.add ( urlPattern );
    }
    public Set<String> getUrlPatterns() {
        return this.urlPattern;
    }
    public void setDefaultContentType ( final String defaultContentType ) {
        this.defaultContentType = defaultContentType;
    }
    public String getDefaultContentType() {
        return this.defaultContentType;
    }
    public void setBuffer ( final String buffer ) {
        this.buffer = buffer;
    }
    public String getBuffer() {
        return this.buffer;
    }
    public void setErrorOnUndeclaredNamespace ( final String errorOnUndeclaredNamespace ) {
        this.errorOnUndeclaredNamespace = Boolean.valueOf ( errorOnUndeclaredNamespace );
    }
    public Boolean getErrorOnUndeclaredNamespace() {
        return this.errorOnUndeclaredNamespace;
    }
}
