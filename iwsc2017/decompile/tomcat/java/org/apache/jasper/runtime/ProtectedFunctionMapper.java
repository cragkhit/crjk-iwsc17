package org.apache.jasper.runtime;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.el.FunctionMapper;
public final class ProtectedFunctionMapper extends FunctionMapper implements javax.servlet.jsp.el.FunctionMapper {
    private HashMap<String, Method> fnmap;
    private Method theMethod;
    private ProtectedFunctionMapper() {
        this.fnmap = null;
        this.theMethod = null;
    }
    public static ProtectedFunctionMapper getInstance() {
        final ProtectedFunctionMapper funcMapper = new ProtectedFunctionMapper();
        funcMapper.fnmap = new HashMap<String, Method>();
        return funcMapper;
    }
    public void mapFunction ( final String fnQName, final Class<?> c, final String methodName, final Class<?>[] args ) {
        if ( fnQName == null ) {
            return;
        }
        Method method;
        try {
            method = c.getMethod ( methodName, args );
        } catch ( NoSuchMethodException e ) {
            throw new RuntimeException ( "Invalid function mapping - no such method: " + e.getMessage() );
        }
        this.fnmap.put ( fnQName, method );
    }
    public static ProtectedFunctionMapper getMapForFunction ( final String fnQName, final Class<?> c, final String methodName, final Class<?>[] args ) {
        Method method = null;
        final ProtectedFunctionMapper funcMapper = new ProtectedFunctionMapper();
        if ( fnQName != null ) {
            try {
                method = c.getMethod ( methodName, args );
            } catch ( NoSuchMethodException e ) {
                throw new RuntimeException ( "Invalid function mapping - no such method: " + e.getMessage() );
            }
        }
        funcMapper.theMethod = method;
        return funcMapper;
    }
    public Method resolveFunction ( final String prefix, final String localName ) {
        if ( this.fnmap != null ) {
            return this.fnmap.get ( prefix + ":" + localName );
        }
        return this.theMethod;
    }
}
