package org.apache.catalina;
public interface Contained {
    Container getContainer();
    void setContainer ( Container container );
}
