package javax.servlet.jsp.tagext;
public class FunctionInfo {
    public FunctionInfo ( String name, String klass, String signature ) {
        this.name = name;
        this.functionClass = klass;
        this.functionSignature = signature;
    }
    public String getName() {
        return name;
    }
    public String getFunctionClass() {
        return functionClass;
    }
    public String getFunctionSignature() {
        return functionSignature;
    }
    private final String name;
    private final String functionClass;
    private final String functionSignature;
}
