package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
public static class Nodes {
    private final List<Node> list;
    private Root root;
    private boolean generatedInBuffer;
    public Nodes() {
        this.list = new Vector<Node>();
    }
    public Nodes ( final Root root ) {
        this.root = root;
        ( this.list = new Vector<Node>() ).add ( root );
    }
    public void add ( final Node n ) {
        this.list.add ( n );
        this.root = null;
    }
    public void remove ( final Node n ) {
        this.list.remove ( n );
    }
    public void visit ( final Visitor v ) throws JasperException {
        for ( final Node n : this.list ) {
            n.accept ( v );
        }
    }
    public int size() {
        return this.list.size();
    }
    public Node getNode ( final int index ) {
        Node n = null;
        try {
            n = this.list.get ( index );
        } catch ( ArrayIndexOutOfBoundsException ex ) {}
        return n;
    }
    public Root getRoot() {
        return this.root;
    }
    public boolean isGeneratedInBuffer() {
        return this.generatedInBuffer;
    }
    public void setGeneratedInBuffer ( final boolean g ) {
        this.generatedInBuffer = g;
    }
}
