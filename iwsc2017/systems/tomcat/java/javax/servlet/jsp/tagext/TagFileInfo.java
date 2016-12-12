package javax.servlet.jsp.tagext;
public class TagFileInfo {
    public TagFileInfo ( String name, String path, TagInfo tagInfo ) {
        this.name = name;
        this.path = path;
        this.tagInfo = tagInfo;
    }
    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public TagInfo getTagInfo() {
        return tagInfo;
    }
    private final String name;
    private final String path;
    private final TagInfo tagInfo;
}
