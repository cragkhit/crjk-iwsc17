package javax.servlet.jsp.tagext;
public class VariableInfo {
    public static final int NESTED = 0;
    public static final int AT_BEGIN = 1;
    public static final int AT_END = 2;
    public VariableInfo ( String varName, String className, boolean declare,
                          int scope ) {
        this.varName = varName;
        this.className = className;
        this.declare = declare;
        this.scope = scope;
    }
    public String getVarName() {
        return varName;
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
    private final String varName;
    private final String className;
    private final boolean declare;
    private final int scope;
}
