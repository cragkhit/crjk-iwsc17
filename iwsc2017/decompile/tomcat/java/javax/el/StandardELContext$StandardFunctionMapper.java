package javax.el;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.Map;
private static class StandardFunctionMapper extends FunctionMapper {
    private final Map<String, Method> methods;
    public StandardFunctionMapper ( final Map<String, Method> initFunctionMap ) {
        this.methods = new HashMap<String, Method>();
        if ( initFunctionMap != null ) {
            this.methods.putAll ( initFunctionMap );
        }
    }
    @Override
    public Method resolveFunction ( final String prefix, final String localName ) {
        final String key = prefix + ':' + localName;
        return this.methods.get ( key );
    }
    @Override
    public void mapFunction ( final String prefix, final String localName, final Method method ) {
        final String key = prefix + ':' + localName;
        if ( method == null ) {
            this.methods.remove ( key );
        } else {
            this.methods.put ( key, method );
        }
    }
}
