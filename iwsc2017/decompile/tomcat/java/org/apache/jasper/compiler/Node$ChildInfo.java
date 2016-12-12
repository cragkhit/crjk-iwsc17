package org.apache.jasper.compiler;
public static class ChildInfo {
    private boolean scriptless;
    private boolean hasUseBean;
    private boolean hasIncludeAction;
    private boolean hasParamAction;
    private boolean hasSetProperty;
    private boolean hasScriptingVars;
    public void setScriptless ( final boolean s ) {
        this.scriptless = s;
    }
    public boolean isScriptless() {
        return this.scriptless;
    }
    public void setHasUseBean ( final boolean u ) {
        this.hasUseBean = u;
    }
    public boolean hasUseBean() {
        return this.hasUseBean;
    }
    public void setHasIncludeAction ( final boolean i ) {
        this.hasIncludeAction = i;
    }
    public boolean hasIncludeAction() {
        return this.hasIncludeAction;
    }
    public void setHasParamAction ( final boolean i ) {
        this.hasParamAction = i;
    }
    public boolean hasParamAction() {
        return this.hasParamAction;
    }
    public void setHasSetProperty ( final boolean s ) {
        this.hasSetProperty = s;
    }
    public boolean hasSetProperty() {
        return this.hasSetProperty;
    }
    public void setHasScriptingVars ( final boolean s ) {
        this.hasScriptingVars = s;
    }
    public boolean hasScriptingVars() {
        return this.hasScriptingVars;
    }
}
