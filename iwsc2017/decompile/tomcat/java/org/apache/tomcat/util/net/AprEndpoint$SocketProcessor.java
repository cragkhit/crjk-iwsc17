package org.apache.tomcat.util.net;
protected class SocketProcessor extends SocketProcessorBase<Long> {
    public SocketProcessor ( final SocketWrapperBase<Long> socketWrapper, final SocketEvent event ) {
        super ( socketWrapper, event );
    }
    @Override
    protected void doRun() {
        try {
            final Handler.SocketState state = AprEndpoint.this.getHandler().process ( ( SocketWrapperBase<Long> ) this.socketWrapper, this.event );
            if ( state == Handler.SocketState.CLOSED ) {
                AprEndpoint.access$000 ( AprEndpoint.this, ( long ) this.socketWrapper.getSocket() );
            }
        } finally {
            this.socketWrapper = null;
            this.event = null;
            if ( AprEndpoint.this.running && !AprEndpoint.this.paused ) {
                AprEndpoint.this.processorCache.push ( ( SocketProcessorBase<S> ) this );
            }
        }
    }
}
