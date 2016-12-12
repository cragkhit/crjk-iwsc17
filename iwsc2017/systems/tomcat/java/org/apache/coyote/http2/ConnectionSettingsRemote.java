package org.apache.coyote.http2;
class ConnectionSettingsRemote extends ConnectionSettingsBase<ConnectionException> {
    ConnectionSettingsRemote ( String connectionId ) {
        super ( connectionId );
    }
    @Override
    final void throwException ( String msg, Http2Error error ) throws ConnectionException {
        throw new ConnectionException ( msg, error );
    }
}
