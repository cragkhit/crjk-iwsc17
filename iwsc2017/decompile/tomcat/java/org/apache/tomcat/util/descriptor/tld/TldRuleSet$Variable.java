package org.apache.tomcat.util.descriptor.tld;
import javax.servlet.jsp.tagext.TagVariableInfo;
public static class Variable {
    private String nameGiven;
    private String nameFromAttribute;
    private String className;
    private boolean declare;
    private int scope;
    public Variable() {
        this.className = "java.lang.String";
        this.declare = true;
        this.scope = 0;
    }
    public void setNameGiven ( final String nameGiven ) {
        this.nameGiven = nameGiven;
    }
    public void setNameFromAttribute ( final String nameFromAttribute ) {
        this.nameFromAttribute = nameFromAttribute;
    }
    public void setClassName ( final String className ) {
        this.className = className;
    }
    public void setDeclare ( final boolean declare ) {
        this.declare = declare;
    }
    public void setScope ( final String scopeName ) {
        switch ( scopeName ) {
        case "NESTED": {
            this.scope = 0;
            break;
        }
        case "AT_BEGIN": {
            this.scope = 1;
            break;
        }
        case "AT_END": {
            this.scope = 2;
            break;
        }
        }
    }
    public TagVariableInfo toTagVariableInfo() {
        return new TagVariableInfo ( this.nameGiven, this.nameFromAttribute, this.className, this.declare, this.scope );
    }
}
