package org.apache.catalina.ssi;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;
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
                ExpressionParseTree.access$700 ( ExpressionParseTree.this ).log ( "Invalid expression: " + expr, pse );
                return 0;
            }
        }
        return val1.compareTo ( val2 );
    }
}
