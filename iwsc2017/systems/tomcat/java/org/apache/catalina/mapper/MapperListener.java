package org.apache.catalina.mapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class MapperListener extends LifecycleMBeanBase
    implements ContainerListener, LifecycleListener {
    private static final Log log = LogFactory.getLog ( MapperListener.class );
    private final Mapper mapper;
    private final Service service;
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private final String domain = null;
    public MapperListener ( Service service ) {
        this.service = service;
        this.mapper = service.getMapper();
    }
    @Override
    public void startInternal() throws LifecycleException {
        setState ( LifecycleState.STARTING );
        Engine engine = service.getContainer();
        if ( engine == null ) {
            return;
        }
        findDefaultHost();
        addListeners ( engine );
        Container[] conHosts = engine.findChildren();
        for ( Container conHost : conHosts ) {
            Host host = ( Host ) conHost;
            if ( !LifecycleState.NEW.equals ( host.getState() ) ) {
                registerHost ( host );
            }
        }
    }
    @Override
    public void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        Engine engine = service.getContainer();
        if ( engine == null ) {
            return;
        }
        removeListeners ( engine );
    }
    @Override
    protected String getDomainInternal() {
        if ( service instanceof LifecycleMBeanBase ) {
            return ( ( LifecycleMBeanBase ) service ).getDomain();
        } else {
            return null;
        }
    }
    @Override
    protected String getObjectNameKeyProperties() {
        return ( "type=Mapper" );
    }
    @Override
    public void containerEvent ( ContainerEvent event ) {
        if ( Container.ADD_CHILD_EVENT.equals ( event.getType() ) ) {
            Container child = ( Container ) event.getData();
            addListeners ( child );
            if ( child.getState().isAvailable() ) {
                if ( child instanceof Host ) {
                    registerHost ( ( Host ) child );
                } else if ( child instanceof Context ) {
                    registerContext ( ( Context ) child );
                } else if ( child instanceof Wrapper ) {
                    if ( child.getParent().getState().isAvailable() ) {
                        registerWrapper ( ( Wrapper ) child );
                    }
                }
            }
        } else if ( Container.REMOVE_CHILD_EVENT.equals ( event.getType() ) ) {
            Container child = ( Container ) event.getData();
            removeListeners ( child );
        } else if ( Host.ADD_ALIAS_EVENT.equals ( event.getType() ) ) {
            mapper.addHostAlias ( ( ( Host ) event.getSource() ).getName(),
                                  event.getData().toString() );
        } else if ( Host.REMOVE_ALIAS_EVENT.equals ( event.getType() ) ) {
            mapper.removeHostAlias ( event.getData().toString() );
        } else if ( Wrapper.ADD_MAPPING_EVENT.equals ( event.getType() ) ) {
            Wrapper wrapper = ( Wrapper ) event.getSource();
            Context context = ( Context ) wrapper.getParent();
            String contextPath = context.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            String version = context.getWebappVersion();
            String hostName = context.getParent().getName();
            String wrapperName = wrapper.getName();
            String mapping = ( String ) event.getData();
            boolean jspWildCard = ( "jsp".equals ( wrapperName )
                                    && mapping.endsWith ( "/*" ) );
            mapper.addWrapper ( hostName, contextPath, version, mapping, wrapper,
                                jspWildCard, context.isResourceOnlyServlet ( wrapperName ) );
        } else if ( Wrapper.REMOVE_MAPPING_EVENT.equals ( event.getType() ) ) {
            Wrapper wrapper = ( Wrapper ) event.getSource();
            Context context = ( Context ) wrapper.getParent();
            String contextPath = context.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            String version = context.getWebappVersion();
            String hostName = context.getParent().getName();
            String mapping = ( String ) event.getData();
            mapper.removeWrapper ( hostName, contextPath, version, mapping );
        } else if ( Context.ADD_WELCOME_FILE_EVENT.equals ( event.getType() ) ) {
            Context context = ( Context ) event.getSource();
            String hostName = context.getParent().getName();
            String contextPath = context.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            String welcomeFile = ( String ) event.getData();
            mapper.addWelcomeFile ( hostName, contextPath,
                                    context.getWebappVersion(), welcomeFile );
        } else if ( Context.REMOVE_WELCOME_FILE_EVENT.equals ( event.getType() ) ) {
            Context context = ( Context ) event.getSource();
            String hostName = context.getParent().getName();
            String contextPath = context.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            String welcomeFile = ( String ) event.getData();
            mapper.removeWelcomeFile ( hostName, contextPath,
                                       context.getWebappVersion(), welcomeFile );
        } else if ( Context.CLEAR_WELCOME_FILES_EVENT.equals ( event.getType() ) ) {
            Context context = ( Context ) event.getSource();
            String hostName = context.getParent().getName();
            String contextPath = context.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            mapper.clearWelcomeFiles ( hostName, contextPath,
                                       context.getWebappVersion() );
        }
    }
    private void findDefaultHost() {
        Engine engine = service.getContainer();
        String defaultHost = engine.getDefaultHost();
        boolean found = false;
        if ( defaultHost != null && defaultHost.length() > 0 ) {
            Container[] containers = engine.findChildren();
            for ( Container container : containers ) {
                Host host = ( Host ) container;
                if ( defaultHost.equalsIgnoreCase ( host.getName() ) ) {
                    found = true;
                    break;
                }
                String[] aliases = host.findAliases();
                for ( String alias : aliases ) {
                    if ( defaultHost.equalsIgnoreCase ( alias ) ) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if ( found ) {
            mapper.setDefaultHostName ( defaultHost );
        } else {
            log.warn ( sm.getString ( "mapperListener.unknownDefaultHost",
                                      defaultHost, service ) );
        }
    }
    private void registerHost ( Host host ) {
        String[] aliases = host.findAliases();
        mapper.addHost ( host.getName(), aliases, host );
        for ( Container container : host.findChildren() ) {
            if ( container.getState().isAvailable() ) {
                registerContext ( ( Context ) container );
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "mapperListener.registerHost",
                                       host.getName(), domain, service ) );
        }
    }
    private void unregisterHost ( Host host ) {
        String hostname = host.getName();
        mapper.removeHost ( hostname );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "mapperListener.unregisterHost", hostname,
                                       domain, service ) );
        }
    }
    private void unregisterWrapper ( Wrapper wrapper ) {
        Context context = ( ( Context ) wrapper.getParent() );
        String contextPath = context.getPath();
        String wrapperName = wrapper.getName();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        String version = context.getWebappVersion();
        String hostName = context.getParent().getName();
        String[] mappings = wrapper.findMappings();
        for ( String mapping : mappings ) {
            mapper.removeWrapper ( hostName, contextPath, version,  mapping );
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "mapperListener.unregisterWrapper",
                                       wrapperName, contextPath, service ) );
        }
    }
    private void registerContext ( Context context ) {
        String contextPath = context.getPath();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        Host host = ( Host ) context.getParent();
        WebResourceRoot resources = context.getResources();
        String[] welcomeFiles = context.findWelcomeFiles();
        List<WrapperMappingInfo> wrappers = new ArrayList<>();
        for ( Container container : context.findChildren() ) {
            prepareWrapperMappingInfo ( context, ( Wrapper ) container, wrappers );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "mapperListener.registerWrapper",
                                           container.getName(), contextPath, service ) );
            }
        }
        mapper.addContextVersion ( host.getName(), host, contextPath,
                                   context.getWebappVersion(), context, welcomeFiles, resources,
                                   wrappers );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "mapperListener.registerContext",
                                       contextPath, service ) );
        }
    }
    private void unregisterContext ( Context context ) {
        String contextPath = context.getPath();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        String hostName = context.getParent().getName();
        if ( context.getPaused() ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "mapperListener.pauseContext",
                                           contextPath, service ) );
            }
            mapper.pauseContextVersion ( context, hostName, contextPath,
                                         context.getWebappVersion() );
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "mapperListener.unregisterContext",
                                           contextPath, service ) );
            }
            mapper.removeContextVersion ( context, hostName, contextPath,
                                          context.getWebappVersion() );
        }
    }
    private void registerWrapper ( Wrapper wrapper ) {
        Context context = ( Context ) wrapper.getParent();
        String contextPath = context.getPath();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        String version = context.getWebappVersion();
        String hostName = context.getParent().getName();
        List<WrapperMappingInfo> wrappers = new ArrayList<>();
        prepareWrapperMappingInfo ( context, wrapper, wrappers );
        mapper.addWrappers ( hostName, contextPath, version, wrappers );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "mapperListener.registerWrapper",
                                       wrapper.getName(), contextPath, service ) );
        }
    }
    private void prepareWrapperMappingInfo ( Context context, Wrapper wrapper,
            List<WrapperMappingInfo> wrappers ) {
        String wrapperName = wrapper.getName();
        boolean resourceOnly = context.isResourceOnlyServlet ( wrapperName );
        String[] mappings = wrapper.findMappings();
        for ( String mapping : mappings ) {
            boolean jspWildCard = ( wrapperName.equals ( "jsp" )
                                    && mapping.endsWith ( "/*" ) );
            wrappers.add ( new WrapperMappingInfo ( mapping, wrapper, jspWildCard,
                                                    resourceOnly ) );
        }
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        if ( event.getType().equals ( Lifecycle.AFTER_START_EVENT ) ) {
            Object obj = event.getSource();
            if ( obj instanceof Wrapper ) {
                Wrapper w = ( Wrapper ) obj;
                if ( w.getParent().getState().isAvailable() ) {
                    registerWrapper ( w );
                }
            } else if ( obj instanceof Context ) {
                Context c = ( Context ) obj;
                if ( c.getParent().getState().isAvailable() ) {
                    registerContext ( c );
                }
            } else if ( obj instanceof Host ) {
                registerHost ( ( Host ) obj );
            }
        } else if ( event.getType().equals ( Lifecycle.BEFORE_STOP_EVENT ) ) {
            Object obj = event.getSource();
            if ( obj instanceof Wrapper ) {
                unregisterWrapper ( ( Wrapper ) obj );
            } else if ( obj instanceof Context ) {
                unregisterContext ( ( Context ) obj );
            } else if ( obj instanceof Host ) {
                unregisterHost ( ( Host ) obj );
            }
        }
    }
    private void addListeners ( Container container ) {
        container.addContainerListener ( this );
        container.addLifecycleListener ( this );
        for ( Container child : container.findChildren() ) {
            addListeners ( child );
        }
    }
    private void removeListeners ( Container container ) {
        container.removeContainerListener ( this );
        container.removeLifecycleListener ( this );
        for ( Container child : container.findChildren() ) {
            removeListeners ( child );
        }
    }
}
