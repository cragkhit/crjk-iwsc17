package org.apache.jasper.compiler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.jsp.tagext.FunctionInfo;
import org.apache.jasper.JasperException;
abstract class ELNode {
    public abstract void accept ( Visitor v ) throws JasperException;
    public static class Root extends ELNode {
        private final ELNode.Nodes expr;
        private final char type;
        Root ( ELNode.Nodes expr, char type ) {
            this.expr = expr;
            this.type = type;
        }
        @Override
        public void accept ( Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public ELNode.Nodes getExpression() {
            return expr;
        }
        public char getType() {
            return type;
        }
    }
    public static class Text extends ELNode {
        private final String text;
        Text ( String text ) {
            this.text = text;
        }
        @Override
        public void accept ( Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getText() {
            return text;
        }
    }
    public static class ELText extends ELNode {
        private final String text;
        ELText ( String text ) {
            this.text = text;
        }
        @Override
        public void accept ( Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getText() {
            return text;
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
        Function ( String prefix, String name, String originalText ) {
            this.prefix = prefix;
            this.name = name;
            this.originalText = originalText;
        }
        @Override
        public void accept ( Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getPrefix() {
            return prefix;
        }
        public String getName() {
            return name;
        }
        public String getOriginalText() {
            return originalText;
        }
        public void setUri ( String uri ) {
            this.uri = uri;
        }
        public String getUri() {
            return uri;
        }
        public void setFunctionInfo ( FunctionInfo f ) {
            this.functionInfo = f;
        }
        public FunctionInfo getFunctionInfo() {
            return functionInfo;
        }
        public void setMethodName ( String methodName ) {
            this.methodName = methodName;
        }
        public String getMethodName() {
            return methodName;
        }
        public void setParameters ( String[] parameters ) {
            this.parameters = parameters;
        }
        public String[] getParameters() {
            return parameters;
        }
    }
    public static class Nodes {
        private String mapName = null;
        private final List<ELNode> list;
        public Nodes() {
            list = new ArrayList<>();
        }
        public void add ( ELNode en ) {
            list.add ( en );
        }
        public void visit ( Visitor v ) throws JasperException {
            Iterator<ELNode> iter = list.iterator();
            while ( iter.hasNext() ) {
                ELNode n = iter.next();
                n.accept ( v );
            }
        }
        public Iterator<ELNode> iterator() {
            return list.iterator();
        }
        public boolean isEmpty() {
            return list.size() == 0;
        }
        public boolean containsEL() {
            Iterator<ELNode> iter = list.iterator();
            while ( iter.hasNext() ) {
                ELNode n = iter.next();
                if ( n instanceof Root ) {
                    return true;
                }
            }
            return false;
        }
        public void setMapName ( String name ) {
            this.mapName = name;
        }
        public String getMapName() {
            return mapName;
        }
    }
    public static class Visitor {
        public void visit ( Root n ) throws JasperException {
            n.getExpression().visit ( this );
        }
        @SuppressWarnings ( "unused" )
        public void visit ( Function n ) throws JasperException {
        }
        @SuppressWarnings ( "unused" )
        public void visit ( Text n ) throws JasperException {
        }
        @SuppressWarnings ( "unused" )
        public void visit ( ELText n ) throws JasperException {
        }
    }
}
