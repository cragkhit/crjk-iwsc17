package org.apache.coyote.http2;
import java.util.Iterator;
import java.util.Map;
class ConnectionSettingsLocal extends ConnectionSettingsBase<IllegalArgumentException> {
    private boolean sendInProgress;
    ConnectionSettingsLocal ( final String connectionId ) {
        super ( connectionId );
        this.sendInProgress = false;
    }
    @Override
    final synchronized void set ( final Setting setting, final Long value ) {
        this.checkSend();
        if ( this.current.get ( setting ) == ( long ) value ) {
            this.pending.remove ( setting );
        } else {
            this.pending.put ( setting, value );
        }
    }
    final synchronized byte[] getSettingsFrameForPending() {
        this.checkSend();
        final int payloadSize = this.pending.size() * 6;
        final byte[] result = new byte[9 + payloadSize];
        ByteUtil.setThreeBytes ( result, 0, payloadSize );
        result[3] = FrameType.SETTINGS.getIdByte();
        int pos = 9;
        for ( final Map.Entry<Setting, Long> setting : this.pending.entrySet() ) {
            ByteUtil.setTwoBytes ( result, pos, setting.getKey().getId() );
            pos += 2;
            ByteUtil.setFourBytes ( result, pos, setting.getValue() );
            pos += 4;
        }
        this.sendInProgress = true;
        return result;
    }
    final synchronized boolean ack() {
        if ( this.sendInProgress ) {
            this.sendInProgress = false;
            this.current.putAll ( this.pending );
            this.pending.clear();
            return true;
        }
        return false;
    }
    private void checkSend() {
        if ( this.sendInProgress ) {
            throw new IllegalStateException();
        }
    }
    @Override
    final void throwException ( final String msg, final Http2Error error ) throws IllegalArgumentException {
        throw new IllegalArgumentException ( msg );
    }
}
