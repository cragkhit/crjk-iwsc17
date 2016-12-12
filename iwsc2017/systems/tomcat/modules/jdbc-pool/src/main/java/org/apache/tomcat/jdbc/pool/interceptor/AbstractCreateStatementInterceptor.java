package org.apache.tomcat.jdbc.pool.interceptor;
import java.lang.reflect.Method;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;
public abstract class  AbstractCreateStatementInterceptor extends JdbcInterceptor {
    protected static final String CREATE_STATEMENT      = "createStatement";
    protected static final int    CREATE_STATEMENT_IDX  = 0;
    protected static final String PREPARE_STATEMENT     = "prepareStatement";
    protected static final int    PREPARE_STATEMENT_IDX = 1;
    protected static final String PREPARE_CALL          = "prepareCall";
    protected static final int    PREPARE_CALL_IDX      = 2;
    protected static final String[] STATEMENT_TYPES = {CREATE_STATEMENT, PREPARE_STATEMENT, PREPARE_CALL};
    protected static final int    STATEMENT_TYPE_COUNT = STATEMENT_TYPES.length;
    protected static final String EXECUTE        = "execute";
    protected static final String EXECUTE_QUERY  = "executeQuery";
    protected static final String EXECUTE_UPDATE = "executeUpdate";
    protected static final String EXECUTE_BATCH  = "executeBatch";
    protected static final String[] EXECUTE_TYPES = {EXECUTE, EXECUTE_QUERY, EXECUTE_UPDATE, EXECUTE_BATCH};
    public  AbstractCreateStatementInterceptor() {
        super();
    }
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        if ( compare ( CLOSE_VAL, method ) ) {
            closeInvoked();
            return super.invoke ( proxy, method, args );
        } else {
            boolean process = false;
            process = isStatement ( method, process );
            if ( process ) {
                long start = System.currentTimeMillis();
                Object statement = super.invoke ( proxy, method, args );
                long delta = System.currentTimeMillis() - start;
                return createStatement ( proxy, method, args, statement, delta );
            } else {
                return super.invoke ( proxy, method, args );
            }
        }
    }
    public abstract Object createStatement ( Object proxy, Method method, Object[] args, Object statement, long time );
    public abstract void closeInvoked();
    protected boolean isStatement ( Method method, boolean process ) {
        return process ( STATEMENT_TYPES, method, process );
    }
    protected boolean isExecute ( Method method, boolean process ) {
        return process ( EXECUTE_TYPES, method, process );
    }
    protected boolean process ( String[] names, Method method, boolean process ) {
        final String name = method.getName();
        for ( int i = 0; ( !process ) && i < names.length; i++ ) {
            process = compare ( names[i], name );
        }
        return process;
    }
    @Override
    public void reset ( ConnectionPool parent, PooledConnection con ) {
    }
}
