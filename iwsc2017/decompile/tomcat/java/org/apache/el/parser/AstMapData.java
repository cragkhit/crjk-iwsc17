package org.apache.el.parser;
import javax.el.ELException;
import java.util.Map;
import java.util.HashMap;
import org.apache.el.lang.EvaluationContext;
public class AstMapData extends SimpleNode {
    public AstMapData ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Map<Object, Object> result = new HashMap<Object, Object>();
        if ( this.children != null ) {
            for ( final Node child : this.children ) {
                final AstMapEntry mapEntry = ( AstMapEntry ) child;
                final Object key = mapEntry.children[0].getValue ( ctx );
                final Object value = mapEntry.children[1].getValue ( ctx );
                result.put ( key, value );
            }
        }
        return result;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return Map.class;
    }
}
