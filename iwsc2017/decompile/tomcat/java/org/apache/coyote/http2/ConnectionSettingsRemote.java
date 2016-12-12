package org.apache.coyote.http2;
class ConnectionSettingsRemote extends ConnectionSettingsBase<ConnectionException> {
    ConnectionSettingsRemote ( final String connectionId ) {
        super ( connectionId );
    }
    @Override
    final void throwException ( final String msg, final Http2Error error ) throws ConnectionException {
        throw new ConnectionException ( msg, error );
    }
}
