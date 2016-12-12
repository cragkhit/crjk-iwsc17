package org.apache.el.parser;
import javax.el.ELContext;
import javax.el.ValueExpression;
import java.util.List;
import javax.el.LambdaExpression;
import java.util.ArrayList;
import org.apache.el.ValueExpressionImpl;
import javax.el.ELException;
import org.apache.el.util.MessageFactory;
import org.apache.el.lang.EvaluationContext;
public class AstLambdaExpression extends SimpleNode {
    private NestedState nestedState;
    public AstLambdaExpression ( final int id ) {
        super ( id );
        this.nestedState = null;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final NestedState state = this.getNestedState();
        final int methodParameterSetCount = this.jjtGetNumChildren() - 2;
        if ( methodParameterSetCount > state.getNestingCount() ) {
            throw new ELException ( MessageFactory.get ( "error.lambda.tooManyMethodParameterSets" ) );
        }
        final AstLambdaParameters formalParametersNode = ( AstLambdaParameters ) this.children[0];
        final Node[] formalParamNodes = formalParametersNode.children;
        final ValueExpressionImpl ve = new ValueExpressionImpl ( "", this.children[1], ctx.getFunctionMapper(), ctx.getVariableMapper(), null );
        final List<String> formalParameters = new ArrayList<String>();
        if ( formalParamNodes != null ) {
            for ( final Node formalParamNode : formalParamNodes ) {
                formalParameters.add ( formalParamNode.getImage() );
            }
        }
        final LambdaExpression le = new LambdaExpression ( ( List ) formalParameters, ( ValueExpression ) ve );
        le.setELContext ( ( ELContext ) ctx );
        if ( this.jjtGetNumChildren() != 2 ) {
            int methodParameterIndex = 2;
            Object result = le.invoke ( ( ( AstMethodParameters ) this.children[methodParameterIndex] ).getParameters ( ctx ) );
            ++methodParameterIndex;
            while ( result instanceof LambdaExpression && methodParameterIndex < this.jjtGetNumChildren() ) {
                result = ( ( LambdaExpression ) result ).invoke ( ( ( AstMethodParameters ) this.children[methodParameterIndex] ).getParameters ( ctx ) );
                ++methodParameterIndex;
            }
            return result;
        }
        if ( state.getHasFormalParameters() ) {
            return le;
        }
        return le.invoke ( ( ELContext ) ctx, ( Object[] ) null );
    }
    private NestedState getNestedState() {
        if ( this.nestedState == null ) {
            this.setNestedState ( new NestedState() );
        }
        return this.nestedState;
    }
    private void setNestedState ( final NestedState nestedState ) {
        if ( this.nestedState != null ) {
            throw new IllegalStateException ( "nestedState may only be set once" );
        }
        ( this.nestedState = nestedState ).incrementNestingCount();
        if ( this.jjtGetNumChildren() > 1 ) {
            final Node firstChild = this.jjtGetChild ( 0 );
            if ( ! ( firstChild instanceof AstLambdaParameters ) ) {
                return;
            }
            if ( firstChild.jjtGetNumChildren() > 0 ) {
                nestedState.setHasFormalParameters();
            }
            final Node secondChild = this.jjtGetChild ( 1 );
            if ( secondChild instanceof AstLambdaExpression ) {
                ( ( AstLambdaExpression ) secondChild ).setNestedState ( nestedState );
            }
        }
    }
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        for ( final Node n : this.children ) {
            result.append ( n.toString() );
        }
        return result.toString();
    }
    private static class NestedState {
        private int nestingCount;
        private boolean hasFormalParameters;
        private NestedState() {
            this.nestingCount = 0;
            this.hasFormalParameters = false;
        }
        private void incrementNestingCount() {
            ++this.nestingCount;
        }
        private int getNestingCount() {
            return this.nestingCount;
        }
        private void setHasFormalParameters() {
            this.hasFormalParameters = true;
        }
        private boolean getHasFormalParameters() {
            return this.hasFormalParameters;
        }
    }
}
