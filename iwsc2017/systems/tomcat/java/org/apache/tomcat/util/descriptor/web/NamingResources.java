package org.apache.tomcat.util.descriptor.web;
public interface NamingResources {
    void addEnvironment ( ContextEnvironment ce );
    void removeEnvironment ( String name );
    void addResource ( ContextResource cr );
    void removeResource ( String name );
    void addResourceLink ( ContextResourceLink crl );
    void removeResourceLink ( String name );
    Object getContainer();
}
