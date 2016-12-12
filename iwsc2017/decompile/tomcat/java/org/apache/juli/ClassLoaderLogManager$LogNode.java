package org.apache.juli;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
protected static final class LogNode {
    Logger logger;
    final Map<String, LogNode> children;
    final LogNode parent;
    LogNode ( final LogNode parent, final Logger logger ) {
        this.children = new HashMap<String, LogNode>();
        this.parent = parent;
        this.logger = logger;
    }
    LogNode ( final LogNode parent ) {
        this ( parent, null );
    }
    LogNode findNode ( String name ) {
        LogNode currentNode = this;
        if ( this.logger.getName().equals ( name ) ) {
            return this;
        }
        while ( name != null ) {
            final int dotIndex = name.indexOf ( 46 );
            String nextName;
            if ( dotIndex < 0 ) {
                nextName = name;
                name = null;
            } else {
                nextName = name.substring ( 0, dotIndex );
                name = name.substring ( dotIndex + 1 );
            }
            LogNode childNode = currentNode.children.get ( nextName );
            if ( childNode == null ) {
                childNode = new LogNode ( currentNode );
                currentNode.children.put ( nextName, childNode );
            }
            currentNode = childNode;
        }
        return currentNode;
    }
    Logger findParentLogger() {
        Logger logger = null;
        for ( LogNode node = this.parent; node != null && logger == null; logger = node.logger, node = node.parent ) {}
        return logger;
    }
    void setParentLogger ( final Logger parent ) {
        for ( final LogNode childNode : this.children.values() ) {
            if ( childNode.logger == null ) {
                childNode.setParentLogger ( parent );
            } else {
                ClassLoaderLogManager.doSetParentLogger ( childNode.logger, parent );
            }
        }
    }
}
