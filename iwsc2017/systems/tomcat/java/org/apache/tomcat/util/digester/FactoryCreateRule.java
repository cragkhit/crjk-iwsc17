package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public class FactoryCreateRule extends Rule {
    private boolean ignoreCreateExceptions;
    private ArrayStack<Boolean> exceptionIgnoredStack;
    public FactoryCreateRule (
        ObjectCreationFactory creationFactory,
        boolean ignoreCreateExceptions ) {
        this.creationFactory = creationFactory;
        this.ignoreCreateExceptions = ignoreCreateExceptions;
    }
    protected ObjectCreationFactory creationFactory = null;
    @Override
    public void begin ( String namespace, String name, Attributes attributes ) throws Exception {
        if ( ignoreCreateExceptions ) {
            if ( exceptionIgnoredStack == null ) {
                exceptionIgnoredStack = new ArrayStack<>();
            }
            try {
                Object instance = creationFactory.createObject ( attributes );
                if ( digester.log.isDebugEnabled() ) {
                    digester.log.debug ( "[FactoryCreateRule]{" + digester.match +
                                         "} New " + instance.getClass().getName() );
                }
                digester.push ( instance );
                exceptionIgnoredStack.push ( Boolean.FALSE );
            } catch ( Exception e ) {
                if ( digester.log.isInfoEnabled() ) {
                    digester.log.info ( "[FactoryCreateRule] Create exception ignored: " +
                                        ( ( e.getMessage() == null ) ? e.getClass().getName() : e.getMessage() ) );
                    if ( digester.log.isDebugEnabled() ) {
                        digester.log.debug ( "[FactoryCreateRule] Ignored exception:", e );
                    }
                }
                exceptionIgnoredStack.push ( Boolean.TRUE );
            }
        } else {
            Object instance = creationFactory.createObject ( attributes );
            if ( digester.log.isDebugEnabled() ) {
                digester.log.debug ( "[FactoryCreateRule]{" + digester.match +
                                     "} New " + instance.getClass().getName() );
            }
            digester.push ( instance );
        }
    }
    @Override
    public void end ( String namespace, String name ) throws Exception {
        if (
            ignoreCreateExceptions &&
            exceptionIgnoredStack != null &&
            ! ( exceptionIgnoredStack.empty() ) ) {
            if ( ( exceptionIgnoredStack.pop() ).booleanValue() ) {
                if ( digester.log.isTraceEnabled() ) {
                    digester.log.trace ( "[FactoryCreateRule] No creation so no push so no pop" );
                }
                return;
            }
        }
        Object top = digester.pop();
        if ( digester.log.isDebugEnabled() ) {
            digester.log.debug ( "[FactoryCreateRule]{" + digester.match +
                                 "} Pop " + top.getClass().getName() );
        }
    }
    @Override
    public void finish() throws Exception {
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "FactoryCreateRule[" );
        if ( creationFactory != null ) {
            sb.append ( "creationFactory=" );
            sb.append ( creationFactory );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
