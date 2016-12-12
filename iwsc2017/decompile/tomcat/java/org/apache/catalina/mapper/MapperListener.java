package org.apache.catalina.mapper;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleEvent;
import java.util.List;
import org.apache.catalina.WebResourceRoot;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.catalina.Wrapper;
import org.apache.catalina.Context;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleState;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Service;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.util.LifecycleMBeanBase;
public class MapperListener extends LifecycleMBeanBase implements ContainerListener, LifecycleListener {
    private static final Log log;
    private final Mapper mapper;
    private final Service service;
    private static final StringManager sm;
    private final String domain;
    public MapperListener ( final Service service ) {
        this.domain = null;
        this.service = service;
        this.mapper = service.getMapper();
    }
    public void startInternal() throws LifecycleException {
        this.setState ( LifecycleState.STARTING );
        final Engine engine = this.service.getContainer();
        if ( engine == null ) {
            return;
        }
        this.findDefaultHost();
        this.addListeners ( engine );
        final Container[] children;
        final Container[] conHosts = children = engine.findChildren();
        for ( final Container conHost : children ) {
            final Host host = ( Host ) conHost;
            if ( !LifecycleState.NEW.equals ( host.getState() ) ) {
                this.registerHost ( host );
            }
        }
    }
    public void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
        final Engine engine = this.service.getContainer();
        if ( engine == null ) {
            return;
        }
        this.removeListeners ( engine );
    }
    @Override
    protected String getDomainInternal() {
        if ( this.service instanceof LifecycleMBeanBase ) {
            return ( ( LifecycleMBeanBase ) this.service ).getDomain();
        }
        return null;
    }
    @Override
    protected String getObjectNameKeyProperties() {
        return "type=Mapper";
    }
    @Override
    public void containerEvent ( final ContainerEvent event ) {
        if ( "addChild".equals ( event.getType() ) ) {
            final Container child = ( Container ) event.getData();
            this.addListeners ( child );
            if ( child.getState().isAvailable() ) {
                if ( child instanceof Host ) {
                    this.registerHost ( ( Host ) child );
                } else if ( child instanceof Context ) {
                    this.registerContext ( ( Context ) child );
                } else if ( child instanceof Wrapper && child.getParent().getState().isAvailable() ) {
                    this.registerWrapper ( ( Wrapper ) child );
                }
            }
        } else if ( "removeChild".equals ( event.getType() ) ) {
            final Container child = ( Container ) event.getData();
            this.removeListeners ( child );
        } else if ( "addAlias".equals ( event.getType() ) ) {
            this.mapper.addHostAlias ( ( ( Host ) event.getSource() ).getName(), event.getData().toString() );
        } else if ( "removeAlias".equals ( event.getType() ) ) {
            this.mapper.removeHostAlias ( event.getData().toString() );
        } else if ( "addMapping".equals ( event.getType() ) ) {
            final Wrapper wrapper = ( Wrapper ) event.getSource();
            final Context context = ( Context ) wrapper.getParent();
            String contextPath = context.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            final String version = context.getWebappVersion();
            final String hostName = context.getParent().getName();
            final String wrapperName = wrapper.getName();
            final String mapping = ( String ) event.getData();
            final boolean jspWildCard = "jsp".equals ( wrapperName ) && mapping.endsWith ( "/*" );
            this.mapper.addWrapper ( hostName, contextPath, version, mapping, wrapper, jspWildCard, context.isResourceOnlyServlet ( wrapperName ) );
        } else if ( "removeMapping".equals ( event.getType() ) ) {
            final Wrapper wrapper = ( Wrapper ) event.getSource();
            final Context context = ( Context ) wrapper.getParent();
            String contextPath = context.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            final String version = context.getWebappVersion();
            final String hostName = context.getParent().getName();
            final String mapping2 = ( String ) event.getData();
            this.mapper.removeWrapper ( hostName, contextPath, version, mapping2 );
        } else if ( "addWelcomeFile".equals ( event.getType() ) ) {
            final Context context2 = ( Context ) event.getSource();
            final String hostName2 = context2.getParent().getName();
            String contextPath = context2.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            final String welcomeFile = ( String ) event.getData();
            this.mapper.addWelcomeFile ( hostName2, contextPath, context2.getWebappVersion(), welcomeFile );
        } else if ( "removeWelcomeFile".equals ( event.getType() ) ) {
            final Context context2 = ( Context ) event.getSource();
            final String hostName2 = context2.getParent().getName();
            String contextPath = context2.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            final String welcomeFile = ( String ) event.getData();
            this.mapper.removeWelcomeFile ( hostName2, contextPath, context2.getWebappVersion(), welcomeFile );
        } else if ( "clearWelcomeFiles".equals ( event.getType() ) ) {
            final Context context2 = ( Context ) event.getSource();
            final String hostName2 = context2.getParent().getName();
            String contextPath = context2.getPath();
            if ( "/".equals ( contextPath ) ) {
                contextPath = "";
            }
            this.mapper.clearWelcomeFiles ( hostName2, contextPath, context2.getWebappVersion() );
        }
    }
    private void findDefaultHost() {
        final Engine engine = this.service.getContainer();
        final String defaultHost = engine.getDefaultHost();
        boolean found = false;
        if ( defaultHost != null && defaultHost.length() > 0 ) {
            final Container[] children;
            final Container[] containers = children = engine.findChildren();
            for ( final Container container : children ) {
                final Host host = ( Host ) container;
                if ( defaultHost.equalsIgnoreCase ( host.getName() ) ) {
                    found = true;
                    break;
                }
                final String[] aliases2;
                final String[] aliases = aliases2 = host.findAliases();
                for ( final String alias : aliases2 ) {
                    if ( defaultHost.equalsIgnoreCase ( alias ) ) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if ( found ) {
            this.mapper.setDefaultHostName ( defaultHost );
        } else {
            MapperListener.log.warn ( MapperListener.sm.getString ( "mapperListener.unknownDefaultHost", defaultHost, this.service ) );
        }
    }
    private void registerHost ( final Host host ) {
        final String[] aliases = host.findAliases();
        this.mapper.addHost ( host.getName(), aliases, host );
        for ( final Container container : host.findChildren() ) {
            if ( container.getState().isAvailable() ) {
                this.registerContext ( ( Context ) container );
            }
        }
        if ( MapperListener.log.isDebugEnabled() ) {
            MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.registerHost", host.getName(), this.domain, this.service ) );
        }
    }
    private void unregisterHost ( final Host host ) {
        final String hostname = host.getName();
        this.mapper.removeHost ( hostname );
        if ( MapperListener.log.isDebugEnabled() ) {
            MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.unregisterHost", hostname, this.domain, this.service ) );
        }
    }
    private void unregisterWrapper ( final Wrapper wrapper ) {
        final Context context = ( Context ) wrapper.getParent();
        String contextPath = context.getPath();
        final String wrapperName = wrapper.getName();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        final String version = context.getWebappVersion();
        final String hostName = context.getParent().getName();
        final String[] mappings2;
        final String[] mappings = mappings2 = wrapper.findMappings();
        for ( final String mapping : mappings2 ) {
            this.mapper.removeWrapper ( hostName, contextPath, version, mapping );
        }
        if ( MapperListener.log.isDebugEnabled() ) {
            MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.unregisterWrapper", wrapperName, contextPath, this.service ) );
        }
    }
    private void registerContext ( final Context context ) {
        String contextPath = context.getPath();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        final Host host = ( Host ) context.getParent();
        final WebResourceRoot resources = context.getResources();
        final String[] welcomeFiles = context.findWelcomeFiles();
        final List<WrapperMappingInfo> wrappers = new ArrayList<WrapperMappingInfo>();
        for ( final Container container : context.findChildren() ) {
            this.prepareWrapperMappingInfo ( context, ( Wrapper ) container, wrappers );
            if ( MapperListener.log.isDebugEnabled() ) {
                MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.registerWrapper", container.getName(), contextPath, this.service ) );
            }
        }
        this.mapper.addContextVersion ( host.getName(), host, contextPath, context.getWebappVersion(), context, welcomeFiles, resources, wrappers );
        if ( MapperListener.log.isDebugEnabled() ) {
            MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.registerContext", contextPath, this.service ) );
        }
    }
    private void unregisterContext ( final Context context ) {
        String contextPath = context.getPath();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        final String hostName = context.getParent().getName();
        if ( context.getPaused() ) {
            if ( MapperListener.log.isDebugEnabled() ) {
                MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.pauseContext", contextPath, this.service ) );
            }
            this.mapper.pauseContextVersion ( context, hostName, contextPath, context.getWebappVersion() );
        } else {
            if ( MapperListener.log.isDebugEnabled() ) {
                MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.unregisterContext", contextPath, this.service ) );
            }
            this.mapper.removeContextVersion ( context, hostName, contextPath, context.getWebappVersion() );
        }
    }
    private void registerWrapper ( final Wrapper wrapper ) {
        final Context context = ( Context ) wrapper.getParent();
        String contextPath = context.getPath();
        if ( "/".equals ( contextPath ) ) {
            contextPath = "";
        }
        final String version = context.getWebappVersion();
        final String hostName = context.getParent().getName();
        final List<WrapperMappingInfo> wrappers = new ArrayList<WrapperMappingInfo>();
        this.prepareWrapperMappingInfo ( context, wrapper, wrappers );
        this.mapper.addWrappers ( hostName, contextPath, version, wrappers );
        if ( MapperListener.log.isDebugEnabled() ) {
            MapperListener.log.debug ( MapperListener.sm.getString ( "mapperListener.registerWrapper", wrapper.getName(), contextPath, this.service ) );
        }
    }
    private void prepareWrapperMappingInfo ( final Context context, final Wrapper wrapper, final List<WrapperMappingInfo> wrappers ) {
        final String wrapperName = wrapper.getName();
        final boolean resourceOnly = context.isResourceOnlyServlet ( wrapperName );
        final String[] mappings2;
        final String[] mappings = mappings2 = wrapper.findMappings();
        for ( final String mapping : mappings2 ) {
            final boolean jspWildCard = wrapperName.equals ( "jsp" ) && mapping.endsWith ( "/*" );
            wrappers.add ( new WrapperMappingInfo ( mapping, wrapper, jspWildCard, resourceOnly ) );
        }
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( event.getType().equals ( "after_start" ) ) {
            final Object obj = event.getSource();
            if ( obj instanceof Wrapper ) {
                final Wrapper w = ( Wrapper ) obj;
                if ( w.getParent().getState().isAvailable() ) {
                    this.registerWrapper ( w );
                }
            } else if ( obj instanceof Context ) {
                final Context c = ( Context ) obj;
                if ( c.getParent().getState().isAvailable() ) {
                    this.registerContext ( c );
                }
            } else if ( obj instanceof Host ) {
                this.registerHost ( ( Host ) obj );
            }
        } else if ( event.getType().equals ( "before_stop" ) ) {
            final Object obj = event.getSource();
            if ( obj instanceof Wrapper ) {
                this.unregisterWrapper ( ( Wrapper ) obj );
            } else if ( obj instanceof Context ) {
                this.unregisterContext ( ( Context ) obj );
            } else if ( obj instanceof Host ) {
                this.unregisterHost ( ( Host ) obj );
            }
        }
    }
    private void addListeners ( final Container container ) {
        container.addContainerListener ( this );
        container.addLifecycleListener ( this );
        for ( final Container child : container.findChildren() ) {
            this.addListeners ( child );
        }
    }
    private void removeListeners ( final Container container ) {
        container.removeContainerListener ( this );
        container.removeLifecycleListener ( this );
        for ( final Container child : container.findChildren() ) {
            this.removeListeners ( child );
        }
    }
    static {
        log = LogFactory.getLog ( MapperListener.class );
        sm = StringManager.getManager ( "org.apache.catalina.mapper" );
    }
}
