package org.apache.catalina.mapper;
protected static final class MappedContext extends MapElement<Void> {
    public volatile ContextVersion[] versions;
    public MappedContext ( final String name, final ContextVersion firstVersion ) {
        super ( name, null );
        this.versions = new ContextVersion[] { firstVersion };
    }
}
