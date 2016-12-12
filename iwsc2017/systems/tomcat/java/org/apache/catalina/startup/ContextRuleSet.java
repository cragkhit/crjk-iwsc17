package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class ContextRuleSet extends RuleSetBase {
    protected final String prefix;
    protected final boolean create;
    public ContextRuleSet() {
        this ( "" );
    }
    public ContextRuleSet ( String prefix ) {
        this ( prefix, true );
    }
    public ContextRuleSet ( String prefix, boolean create ) {
        this.namespaceURI = null;
        this.prefix = prefix;
        this.create = create;
    }
    @Override
    public void addRuleInstances ( Digester digester ) {
        if ( create ) {
            digester.addObjectCreate ( prefix + "Context",
                                       "org.apache.catalina.core.StandardContext", "className" );
            digester.addSetProperties ( prefix + "Context" );
        } else {
            digester.addRule ( prefix + "Context", new SetContextPropertiesRule() );
        }
        if ( create ) {
            digester.addRule ( prefix + "Context",
                               new LifecycleListenerRule
                               ( "org.apache.catalina.startup.ContextConfig",
                                 "configClass" ) );
            digester.addSetNext ( prefix + "Context",
                                  "addChild",
                                  "org.apache.catalina.Container" );
        }
        digester.addObjectCreate ( prefix + "Context/Listener",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Listener" );
        digester.addSetNext ( prefix + "Context/Listener",
                              "addLifecycleListener",
                              "org.apache.catalina.LifecycleListener" );
        digester.addObjectCreate ( prefix + "Context/Loader",
                                   "org.apache.catalina.loader.WebappLoader",
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Loader" );
        digester.addSetNext ( prefix + "Context/Loader",
                              "setLoader",
                              "org.apache.catalina.Loader" );
        digester.addObjectCreate ( prefix + "Context/Manager",
                                   "org.apache.catalina.session.StandardManager",
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Manager" );
        digester.addSetNext ( prefix + "Context/Manager",
                              "setManager",
                              "org.apache.catalina.Manager" );
        digester.addObjectCreate ( prefix + "Context/Manager/Store",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Manager/Store" );
        digester.addSetNext ( prefix + "Context/Manager/Store",
                              "setStore",
                              "org.apache.catalina.Store" );
        digester.addObjectCreate ( prefix + "Context/Manager/SessionIdGenerator",
                                   "org.apache.catalina.util.StandardSessionIdGenerator",
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Manager/SessionIdGenerator" );
        digester.addSetNext ( prefix + "Context/Manager/SessionIdGenerator",
                              "setSessionIdGenerator",
                              "org.apache.catalina.SessionIdGenerator" );
        digester.addObjectCreate ( prefix + "Context/Parameter",
                                   "org.apache.tomcat.util.descriptor.web.ApplicationParameter" );
        digester.addSetProperties ( prefix + "Context/Parameter" );
        digester.addSetNext ( prefix + "Context/Parameter",
                              "addApplicationParameter",
                              "org.apache.tomcat.util.descriptor.web.ApplicationParameter" );
        digester.addRuleSet ( new RealmRuleSet ( prefix + "Context/" ) );
        digester.addObjectCreate ( prefix + "Context/Resources",
                                   "org.apache.catalina.webresources.StandardRoot",
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Resources" );
        digester.addSetNext ( prefix + "Context/Resources",
                              "setResources",
                              "org.apache.catalina.WebResourceRoot" );
        digester.addObjectCreate ( prefix + "Context/Resources/PreResources",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Resources/PreResources" );
        digester.addSetNext ( prefix + "Context/Resources/PreResources",
                              "addPreResources",
                              "org.apache.catalina.WebResourceSet" );
        digester.addObjectCreate ( prefix + "Context/Resources/JarResources",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Resources/JarResources" );
        digester.addSetNext ( prefix + "Context/Resources/JarResources",
                              "addJarResources",
                              "org.apache.catalina.WebResourceSet" );
        digester.addObjectCreate ( prefix + "Context/Resources/PostResources",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Resources/PostResources" );
        digester.addSetNext ( prefix + "Context/Resources/PostResources",
                              "addPostResources",
                              "org.apache.catalina.WebResourceSet" );
        digester.addObjectCreate ( prefix + "Context/ResourceLink",
                                   "org.apache.tomcat.util.descriptor.web.ContextResourceLink" );
        digester.addSetProperties ( prefix + "Context/ResourceLink" );
        digester.addRule ( prefix + "Context/ResourceLink",
                           new SetNextNamingRule ( "addResourceLink",
                                   "org.apache.tomcat.util.descriptor.web.ContextResourceLink" ) );
        digester.addObjectCreate ( prefix + "Context/Valve",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Context/Valve" );
        digester.addSetNext ( prefix + "Context/Valve",
                              "addValve",
                              "org.apache.catalina.Valve" );
        digester.addCallMethod ( prefix + "Context/WatchedResource",
                                 "addWatchedResource", 0 );
        digester.addCallMethod ( prefix + "Context/WrapperLifecycle",
                                 "addWrapperLifecycle", 0 );
        digester.addCallMethod ( prefix + "Context/WrapperListener",
                                 "addWrapperListener", 0 );
        digester.addObjectCreate ( prefix + "Context/JarScanner",
                                   "org.apache.tomcat.util.scan.StandardJarScanner",
                                   "className" );
        digester.addSetProperties ( prefix + "Context/JarScanner" );
        digester.addSetNext ( prefix + "Context/JarScanner",
                              "setJarScanner",
                              "org.apache.tomcat.JarScanner" );
        digester.addObjectCreate ( prefix + "Context/JarScanner/JarScanFilter",
                                   "org.apache.tomcat.util.scan.StandardJarScanFilter",
                                   "className" );
        digester.addSetProperties ( prefix + "Context/JarScanner/JarScanFilter" );
        digester.addSetNext ( prefix + "Context/JarScanner/JarScanFilter",
                              "setJarScanFilter",
                              "org.apache.tomcat.JarScanFilter" );
        digester.addObjectCreate ( prefix + "Context/CookieProcessor",
                                   "org.apache.tomcat.util.http.Rfc6265CookieProcessor",
                                   "className" );
        digester.addSetProperties ( prefix + "Context/CookieProcessor" );
        digester.addSetNext ( prefix + "Context/CookieProcessor",
                              "setCookieProcessor",
                              "org.apache.tomcat.util.http.CookieProcessor" );
    }
}
