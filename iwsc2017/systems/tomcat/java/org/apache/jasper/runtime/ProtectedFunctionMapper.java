package org.apache.jasper.runtime;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.servlet.jsp.el.FunctionMapper;
@SuppressWarnings ( "deprecation" )
public final class ProtectedFunctionMapper extends javax.el.FunctionMapper
    implements FunctionMapper {
    private HashMap<String, Method> fnmap = null;
    private Method theMethod = null;
    private ProtectedFunctionMapper() {
    }
    public static ProtectedFunctionMapper getInstance() {
        ProtectedFunctionMapper funcMapper = new ProtectedFunctionMapper();
        funcMapper.fnmap = new HashMap<>();
        return funcMapper;
    }
    public void mapFunction ( String fnQName, final Class<?> c,
                              final String methodName, final Class<?>[] args ) {
        if ( fnQName == null ) {
            return;
        }
        java.lang.reflect.Method method;
        try {
            method = c.getMethod ( methodName, args );
        } catch ( NoSuchMethodException e ) {
            throw new RuntimeException (
                "Invalid function mapping - no such method: "
                + e.getMessage() );
        }
        this.fnmap.put ( fnQName, method );
    }
    public static ProtectedFunctionMapper getMapForFunction ( String fnQName,
            final Class<?> c, final String methodName, final Class<?>[] args ) {
        java.lang.reflect.Method method = null;
        ProtectedFunctionMapper funcMapper = new ProtectedFunctionMapper();
        if ( fnQName != null ) {
            try {
                method = c.getMethod ( methodName, args );
            } catch ( NoSuchMethodException e ) {
                throw new RuntimeException (
                    "Invalid function mapping - no such method: "
                    + e.getMessage() );
            }
        }
        funcMapper.theMethod = method;
        return funcMapper;
    }
    @Override
    public Method resolveFunction ( String prefix, String localName ) {
        if ( this.fnmap != null ) {
            return this.fnmap.get ( prefix + ":" + localName );
        }
        return theMethod;
    }
}
