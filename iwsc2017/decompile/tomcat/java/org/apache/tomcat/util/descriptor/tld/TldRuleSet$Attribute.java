package org.apache.tomcat.util.descriptor.tld;
import javax.servlet.jsp.tagext.TagAttributeInfo;
public static class Attribute {
    private final boolean allowShortNames;
    private String name;
    private boolean required;
    private String type;
    private boolean requestTime;
    private boolean fragment;
    private String description;
    private boolean deferredValue;
    private boolean deferredMethod;
    private String expectedTypeName;
    private String methodSignature;
    private Attribute ( final boolean allowShortNames ) {
        this.allowShortNames = allowShortNames;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public void setRequired ( final boolean required ) {
        this.required = required;
    }
    public void setType ( final String type ) {
        if ( this.allowShortNames ) {
            switch ( type ) {
            case "Boolean": {
                this.type = "java.lang.Boolean";
                break;
            }
            case "Character": {
                this.type = "java.lang.Character";
                break;
            }
            case "Byte": {
                this.type = "java.lang.Byte";
                break;
            }
            case "Short": {
                this.type = "java.lang.Short";
                break;
            }
            case "Integer": {
                this.type = "java.lang.Integer";
                break;
            }
            case "Long": {
                this.type = "java.lang.Long";
                break;
            }
            case "Float": {
                this.type = "java.lang.Float";
                break;
            }
            case "Double": {
                this.type = "java.lang.Double";
                break;
            }
            case "String": {
                this.type = "java.lang.String";
                break;
            }
            case "Object": {
                this.type = "java.lang.Object";
                break;
            }
            default: {
                this.type = type;
                break;
            }
            }
        } else {
            this.type = type;
        }
    }
    public void setRequestTime ( final boolean requestTime ) {
        this.requestTime = requestTime;
    }
    public void setFragment ( final boolean fragment ) {
        this.fragment = fragment;
    }
    public void setDescription ( final String description ) {
        this.description = description;
    }
    public void setDeferredValue() {
        this.deferredValue = true;
    }
    public void setDeferredMethod() {
        this.deferredMethod = true;
    }
    public void setExpectedTypeName ( final String expectedTypeName ) {
        this.expectedTypeName = expectedTypeName;
    }
    public void setMethodSignature ( final String methodSignature ) {
        this.methodSignature = methodSignature;
    }
    public TagAttributeInfo toTagAttributeInfo() {
        if ( this.fragment ) {
            this.type = "javax.servlet.jsp.tagext.JspFragment";
            this.requestTime = true;
        } else if ( this.deferredValue ) {
            this.type = "javax.el.ValueExpression";
            if ( this.expectedTypeName == null ) {
                this.expectedTypeName = "java.lang.Object";
            }
        } else if ( this.deferredMethod ) {
            this.type = "javax.el.MethodExpression";
            if ( this.methodSignature == null ) {
                this.methodSignature = "java.lang.Object method()";
            }
        }
        if ( !this.requestTime && this.type == null ) {
            this.type = "java.lang.String";
        }
        return new TagAttributeInfo ( this.name, this.required, this.type, this.requestTime, this.fragment, this.description, this.deferredValue, this.deferredMethod, this.expectedTypeName, this.methodSignature );
    }
}
