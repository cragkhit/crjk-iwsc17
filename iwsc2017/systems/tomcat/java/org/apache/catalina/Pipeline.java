package org.apache.catalina;
public interface Pipeline {
    public Valve getBasic();
    public void setBasic ( Valve valve );
    public void addValve ( Valve valve );
    public Valve[] getValves();
    public void removeValve ( Valve valve );
    public Valve getFirst();
    public boolean isAsyncSupported();
    public Container getContainer();
    public void setContainer ( Container container );
}
