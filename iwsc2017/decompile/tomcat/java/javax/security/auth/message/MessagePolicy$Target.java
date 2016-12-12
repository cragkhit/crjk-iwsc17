package javax.security.auth.message;
public interface Target {
    Object get ( MessageInfo p0 );
    void remove ( MessageInfo p0 );
    void put ( MessageInfo p0, Object p1 );
}
