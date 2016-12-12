package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import java.util.Vector;
class ScriptingVarVisitor extends Node.Visitor {
    private final Vector<String> vars;
    ScriptingVarVisitor() {
        this.vars = new Vector<String>();
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        if ( n.getCustomNestingLevel() > 0 ) {
            final TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
            final VariableInfo[] varInfos = n.getVariableInfos();
            if ( varInfos.length > 0 ) {
                for ( int i = 0; i < varInfos.length; ++i ) {
                    final String varName = varInfos[i].getVarName();
                    final String tmpVarName = "_jspx_" + varName + "_" + n.getCustomNestingLevel();
                    if ( !this.vars.contains ( tmpVarName ) ) {
                        this.vars.add ( tmpVarName );
                        Generator.access$000 ( Generator.this ).printin ( varInfos[i].getClassName() );
                        Generator.access$000 ( Generator.this ).print ( " " );
                        Generator.access$000 ( Generator.this ).print ( tmpVarName );
                        Generator.access$000 ( Generator.this ).print ( " = " );
                        Generator.access$000 ( Generator.this ).print ( null );
                        Generator.access$000 ( Generator.this ).println ( ";" );
                    }
                }
            } else {
                for ( int i = 0; i < tagVarInfos.length; ++i ) {
                    String varName = tagVarInfos[i].getNameGiven();
                    if ( varName == null ) {
                        varName = n.getTagData().getAttributeString ( tagVarInfos[i].getNameFromAttribute() );
                    } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                        continue;
                    }
                    final String tmpVarName = "_jspx_" + varName + "_" + n.getCustomNestingLevel();
                    if ( !this.vars.contains ( tmpVarName ) ) {
                        this.vars.add ( tmpVarName );
                        Generator.access$000 ( Generator.this ).printin ( tagVarInfos[i].getClassName() );
                        Generator.access$000 ( Generator.this ).print ( " " );
                        Generator.access$000 ( Generator.this ).print ( tmpVarName );
                        Generator.access$000 ( Generator.this ).print ( " = " );
                        Generator.access$000 ( Generator.this ).print ( null );
                        Generator.access$000 ( Generator.this ).println ( ";" );
                    }
                }
            }
        }
        this.visitBody ( n );
    }
}
