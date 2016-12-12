package org.apache.catalina.mapper;
import org.apache.catalina.Wrapper;
protected static class MappedWrapper extends MapElement<Wrapper> {
    public final boolean jspWildCard;
    public final boolean resourceOnly;
    public MappedWrapper ( final String name, final Wrapper wrapper, final boolean jspWildCard, final boolean resourceOnly ) {
        super ( name, wrapper );
        this.jspWildCard = jspWildCard;
        this.resourceOnly = resourceOnly;
    }
}
