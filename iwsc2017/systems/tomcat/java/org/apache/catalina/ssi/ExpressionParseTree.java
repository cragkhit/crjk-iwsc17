package org.apache.catalina.ssi;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
public class ExpressionParseTree {
    private final LinkedList<Node> nodeStack = new LinkedList<>();
    private final LinkedList<OppNode> oppStack = new LinkedList<>();
    private Node root;
    private final SSIMediator ssiMediator;
    public ExpressionParseTree ( String expr, SSIMediator ssiMediator )
    throws ParseException {
        this.ssiMediator = ssiMediator;
        parseExpression ( expr );
    }
    public boolean evaluateTree() {
        return root.evaluate();
    }
    private void pushOpp ( OppNode node ) {
        if ( node == null ) {
            oppStack.add ( 0, node );
            return;
        }
        while ( true ) {
            if ( oppStack.size() == 0 ) {
                break;
            }
            OppNode top = oppStack.get ( 0 );
            if ( top == null ) {
                break;
            }
            if ( top.getPrecedence() < node.getPrecedence() ) {
                break;
            }
            oppStack.remove ( 0 );
            top.popValues ( nodeStack );
            nodeStack.add ( 0, top );
        }
        oppStack.add ( 0, node );
    }
    private void resolveGroup() {
        OppNode top = null;
        while ( ( top = oppStack.remove ( 0 ) ) != null ) {
            top.popValues ( nodeStack );
            nodeStack.add ( 0, top );
        }
    }
    private void parseExpression ( String expr ) throws ParseException {
        StringNode currStringNode = null;
        pushOpp ( null );
        ExpressionTokenizer et = new ExpressionTokenizer ( expr );
        while ( et.hasMoreTokens() ) {
            int token = et.nextToken();
            if ( token != ExpressionTokenizer.TOKEN_STRING ) {
                currStringNode = null;
            }
            switch ( token ) {
            case ExpressionTokenizer.TOKEN_STRING :
                if ( currStringNode == null ) {
                    currStringNode = new StringNode ( et.getTokenValue() );
                    nodeStack.add ( 0, currStringNode );
                } else {
                    currStringNode.value.append ( " " );
                    currStringNode.value.append ( et.getTokenValue() );
                }
                break;
            case ExpressionTokenizer.TOKEN_AND :
                pushOpp ( new AndNode() );
                break;
            case ExpressionTokenizer.TOKEN_OR :
                pushOpp ( new OrNode() );
                break;
            case ExpressionTokenizer.TOKEN_NOT :
                pushOpp ( new NotNode() );
                break;
            case ExpressionTokenizer.TOKEN_EQ :
                pushOpp ( new EqualNode() );
                break;
            case ExpressionTokenizer.TOKEN_NOT_EQ :
                pushOpp ( new NotNode() );
                oppStack.add ( 0, new EqualNode() );
                break;
            case ExpressionTokenizer.TOKEN_RBRACE :
                resolveGroup();
                break;
            case ExpressionTokenizer.TOKEN_LBRACE :
                pushOpp ( null );
                break;
            case ExpressionTokenizer.TOKEN_GE :
                pushOpp ( new NotNode() );
                oppStack.add ( 0, new LessThanNode() );
                break;
            case ExpressionTokenizer.TOKEN_LE :
                pushOpp ( new NotNode() );
                oppStack.add ( 0, new GreaterThanNode() );
                break;
            case ExpressionTokenizer.TOKEN_GT :
                pushOpp ( new GreaterThanNode() );
                break;
            case ExpressionTokenizer.TOKEN_LT :
                pushOpp ( new LessThanNode() );
                break;
            case ExpressionTokenizer.TOKEN_END :
                break;
            }
        }
        resolveGroup();
        if ( nodeStack.size() == 0 ) {
            throw new ParseException ( "No nodes created.", et.getIndex() );
        }
        if ( nodeStack.size() > 1 ) {
            throw new ParseException ( "Extra nodes created.", et.getIndex() );
        }
        if ( oppStack.size() != 0 ) {
            throw new ParseException ( "Unused opp nodes exist.", et.getIndex() );
        }
        root = nodeStack.get ( 0 );
    }
    private abstract class Node {
        public abstract boolean evaluate();
    }
    private class StringNode extends Node {
        StringBuilder value;
        String resolved = null;
        public StringNode ( String value ) {
            this.value = new StringBuilder ( value );
        }
        public String getValue() {
            if ( resolved == null ) {
                resolved = ssiMediator.substituteVariables ( value.toString() );
            }
            return resolved;
        }
        @Override
        public boolean evaluate() {
            return ! ( getValue().length() == 0 );
        }
        @Override
        public String toString() {
            return value.toString();
        }
    }
    private static final int PRECEDENCE_NOT = 5;
    private static final int PRECEDENCE_COMPARE = 4;
    private static final int PRECEDENCE_LOGICAL = 1;
    private abstract class OppNode extends Node {
        Node left;
        Node right;
        public abstract int getPrecedence();
        public void popValues ( List<Node> values ) {
            right = values.remove ( 0 );
            left = values.remove ( 0 );
        }
    }
    private final class NotNode extends OppNode {
        @Override
        public boolean evaluate() {
            return !left.evaluate();
        }
        @Override
        public int getPrecedence() {
            return PRECEDENCE_NOT;
        }
        @Override
        public void popValues ( List<Node> values ) {
            left = values.remove ( 0 );
        }
        @Override
        public String toString() {
            return left + " NOT";
        }
    }
    private final class AndNode extends OppNode {
        @Override
        public boolean evaluate() {
            if ( !left.evaluate() ) {
                return false;
            }
            return right.evaluate();
        }
        @Override
        public int getPrecedence() {
            return PRECEDENCE_LOGICAL;
        }
        @Override
        public String toString() {
            return left + " " + right + " AND";
        }
    }
    private final class OrNode extends OppNode {
        @Override
        public boolean evaluate() {
            if ( left.evaluate() ) {
                return true;
            }
            return right.evaluate();
        }
        @Override
        public int getPrecedence() {
            return PRECEDENCE_LOGICAL;
        }
        @Override
        public String toString() {
            return left + " " + right + " OR";
        }
    }
    private abstract class CompareNode extends OppNode {
        protected int compareBranches() {
            String val1 = ( ( StringNode ) left ).getValue();
            String val2 = ( ( StringNode ) right ).getValue();
            int val2Len = val2.length();
            if ( val2Len > 1 && val2.charAt ( 0 ) == '/' &&
                    val2.charAt ( val2Len - 1 ) == '/' ) {
                String expr = val2.substring ( 1, val2Len - 1 );
                try {
                    Pattern pattern = Pattern.compile ( expr );
                    if ( pattern.matcher ( val1 ).find() ) {
                        return 0;
                    } else {
                        return -1;
                    }
                } catch ( PatternSyntaxException pse ) {
                    ssiMediator.log ( "Invalid expression: " + expr, pse );
                    return 0;
                }
            }
            return val1.compareTo ( val2 );
        }
    }
    private final class EqualNode extends CompareNode {
        @Override
        public boolean evaluate() {
            return ( compareBranches() == 0 );
        }
        @Override
        public int getPrecedence() {
            return PRECEDENCE_COMPARE;
        }
        @Override
        public String toString() {
            return left + " " + right + " EQ";
        }
    }
    private final class GreaterThanNode extends CompareNode {
        @Override
        public boolean evaluate() {
            return ( compareBranches() > 0 );
        }
        @Override
        public int getPrecedence() {
            return PRECEDENCE_COMPARE;
        }
        @Override
        public String toString() {
            return left + " " + right + " GT";
        }
    }
    private final class LessThanNode extends CompareNode {
        @Override
        public boolean evaluate() {
            return ( compareBranches() < 0 );
        }
        @Override
        public int getPrecedence() {
            return PRECEDENCE_COMPARE;
        }
        @Override
        public String toString() {
            return left + " " + right + " LT";
        }
    }
}
