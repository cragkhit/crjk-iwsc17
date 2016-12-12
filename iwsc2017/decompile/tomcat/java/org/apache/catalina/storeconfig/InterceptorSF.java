package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class InterceptorSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        if ( aElement instanceof StaticMembershipInterceptor ) {
            final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass() );
            if ( elementDesc != null ) {
                if ( InterceptorSF.log.isDebugEnabled() ) {
                    InterceptorSF.log.debug ( InterceptorSF.sm.getString ( "factory.storeTag", elementDesc.getTag(), aElement ) );
                }
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printOpenTag ( aWriter, indent + 2, aElement, elementDesc );
                this.storeChildren ( aWriter, indent + 2, aElement, elementDesc );
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printCloseTag ( aWriter, elementDesc );
            } else if ( InterceptorSF.log.isWarnEnabled() ) {
                InterceptorSF.log.warn ( InterceptorSF.sm.getString ( "factory.storeNoDescriptor", aElement.getClass() ) );
            }
        } else {
            super.store ( aWriter, indent, aElement );
        }
    }
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aInterceptor, final StoreDescription parentDesc ) throws Exception {
        if ( aInterceptor instanceof StaticMembershipInterceptor ) {
            final ChannelInterceptor interceptor = ( ChannelInterceptor ) aInterceptor;
            this.storeElementArray ( aWriter, indent + 2, interceptor.getMembers() );
        }
    }
    static {
        InterceptorSF.log = LogFactory.getLog ( InterceptorSF.class );
    }
}
