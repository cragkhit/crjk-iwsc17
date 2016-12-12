package org.apache.el.parser;
import org.apache.el.util.MessageFactory;
import org.apache.el.util.Validation;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstDotSuffix extends SimpleNode {
    public AstDotSuffix ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        return this.image;
    }
    @Override
    public void setImage ( final String image ) {
        if ( !Validation.isIdentifier ( image ) ) {
            throw new ELException ( MessageFactory.get ( "error.identifier.notjava", image ) );
        }
        this.image = image;
    }
}
