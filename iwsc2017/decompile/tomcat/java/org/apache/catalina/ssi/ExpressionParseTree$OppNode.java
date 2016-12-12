package org.apache.catalina.ssi;
import java.util.List;
private abstract class OppNode extends Node {
    Node left;
    Node right;
    public abstract int getPrecedence();
    public void popValues ( final List<Node> values ) {
        this.right = values.remove ( 0 );
        this.left = values.remove ( 0 );
    }
}
