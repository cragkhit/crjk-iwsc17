package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
private static class CollectVisitor extends Node.Visitor {
    private boolean scriptingElementSeen;
    private boolean usebeanSeen;
    private boolean includeActionSeen;
    private boolean paramActionSeen;
    private boolean setPropertySeen;
    private boolean hasScriptingVars;
    private CollectVisitor() {
        this.scriptingElementSeen = false;
        this.usebeanSeen = false;
        this.includeActionSeen = false;
        this.paramActionSeen = false;
        this.setPropertySeen = false;
        this.hasScriptingVars = false;
    }
    @Override
    public void visit ( final Node.ParamAction n ) throws JasperException {
        if ( n.getValue().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        this.paramActionSeen = true;
    }
    @Override
    public void visit ( final Node.IncludeAction n ) throws JasperException {
        if ( n.getPage().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        this.includeActionSeen = true;
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.ForwardAction n ) throws JasperException {
        if ( n.getPage().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.SetProperty n ) throws JasperException {
        if ( n.getValue() != null && n.getValue().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        this.setPropertySeen = true;
    }
    @Override
    public void visit ( final Node.UseBean n ) throws JasperException {
        if ( n.getBeanName() != null && n.getBeanName().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        this.usebeanSeen = true;
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.PlugIn n ) throws JasperException {
        if ( n.getHeight() != null && n.getHeight().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        if ( n.getWidth() != null && n.getWidth().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        this.checkSeen ( n.getChildInfo(), n );
    }
    private void checkSeen ( final Node.ChildInfo ci, final Node n ) throws JasperException {
        final boolean scriptingElementSeenSave = this.scriptingElementSeen;
        this.scriptingElementSeen = false;
        final boolean usebeanSeenSave = this.usebeanSeen;
        this.usebeanSeen = false;
        final boolean includeActionSeenSave = this.includeActionSeen;
        this.includeActionSeen = false;
        final boolean paramActionSeenSave = this.paramActionSeen;
        this.paramActionSeen = false;
        final boolean setPropertySeenSave = this.setPropertySeen;
        this.setPropertySeen = false;
        final boolean hasScriptingVarsSave = this.hasScriptingVars;
        this.hasScriptingVars = false;
        if ( n instanceof Node.CustomTag ) {
            final Node.CustomTag ct = ( Node.CustomTag ) n;
            final Node.JspAttribute[] attrs = ct.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
                if ( attrs[i].isExpression() ) {
                    this.scriptingElementSeen = true;
                    break;
                }
            }
        }
        this.visitBody ( n );
        if ( n instanceof Node.CustomTag && !this.hasScriptingVars ) {
            final Node.CustomTag ct = ( Node.CustomTag ) n;
            this.hasScriptingVars = ( ct.getVariableInfos().length > 0 || ct.getTagVariableInfos().length > 0 );
        }
        ci.setScriptless ( !this.scriptingElementSeen );
        ci.setHasUseBean ( this.usebeanSeen );
        ci.setHasIncludeAction ( this.includeActionSeen );
        ci.setHasParamAction ( this.paramActionSeen );
        ci.setHasSetProperty ( this.setPropertySeen );
        ci.setHasScriptingVars ( this.hasScriptingVars );
        this.scriptingElementSeen = ( this.scriptingElementSeen || scriptingElementSeenSave );
        this.usebeanSeen = ( this.usebeanSeen || usebeanSeenSave );
        this.setPropertySeen = ( this.setPropertySeen || setPropertySeenSave );
        this.includeActionSeen = ( this.includeActionSeen || includeActionSeenSave );
        this.paramActionSeen = ( this.paramActionSeen || paramActionSeenSave );
        this.hasScriptingVars = ( this.hasScriptingVars || hasScriptingVarsSave );
    }
    @Override
    public void visit ( final Node.JspElement n ) throws JasperException {
        if ( n.getNameAttribute().isExpression() ) {
            this.scriptingElementSeen = true;
        }
        final Node.JspAttribute[] attrs = n.getJspAttributes();
        for ( int i = 0; i < attrs.length; ++i ) {
            if ( attrs[i].isExpression() ) {
                this.scriptingElementSeen = true;
                break;
            }
        }
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.JspBody n ) throws JasperException {
        this.checkSeen ( n.getChildInfo(), n );
    }
    @Override
    public void visit ( final Node.NamedAttribute n ) throws JasperException {
        this.checkSeen ( n.getChildInfo(), n );
    }
    @Override
    public void visit ( final Node.Declaration n ) throws JasperException {
        this.scriptingElementSeen = true;
    }
    @Override
    public void visit ( final Node.Expression n ) throws JasperException {
        this.scriptingElementSeen = true;
    }
    @Override
    public void visit ( final Node.Scriptlet n ) throws JasperException {
        this.scriptingElementSeen = true;
    }
    private void updatePageInfo ( final PageInfo pageInfo ) {
        pageInfo.setScriptless ( !this.scriptingElementSeen );
    }
}
