package javax.servlet.jsp.tagext;
public interface TryCatchFinally {
    void doCatch ( Throwable t ) throws Throwable;
    void doFinally();
}
