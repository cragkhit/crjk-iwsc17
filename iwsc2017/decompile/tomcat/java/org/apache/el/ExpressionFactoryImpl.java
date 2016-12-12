package org.apache.el;
import org.apache.el.stream.StreamELResolverImpl;
import javax.el.ELResolver;
import org.apache.el.util.MessageFactory;
import javax.el.ValueExpression;
import org.apache.el.lang.ExpressionBuilder;
import javax.el.MethodExpression;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import javax.el.ExpressionFactory;
public class ExpressionFactoryImpl extends ExpressionFactory {
    public Object coerceToType ( final Object obj, final Class<?> type ) {
        return ELSupport.coerceToType ( null, obj, type );
    }
    public MethodExpression createMethodExpression ( final ELContext context, final String expression, final Class<?> expectedReturnType, final Class<?>[] expectedParamTypes ) {
        final ExpressionBuilder builder = new ExpressionBuilder ( expression, context );
        return builder.createMethodExpression ( expectedReturnType, expectedParamTypes );
    }
    public ValueExpression createValueExpression ( final ELContext context, final String expression, final Class<?> expectedType ) {
        if ( expectedType == null ) {
            throw new NullPointerException ( MessageFactory.get ( "error.value.expectedType" ) );
        }
        final ExpressionBuilder builder = new ExpressionBuilder ( expression, context );
        return builder.createValueExpression ( expectedType );
    }
    public ValueExpression createValueExpression ( final Object instance, final Class<?> expectedType ) {
        if ( expectedType == null ) {
            throw new NullPointerException ( MessageFactory.get ( "error.value.expectedType" ) );
        }
        return new ValueExpressionLiteral ( instance, expectedType );
    }
    public ELResolver getStreamELResolver() {
        return new StreamELResolverImpl();
    }
}
