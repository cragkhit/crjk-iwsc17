package org.apache.catalina.mapper;
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
        if ( Mapper.access$000 ( this.contexts, newContexts, mappedContext ) ) {
            return new ContextList ( newContexts, Math.max ( this.nesting, slashCount ) );
        }
        return null;
    }
    public ContextList removeContext ( final String path ) {
        final MappedContext[] newContexts = new MappedContext[this.contexts.length - 1];
        if ( Mapper.access$100 ( this.contexts, newContexts, path ) ) {
            int newNesting = 0;
            for ( final MappedContext context : newContexts ) {
                newNesting = Math.max ( newNesting, Mapper.access$200 ( context.name ) );
            }
            return new ContextList ( newContexts, newNesting );
        }
        return null;
    }
}
