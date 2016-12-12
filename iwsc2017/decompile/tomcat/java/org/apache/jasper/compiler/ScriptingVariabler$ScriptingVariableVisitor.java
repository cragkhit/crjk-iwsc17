package org.apache.jasper.compiler;
import java.util.List;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import java.util.ArrayList;
import org.apache.jasper.JasperException;
import java.util.HashMap;
import java.util.Map;
private static class ScriptingVariableVisitor extends Node.Visitor {
    private final ErrorDispatcher err;
    private final Map<String, Integer> scriptVars;
    public ScriptingVariableVisitor ( final ErrorDispatcher err ) {
        this.err = err;
        this.scriptVars = new HashMap<String, Integer>();
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        this.setScriptingVars ( n, 1 );
        this.setScriptingVars ( n, 0 );
        this.visitBody ( n );
        this.setScriptingVars ( n, 2 );
    }
    private void setScriptingVars ( final Node.CustomTag n, final int scope ) throws JasperException {
        final TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
        final VariableInfo[] varInfos = n.getVariableInfos();
        if ( tagVarInfos.length == 0 && varInfos.length == 0 ) {
            return;
        }
        final List<Object> vec = new ArrayList<Object>();
        Integer ownRange = null;
        final Node.CustomTag parent = n.getCustomTagParent();
        if ( scope == 1 || scope == 2 ) {
            if ( parent == null ) {
                ownRange = ScriptingVariabler.access$000();
            } else {
                ownRange = parent.getNumCount();
            }
        } else {
            ownRange = n.getNumCount();
        }
        if ( varInfos.length > 0 ) {
            for ( int i = 0; i < varInfos.length; ++i ) {
                if ( varInfos[i].getScope() == scope ) {
                    if ( varInfos[i].getDeclare() ) {
                        final String varName = varInfos[i].getVarName();
                        final Integer currentRange = this.scriptVars.get ( varName );
                        if ( currentRange == null || ownRange.compareTo ( currentRange ) > 0 ) {
                            this.scriptVars.put ( varName, ownRange );
                            vec.add ( varInfos[i] );
                        }
                    }
                }
            }
        } else {
            for ( int i = 0; i < tagVarInfos.length; ++i ) {
                if ( tagVarInfos[i].getScope() == scope ) {
                    if ( tagVarInfos[i].getDeclare() ) {
                        String varName = tagVarInfos[i].getNameGiven();
                        if ( varName == null ) {
                            varName = n.getTagData().getAttributeString ( tagVarInfos[i].getNameFromAttribute() );
                            if ( varName == null ) {
                                this.err.jspError ( n, "jsp.error.scripting.variable.missing_name", tagVarInfos[i].getNameFromAttribute() );
                            }
                        }
                        final Integer currentRange = this.scriptVars.get ( varName );
                        if ( currentRange == null || ownRange.compareTo ( currentRange ) > 0 ) {
                            this.scriptVars.put ( varName, ownRange );
                            vec.add ( tagVarInfos[i] );
                        }
                    }
                }
            }
        }
        n.setScriptingVars ( vec, scope );
    }
}
