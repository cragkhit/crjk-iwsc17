package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
private static class NodeVisitor extends Node.Visitor {
    private final TagPluginManager manager;
    private final PageInfo pageInfo;
    public NodeVisitor ( final TagPluginManager manager, final PageInfo pageInfo ) {
        this.manager = manager;
        this.pageInfo = pageInfo;
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        TagPluginManager.access$000 ( this.manager, n, this.pageInfo );
        this.visitBody ( n );
    }
}
