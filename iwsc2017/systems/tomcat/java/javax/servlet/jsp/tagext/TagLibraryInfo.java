package javax.servlet.jsp.tagext;
public abstract class TagLibraryInfo {
    protected TagLibraryInfo ( String prefix, String uri ) {
        this.prefix = prefix;
        this.uri = uri;
    }
    public String getURI() {
        return uri;
    }
    public String getPrefixString() {
        return prefix;
    }
    public String getShortName() {
        return shortname;
    }
    public String getReliableURN() {
        return urn;
    }
    public String getInfoString() {
        return info;
    }
    public String getRequiredVersion() {
        return jspversion;
    }
    public TagInfo[] getTags() {
        return tags;
    }
    public TagFileInfo[] getTagFiles() {
        return tagFiles;
    }
    public TagInfo getTag ( String shortname ) {
        TagInfo tags[] = getTags();
        if ( tags == null || tags.length == 0 || shortname == null ) {
            return null;
        }
        for ( int i = 0; i < tags.length; i++ ) {
            if ( shortname.equals ( tags[i].getTagName() ) ) {
                return tags[i];
            }
        }
        return null;
    }
    public TagFileInfo getTagFile ( String shortname ) {
        TagFileInfo tagFiles[] = getTagFiles();
        if ( tagFiles == null || tagFiles.length == 0 ) {
            return null;
        }
        for ( int i = 0; i < tagFiles.length; i++ ) {
            if ( tagFiles[i].getName().equals ( shortname ) ) {
                return tagFiles[i];
            }
        }
        return null;
    }
    public FunctionInfo[] getFunctions() {
        return functions;
    }
    public FunctionInfo getFunction ( String name ) {
        if ( functions == null || functions.length == 0 ) {
            return null;
        }
        for ( int i = 0; i < functions.length; i++ ) {
            if ( functions[i].getName().equals ( name ) ) {
                return functions[i];
            }
        }
        return null;
    }
    public abstract javax.servlet.jsp.tagext.TagLibraryInfo[] getTagLibraryInfos();
    protected final String prefix;
    protected final String uri;
    protected TagInfo[] tags;
    protected TagFileInfo[] tagFiles;
    protected FunctionInfo[] functions;
    protected String tlibversion;
    protected String jspversion;
    protected String shortname;
    protected String urn;
    protected String info;
}
