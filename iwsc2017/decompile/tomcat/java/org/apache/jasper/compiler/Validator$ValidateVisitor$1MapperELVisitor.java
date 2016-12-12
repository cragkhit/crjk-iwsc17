package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.lang.reflect.Method;
class MapperELVisitor extends ELNode.Visitor {
    private ValidateFunctionMapper fmapper;
    MapperELVisitor ( final ValidateFunctionMapper fmapper ) {
        this.fmapper = fmapper;
    }
    @Override
    public void visit ( final ELNode.Function n ) throws JasperException {
        if ( n.getFunctionInfo() == null ) {
            return;
        }
        Class<?> c = null;
        Method method = null;
        try {
            c = ValidateVisitor.access$500 ( ValidateVisitor.this ).loadClass ( n.getFunctionInfo().getFunctionClass() );
        } catch ( ClassNotFoundException e ) {
            ValidateVisitor.access$300 ( ValidateVisitor.this ).jspError ( "jsp.error.function.classnotfound", n.getFunctionInfo().getFunctionClass(), n.getPrefix() + ':' + n.getName(), e.getMessage() );
        }
        final String[] paramTypes = n.getParameters();
        final int size = paramTypes.length;
        final Class<?>[] params = ( Class<?>[] ) new Class[size];
        int i = 0;
        try {
            for ( i = 0; i < size; ++i ) {
                params[i] = JspUtil.toClass ( paramTypes[i], ValidateVisitor.access$500 ( ValidateVisitor.this ) );
            }
            method = c.getDeclaredMethod ( n.getMethodName(), params );
        } catch ( ClassNotFoundException e2 ) {
            ValidateVisitor.access$300 ( ValidateVisitor.this ).jspError ( "jsp.error.signature.classnotfound", paramTypes[i], n.getPrefix() + ':' + n.getName(), e2.getMessage() );
        } catch ( NoSuchMethodException e3 ) {
            ValidateVisitor.access$300 ( ValidateVisitor.this ).jspError ( "jsp.error.noFunctionMethod", n.getMethodName(), n.getName(), c.getName() );
        }
        this.fmapper.mapFunction ( n.getPrefix(), n.getName(), method );
    }
}
