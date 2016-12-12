package javax.servlet.jsp.tagext;
public class TagFileInfo {
    private final String name;
    private final String path;
    private final TagInfo tagInfo;
    public TagFileInfo ( final String name, final String path, final TagInfo tagInfo ) {
        this.name = name;
        this.path = path;
        this.tagInfo = tagInfo;
    }
    public String getName() {
        return this.name;
    }
    public String getPath() {
        return this.path;
    }
    public TagInfo getTagInfo() {
        return this.tagInfo;
    }
}
