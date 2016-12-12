package org.apache.catalina.mapper;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.Ascii;
import org.apache.catalina.WebResource;
import javax.servlet.http.MappingMatch;
import org.apache.tomcat.util.buf.CharChunk;
import java.io.IOException;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.catalina.Wrapper;
import org.apache.catalina.WebResourceRoot;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.catalina.Host;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.catalina.Context;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public final class Mapper {
    private static final Log log;
    private static final StringManager sm;
    volatile MappedHost[] hosts;
    private String defaultHostName;
    private volatile MappedHost defaultHost;
    private final Map<Context, ContextVersion> contextObjectToContextVersionMap;
    public Mapper() {
        this.hosts = new MappedHost[0];
        this.defaultHostName = null;
        this.defaultHost = null;
        this.contextObjectToContextVersionMap = new ConcurrentHashMap<Context, ContextVersion>();
    }
    public synchronized void setDefaultHostName ( final String defaultHostName ) {
        this.defaultHostName = renameWildcardHost ( defaultHostName );
        if ( this.defaultHostName == null ) {
            this.defaultHost = null;
        } else {
            this.defaultHost = exactFind ( this.hosts, this.defaultHostName );
        }
    }
    public synchronized void addHost ( String name, final String[] aliases, final Host host ) {
        name = renameWildcardHost ( name );
        final MappedHost[] newHosts = new MappedHost[this.hosts.length + 1];
        MappedHost newHost = new MappedHost ( name, host );
        if ( insertMap ( ( MapElement<Object>[] ) this.hosts, ( MapElement<Object>[] ) newHosts, ( MapElement<Object> ) newHost ) ) {
            this.hosts = newHosts;
            if ( newHost.name.equals ( this.defaultHostName ) ) {
                this.defaultHost = newHost;
            }
            if ( Mapper.log.isDebugEnabled() ) {
                Mapper.log.debug ( Mapper.sm.getString ( "mapper.addHost.success", name ) );
            }
        } else {
            final MappedHost duplicate = this.hosts[find ( ( MapElement<Object>[] ) this.hosts, name )];
            if ( duplicate.object != host ) {
                Mapper.log.error ( Mapper.sm.getString ( "mapper.duplicateHost", name, duplicate.getRealHostName() ) );
                return;
            }
            if ( Mapper.log.isDebugEnabled() ) {
                Mapper.log.debug ( Mapper.sm.getString ( "mapper.addHost.sameHost", name ) );
            }
            newHost = duplicate;
        }
        final List<MappedHost> newAliases = new ArrayList<MappedHost> ( aliases.length );
        for ( String alias : aliases ) {
            alias = renameWildcardHost ( alias );
            final MappedHost newAlias = new MappedHost ( alias, newHost );
            if ( this.addHostAliasImpl ( newAlias ) ) {
                newAliases.add ( newAlias );
            }
        }
        newHost.addAliases ( newAliases );
    }
    public synchronized void removeHost ( String name ) {
        name = renameWildcardHost ( name );
        final MappedHost host = exactFind ( this.hosts, name );
        if ( host == null || host.isAlias() ) {
            return;
        }
        final MappedHost[] newHosts = this.hosts.clone();
        int j = 0;
        for ( int i = 0; i < newHosts.length; ++i ) {
            if ( newHosts[i].getRealHost() != host ) {
                newHosts[j++] = newHosts[i];
            }
        }
        this.hosts = Arrays.copyOf ( newHosts, j );
    }
    public synchronized void addHostAlias ( final String name, String alias ) {
        final MappedHost realHost = exactFind ( this.hosts, name );
        if ( realHost == null ) {
            return;
        }
        alias = renameWildcardHost ( alias );
        final MappedHost newAlias = new MappedHost ( alias, realHost );
        if ( this.addHostAliasImpl ( newAlias ) ) {
            realHost.addAlias ( newAlias );
        }
    }
    private synchronized boolean addHostAliasImpl ( final MappedHost newAlias ) {
        final MappedHost[] newHosts = new MappedHost[this.hosts.length + 1];
        if ( insertMap ( ( MapElement<Object>[] ) this.hosts, ( MapElement<Object>[] ) newHosts, ( MapElement<Object> ) newAlias ) ) {
            this.hosts = newHosts;
            if ( newAlias.name.equals ( this.defaultHostName ) ) {
                this.defaultHost = newAlias;
            }
            if ( Mapper.log.isDebugEnabled() ) {
                Mapper.log.debug ( Mapper.sm.getString ( "mapper.addHostAlias.success", newAlias.name, newAlias.getRealHostName() ) );
            }
            return true;
        }
        final MappedHost duplicate = this.hosts[find ( ( MapElement<Object>[] ) this.hosts, newAlias.name )];
        if ( duplicate.getRealHost() == newAlias.getRealHost() ) {
            if ( Mapper.log.isDebugEnabled() ) {
                Mapper.log.debug ( Mapper.sm.getString ( "mapper.addHostAlias.sameHost", newAlias.name, newAlias.getRealHostName() ) );
            }
            return false;
        }
        Mapper.log.error ( Mapper.sm.getString ( "mapper.duplicateHostAlias", newAlias.name, newAlias.getRealHostName(), duplicate.getRealHostName() ) );
        return false;
    }
    public synchronized void removeHostAlias ( String alias ) {
        alias = renameWildcardHost ( alias );
        final MappedHost hostMapping = exactFind ( this.hosts, alias );
        if ( hostMapping == null || !hostMapping.isAlias() ) {
            return;
        }
        final MappedHost[] newHosts = new MappedHost[this.hosts.length - 1];
        if ( removeMap ( ( MapElement<Object>[] ) this.hosts, ( MapElement<Object>[] ) newHosts, alias ) ) {
            this.hosts = newHosts;
            hostMapping.getRealHost().removeAlias ( hostMapping );
        }
    }
    private void updateContextList ( final MappedHost realHost, final ContextList newContextList ) {
        realHost.contextList = newContextList;
        for ( final MappedHost alias : realHost.getAliases() ) {
            alias.contextList = newContextList;
        }
    }
    public void addContextVersion ( String hostName, final Host host, final String path, final String version, final Context context, final String[] welcomeResources, final WebResourceRoot resources, final Collection<WrapperMappingInfo> wrappers ) {
        hostName = renameWildcardHost ( hostName );
        MappedHost mappedHost = exactFind ( this.hosts, hostName );
        if ( mappedHost == null ) {
            this.addHost ( hostName, new String[0], host );
            mappedHost = exactFind ( this.hosts, hostName );
            if ( mappedHost == null ) {
                Mapper.log.error ( "No host found: " + hostName );
                return;
            }
        }
        if ( mappedHost.isAlias() ) {
            Mapper.log.error ( "No host found: " + hostName );
            return;
        }
        final int slashCount = slashCount ( path );
        synchronized ( mappedHost ) {
            final ContextVersion newContextVersion = new ContextVersion ( version, path, slashCount, context, resources, welcomeResources );
            if ( wrappers != null ) {
                this.addWrappers ( newContextVersion, wrappers );
            }
            final ContextList contextList = mappedHost.contextList;
            MappedContext mappedContext = exactFind ( contextList.contexts, path );
            if ( mappedContext == null ) {
                mappedContext = new MappedContext ( path, newContextVersion );
                final ContextList newContextList = contextList.addContext ( mappedContext, slashCount );
                if ( newContextList != null ) {
                    this.updateContextList ( mappedHost, newContextList );
                    this.contextObjectToContextVersionMap.put ( context, newContextVersion );
                }
            } else {
                final ContextVersion[] contextVersions = mappedContext.versions;
                final ContextVersion[] newContextVersions = new ContextVersion[contextVersions.length + 1];
                if ( insertMap ( ( MapElement<Object>[] ) contextVersions, ( MapElement<Object>[] ) newContextVersions, ( MapElement<Object> ) newContextVersion ) ) {
                    mappedContext.versions = newContextVersions;
                    this.contextObjectToContextVersionMap.put ( context, newContextVersion );
                } else {
                    final int pos = find ( ( MapElement<Object>[] ) contextVersions, version );
                    if ( pos >= 0 && contextVersions[pos].name.equals ( version ) ) {
                        contextVersions[pos] = newContextVersion;
                        this.contextObjectToContextVersionMap.put ( context, newContextVersion );
                    }
                }
            }
        }
    }
    public void removeContextVersion ( final Context ctxt, String hostName, final String path, final String version ) {
        hostName = renameWildcardHost ( hostName );
        this.contextObjectToContextVersionMap.remove ( ctxt );
        final MappedHost host = exactFind ( this.hosts, hostName );
        if ( host == null || host.isAlias() ) {
            return;
        }
        synchronized ( host ) {
            final ContextList contextList = host.contextList;
            final MappedContext context = exactFind ( contextList.contexts, path );
            if ( context == null ) {
                return;
            }
            final ContextVersion[] contextVersions = context.versions;
            final ContextVersion[] newContextVersions = new ContextVersion[contextVersions.length - 1];
            if ( removeMap ( ( MapElement<Object>[] ) contextVersions, ( MapElement<Object>[] ) newContextVersions, version ) ) {
                if ( newContextVersions.length == 0 ) {
                    final ContextList newContextList = contextList.removeContext ( path );
                    if ( newContextList != null ) {
                        this.updateContextList ( host, newContextList );
                    }
                } else {
                    context.versions = newContextVersions;
                }
            }
        }
    }
    public void pauseContextVersion ( final Context ctxt, String hostName, final String contextPath, final String version ) {
        hostName = renameWildcardHost ( hostName );
        final ContextVersion contextVersion = this.findContextVersion ( hostName, contextPath, version, true );
        if ( contextVersion == null || !ctxt.equals ( contextVersion.object ) ) {
            return;
        }
        contextVersion.markPaused();
    }
    private ContextVersion findContextVersion ( final String hostName, final String contextPath, final String version, final boolean silent ) {
        final MappedHost host = exactFind ( this.hosts, hostName );
        if ( host == null || host.isAlias() ) {
            if ( !silent ) {
                Mapper.log.error ( "No host found: " + hostName );
            }
            return null;
        }
        final MappedContext context = exactFind ( host.contextList.contexts, contextPath );
        if ( context == null ) {
            if ( !silent ) {
                Mapper.log.error ( "No context found: " + contextPath );
            }
            return null;
        }
        final ContextVersion contextVersion = exactFind ( context.versions, version );
        if ( contextVersion == null ) {
            if ( !silent ) {
                Mapper.log.error ( "No context version found: " + contextPath + " " + version );
            }
            return null;
        }
        return contextVersion;
    }
    public void addWrapper ( String hostName, final String contextPath, final String version, final String path, final Wrapper wrapper, final boolean jspWildCard, final boolean resourceOnly ) {
        hostName = renameWildcardHost ( hostName );
        final ContextVersion contextVersion = this.findContextVersion ( hostName, contextPath, version, false );
        if ( contextVersion == null ) {
            return;
        }
        this.addWrapper ( contextVersion, path, wrapper, jspWildCard, resourceOnly );
    }
    public void addWrappers ( String hostName, final String contextPath, final String version, final Collection<WrapperMappingInfo> wrappers ) {
        hostName = renameWildcardHost ( hostName );
        final ContextVersion contextVersion = this.findContextVersion ( hostName, contextPath, version, false );
        if ( contextVersion == null ) {
            return;
        }
        this.addWrappers ( contextVersion, wrappers );
    }
    private void addWrappers ( final ContextVersion contextVersion, final Collection<WrapperMappingInfo> wrappers ) {
        for ( final WrapperMappingInfo wrapper : wrappers ) {
            this.addWrapper ( contextVersion, wrapper.getMapping(), wrapper.getWrapper(), wrapper.isJspWildCard(), wrapper.isResourceOnly() );
        }
    }
    protected void addWrapper ( final ContextVersion context, final String path, final Wrapper wrapper, final boolean jspWildCard, final boolean resourceOnly ) {
        synchronized ( context ) {
            if ( path.endsWith ( "/*" ) ) {
                final String name = path.substring ( 0, path.length() - 2 );
                final MappedWrapper newWrapper = new MappedWrapper ( name, wrapper, jspWildCard, resourceOnly );
                final MappedWrapper[] oldWrappers = context.wildcardWrappers;
                final MappedWrapper[] newWrappers = new MappedWrapper[oldWrappers.length + 1];
                if ( insertMap ( ( MapElement<Object>[] ) oldWrappers, ( MapElement<Object>[] ) newWrappers, ( MapElement<Object> ) newWrapper ) ) {
                    context.wildcardWrappers = newWrappers;
                    final int slashCount = slashCount ( newWrapper.name );
                    if ( slashCount > context.nesting ) {
                        context.nesting = slashCount;
                    }
                }
            } else if ( path.startsWith ( "*." ) ) {
                final String name = path.substring ( 2 );
                final MappedWrapper newWrapper = new MappedWrapper ( name, wrapper, jspWildCard, resourceOnly );
                final MappedWrapper[] oldWrappers = context.extensionWrappers;
                final MappedWrapper[] newWrappers = new MappedWrapper[oldWrappers.length + 1];
                if ( insertMap ( ( MapElement<Object>[] ) oldWrappers, ( MapElement<Object>[] ) newWrappers, ( MapElement<Object> ) newWrapper ) ) {
                    context.extensionWrappers = newWrappers;
                }
            } else if ( path.equals ( "/" ) ) {
                final MappedWrapper newWrapper2 = new MappedWrapper ( "", wrapper, jspWildCard, resourceOnly );
                context.defaultWrapper = newWrapper2;
            } else {
                String name;
                if ( path.length() == 0 ) {
                    name = "/";
                } else {
                    name = path;
                }
                final MappedWrapper newWrapper = new MappedWrapper ( name, wrapper, jspWildCard, resourceOnly );
                final MappedWrapper[] oldWrappers = context.exactWrappers;
                final MappedWrapper[] newWrappers = new MappedWrapper[oldWrappers.length + 1];
                if ( insertMap ( ( MapElement<Object>[] ) oldWrappers, ( MapElement<Object>[] ) newWrappers, ( MapElement<Object> ) newWrapper ) ) {
                    context.exactWrappers = newWrappers;
                }
            }
        }
    }
    public void removeWrapper ( String hostName, final String contextPath, final String version, final String path ) {
        hostName = renameWildcardHost ( hostName );
        final ContextVersion contextVersion = this.findContextVersion ( hostName, contextPath, version, true );
        if ( contextVersion == null || contextVersion.isPaused() ) {
            return;
        }
        this.removeWrapper ( contextVersion, path );
    }
    protected void removeWrapper ( final ContextVersion context, final String path ) {
        if ( Mapper.log.isDebugEnabled() ) {
            Mapper.log.debug ( Mapper.sm.getString ( "mapper.removeWrapper", context.name, path ) );
        }
        synchronized ( context ) {
            if ( path.endsWith ( "/*" ) ) {
                final String name = path.substring ( 0, path.length() - 2 );
                final MappedWrapper[] oldWrappers = context.wildcardWrappers;
                if ( oldWrappers.length == 0 ) {
                    return;
                }
                final MappedWrapper[] newWrappers = new MappedWrapper[oldWrappers.length - 1];
                if ( removeMap ( ( MapElement<Object>[] ) oldWrappers, ( MapElement<Object>[] ) newWrappers, name ) ) {
                    context.nesting = 0;
                    for ( int i = 0; i < newWrappers.length; ++i ) {
                        final int slashCount = slashCount ( newWrappers[i].name );
                        if ( slashCount > context.nesting ) {
                            context.nesting = slashCount;
                        }
                    }
                    context.wildcardWrappers = newWrappers;
                }
            } else if ( path.startsWith ( "*." ) ) {
                final String name = path.substring ( 2 );
                final MappedWrapper[] oldWrappers = context.extensionWrappers;
                if ( oldWrappers.length == 0 ) {
                    return;
                }
                final MappedWrapper[] newWrappers = new MappedWrapper[oldWrappers.length - 1];
                if ( removeMap ( ( MapElement<Object>[] ) oldWrappers, ( MapElement<Object>[] ) newWrappers, name ) ) {
                    context.extensionWrappers = newWrappers;
                }
            } else if ( path.equals ( "/" ) ) {
                context.defaultWrapper = null;
            } else {
                String name;
                if ( path.length() == 0 ) {
                    name = "/";
                } else {
                    name = path;
                }
                final MappedWrapper[] oldWrappers = context.exactWrappers;
                if ( oldWrappers.length == 0 ) {
                    return;
                }
                final MappedWrapper[] newWrappers = new MappedWrapper[oldWrappers.length - 1];
                if ( removeMap ( ( MapElement<Object>[] ) oldWrappers, ( MapElement<Object>[] ) newWrappers, name ) ) {
                    context.exactWrappers = newWrappers;
                }
            }
        }
    }
    public void addWelcomeFile ( String hostName, final String contextPath, final String version, final String welcomeFile ) {
        hostName = renameWildcardHost ( hostName );
        final ContextVersion contextVersion = this.findContextVersion ( hostName, contextPath, version, false );
        if ( contextVersion == null ) {
            return;
        }
        final int len = contextVersion.welcomeResources.length + 1;
        final String[] newWelcomeResources = new String[len];
        System.arraycopy ( contextVersion.welcomeResources, 0, newWelcomeResources, 0, len - 1 );
        newWelcomeResources[len - 1] = welcomeFile;
        contextVersion.welcomeResources = newWelcomeResources;
    }
    public void removeWelcomeFile ( String hostName, final String contextPath, final String version, final String welcomeFile ) {
        hostName = renameWildcardHost ( hostName );
        final ContextVersion contextVersion = this.findContextVersion ( hostName, contextPath, version, false );
        if ( contextVersion == null || contextVersion.isPaused() ) {
            return;
        }
        int match = -1;
        for ( int i = 0; i < contextVersion.welcomeResources.length; ++i ) {
            if ( welcomeFile.equals ( contextVersion.welcomeResources[i] ) ) {
                match = i;
                break;
            }
        }
        if ( match > -1 ) {
            final int len = contextVersion.welcomeResources.length - 1;
            final String[] newWelcomeResources = new String[len];
            System.arraycopy ( contextVersion.welcomeResources, 0, newWelcomeResources, 0, match );
            if ( match < len ) {
                System.arraycopy ( contextVersion.welcomeResources, match + 1, newWelcomeResources, match, len - match );
            }
            contextVersion.welcomeResources = newWelcomeResources;
        }
    }
    public void clearWelcomeFiles ( String hostName, final String contextPath, final String version ) {
        hostName = renameWildcardHost ( hostName );
        final ContextVersion contextVersion = this.findContextVersion ( hostName, contextPath, version, false );
        if ( contextVersion == null ) {
            return;
        }
        contextVersion.welcomeResources = new String[0];
    }
    public void map ( final MessageBytes host, final MessageBytes uri, final String version, final MappingData mappingData ) throws IOException {
        if ( host.isNull() ) {
            host.getCharChunk().append ( this.defaultHostName );
        }
        host.toChars();
        uri.toChars();
        this.internalMap ( host.getCharChunk(), uri.getCharChunk(), version, mappingData );
    }
    public void map ( final Context context, final MessageBytes uri, final MappingData mappingData ) throws IOException {
        final ContextVersion contextVersion = this.contextObjectToContextVersionMap.get ( context );
        uri.toChars();
        final CharChunk uricc = uri.getCharChunk();
        uricc.setLimit ( -1 );
        this.internalMapWrapper ( contextVersion, uricc, mappingData );
    }
    private final void internalMap ( final CharChunk host, final CharChunk uri, final String version, final MappingData mappingData ) throws IOException {
        if ( mappingData.host != null ) {
            throw new AssertionError();
        }
        uri.setLimit ( -1 );
        final MappedHost[] hosts = this.hosts;
        MappedHost mappedHost = exactFindIgnoreCase ( hosts, host );
        if ( mappedHost == null ) {
            final int firstDot = host.indexOf ( '.' );
            if ( firstDot > -1 ) {
                final int offset = host.getOffset();
                try {
                    host.setOffset ( firstDot + offset );
                    mappedHost = exactFindIgnoreCase ( hosts, host );
                } finally {
                    host.setOffset ( offset );
                }
            }
            if ( mappedHost == null ) {
                mappedHost = this.defaultHost;
                if ( mappedHost == null ) {
                    return;
                }
            }
        }
        mappingData.host = ( Host ) mappedHost.object;
        final ContextList contextList = mappedHost.contextList;
        final MappedContext[] contexts = contextList.contexts;
        int pos = find ( ( MapElement<Object>[] ) contexts, uri );
        if ( pos == -1 ) {
            return;
        }
        int lastSlash = -1;
        final int uriEnd = uri.getEnd();
        int length = -1;
        boolean found = false;
        MappedContext context = null;
        while ( pos >= 0 ) {
            context = contexts[pos];
            if ( uri.startsWith ( context.name ) ) {
                length = context.name.length();
                if ( uri.getLength() == length ) {
                    found = true;
                    break;
                }
                if ( uri.startsWithIgnoreCase ( "/", length ) ) {
                    found = true;
                    break;
                }
            }
            if ( lastSlash == -1 ) {
                lastSlash = nthSlash ( uri, contextList.nesting + 1 );
            } else {
                lastSlash = lastSlash ( uri );
            }
            uri.setEnd ( lastSlash );
            pos = find ( ( MapElement<Object>[] ) contexts, uri );
        }
        uri.setEnd ( uriEnd );
        if ( !found ) {
            if ( contexts[0].name.equals ( "" ) ) {
                context = contexts[0];
            } else {
                context = null;
            }
        }
        if ( context == null ) {
            return;
        }
        mappingData.contextPath.setString ( context.name );
        ContextVersion contextVersion = null;
        final ContextVersion[] contextVersions = context.versions;
        final int versionCount = contextVersions.length;
        if ( versionCount > 1 ) {
            final Context[] contextObjects = new Context[contextVersions.length];
            for ( int i = 0; i < contextObjects.length; ++i ) {
                contextObjects[i] = ( Context ) contextVersions[i].object;
            }
            mappingData.contexts = contextObjects;
            if ( version != null ) {
                contextVersion = exactFind ( contextVersions, version );
            }
        }
        if ( contextVersion == null ) {
            contextVersion = contextVersions[versionCount - 1];
        }
        mappingData.context = ( Context ) contextVersion.object;
        mappingData.contextSlashCount = contextVersion.slashCount;
        if ( !contextVersion.isPaused() ) {
            this.internalMapWrapper ( contextVersion, uri, mappingData );
        }
    }
    private final void internalMapWrapper ( final ContextVersion contextVersion, final CharChunk path, final MappingData mappingData ) throws IOException {
        final int pathOffset = path.getOffset();
        int pathEnd = path.getEnd();
        boolean noServletPath = false;
        final int length = contextVersion.path.length();
        if ( length == pathEnd - pathOffset ) {
            noServletPath = true;
        }
        final int servletPath = pathOffset + length;
        path.setOffset ( servletPath );
        final MappedWrapper[] exactWrappers = contextVersion.exactWrappers;
        this.internalMapExactWrapper ( exactWrappers, path, mappingData );
        boolean checkJspWelcomeFiles = false;
        final MappedWrapper[] wildcardWrappers = contextVersion.wildcardWrappers;
        if ( mappingData.wrapper == null ) {
            this.internalMapWildcardWrapper ( wildcardWrappers, contextVersion.nesting, path, mappingData );
            if ( mappingData.wrapper != null && mappingData.jspWildCard ) {
                final char[] buf = path.getBuffer();
                if ( buf[pathEnd - 1] == '/' ) {
                    mappingData.wrapper = null;
                    checkJspWelcomeFiles = true;
                } else {
                    mappingData.wrapperPath.setChars ( buf, path.getStart(), path.getLength() );
                    mappingData.pathInfo.recycle();
                }
            }
        }
        if ( mappingData.wrapper == null && noServletPath && ( ( Context ) contextVersion.object ).getMapperContextRootRedirectEnabled() ) {
            path.append ( '/' );
            pathEnd = path.getEnd();
            mappingData.redirectPath.setChars ( path.getBuffer(), pathOffset, pathEnd - pathOffset );
            path.setEnd ( pathEnd - 1 );
            return;
        }
        final MappedWrapper[] extensionWrappers = contextVersion.extensionWrappers;
        if ( mappingData.wrapper == null && !checkJspWelcomeFiles ) {
            this.internalMapExtensionWrapper ( extensionWrappers, path, mappingData, true );
        }
        if ( mappingData.wrapper == null ) {
            boolean checkWelcomeFiles = checkJspWelcomeFiles;
            if ( !checkWelcomeFiles ) {
                final char[] buf2 = path.getBuffer();
                checkWelcomeFiles = ( buf2[pathEnd - 1] == '/' );
            }
            if ( checkWelcomeFiles ) {
                for ( int i = 0; i < contextVersion.welcomeResources.length && mappingData.wrapper == null; ++i ) {
                    path.setOffset ( pathOffset );
                    path.setEnd ( pathEnd );
                    path.append ( contextVersion.welcomeResources[i], 0, contextVersion.welcomeResources[i].length() );
                    path.setOffset ( servletPath );
                    this.internalMapExactWrapper ( exactWrappers, path, mappingData );
                    if ( mappingData.wrapper == null ) {
                        this.internalMapWildcardWrapper ( wildcardWrappers, contextVersion.nesting, path, mappingData );
                    }
                    if ( mappingData.wrapper == null && contextVersion.resources != null ) {
                        final String pathStr = path.toString();
                        final WebResource file = contextVersion.resources.getResource ( pathStr );
                        if ( file != null && file.isFile() ) {
                            this.internalMapExtensionWrapper ( extensionWrappers, path, mappingData, true );
                            if ( mappingData.wrapper == null && contextVersion.defaultWrapper != null ) {
                                mappingData.wrapper = ( Wrapper ) contextVersion.defaultWrapper.object;
                                mappingData.requestPath.setChars ( path.getBuffer(), path.getStart(), path.getLength() );
                                mappingData.wrapperPath.setChars ( path.getBuffer(), path.getStart(), path.getLength() );
                                mappingData.requestPath.setString ( pathStr );
                                mappingData.wrapperPath.setString ( pathStr );
                            }
                        }
                    }
                }
                path.setOffset ( servletPath );
                path.setEnd ( pathEnd );
            }
        }
        if ( mappingData.wrapper == null ) {
            boolean checkWelcomeFiles = checkJspWelcomeFiles;
            if ( !checkWelcomeFiles ) {
                final char[] buf2 = path.getBuffer();
                checkWelcomeFiles = ( buf2[pathEnd - 1] == '/' );
            }
            if ( checkWelcomeFiles ) {
                for ( int i = 0; i < contextVersion.welcomeResources.length && mappingData.wrapper == null; ++i ) {
                    path.setOffset ( pathOffset );
                    path.setEnd ( pathEnd );
                    path.append ( contextVersion.welcomeResources[i], 0, contextVersion.welcomeResources[i].length() );
                    path.setOffset ( servletPath );
                    this.internalMapExtensionWrapper ( extensionWrappers, path, mappingData, false );
                }
                path.setOffset ( servletPath );
                path.setEnd ( pathEnd );
            }
        }
        if ( mappingData.wrapper == null && !checkJspWelcomeFiles ) {
            if ( contextVersion.defaultWrapper != null ) {
                mappingData.wrapper = ( Wrapper ) contextVersion.defaultWrapper.object;
                mappingData.requestPath.setChars ( path.getBuffer(), path.getStart(), path.getLength() );
                mappingData.wrapperPath.setChars ( path.getBuffer(), path.getStart(), path.getLength() );
                mappingData.matchType = MappingMatch.DEFAULT;
            }
            final char[] buf3 = path.getBuffer();
            if ( contextVersion.resources != null && buf3[pathEnd - 1] != '/' ) {
                final String pathStr2 = path.toString();
                WebResource file2;
                if ( pathStr2.length() == 0 ) {
                    file2 = contextVersion.resources.getResource ( "/" );
                } else {
                    file2 = contextVersion.resources.getResource ( pathStr2 );
                }
                if ( file2 != null && file2.isDirectory() && ( ( Context ) contextVersion.object ).getMapperDirectoryRedirectEnabled() ) {
                    path.setOffset ( pathOffset );
                    path.append ( '/' );
                    mappingData.redirectPath.setChars ( path.getBuffer(), path.getStart(), path.getLength() );
                } else {
                    mappingData.requestPath.setString ( pathStr2 );
                    mappingData.wrapperPath.setString ( pathStr2 );
                }
            }
        }
        path.setOffset ( pathOffset );
        path.setEnd ( pathEnd );
    }
    private final void internalMapExactWrapper ( final MappedWrapper[] wrappers, final CharChunk path, final MappingData mappingData ) {
        final MappedWrapper wrapper = exactFind ( wrappers, path );
        if ( wrapper != null ) {
            mappingData.requestPath.setString ( wrapper.name );
            mappingData.wrapper = ( Wrapper ) wrapper.object;
            if ( path.equals ( "/" ) ) {
                mappingData.pathInfo.setString ( "/" );
                mappingData.wrapperPath.setString ( "" );
                mappingData.contextPath.setString ( "" );
                mappingData.matchType = MappingMatch.CONTEXT_ROOT;
            } else {
                mappingData.wrapperPath.setString ( wrapper.name );
                mappingData.matchType = MappingMatch.EXACT;
            }
        }
    }
    private final void internalMapWildcardWrapper ( final MappedWrapper[] wrappers, final int nesting, final CharChunk path, final MappingData mappingData ) {
        final int pathEnd = path.getEnd();
        int lastSlash = -1;
        int length = -1;
        int pos = find ( ( MapElement<Object>[] ) wrappers, path );
        if ( pos != -1 ) {
            boolean found = false;
            while ( pos >= 0 ) {
                if ( path.startsWith ( wrappers[pos].name ) ) {
                    length = wrappers[pos].name.length();
                    if ( path.getLength() == length ) {
                        found = true;
                        break;
                    }
                    if ( path.startsWithIgnoreCase ( "/", length ) ) {
                        found = true;
                        break;
                    }
                }
                if ( lastSlash == -1 ) {
                    lastSlash = nthSlash ( path, nesting + 1 );
                } else {
                    lastSlash = lastSlash ( path );
                }
                path.setEnd ( lastSlash );
                pos = find ( ( MapElement<Object>[] ) wrappers, path );
            }
            path.setEnd ( pathEnd );
            if ( found ) {
                mappingData.wrapperPath.setString ( wrappers[pos].name );
                if ( path.getLength() > length ) {
                    mappingData.pathInfo.setChars ( path.getBuffer(), path.getOffset() + length, path.getLength() - length );
                }
                mappingData.requestPath.setChars ( path.getBuffer(), path.getOffset(), path.getLength() );
                mappingData.wrapper = ( Wrapper ) wrappers[pos].object;
                mappingData.jspWildCard = wrappers[pos].jspWildCard;
                mappingData.matchType = MappingMatch.PATH;
            }
        }
    }
    private final void internalMapExtensionWrapper ( final MappedWrapper[] wrappers, final CharChunk path, final MappingData mappingData, final boolean resourceExpected ) {
        final char[] buf = path.getBuffer();
        final int pathEnd = path.getEnd();
        final int servletPath = path.getOffset();
        int slash = -1;
        for ( int i = pathEnd - 1; i >= servletPath; --i ) {
            if ( buf[i] == '/' ) {
                slash = i;
                break;
            }
        }
        if ( slash >= 0 ) {
            int period = -1;
            for ( int j = pathEnd - 1; j > slash; --j ) {
                if ( buf[j] == '.' ) {
                    period = j;
                    break;
                }
            }
            if ( period >= 0 ) {
                path.setOffset ( period + 1 );
                path.setEnd ( pathEnd );
                final MappedWrapper wrapper = exactFind ( wrappers, path );
                if ( wrapper != null && ( resourceExpected || !wrapper.resourceOnly ) ) {
                    mappingData.wrapperPath.setChars ( buf, servletPath, pathEnd - servletPath );
                    mappingData.requestPath.setChars ( buf, servletPath, pathEnd - servletPath );
                    mappingData.wrapper = ( Wrapper ) wrapper.object;
                    mappingData.matchType = MappingMatch.EXTENSION;
                }
                path.setOffset ( servletPath );
                path.setEnd ( pathEnd );
            }
        }
    }
    private static final <T> int find ( final MapElement<T>[] map, final CharChunk name ) {
        return find ( map, name, name.getStart(), name.getEnd() );
    }
    private static final <T> int find ( final MapElement<T>[] map, final CharChunk name, final int start, final int end ) {
        int a = 0;
        int b = map.length - 1;
        if ( b == -1 ) {
            return -1;
        }
        if ( compare ( name, start, end, map[0].name ) < 0 ) {
            return -1;
        }
        if ( b == 0 ) {
            return 0;
        }
        int i = 0;
        while ( true ) {
            i = ( b + a ) / 2;
            final int result = compare ( name, start, end, map[i].name );
            if ( result == 1 ) {
                a = i;
            } else {
                if ( result == 0 ) {
                    return i;
                }
                b = i;
            }
            if ( b - a == 1 ) {
                final int result2 = compare ( name, start, end, map[b].name );
                if ( result2 < 0 ) {
                    return a;
                }
                return b;
            }
        }
    }
    private static final <T> int findIgnoreCase ( final MapElement<T>[] map, final CharChunk name ) {
        return findIgnoreCase ( map, name, name.getStart(), name.getEnd() );
    }
    private static final <T> int findIgnoreCase ( final MapElement<T>[] map, final CharChunk name, final int start, final int end ) {
        int a = 0;
        int b = map.length - 1;
        if ( b == -1 ) {
            return -1;
        }
        if ( compareIgnoreCase ( name, start, end, map[0].name ) < 0 ) {
            return -1;
        }
        if ( b == 0 ) {
            return 0;
        }
        int i = 0;
        while ( true ) {
            i = ( b + a ) / 2;
            final int result = compareIgnoreCase ( name, start, end, map[i].name );
            if ( result == 1 ) {
                a = i;
            } else {
                if ( result == 0 ) {
                    return i;
                }
                b = i;
            }
            if ( b - a == 1 ) {
                final int result2 = compareIgnoreCase ( name, start, end, map[b].name );
                if ( result2 < 0 ) {
                    return a;
                }
                return b;
            }
        }
    }
    private static final <T> int find ( final MapElement<T>[] map, final String name ) {
        int a = 0;
        int b = map.length - 1;
        if ( b == -1 ) {
            return -1;
        }
        if ( name.compareTo ( map[0].name ) < 0 ) {
            return -1;
        }
        if ( b == 0 ) {
            return 0;
        }
        int i = 0;
        while ( true ) {
            i = ( b + a ) / 2;
            final int result = name.compareTo ( map[i].name );
            if ( result > 0 ) {
                a = i;
            } else {
                if ( result == 0 ) {
                    return i;
                }
                b = i;
            }
            if ( b - a == 1 ) {
                final int result2 = name.compareTo ( map[b].name );
                if ( result2 < 0 ) {
                    return a;
                }
                return b;
            }
        }
    }
    private static final <T, E extends MapElement<T>> E exactFind ( final E[] map, final String name ) {
        final int pos = find ( ( MapElement<Object>[] ) map, name );
        if ( pos >= 0 ) {
            final E result = map[pos];
            if ( name.equals ( result.name ) ) {
                return result;
            }
        }
        return null;
    }
    private static final <T, E extends MapElement<T>> E exactFind ( final E[] map, final CharChunk name ) {
        final int pos = find ( ( MapElement<Object>[] ) map, name );
        if ( pos >= 0 ) {
            final E result = map[pos];
            if ( name.equals ( result.name ) ) {
                return result;
            }
        }
        return null;
    }
    private static final <T, E extends MapElement<T>> E exactFindIgnoreCase ( final E[] map, final CharChunk name ) {
        final int pos = findIgnoreCase ( ( MapElement<Object>[] ) map, name );
        if ( pos >= 0 ) {
            final E result = map[pos];
            if ( name.equalsIgnoreCase ( result.name ) ) {
                return result;
            }
        }
        return null;
    }
    private static final int compare ( final CharChunk name, final int start, final int end, final String compareTo ) {
        int result = 0;
        final char[] c = name.getBuffer();
        int len = compareTo.length();
        if ( end - start < len ) {
            len = end - start;
        }
        for ( int i = 0; i < len && result == 0; ++i ) {
            if ( c[i + start] > compareTo.charAt ( i ) ) {
                result = 1;
            } else if ( c[i + start] < compareTo.charAt ( i ) ) {
                result = -1;
            }
        }
        if ( result == 0 ) {
            if ( compareTo.length() > end - start ) {
                result = -1;
            } else if ( compareTo.length() < end - start ) {
                result = 1;
            }
        }
        return result;
    }
    private static final int compareIgnoreCase ( final CharChunk name, final int start, final int end, final String compareTo ) {
        int result = 0;
        final char[] c = name.getBuffer();
        int len = compareTo.length();
        if ( end - start < len ) {
            len = end - start;
        }
        for ( int i = 0; i < len && result == 0; ++i ) {
            if ( Ascii.toLower ( c[i + start] ) > Ascii.toLower ( compareTo.charAt ( i ) ) ) {
                result = 1;
            } else if ( Ascii.toLower ( c[i + start] ) < Ascii.toLower ( compareTo.charAt ( i ) ) ) {
                result = -1;
            }
        }
        if ( result == 0 ) {
            if ( compareTo.length() > end - start ) {
                result = -1;
            } else if ( compareTo.length() < end - start ) {
                result = 1;
            }
        }
        return result;
    }
    private static final int lastSlash ( final CharChunk name ) {
        final char[] c = name.getBuffer();
        final int end = name.getEnd();
        final int start = name.getStart();
        int pos = end;
        while ( pos > start && c[--pos] != '/' ) {}
        return pos;
    }
    private static final int nthSlash ( final CharChunk name, final int n ) {
        final char[] c = name.getBuffer();
        final int end = name.getEnd();
        int pos;
        final int start = pos = name.getStart();
        int count = 0;
        while ( pos < end ) {
            if ( c[pos++] == '/' && ++count == n ) {
                --pos;
                break;
            }
        }
        return pos;
    }
    private static final int slashCount ( final String name ) {
        int pos = -1;
        int count = 0;
        while ( ( pos = name.indexOf ( 47, pos + 1 ) ) != -1 ) {
            ++count;
        }
        return count;
    }
    private static final <T> boolean insertMap ( final MapElement<T>[] oldMap, final MapElement<T>[] newMap, final MapElement<T> newElement ) {
        final int pos = find ( oldMap, newElement.name );
        if ( pos != -1 && newElement.name.equals ( oldMap[pos].name ) ) {
            return false;
        }
        System.arraycopy ( oldMap, 0, newMap, 0, pos + 1 );
        newMap[pos + 1] = newElement;
        System.arraycopy ( oldMap, pos + 1, newMap, pos + 2, oldMap.length - pos - 1 );
        return true;
    }
    private static final <T> boolean removeMap ( final MapElement<T>[] oldMap, final MapElement<T>[] newMap, final String name ) {
        final int pos = find ( oldMap, name );
        if ( pos != -1 && name.equals ( oldMap[pos].name ) ) {
            System.arraycopy ( oldMap, 0, newMap, 0, pos );
            System.arraycopy ( oldMap, pos + 1, newMap, pos, oldMap.length - pos - 1 );
            return true;
        }
        return false;
    }
    private static String renameWildcardHost ( final String hostName ) {
        if ( hostName.startsWith ( "*." ) ) {
            return hostName.substring ( 1 );
        }
        return hostName;
    }
    static {
        log = LogFactory.getLog ( Mapper.class );
        sm = StringManager.getManager ( Mapper.class );
    }
    protected abstract static class MapElement<T> {
        public final String name;
        public final T object;
        public MapElement ( final String name, final T object ) {
            this.name = name;
            this.object = object;
        }
    }
    protected static final class MappedHost extends MapElement<Host> {
        public volatile ContextList contextList;
        private final MappedHost realHost;
        private final List<MappedHost> aliases;
        public MappedHost ( final String name, final Host host ) {
            super ( name, host );
            this.realHost = this;
            this.contextList = new ContextList();
            this.aliases = new CopyOnWriteArrayList<MappedHost>();
        }
        public MappedHost ( final String alias, final MappedHost realHost ) {
            super ( alias, realHost.object );
            this.realHost = realHost;
            this.contextList = realHost.contextList;
            this.aliases = null;
        }
        public boolean isAlias() {
            return this.realHost != this;
        }
        public MappedHost getRealHost() {
            return this.realHost;
        }
        public String getRealHostName() {
            return this.realHost.name;
        }
        public Collection<MappedHost> getAliases() {
            return this.aliases;
        }
        public void addAlias ( final MappedHost alias ) {
            this.aliases.add ( alias );
        }
        public void addAliases ( final Collection<? extends MappedHost> c ) {
            this.aliases.addAll ( c );
        }
        public void removeAlias ( final MappedHost alias ) {
            this.aliases.remove ( alias );
        }
    }
    protected static final class ContextList {
        public final MappedContext[] contexts;
        public final int nesting;
        public ContextList() {
            this ( new MappedContext[0], 0 );
        }
        private ContextList ( final MappedContext[] contexts, final int nesting ) {
            this.contexts = contexts;
            this.nesting = nesting;
        }
        public ContextList addContext ( final MappedContext mappedContext, final int slashCount ) {
            final MappedContext[] newContexts = new MappedContext[this.contexts.length + 1];
            if ( insertMap ( this.contexts, newContexts, ( MapElement<Object> ) mappedContext ) ) {
                return new ContextList ( newContexts, Math.max ( this.nesting, slashCount ) );
            }
            return null;
        }
        public ContextList removeContext ( final String path ) {
            final MappedContext[] newContexts = new MappedContext[this.contexts.length - 1];
            if ( removeMap ( this.contexts, ( MapElement<Object>[] ) newContexts, path ) ) {
                int newNesting = 0;
                for ( final MappedContext context : newContexts ) {
                    newNesting = Math.max ( newNesting, slashCount ( context.name ) );
                }
                return new ContextList ( newContexts, newNesting );
            }
            return null;
        }
    }
    protected static final class MappedContext extends MapElement<Void> {
        public volatile ContextVersion[] versions;
        public MappedContext ( final String name, final ContextVersion firstVersion ) {
            super ( name, null );
            this.versions = new ContextVersion[] { firstVersion };
        }
    }
    protected static final class ContextVersion extends MapElement<Context> {
        public final String path;
        public final int slashCount;
        public final WebResourceRoot resources;
        public String[] welcomeResources;
        public MappedWrapper defaultWrapper;
        public MappedWrapper[] exactWrappers;
        public MappedWrapper[] wildcardWrappers;
        public MappedWrapper[] extensionWrappers;
        public int nesting;
        private volatile boolean paused;
        public ContextVersion ( final String version, final String path, final int slashCount, final Context context, final WebResourceRoot resources, final String[] welcomeResources ) {
            super ( version, context );
            this.defaultWrapper = null;
            this.exactWrappers = new MappedWrapper[0];
            this.wildcardWrappers = new MappedWrapper[0];
            this.extensionWrappers = new MappedWrapper[0];
            this.nesting = 0;
            this.path = path;
            this.slashCount = slashCount;
            this.resources = resources;
            this.welcomeResources = welcomeResources;
        }
        public boolean isPaused() {
            return this.paused;
        }
        public void markPaused() {
            this.paused = true;
        }
    }
    protected static class MappedWrapper extends MapElement<Wrapper> {
        public final boolean jspWildCard;
        public final boolean resourceOnly;
        public MappedWrapper ( final String name, final Wrapper wrapper, final boolean jspWildCard, final boolean resourceOnly ) {
            super ( name, wrapper );
            this.jspWildCard = jspWildCard;
            this.resourceOnly = resourceOnly;
        }
    }
}
