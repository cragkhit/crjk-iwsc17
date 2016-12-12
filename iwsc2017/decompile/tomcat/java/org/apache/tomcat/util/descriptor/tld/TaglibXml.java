package org.apache.tomcat.util.descriptor.tld;
import java.util.ArrayList;
import javax.servlet.jsp.tagext.FunctionInfo;
import java.util.List;
public class TaglibXml {
    private String tlibVersion;
    private String jspVersion;
    private String shortName;
    private String uri;
    private String info;
    private ValidatorXml validator;
    private final List<TagXml> tags;
    private final List<TagFileXml> tagFiles;
    private final List<String> listeners;
    private final List<FunctionInfo> functions;
    public TaglibXml() {
        this.tags = new ArrayList<TagXml>();
        this.tagFiles = new ArrayList<TagFileXml>();
        this.listeners = new ArrayList<String>();
        this.functions = new ArrayList<FunctionInfo>();
    }
    public String getTlibVersion() {
        return this.tlibVersion;
    }
    public void setTlibVersion ( final String tlibVersion ) {
        this.tlibVersion = tlibVersion;
    }
    public String getJspVersion() {
        return this.jspVersion;
    }
    public void setJspVersion ( final String jspVersion ) {
        this.jspVersion = jspVersion;
    }
    public String getShortName() {
        return this.shortName;
    }
    public void setShortName ( final String shortName ) {
        this.shortName = shortName;
    }
    public String getUri() {
        return this.uri;
    }
    public void setUri ( final String uri ) {
        this.uri = uri;
    }
    public String getInfo() {
        return this.info;
    }
    public void setInfo ( final String info ) {
        this.info = info;
    }
    public ValidatorXml getValidator() {
        return this.validator;
    }
    public void setValidator ( final ValidatorXml validator ) {
        this.validator = validator;
    }
    public void addTag ( final TagXml tag ) {
        this.tags.add ( tag );
    }
    public List<TagXml> getTags() {
        return this.tags;
    }
    public void addTagFile ( final TagFileXml tag ) {
        this.tagFiles.add ( tag );
    }
    public List<TagFileXml> getTagFiles() {
        return this.tagFiles;
    }
    public void addListener ( final String listener ) {
        this.listeners.add ( listener );
    }
    public List<String> getListeners() {
        return this.listeners;
    }
    public void addFunction ( final String name, final String klass, final String signature ) {
        this.functions.add ( new FunctionInfo ( name, klass, signature ) );
    }
    public List<FunctionInfo> getFunctions() {
        return this.functions;
    }
}
