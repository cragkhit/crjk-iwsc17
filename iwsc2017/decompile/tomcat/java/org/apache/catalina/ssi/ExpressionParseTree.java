package org.apache.catalina.ssi;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;
import java.util.List;
import java.text.ParseException;
import java.util.LinkedList;
public class ExpressionParseTree {
    private final LinkedList<Node> nodeStack;
    private final LinkedList<OppNode> oppStack;
    private Node root;
    private final SSIMediator ssiMediator;
    private static final int PRECEDENCE_NOT = 5;
    private static final int PRECEDENCE_COMPARE = 4;
    private static final int PRECEDENCE_LOGICAL = 1;
    public ExpressionParseTree ( final String expr, final SSIMediator ssiMediator ) throws ParseException {
        this.nodeStack = new LinkedList<Node>();
        this.oppStack = new LinkedList<OppNode>();
        this.ssiMediator = ssiMediator;
        this.parseExpression ( expr );
    }
    public boolean evaluateTree() {
        return this.root.evaluate();
    }
    private void pushOpp ( final OppNode node ) {
        if ( node == null ) {
            this.oppStack.add ( 0, node );
            return;
        }
        while ( true ) {
            while ( this.oppStack.size() != 0 ) {
                final OppNode top = this.oppStack.get ( 0 );
                if ( top != null ) {
                    if ( top.getPrecedence() >= node.getPrecedence() ) {
                        this.oppStack.remove ( 0 );
                        top.popValues ( this.nodeStack );
                        this.nodeStack.add ( 0, top );
                        continue;
                    }
                }
                this.oppStack.add ( 0, node );
                return;
            }
            continue;
        }
    }
    private void resolveGroup() {
        OppNode top = null;
        while ( ( top = this.oppStack.remove ( 0 ) ) != null ) {
            top.popValues ( this.nodeStack );
            this.nodeStack.add ( 0, top );
        }
    }
    private void parseExpression ( final String expr ) throws ParseException {
        StringNode currStringNode = null;
        this.pushOpp ( null );
        final ExpressionTokenizer et = new ExpressionTokenizer ( expr );
        while ( et.hasMoreTokens() ) {
            final int token = et.nextToken();
            if ( token != 0 ) {
                currStringNode = null;
            }
            switch ( token ) {
            case 0: {
                if ( currStringNode == null ) {
                    currStringNode = new StringNode ( et.getTokenValue() );
                    this.nodeStack.add ( 0, currStringNode );
                    continue;
                }
                currStringNode.value.append ( " " );
                currStringNode.value.append ( et.getTokenValue() );
                continue;
            }
            case 1: {
                this.pushOpp ( new AndNode() );
                continue;
            }
            case 2: {
                this.pushOpp ( new OrNode() );
                continue;
            }
            case 3: {
                this.pushOpp ( new NotNode() );
                continue;
            }
            case 4: {
                this.pushOpp ( new EqualNode() );
                continue;
            }
            case 5: {
                this.pushOpp ( new NotNode() );
                this.oppStack.add ( 0, new EqualNode() );
                continue;
            }
            case 6: {
                this.resolveGroup();
                continue;
            }
            case 7: {
                this.pushOpp ( null );
                continue;
            }
            case 8: {
                this.pushOpp ( new NotNode() );
                this.oppStack.add ( 0, new LessThanNode() );
                continue;
            }
            case 9: {
                this.pushOpp ( new NotNode() );
                this.oppStack.add ( 0, new GreaterThanNode() );
                continue;
            }
            case 10: {
                this.pushOpp ( new GreaterThanNode() );
                continue;
            }
            case 11: {
                this.pushOpp ( new LessThanNode() );
                continue;
            }
            }
        }
        this.resolveGroup();
        if ( this.nodeStack.size() == 0 ) {
            throw new ParseException ( "No nodes created.", et.getIndex() );
        }
        if ( this.nodeStack.size() > 1 ) {
            throw new ParseException ( "Extra nodes created.", et.getIndex() );
        }
        if ( this.oppStack.size() != 0 ) {
            throw new ParseException ( "Unused opp nodes exist.", et.getIndex() );
        }
        this.root = this.nodeStack.get ( 0 );
    }
    private abstract class Node {
        public abstract boolean evaluate();
    }
    private class StringNode extends Node {
        StringBuilder value;
        String resolved;
        public StringNode ( final String value ) {
            this.resolved = null;
            this.value = new StringBuilder ( value );
        }
        public String getValue() {
            if ( this.resolved == null ) {
                this.resolved = ExpressionParseTree.this.ssiMediator.substituteVariables ( this.value.toString() );
            }
            return this.resolved;
        }
        @Override
        public boolean evaluate() {
            return this.getValue().length() != 0;
        }
        @Override
        public String toString() {
            return this.value.toString();
        }
    }
    private abstract class OppNode extends Node {
        Node left;
        Node right;
        public abstract int getPrecedence();
        public void popValues ( final List<Node> values ) {
            this.right = values.remove ( 0 );
            this.left = values.remove ( 0 );
        }
    }
    private final class NotNode extends OppNode {
        @Override
        public boolean evaluate() {
            return !this.left.evaluate();
        }
        @Override
        public int getPrecedence() {
            return 5;
        }
        @Override
        public void popValues ( final List<Node> values ) {
            this.left = values.remove ( 0 );
        }
        @Override
        public String toString() {
            return this.left + " NOT";
        }
    }
    private final class AndNode extends OppNode {
        @Override
        public boolean evaluate() {
            return this.left.evaluate() && this.right.evaluate();
        }
        @Override
        public int getPrecedence() {
            return 1;
        }
        @Override
        public String toString() {
            return this.left + " " + this.right + " AND";
        }
    }
    private final class OrNode extends OppNode {
        @Override
        public boolean evaluate() {
            return this.left.evaluate() || this.right.evaluate();
        }
        @Override
        public int getPrecedence() {
            return 1;
        }
        @Override
        public String toString() {
            return this.left + " " + this.right + " OR";
        }
    }
    private abstract class CompareNode extends OppNode {
        protected int compareBranches() {
            final String val1 = ( ( StringNode ) this.left ).getValue();
            final String val2 = ( ( StringNode ) this.right ).getValue();
            final int val2Len = val2.length();
            if ( val2Len > 1 && val2.charAt ( 0 ) == '/' && val2.charAt ( val2Len - 1 ) == '/' ) {
                final String expr = val2.substring ( 1, val2Len - 1 );
                try {
                    final Pattern pattern = Pattern.compile ( expr );
                    if ( pattern.matcher ( val1 ).find() ) {
                        return 0;
                    }
                    return -1;
                } catch ( PatternSyntaxException pse ) {
                    ExpressionParseTree.this.ssiMediator.log ( "Invalid expression: " + expr, pse );
                    return 0;
                }
            }
            return val1.compareTo ( val2 );
        }
    }
    private final class EqualNode extends CompareNode {
        @Override
        public boolean evaluate() {
            return this.compareBranches() == 0;
        }
        @Override
        public int getPrecedence() {
            return 4;
        }
        @Override
        public String toString() {
            return this.left + " " + this.right + " EQ";
        }
    }
    private final class GreaterThanNode extends CompareNode {
        @Override
        public boolean evaluate() {
            return this.compareBranches() > 0;
        }
        @Override
        public int getPrecedence() {
            return 4;
        }
        @Override
        public String toString() {
            return this.left + " " + this.right + " GT";
        }
    }
    private final class LessThanNode extends CompareNode {
        @Override
        public boolean evaluate() {
            return this.compareBranches() < 0;
        }
        @Override
        public int getPrecedence() {
            return 4;
        }
        @Override
        public String toString() {
            return this.left + " " + this.right + " LT";
        }
    }
}
