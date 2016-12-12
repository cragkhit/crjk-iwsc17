package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public class FactoryCreateRule extends Rule {
    private boolean ignoreCreateExceptions;
    private ArrayStack<Boolean> exceptionIgnoredStack;
    protected ObjectCreationFactory creationFactory;
    public FactoryCreateRule ( final ObjectCreationFactory creationFactory, final boolean ignoreCreateExceptions ) {
        this.creationFactory = null;
        this.creationFactory = creationFactory;
        this.ignoreCreateExceptions = ignoreCreateExceptions;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.ignoreCreateExceptions ) {
            if ( this.exceptionIgnoredStack == null ) {
                this.exceptionIgnoredStack = new ArrayStack<Boolean>();
            }
            try {
                final Object instance = this.creationFactory.createObject ( attributes );
                if ( this.digester.log.isDebugEnabled() ) {
                    this.digester.log.debug ( "[FactoryCreateRule]{" + this.digester.match + "} New " + instance.getClass().getName() );
                }
                this.digester.push ( instance );
                this.exceptionIgnoredStack.push ( Boolean.FALSE );
            } catch ( Exception e ) {
                if ( this.digester.log.isInfoEnabled() ) {
                    this.digester.log.info ( "[FactoryCreateRule] Create exception ignored: " + ( ( e.getMessage() == null ) ? e.getClass().getName() : e.getMessage() ) );
                    if ( this.digester.log.isDebugEnabled() ) {
                        this.digester.log.debug ( "[FactoryCreateRule] Ignored exception:", e );
                    }
                }
                this.exceptionIgnoredStack.push ( Boolean.TRUE );
            }
        } else {
            final Object instance = this.creationFactory.createObject ( attributes );
            if ( this.digester.log.isDebugEnabled() ) {
                this.digester.log.debug ( "[FactoryCreateRule]{" + this.digester.match + "} New " + instance.getClass().getName() );
            }
            this.digester.push ( instance );
        }
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        if ( this.ignoreCreateExceptions && this.exceptionIgnoredStack != null && !this.exceptionIgnoredStack.empty() && this.exceptionIgnoredStack.pop() ) {
            if ( this.digester.log.isTraceEnabled() ) {
                this.digester.log.trace ( "[FactoryCreateRule] No creation so no push so no pop" );
            }
            return;
        }
        final Object top = this.digester.pop();
        if ( this.digester.log.isDebugEnabled() ) {
            this.digester.log.debug ( "[FactoryCreateRule]{" + this.digester.match + "} Pop " + top.getClass().getName() );
        }
    }
    @Override
    public void finish() throws Exception {
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "FactoryCreateRule[" );
        if ( this.creationFactory != null ) {
            sb.append ( "creationFactory=" );
            sb.append ( this.creationFactory );
        }
        sb.append ( "]" );
        return sb.toString();
    }
}
