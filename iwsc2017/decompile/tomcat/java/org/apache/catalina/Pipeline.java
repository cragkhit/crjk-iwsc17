package org.apache.catalina;
public interface Pipeline {
    Valve getBasic();
    void setBasic ( Valve p0 );
    void addValve ( Valve p0 );
    Valve[] getValves();
    void removeValve ( Valve p0 );
    Valve getFirst();
    boolean isAsyncSupported();
    Container getContainer();
    void setContainer ( Container p0 );
}
