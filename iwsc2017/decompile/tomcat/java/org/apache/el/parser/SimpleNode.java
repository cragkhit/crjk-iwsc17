package org.apache.el.parser;
import javax.el.ValueReference;
import java.util.Arrays;
import javax.el.MethodInfo;
import javax.el.PropertyNotWritableException;
import org.apache.el.util.MessageFactory;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
import org.apache.el.lang.ELSupport;
public abstract class SimpleNode extends ELSupport implements Node {
    protected Node parent;
    protected Node[] children;
    protected final int id;
    protected String image;
    public SimpleNode ( final int i ) {
        this.id = i;
    }
    @Override
    public void jjtOpen() {
    }
    @Override
    public void jjtClose() {
    }
    @Override
    public void jjtSetParent ( final Node n ) {
        this.parent = n;
    }
    @Override
    public Node jjtGetParent() {
        return this.parent;
    }
    @Override
    public void jjtAddChild ( final Node n, final int i ) {
        if ( this.children == null ) {
            this.children = new Node[i + 1];
        } else if ( i >= this.children.length ) {
            final Node[] c = new Node[i + 1];
            System.arraycopy ( this.children, 0, c, 0, this.children.length );
            this.children = c;
        }
        this.children[i] = n;
    }
    @Override
    public Node jjtGetChild ( final int i ) {
        return this.children[i];
    }
    @Override
    public int jjtGetNumChildren() {
        return ( this.children == null ) ? 0 : this.children.length;
    }
    @Override
    public String toString() {
        if ( this.image != null ) {
            return ELParserTreeConstants.jjtNodeName[this.id] + "[" + this.image + "]";
        }
        return ELParserTreeConstants.jjtNodeName[this.id];
    }
    @Override
    public String getImage() {
        return this.image;
    }
    public void setImage ( final String image ) {
        this.image = image;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        throw new UnsupportedOperationException();
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isReadOnly ( final EvaluationContext ctx ) throws ELException {
        return true;
    }
    @Override
    public void setValue ( final EvaluationContext ctx, final Object value ) throws ELException {
        throw new PropertyNotWritableException ( MessageFactory.get ( "error.syntax.set" ) );
    }
    @Override
    public void accept ( final NodeVisitor visitor ) throws Exception {
        visitor.visit ( this );
        if ( this.children != null && this.children.length > 0 ) {
            for ( int i = 0; i < this.children.length; ++i ) {
                this.children[i].accept ( visitor );
            }
        }
    }
    @Override
    public Object invoke ( final EvaluationContext ctx, final Class<?>[] paramTypes, final Object[] paramValues ) throws ELException {
        throw new UnsupportedOperationException();
    }
    @Override
    public MethodInfo getMethodInfo ( final EvaluationContext ctx, final Class<?>[] paramTypes ) throws ELException {
        throw new UnsupportedOperationException();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + Arrays.hashCode ( this.children );
        result = 31 * result + this.id;
        result = 31 * result + ( ( this.image == null ) ? 0 : this.image.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof SimpleNode ) ) {
            return false;
        }
        final SimpleNode other = ( SimpleNode ) obj;
        if ( !Arrays.equals ( this.children, other.children ) ) {
            return false;
        }
        if ( this.id != other.id ) {
            return false;
        }
        if ( this.image == null ) {
            if ( other.image != null ) {
                return false;
            }
        } else if ( !this.image.equals ( other.image ) ) {
            return false;
        }
        return true;
    }
    @Override
    public ValueReference getValueReference ( final EvaluationContext ctx ) {
        return null;
    }
    @Override
    public boolean isParametersProvided() {
        return false;
    }
}
