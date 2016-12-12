package org.apache.el.lang;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.el.MethodExpressionLiteral;
import org.apache.el.MethodExpressionImpl;
import org.apache.el.parser.AstValue;
import javax.el.MethodExpression;
import org.apache.el.ValueExpressionImpl;
import javax.el.ValueExpression;
import java.lang.reflect.Method;
import org.apache.el.parser.AstIdentifier;
import org.apache.el.parser.AstFunction;
import org.apache.el.parser.AstDynamicExpression;
import org.apache.el.parser.AstDeferredExpression;
import org.apache.el.parser.AstLiteralExpression;
import java.io.Reader;
import java.io.StringReader;
import org.apache.el.util.MessageFactory;
import javax.el.ELException;
import javax.el.ELContext;
import javax.el.VariableMapper;
import javax.el.FunctionMapper;
import org.apache.el.parser.Node;
import org.apache.el.util.ConcurrentCache;
import org.apache.el.parser.ELParser;
import org.apache.el.parser.NodeVisitor;
public final class ExpressionBuilder implements NodeVisitor {
    private static final SynchronizedStack<ELParser> parserCache;
    private static final int CACHE_SIZE;
    private static final String CACHE_SIZE_PROP = "org.apache.el.ExpressionBuilder.CACHE_SIZE";
    private static final ConcurrentCache<String, Node> expressionCache;
    private FunctionMapper fnMapper;
    private VariableMapper varMapper;
    private final String expression;
    public ExpressionBuilder ( final String expression, final ELContext ctx ) throws ELException {
        this.expression = expression;
        final FunctionMapper ctxFn = ctx.getFunctionMapper();
        final VariableMapper ctxVar = ctx.getVariableMapper();
        if ( ctxFn != null ) {
            this.fnMapper = new FunctionMapperFactory ( ctxFn );
        }
        if ( ctxVar != null ) {
            this.varMapper = new VariableMapperFactory ( ctxVar );
        }
    }
    public static final Node createNode ( final String expr ) throws ELException {
        final Node n = createNodeInternal ( expr );
        return n;
    }
    private static final Node createNodeInternal ( final String expr ) throws ELException {
        if ( expr == null ) {
            throw new ELException ( MessageFactory.get ( "error.null" ) );
        }
        Node n = ExpressionBuilder.expressionCache.get ( expr );
        if ( n == null ) {
            ELParser parser = ExpressionBuilder.parserCache.pop();
            try {
                if ( parser == null ) {
                    parser = new ELParser ( new StringReader ( expr ) );
                } else {
                    parser.ReInit ( new StringReader ( expr ) );
                }
                n = parser.CompositeExpression();
                final int numChildren = n.jjtGetNumChildren();
                if ( numChildren == 1 ) {
                    n = n.jjtGetChild ( 0 );
                } else {
                    Class<?> type = null;
                    Node child = null;
                    for ( int i = 0; i < numChildren; ++i ) {
                        child = n.jjtGetChild ( i );
                        if ( ! ( child instanceof AstLiteralExpression ) ) {
                            if ( type == null ) {
                                type = child.getClass();
                            } else if ( !type.equals ( child.getClass() ) ) {
                                throw new ELException ( MessageFactory.get ( "error.mixed", expr ) );
                            }
                        }
                    }
                }
                if ( n instanceof AstDeferredExpression || n instanceof AstDynamicExpression ) {
                    n = n.jjtGetChild ( 0 );
                }
                ExpressionBuilder.expressionCache.put ( expr, n );
            } catch ( Exception e ) {
                throw new ELException ( MessageFactory.get ( "error.parseFail", expr ), ( Throwable ) e );
            } finally {
                if ( parser != null ) {
                    ExpressionBuilder.parserCache.push ( parser );
                }
            }
        }
        return n;
    }
    private void prepare ( final Node node ) throws ELException {
        try {
            node.accept ( this );
        } catch ( Exception e ) {
            if ( e instanceof ELException ) {
                throw ( ELException ) e;
            }
            throw new ELException ( ( Throwable ) e );
        }
        if ( this.fnMapper instanceof FunctionMapperFactory ) {
            this.fnMapper = ( ( FunctionMapperFactory ) this.fnMapper ).create();
        }
        if ( this.varMapper instanceof VariableMapperFactory ) {
            this.varMapper = ( ( VariableMapperFactory ) this.varMapper ).create();
        }
    }
    private Node build() throws ELException {
        Node n = createNodeInternal ( this.expression );
        this.prepare ( n );
        if ( n instanceof AstDeferredExpression || n instanceof AstDynamicExpression ) {
            n = n.jjtGetChild ( 0 );
        }
        return n;
    }
    @Override
    public void visit ( final Node node ) throws ELException {
        if ( node instanceof AstFunction ) {
            final AstFunction funcNode = ( AstFunction ) node;
            Method m = null;
            if ( this.fnMapper != null ) {
                m = this.fnMapper.resolveFunction ( funcNode.getPrefix(), funcNode.getLocalName() );
            }
            if ( m == null && this.varMapper != null && funcNode.getPrefix().length() == 0 ) {
                this.varMapper.resolveVariable ( funcNode.getLocalName() );
                return;
            }
            if ( this.fnMapper == null ) {
                throw new ELException ( MessageFactory.get ( "error.fnMapper.null" ) );
            }
            if ( m == null ) {
                throw new ELException ( MessageFactory.get ( "error.fnMapper.method", funcNode.getOutputName() ) );
            }
            final int methodParameterCount = m.getParameterTypes().length;
            final int inputParameterCount = node.jjtGetChild ( 0 ).jjtGetNumChildren();
            if ( ( m.isVarArgs() && inputParameterCount < methodParameterCount - 1 ) || ( !m.isVarArgs() && inputParameterCount != methodParameterCount ) ) {
                throw new ELException ( MessageFactory.get ( "error.fnMapper.paramcount", funcNode.getOutputName(), "" + methodParameterCount, "" + node.jjtGetChild ( 0 ).jjtGetNumChildren() ) );
            }
        } else if ( node instanceof AstIdentifier && this.varMapper != null ) {
            final String variable = ( ( AstIdentifier ) node ).getImage();
            this.varMapper.resolveVariable ( variable );
        }
    }
    public ValueExpression createValueExpression ( final Class<?> expectedType ) throws ELException {
        final Node n = this.build();
        return new ValueExpressionImpl ( this.expression, n, this.fnMapper, this.varMapper, expectedType );
    }
    public MethodExpression createMethodExpression ( final Class<?> expectedReturnType, final Class<?>[] expectedParamTypes ) throws ELException {
        final Node n = this.build();
        if ( !n.isParametersProvided() && expectedParamTypes == null ) {
            throw new NullPointerException ( MessageFactory.get ( "error.method.nullParms" ) );
        }
        if ( n instanceof AstValue || n instanceof AstIdentifier ) {
            return new MethodExpressionImpl ( this.expression, n, this.fnMapper, this.varMapper, expectedReturnType, expectedParamTypes );
        }
        if ( n instanceof AstLiteralExpression ) {
            return new MethodExpressionLiteral ( this.expression, expectedReturnType, expectedParamTypes );
        }
        throw new ELException ( "Not a Valid Method Expression: " + this.expression );
    }
    static {
        parserCache = new SynchronizedStack<ELParser>();
        String cacheSizeStr;
        if ( System.getSecurityManager() == null ) {
            cacheSizeStr = System.getProperty ( "org.apache.el.ExpressionBuilder.CACHE_SIZE", "5000" );
        } else {
            cacheSizeStr = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty ( "org.apache.el.ExpressionBuilder.CACHE_SIZE", "5000" );
                }
            } );
        }
        CACHE_SIZE = Integer.parseInt ( cacheSizeStr );
        expressionCache = new ConcurrentCache<String, Node> ( ExpressionBuilder.CACHE_SIZE );
    }
    private static class SynchronizedStack<T> {
        public static final int DEFAULT_SIZE = 128;
        private static final int DEFAULT_LIMIT = -1;
        private int size;
        private final int limit;
        private int index;
        private Object[] stack;
        public SynchronizedStack() {
            this ( 128, -1 );
        }
        public SynchronizedStack ( final int size, final int limit ) {
            this.index = -1;
            this.size = size;
            this.limit = limit;
            this.stack = new Object[size];
        }
        public synchronized boolean push ( final T obj ) {
            ++this.index;
            if ( this.index == this.size ) {
                if ( this.limit != -1 && this.size >= this.limit ) {
                    --this.index;
                    return false;
                }
                this.expand();
            }
            this.stack[this.index] = obj;
            return true;
        }
        public synchronized T pop() {
            if ( this.index == -1 ) {
                return null;
            }
            final T result = ( T ) this.stack[this.index];
            this.stack[this.index--] = null;
            return result;
        }
        private void expand() {
            int newSize = this.size * 2;
            if ( this.limit != -1 && newSize > this.limit ) {
                newSize = this.limit;
            }
            final Object[] newStack = new Object[newSize];
            System.arraycopy ( this.stack, 0, newStack, 0, this.size );
            this.stack = newStack;
            this.size = newSize;
        }
    }
}
