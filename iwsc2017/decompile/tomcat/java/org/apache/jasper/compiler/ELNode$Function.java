package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import javax.servlet.jsp.tagext.FunctionInfo;
public static class Function extends ELNode {
    private final String prefix;
    private final String name;
    private final String originalText;
    private String uri;
    private FunctionInfo functionInfo;
    private String methodName;
    private String[] parameters;
    Function ( final String prefix, final String name, final String originalText ) {
        this.prefix = prefix;
        this.name = name;
        this.originalText = originalText;
    }
    @Override
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public String getPrefix() {
        return this.prefix;
    }
    public String getName() {
        return this.name;
    }
    public String getOriginalText() {
        return this.originalText;
    }
    public void setUri ( final String uri ) {
        this.uri = uri;
    }
    public String getUri() {
        return this.uri;
    }
    public void setFunctionInfo ( final FunctionInfo f ) {
        this.functionInfo = f;
    }
    public FunctionInfo getFunctionInfo() {
        return this.functionInfo;
    }
    public void setMethodName ( final String methodName ) {
        this.methodName = methodName;
    }
    public String getMethodName() {
        return this.methodName;
    }
    public void setParameters ( final String[] parameters ) {
        this.parameters = parameters;
    }
    public String[] getParameters() {
        return this.parameters;
    }
}
