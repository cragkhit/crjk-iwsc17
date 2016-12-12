package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.SAXException;
import org.apache.tomcat.util.IntrospectionUtils;
import java.util.ArrayList;
import org.apache.tomcat.util.digester.CallMethodRule;
final class CallMethodMultiRule extends CallMethodRule {
    final int multiParamIndex;
    public CallMethodMultiRule ( final String methodName, final int paramCount, final int multiParamIndex ) {
        super ( methodName, paramCount );
        this.multiParamIndex = multiParamIndex;
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        Object[] parameters = null;
        if ( this.paramCount > 0 ) {
            parameters = ( Object[] ) this.digester.popParams();
        } else {
            parameters = new Object[0];
            super.end ( namespace, name );
        }
        final ArrayList<?> multiParams = ( ArrayList<?> ) parameters[this.multiParamIndex];
        final Object[] paramValues = new Object[this.paramTypes.length];
        for ( int i = 0; i < this.paramTypes.length; ++i ) {
            if ( i != this.multiParamIndex ) {
                if ( parameters[i] == null || ( parameters[i] instanceof String && !String.class.isAssignableFrom ( this.paramTypes[i] ) ) ) {
                    paramValues[i] = IntrospectionUtils.convert ( ( String ) parameters[i], this.paramTypes[i] );
                } else {
                    paramValues[i] = parameters[i];
                }
            }
        }
        Object target;
        if ( this.targetOffset >= 0 ) {
            target = this.digester.peek ( this.targetOffset );
        } else {
            target = this.digester.peek ( this.digester.getCount() + this.targetOffset );
        }
        if ( target == null ) {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "[CallMethodRule]{" );
            sb.append ( "" );
            sb.append ( "} Call target is null (" );
            sb.append ( "targetOffset=" );
            sb.append ( this.targetOffset );
            sb.append ( ",stackdepth=" );
            sb.append ( this.digester.getCount() );
            sb.append ( ")" );
            throw new SAXException ( sb.toString() );
        }
        if ( multiParams == null ) {
            paramValues[this.multiParamIndex] = null;
            IntrospectionUtils.callMethodN ( target, this.methodName, paramValues, this.paramTypes );
            return;
        }
        for ( int j = 0; j < multiParams.size(); ++j ) {
            final Object param = multiParams.get ( j );
            if ( param == null || ( param instanceof String && !String.class.isAssignableFrom ( this.paramTypes[this.multiParamIndex] ) ) ) {
                paramValues[this.multiParamIndex] = IntrospectionUtils.convert ( ( String ) param, this.paramTypes[this.multiParamIndex] );
            } else {
                paramValues[this.multiParamIndex] = param;
            }
            IntrospectionUtils.callMethodN ( target, this.methodName, paramValues, this.paramTypes );
        }
    }
}
