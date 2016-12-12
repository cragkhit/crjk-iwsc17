package javax.servlet.jsp.tagext;
public class FunctionInfo {
    private final String name;
    private final String functionClass;
    private final String functionSignature;
    public FunctionInfo ( final String name, final String klass, final String signature ) {
        this.name = name;
        this.functionClass = klass;
        this.functionSignature = signature;
    }
    public String getName() {
        return this.name;
    }
    public String getFunctionClass() {
        return this.functionClass;
    }
    public String getFunctionSignature() {
        return this.functionSignature;
    }
}
