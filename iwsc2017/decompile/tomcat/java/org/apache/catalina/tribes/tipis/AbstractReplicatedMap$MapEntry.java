package org.apache.catalina.tribes.tipis;
import java.io.IOException;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
import java.util.Map;
public static class MapEntry<K, V> implements Entry<K, V> {
    private boolean backup;
    private boolean proxy;
    private boolean copy;
    private Member[] backupNodes;
    private Member primary;
    private K key;
    private V value;
    public MapEntry ( final K key, final V value ) {
        this.setKey ( key );
        this.setValue ( value );
    }
    public boolean isKeySerializable() {
        return this.key == null || this.key instanceof Serializable;
    }
    public boolean isValueSerializable() {
        return this.value == null || this.value instanceof Serializable;
    }
    public boolean isSerializable() {
        return this.isKeySerializable() && this.isValueSerializable();
    }
    public boolean isBackup() {
        return this.backup;
    }
    public void setBackup ( final boolean backup ) {
        this.backup = backup;
    }
    public boolean isProxy() {
        return this.proxy;
    }
    public boolean isPrimary() {
        return !this.proxy && !this.backup && !this.copy;
    }
    public boolean isActive() {
        return !this.proxy;
    }
    public void setProxy ( final boolean proxy ) {
        this.proxy = proxy;
    }
    public boolean isCopy() {
        return this.copy;
    }
    public void setCopy ( final boolean copy ) {
        this.copy = copy;
    }
    public boolean isDiffable() {
        return this.value instanceof ReplicatedMapEntry && ( ( ReplicatedMapEntry ) this.value ).isDiffable();
    }
    public void setBackupNodes ( final Member[] nodes ) {
        this.backupNodes = nodes;
    }
    public Member[] getBackupNodes() {
        return this.backupNodes;
    }
    public void setPrimary ( final Member m ) {
        this.primary = m;
    }
    public Member getPrimary() {
        return this.primary;
    }
    @Override
    public V getValue() {
        return this.value;
    }
    @Override
    public V setValue ( final V value ) {
        final V old = this.value;
        this.value = value;
        return old;
    }
    @Override
    public K getKey() {
        return this.key;
    }
    public K setKey ( final K key ) {
        final K old = this.key;
        this.key = key;
        return old;
    }
    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
    @Override
    public boolean equals ( final Object o ) {
        return this.key.equals ( o );
    }
    public void apply ( final byte[] data, final int offset, final int length, final boolean diff ) throws IOException, ClassNotFoundException {
        if ( this.isDiffable() && diff ) {
            final ReplicatedMapEntry rentry = ( ReplicatedMapEntry ) this.value;
            rentry.lock();
            try {
                rentry.applyDiff ( data, offset, length );
            } finally {
                rentry.unlock();
            }
        } else if ( length == 0 ) {
            this.value = null;
            this.proxy = true;
        } else {
            this.value = ( V ) XByteBuffer.deserialize ( data, offset, length );
        }
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( "MapEntry[key:" );
        buf.append ( this.getKey() ).append ( "; " );
        buf.append ( "value:" ).append ( this.getValue() ).append ( "; " );
        buf.append ( "primary:" ).append ( this.isPrimary() ).append ( "; " );
        buf.append ( "backup:" ).append ( this.isBackup() ).append ( "; " );
        buf.append ( "proxy:" ).append ( this.isProxy() ).append ( ";]" );
        return buf.toString();
    }
}
