package org.apache.catalina.mapper;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import org.apache.catalina.Host;
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
