package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
class Fvisitor extends ELNode.Visitor {
    private final List<ELNode.Function> funcs;
    private final Set<String> keySet;
    Fvisitor() {
        this.funcs = new ArrayList<ELNode.Function>();
        this.keySet = new HashSet<String>();
    }
    @Override
    public void visit ( final ELNode.Function n ) throws JasperException {
        final String key = n.getPrefix() + ":" + n.getName();
        if ( this.keySet.add ( key ) ) {
            this.funcs.add ( n );
        }
    }
}
