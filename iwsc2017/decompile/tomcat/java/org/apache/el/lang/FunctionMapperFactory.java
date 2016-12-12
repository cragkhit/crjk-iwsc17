package org.apache.el.lang;
import java.lang.reflect.Method;
import javax.el.FunctionMapper;
public class FunctionMapperFactory extends FunctionMapper {
    protected FunctionMapperImpl memento;
    protected final FunctionMapper target;
    public FunctionMapperFactory ( final FunctionMapper mapper ) {
        this.memento = null;
        if ( mapper == null ) {
            throw new NullPointerException ( "FunctionMapper target cannot be null" );
        }
        this.target = mapper;
    }
    public Method resolveFunction ( final String prefix, final String localName ) {
        if ( this.memento == null ) {
            this.memento = new FunctionMapperImpl();
        }
        final Method m = this.target.resolveFunction ( prefix, localName );
        if ( m != null ) {
            this.memento.mapFunction ( prefix, localName, m );
        }
        return m;
    }
    public void mapFunction ( final String prefix, final String localName, final Method method ) {
        if ( this.memento == null ) {
            this.memento = new FunctionMapperImpl();
        }
        this.memento.mapFunction ( prefix, localName, method );
    }
    public FunctionMapper create() {
        return this.memento;
    }
}
