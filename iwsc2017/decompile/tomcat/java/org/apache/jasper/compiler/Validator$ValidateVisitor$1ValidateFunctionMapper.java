package org.apache.jasper.compiler;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.el.FunctionMapper;
class ValidateFunctionMapper extends FunctionMapper {
    private HashMap<String, Method> fnmap;
    ValidateFunctionMapper() {
        this.fnmap = new HashMap<String, Method>();
    }
    public void mapFunction ( final String prefix, final String localName, final Method method ) {
        this.fnmap.put ( prefix + ":" + localName, method );
    }
    public Method resolveFunction ( final String prefix, final String localName ) {
        return this.fnmap.get ( prefix + ":" + localName );
    }
}
