package javax.servlet.jsp.tagext;
public class TagVariableInfo {
    public TagVariableInfo ( String nameGiven, String nameFromAttribute,
                             String className, boolean declare, int scope ) {
        this.nameGiven = nameGiven;
        this.nameFromAttribute = nameFromAttribute;
        this.className = className;
        this.declare = declare;
        this.scope = scope;
    }
    public String getNameGiven() {
        return nameGiven;
    }
    public String getNameFromAttribute() {
        return nameFromAttribute;
    }
    public String getClassName() {
        return className;
    }
    public boolean getDeclare() {
        return declare;
    }
    public int getScope() {
        return scope;
    }
    private final String nameGiven;
    private final String nameFromAttribute;
    private final String className;
    private final boolean declare;
    private final int scope;
}
