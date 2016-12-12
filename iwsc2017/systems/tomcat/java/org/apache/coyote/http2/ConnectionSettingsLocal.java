package org.apache.coyote.http2;
import java.util.Map;
class ConnectionSettingsLocal extends ConnectionSettingsBase<IllegalArgumentException> {
    private boolean sendInProgress = false;
    ConnectionSettingsLocal ( String connectionId ) {
        super ( connectionId );
    }
    @Override
    final synchronized void set ( Setting setting, Long value ) {
        checkSend();
        if ( current.get ( setting ).longValue() == value.longValue() ) {
            pending.remove ( setting );
        } else {
            pending.put ( setting, value );
        }
    }
    final synchronized byte[] getSettingsFrameForPending() {
        checkSend();
        int payloadSize = pending.size() * 6;
        byte[] result = new byte[9 + payloadSize];
        ByteUtil.setThreeBytes ( result, 0, payloadSize );
        result[3] = FrameType.SETTINGS.getIdByte();
        int pos = 9;
        for ( Map.Entry<Setting, Long> setting : pending.entrySet() ) {
            ByteUtil.setTwoBytes ( result, pos, setting.getKey().getId() );
            pos += 2;
            ByteUtil.setFourBytes ( result, pos, setting.getValue().longValue() );
            pos += 4;
        }
        sendInProgress = true;
        return result;
    }
    final synchronized boolean ack() {
        if ( sendInProgress ) {
            sendInProgress = false;
            current.putAll ( pending );
            pending.clear();
            return true;
        } else {
            return false;
        }
    }
    private void checkSend() {
        if ( sendInProgress ) {
            throw new IllegalStateException();
        }
    }
    @Override
    final void throwException ( String msg, Http2Error error ) throws IllegalArgumentException {
        throw new IllegalArgumentException ( msg );
    }
}
