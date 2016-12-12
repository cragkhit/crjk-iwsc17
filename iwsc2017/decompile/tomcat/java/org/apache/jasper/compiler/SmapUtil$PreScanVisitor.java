package org.apache.jasper.compiler;
import java.util.HashMap;
private static class PreScanVisitor extends Node.Visitor {
    HashMap<String, SmapStratum> map;
    private PreScanVisitor() {
        this.map = new HashMap<String, SmapStratum>();
    }
    public void doVisit ( final Node n ) {
        final String inner = n.getInnerClassName();
        if ( inner != null && !this.map.containsKey ( inner ) ) {
            this.map.put ( inner, new SmapStratum ( "JSP" ) );
        }
    }
    HashMap<String, SmapStratum> getMap() {
        return this.map;
    }
}
