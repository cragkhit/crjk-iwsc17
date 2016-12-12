package org.apache.jasper.compiler;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.jsp.tagext.FunctionInfo;
import org.apache.jasper.JasperException;
abstract class ELNode {
    public abstract void accept ( final Visitor p0 ) throws JasperException;
    public static class Root extends ELNode {
        private final Nodes expr;
        private final char type;
        Root ( final Nodes expr, final char type ) {
            this.expr = expr;
            this.type = type;
        }
        @Override
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public Nodes getExpression() {
            return this.expr;
        }
        public char getType() {
            return this.type;
        }
    }
    public static class Text extends ELNode {
        private final String text;
        Text ( final String text ) {
            this.text = text;
        }
        @Override
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getText() {
            return this.text;
        }
    }
    public static class ELText extends ELNode {
        private final String text;
        ELText ( final String text ) {
            this.text = text;
        }
        @Override
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getText() {
            return this.text;
        }
    }
    public static class Function extends ELNode {
        private final String prefix;
        private final String name;
        private final String originalText;
        private String uri;
        private FunctionInfo functionInfo;
        private String methodName;
        private String[] parameters;
        Function ( final String prefix, final String name, final String originalText ) {
            this.prefix = prefix;
            this.name = name;
            this.originalText = originalText;
        }
        @Override
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getPrefix() {
            return this.prefix;
        }
        public String getName() {
            return this.name;
        }
        public String getOriginalText() {
            return this.originalText;
        }
        public void setUri ( final String uri ) {
            this.uri = uri;
        }
        public String getUri() {
            return this.uri;
        }
        public void setFunctionInfo ( final FunctionInfo f ) {
            this.functionInfo = f;
        }
        public FunctionInfo getFunctionInfo() {
            return this.functionInfo;
        }
        public void setMethodName ( final String methodName ) {
            this.methodName = methodName;
        }
        public String getMethodName() {
            return this.methodName;
        }
        public void setParameters ( final String[] parameters ) {
            this.parameters = parameters;
        }
        public String[] getParameters() {
            return this.parameters;
        }
    }
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
    public static class Visitor {
        public void visit ( final Root n ) throws JasperException {
            n.getExpression().visit ( this );
        }
        public void visit ( final Function n ) throws JasperException {
        }
        public void visit ( final Text n ) throws JasperException {
        }
        public void visit ( final ELText n ) throws JasperException {
        }
    }
}
