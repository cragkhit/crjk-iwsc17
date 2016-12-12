package javax.servlet.jsp.tagext;
public abstract class TagExtraInfo {
    private TagInfo tagInfo;
    private static final VariableInfo[] ZERO_VARIABLE_INFO;
    public VariableInfo[] getVariableInfo ( final TagData data ) {
        return TagExtraInfo.ZERO_VARIABLE_INFO;
    }
    public boolean isValid ( final TagData data ) {
        return true;
    }
    public ValidationMessage[] validate ( final TagData data ) {
        ValidationMessage[] result = null;
        if ( !this.isValid ( data ) ) {
            result = new ValidationMessage[] { new ValidationMessage ( data.getId(), "isValid() == false" ) };
        }
        return result;
    }
    public final void setTagInfo ( final TagInfo tagInfo ) {
        this.tagInfo = tagInfo;
    }
    public final TagInfo getTagInfo() {
        return this.tagInfo;
    }
    static {
        ZERO_VARIABLE_INFO = new VariableInfo[0];
    }
}
