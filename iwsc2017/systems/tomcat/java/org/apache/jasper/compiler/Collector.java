package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
class Collector {
    private static class CollectVisitor extends Node.Visitor {
        private boolean scriptingElementSeen = false;
        private boolean usebeanSeen = false;
        private boolean includeActionSeen = false;
        private boolean paramActionSeen = false;
        private boolean setPropertySeen = false;
        private boolean hasScriptingVars = false;
        @Override
        public void visit ( Node.ParamAction n ) throws JasperException {
            if ( n.getValue().isExpression() ) {
                scriptingElementSeen = true;
            }
            paramActionSeen = true;
        }
        @Override
        public void visit ( Node.IncludeAction n ) throws JasperException {
            if ( n.getPage().isExpression() ) {
                scriptingElementSeen = true;
            }
            includeActionSeen = true;
            visitBody ( n );
        }
        @Override
        public void visit ( Node.ForwardAction n ) throws JasperException {
            if ( n.getPage().isExpression() ) {
                scriptingElementSeen = true;
            }
            visitBody ( n );
        }
        @Override
        public void visit ( Node.SetProperty n ) throws JasperException {
            if ( n.getValue() != null && n.getValue().isExpression() ) {
                scriptingElementSeen = true;
            }
            setPropertySeen = true;
        }
        @Override
        public void visit ( Node.UseBean n ) throws JasperException {
            if ( n.getBeanName() != null && n.getBeanName().isExpression() ) {
                scriptingElementSeen = true;
            }
            usebeanSeen = true;
            visitBody ( n );
        }
        @Override
        public void visit ( Node.PlugIn n ) throws JasperException {
            if ( n.getHeight() != null && n.getHeight().isExpression() ) {
                scriptingElementSeen = true;
            }
            if ( n.getWidth() != null && n.getWidth().isExpression() ) {
                scriptingElementSeen = true;
            }
            visitBody ( n );
        }
        @Override
        public void visit ( Node.CustomTag n ) throws JasperException {
            checkSeen ( n.getChildInfo(), n );
        }
        private void checkSeen ( Node.ChildInfo ci, Node n )
        throws JasperException {
            boolean scriptingElementSeenSave = scriptingElementSeen;
            scriptingElementSeen = false;
            boolean usebeanSeenSave = usebeanSeen;
            usebeanSeen = false;
            boolean includeActionSeenSave = includeActionSeen;
            includeActionSeen = false;
            boolean paramActionSeenSave = paramActionSeen;
            paramActionSeen = false;
            boolean setPropertySeenSave = setPropertySeen;
            setPropertySeen = false;
            boolean hasScriptingVarsSave = hasScriptingVars;
            hasScriptingVars = false;
            if ( n instanceof Node.CustomTag ) {
                Node.CustomTag ct = ( Node.CustomTag ) n;
                Node.JspAttribute[] attrs = ct.getJspAttributes();
                for ( int i = 0; attrs != null && i < attrs.length; i++ ) {
                    if ( attrs[i].isExpression() ) {
                        scriptingElementSeen = true;
                        break;
                    }
                }
            }
            visitBody ( n );
            if ( ( n instanceof Node.CustomTag ) && !hasScriptingVars ) {
                Node.CustomTag ct = ( Node.CustomTag ) n;
                hasScriptingVars = ct.getVariableInfos().length > 0 ||
                                   ct.getTagVariableInfos().length > 0;
            }
            ci.setScriptless ( ! scriptingElementSeen );
            ci.setHasUseBean ( usebeanSeen );
            ci.setHasIncludeAction ( includeActionSeen );
            ci.setHasParamAction ( paramActionSeen );
            ci.setHasSetProperty ( setPropertySeen );
            ci.setHasScriptingVars ( hasScriptingVars );
            scriptingElementSeen = scriptingElementSeen || scriptingElementSeenSave;
            usebeanSeen = usebeanSeen || usebeanSeenSave;
            setPropertySeen = setPropertySeen || setPropertySeenSave;
            includeActionSeen = includeActionSeen || includeActionSeenSave;
            paramActionSeen = paramActionSeen || paramActionSeenSave;
            hasScriptingVars = hasScriptingVars || hasScriptingVarsSave;
        }
        @Override
        public void visit ( Node.JspElement n ) throws JasperException {
            if ( n.getNameAttribute().isExpression() ) {
                scriptingElementSeen = true;
            }
            Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; i < attrs.length; i++ ) {
                if ( attrs[i].isExpression() ) {
                    scriptingElementSeen = true;
                    break;
                }
            }
            visitBody ( n );
        }
        @Override
        public void visit ( Node.JspBody n ) throws JasperException {
            checkSeen ( n.getChildInfo(), n );
        }
        @Override
        public void visit ( Node.NamedAttribute n ) throws JasperException {
            checkSeen ( n.getChildInfo(), n );
        }
        @Override
        public void visit ( Node.Declaration n ) throws JasperException {
            scriptingElementSeen = true;
        }
        @Override
        public void visit ( Node.Expression n ) throws JasperException {
            scriptingElementSeen = true;
        }
        @Override
        public void visit ( Node.Scriptlet n ) throws JasperException {
            scriptingElementSeen = true;
        }
        private void updatePageInfo ( PageInfo pageInfo ) {
            pageInfo.setScriptless ( ! scriptingElementSeen );
        }
    }
    public static void collect ( Compiler compiler, Node.Nodes page )
    throws JasperException {
        CollectVisitor collectVisitor = new CollectVisitor();
        page.visit ( collectVisitor );
        collectVisitor.updatePageInfo ( compiler.getPageInfo() );
    }
}