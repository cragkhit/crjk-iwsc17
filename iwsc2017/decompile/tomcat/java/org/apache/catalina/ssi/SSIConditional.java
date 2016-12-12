package org.apache.catalina.ssi;
import java.text.ParseException;
import java.io.PrintWriter;
public class SSIConditional implements SSICommand {
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) throws SSIStopProcessingException {
        final long lastModified = System.currentTimeMillis();
        final SSIConditionalState state = ssiMediator.getConditionalState();
        if ( "if".equalsIgnoreCase ( commandName ) ) {
            if ( state.processConditionalCommandsOnly ) {
                final SSIConditionalState ssiConditionalState = state;
                ++ssiConditionalState.nestingCount;
                return lastModified;
            }
            state.nestingCount = 0;
            if ( this.evaluateArguments ( paramNames, paramValues, ssiMediator ) ) {
                state.branchTaken = true;
            } else {
                state.processConditionalCommandsOnly = true;
                state.branchTaken = false;
            }
        } else if ( "elif".equalsIgnoreCase ( commandName ) ) {
            if ( state.nestingCount > 0 ) {
                return lastModified;
            }
            if ( state.branchTaken ) {
                state.processConditionalCommandsOnly = true;
                return lastModified;
            }
            if ( this.evaluateArguments ( paramNames, paramValues, ssiMediator ) ) {
                state.processConditionalCommandsOnly = false;
                state.branchTaken = true;
            } else {
                state.processConditionalCommandsOnly = true;
                state.branchTaken = false;
            }
        } else if ( "else".equalsIgnoreCase ( commandName ) ) {
            if ( state.nestingCount > 0 ) {
                return lastModified;
            }
            state.processConditionalCommandsOnly = state.branchTaken;
            state.branchTaken = true;
        } else {
            if ( !"endif".equalsIgnoreCase ( commandName ) ) {
                throw new SSIStopProcessingException();
            }
            if ( state.nestingCount > 0 ) {
                final SSIConditionalState ssiConditionalState2 = state;
                --ssiConditionalState2.nestingCount;
                return lastModified;
            }
            state.processConditionalCommandsOnly = false;
            state.branchTaken = true;
        }
        return lastModified;
    }
    private boolean evaluateArguments ( final String[] names, final String[] values, final SSIMediator ssiMediator ) throws SSIStopProcessingException {
        final String expr = this.getExpression ( names, values );
        if ( expr == null ) {
            throw new SSIStopProcessingException();
        }
        try {
            final ExpressionParseTree tree = new ExpressionParseTree ( expr, ssiMediator );
            return tree.evaluateTree();
        } catch ( ParseException e ) {
            throw new SSIStopProcessingException();
        }
    }
    private String getExpression ( final String[] paramNames, final String[] paramValues ) {
        if ( "expr".equalsIgnoreCase ( paramNames[0] ) ) {
            return paramValues[0];
        }
        return null;
    }
}
