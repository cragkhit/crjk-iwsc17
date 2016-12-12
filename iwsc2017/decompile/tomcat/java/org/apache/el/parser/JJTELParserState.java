package org.apache.el.parser;
import java.util.ArrayList;
import java.util.List;
public class JJTELParserState {
    private List<Node> nodes;
    private List<Integer> marks;
    private int sp;
    private int mk;
    private boolean node_created;
    public JJTELParserState() {
        this.nodes = new ArrayList<Node>();
        this.marks = new ArrayList<Integer>();
        this.sp = 0;
        this.mk = 0;
    }
    public boolean nodeCreated() {
        return this.node_created;
    }
    public void reset() {
        this.nodes.clear();
        this.marks.clear();
        this.sp = 0;
        this.mk = 0;
    }
    public Node rootNode() {
        return this.nodes.get ( 0 );
    }
    public void pushNode ( final Node n ) {
        this.nodes.add ( n );
        ++this.sp;
    }
    public Node popNode() {
        final int sp = this.sp - 1;
        this.sp = sp;
        if ( sp < this.mk ) {
            this.mk = this.marks.remove ( this.marks.size() - 1 );
        }
        return this.nodes.remove ( this.nodes.size() - 1 );
    }
    public Node peekNode() {
        return this.nodes.get ( this.nodes.size() - 1 );
    }
    public int nodeArity() {
        return this.sp - this.mk;
    }
    public void clearNodeScope ( final Node n ) {
        while ( this.sp > this.mk ) {
            this.popNode();
        }
        this.mk = this.marks.remove ( this.marks.size() - 1 );
    }
    public void openNodeScope ( final Node n ) {
        this.marks.add ( this.mk );
        this.mk = this.sp;
        n.jjtOpen();
    }
    public void closeNodeScope ( final Node n, int num ) {
        this.mk = this.marks.remove ( this.marks.size() - 1 );
        while ( num-- > 0 ) {
            final Node c = this.popNode();
            c.jjtSetParent ( n );
            n.jjtAddChild ( c, num );
        }
        n.jjtClose();
        this.pushNode ( n );
        this.node_created = true;
    }
    public void closeNodeScope ( final Node n, final boolean condition ) {
        if ( condition ) {
            int a = this.nodeArity();
            this.mk = this.marks.remove ( this.marks.size() - 1 );
            while ( a-- > 0 ) {
                final Node c = this.popNode();
                c.jjtSetParent ( n );
                n.jjtAddChild ( c, a );
            }
            n.jjtClose();
            this.pushNode ( n );
            this.node_created = true;
        } else {
            this.mk = this.marks.remove ( this.marks.size() - 1 );
            this.node_created = false;
        }
    }
}
