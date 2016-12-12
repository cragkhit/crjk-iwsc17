package org.apache.jasper.compiler;
import java.util.Collection;
public static class JspProperty {
    private final String isXml;
    private final String elIgnored;
    private final String scriptingInvalid;
    private final String pageEncoding;
    private final Collection<String> includePrelude;
    private final Collection<String> includeCoda;
    private final String deferedSyntaxAllowedAsLiteral;
    private final String trimDirectiveWhitespaces;
    private final String defaultContentType;
    private final String buffer;
    private final String errorOnUndeclaredNamespace;
    public JspProperty ( final String isXml, final String elIgnored, final String scriptingInvalid, final String pageEncoding, final Collection<String> includePrelude, final Collection<String> includeCoda, final String deferedSyntaxAllowedAsLiteral, final String trimDirectiveWhitespaces, final String defaultContentType, final String buffer, final String errorOnUndeclaredNamespace ) {
        this.isXml = isXml;
        this.elIgnored = elIgnored;
        this.scriptingInvalid = scriptingInvalid;
        this.pageEncoding = pageEncoding;
        this.includePrelude = includePrelude;
        this.includeCoda = includeCoda;
        this.deferedSyntaxAllowedAsLiteral = deferedSyntaxAllowedAsLiteral;
        this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
        this.defaultContentType = defaultContentType;
        this.buffer = buffer;
        this.errorOnUndeclaredNamespace = errorOnUndeclaredNamespace;
    }
    public String isXml() {
        return this.isXml;
    }
    public String isELIgnored() {
        return this.elIgnored;
    }
    public String isScriptingInvalid() {
        return this.scriptingInvalid;
    }
    public String getPageEncoding() {
        return this.pageEncoding;
    }
    public Collection<String> getIncludePrelude() {
        return this.includePrelude;
    }
    public Collection<String> getIncludeCoda() {
        return this.includeCoda;
    }
    public String isDeferedSyntaxAllowedAsLiteral() {
        return this.deferedSyntaxAllowedAsLiteral;
    }
    public String isTrimDirectiveWhitespaces() {
        return this.trimDirectiveWhitespaces;
    }
    public String getDefaultContentType() {
        return this.defaultContentType;
    }
    public String getBuffer() {
        return this.buffer;
    }
    public String isErrorOnUndeclaredNamespace() {
        return this.errorOnUndeclaredNamespace;
    }
}
