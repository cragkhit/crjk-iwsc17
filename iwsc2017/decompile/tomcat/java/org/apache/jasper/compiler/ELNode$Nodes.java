package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
public static class Nodes {
    private String mapName;
    private final List<ELNode> list;
    public Nodes() {
        this.mapName = null;
        this.list = new ArrayList<ELNode>();
    }
    public void add ( final ELNode en ) {
        this.list.add ( en );
    }
    public void visit ( final Visitor v ) throws JasperException {
        for ( final ELNode n : this.list ) {
            n.accept ( v );
        }
    }
    public Iterator<ELNode> iterator() {
        return this.list.iterator();
    }
    public boolean isEmpty() {
        return this.list.size() == 0;
    }
    public boolean containsEL() {
        for ( final ELNode n : this.list ) {
            if ( n instanceof Root ) {
                return true;
            }
        }
        return false;
    }
    public void setMapName ( final String name ) {
        this.mapName = name;
    }
    public String getMapName() {
        return this.mapName;
    }
}
