package org.apache.tomcat.util.descriptor.web;
public interface NamingResources {
    void addEnvironment ( ContextEnvironment p0 );
    void removeEnvironment ( String p0 );
    void addResource ( ContextResource p0 );
    void removeResource ( String p0 );
    void addResourceLink ( ContextResourceLink p0 );
    void removeResourceLink ( String p0 );
    Object getContainer();
}
