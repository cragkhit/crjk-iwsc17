package javax.servlet.jsp.tagext;
public abstract class TagExtraInfo {
    public TagExtraInfo() {
    }
    public VariableInfo[] getVariableInfo ( TagData data ) {
        return ZERO_VARIABLE_INFO;
    }
    public boolean isValid ( TagData data ) {
        return true;
    }
    public ValidationMessage[] validate ( TagData data ) {
        ValidationMessage[] result = null;
        if ( !isValid ( data ) ) {
            result = new ValidationMessage[] {
                new ValidationMessage ( data.getId(), "isValid() == false" )
            };
        }
        return result;
    }
    public final void setTagInfo ( TagInfo tagInfo ) {
        this.tagInfo = tagInfo;
    }
    public final TagInfo getTagInfo() {
        return tagInfo;
    }
    private  TagInfo tagInfo;
    private static final VariableInfo[] ZERO_VARIABLE_INFO = { };
}
