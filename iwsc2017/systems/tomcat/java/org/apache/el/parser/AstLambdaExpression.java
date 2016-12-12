package org.apache.el.parser;
import java.util.ArrayList;
import java.util.List;
import javax.el.ELException;
import javax.el.LambdaExpression;
import org.apache.el.ValueExpressionImpl;
import org.apache.el.lang.EvaluationContext;
import org.apache.el.util.MessageFactory;
public class AstLambdaExpression extends SimpleNode {
    private NestedState nestedState = null;
    public AstLambdaExpression ( int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( EvaluationContext ctx ) throws ELException {
        NestedState state = getNestedState();
        int methodParameterSetCount = jjtGetNumChildren() - 2;
        if ( methodParameterSetCount > state.getNestingCount() ) {
            throw new ELException ( MessageFactory.get (
                                        "error.lambda.tooManyMethodParameterSets" ) );
        }
        AstLambdaParameters formalParametersNode =
            ( AstLambdaParameters ) children[0];
        Node[] formalParamNodes = formalParametersNode.children;
        ValueExpressionImpl ve = new ValueExpressionImpl ( "", children[1],
                ctx.getFunctionMapper(), ctx.getVariableMapper(), null );
        List<String> formalParameters = new ArrayList<>();
        if ( formalParamNodes != null ) {
            for ( Node formalParamNode : formalParamNodes ) {
                formalParameters.add ( formalParamNode.getImage() );
            }
        }
        LambdaExpression le = new LambdaExpression ( formalParameters, ve );
        le.setELContext ( ctx );
        if ( jjtGetNumChildren() == 2 ) {
            if ( state.getHasFormalParameters() ) {
                return le;
            } else {
                return le.invoke ( ctx, ( Object[] ) null );
            }
        }
        int methodParameterIndex = 2;
        Object result = le.invoke ( ( ( AstMethodParameters )
                                      children[methodParameterIndex] ).getParameters ( ctx ) );
        methodParameterIndex++;
        while ( result instanceof LambdaExpression &&
                methodParameterIndex < jjtGetNumChildren() ) {
            result = ( ( LambdaExpression ) result ).invoke ( ( ( AstMethodParameters )
                     children[methodParameterIndex] ).getParameters ( ctx ) );
            methodParameterIndex++;
        }
        return result;
    }
    private NestedState getNestedState() {
        if ( nestedState == null ) {
            setNestedState ( new NestedState() );
        }
        return nestedState;
    }
    private void setNestedState ( NestedState nestedState ) {
        if ( this.nestedState != null ) {
            throw new IllegalStateException ( "nestedState may only be set once" );
        }
        this.nestedState = nestedState;
        nestedState.incrementNestingCount();
        if ( jjtGetNumChildren() > 1 ) {
            Node firstChild = jjtGetChild ( 0 );
            if ( firstChild instanceof AstLambdaParameters ) {
                if ( firstChild.jjtGetNumChildren() > 0 ) {
                    nestedState.setHasFormalParameters();
                }
            } else {
                return;
            }
            Node secondChild = jjtGetChild ( 1 );
            if ( secondChild instanceof AstLambdaExpression ) {
                ( ( AstLambdaExpression ) secondChild ).setNestedState ( nestedState );
            }
        }
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for ( Node n : children ) {
            result.append ( n.toString() );
        }
        return result.toString();
    }
    private static class NestedState {
        private int nestingCount = 0;
        private boolean hasFormalParameters = false;
        private void incrementNestingCount() {
            nestingCount++;
        }
        private int getNestingCount() {
            return nestingCount;
        }
        private void setHasFormalParameters() {
            hasFormalParameters = true;
        }
        private boolean getHasFormalParameters() {
            return hasFormalParameters;
        }
    }
}
