package org.apache.coyote.http2;
import java.util.Collections;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Locale;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Deque;
import java.util.Map;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
class HpackEncoder {
    private static final Log log;
    private static final StringManager sm;
    private static final HpackHeaderFunction DEFAULT_HEADER_FUNCTION;
    private int headersIterator;
    private boolean firstPass;
    private MimeHeaders currentHeaders;
    private int entryPositionCounter;
    private int newMaxHeaderSize;
    private int minNewMaxHeaderSize;
    private static final Map<String, TableEntry[]> ENCODING_STATIC_TABLE;
    private final Deque<TableEntry> evictionQueue;
    private final Map<String, List<TableEntry>> dynamicTable;
    private int maxTableSize;
    private int currentTableSize;
    private final HpackHeaderFunction hpackHeaderFunction;
    HpackEncoder ( final int maxTableSize ) {
        this.headersIterator = -1;
        this.firstPass = true;
        this.newMaxHeaderSize = -1;
        this.minNewMaxHeaderSize = -1;
        this.evictionQueue = new ArrayDeque<TableEntry>();
        this.dynamicTable = new HashMap<String, List<TableEntry>>();
        this.maxTableSize = maxTableSize;
        this.hpackHeaderFunction = HpackEncoder.DEFAULT_HEADER_FUNCTION;
    }
    State encode ( final MimeHeaders headers, final ByteBuffer target ) {
        int it = this.headersIterator;
        if ( this.headersIterator == -1 ) {
            this.handleTableSizeChange ( target );
            it = 0;
            this.currentHeaders = headers;
        } else if ( headers != this.currentHeaders ) {
            throw new IllegalStateException();
        }
        while ( it < this.currentHeaders.size() ) {
            final String headerName = headers.getName ( it ).toString().toLowerCase ( Locale.US );
            boolean skip = false;
            if ( this.firstPass ) {
                if ( headerName.charAt ( 0 ) != ':' ) {
                    skip = true;
                }
            } else if ( headerName.charAt ( 0 ) == ':' ) {
                skip = true;
            }
            if ( !skip ) {
                final String val = headers.getValue ( it ).toString();
                if ( HpackEncoder.log.isDebugEnabled() ) {
                    HpackEncoder.log.debug ( HpackEncoder.sm.getString ( "hpackEncoder.encodeHeader", headerName, val ) );
                }
                final TableEntry tableEntry = this.findInTable ( headerName, val );
                final int required = 11 + headerName.length() + 1 + val.length();
                if ( target.remaining() < required ) {
                    this.headersIterator = it;
                    return State.UNDERFLOW;
                }
                final boolean canIndex = this.hpackHeaderFunction.shouldUseIndexing ( headerName, val ) && headerName.length() + val.length() + 32 < this.maxTableSize;
                if ( tableEntry == null && canIndex ) {
                    target.put ( ( byte ) 64 );
                    this.writeHuffmanEncodableName ( target, headerName );
                    this.writeHuffmanEncodableValue ( target, headerName, val );
                    this.addToDynamicTable ( headerName, val );
                } else if ( tableEntry == null ) {
                    target.put ( ( byte ) 16 );
                    this.writeHuffmanEncodableName ( target, headerName );
                    this.writeHuffmanEncodableValue ( target, headerName, val );
                } else if ( val.equals ( tableEntry.value ) ) {
                    target.put ( ( byte ) ( -128 ) );
                    Hpack.encodeInteger ( target, tableEntry.getPosition(), 7 );
                } else if ( canIndex ) {
                    target.put ( ( byte ) 64 );
                    Hpack.encodeInteger ( target, tableEntry.getPosition(), 6 );
                    this.writeHuffmanEncodableValue ( target, headerName, val );
                    this.addToDynamicTable ( headerName, val );
                } else {
                    target.put ( ( byte ) 16 );
                    Hpack.encodeInteger ( target, tableEntry.getPosition(), 4 );
                    this.writeHuffmanEncodableValue ( target, headerName, val );
                }
            }
            if ( ++it == this.currentHeaders.size() && this.firstPass ) {
                this.firstPass = false;
                it = 0;
            }
        }
        this.headersIterator = -1;
        this.firstPass = true;
        return State.COMPLETE;
    }
    private void writeHuffmanEncodableName ( final ByteBuffer target, final String headerName ) {
        if ( this.hpackHeaderFunction.shouldUseHuffman ( headerName ) && HPackHuffman.encode ( target, headerName, true ) ) {
            return;
        }
        target.put ( ( byte ) 0 );
        Hpack.encodeInteger ( target, headerName.length(), 7 );
        for ( int j = 0; j < headerName.length(); ++j ) {
            target.put ( Hpack.toLower ( ( byte ) headerName.charAt ( j ) ) );
        }
    }
    private void writeHuffmanEncodableValue ( final ByteBuffer target, final String headerName, final String val ) {
        if ( this.hpackHeaderFunction.shouldUseHuffman ( headerName, val ) ) {
            if ( !HPackHuffman.encode ( target, val, false ) ) {
                this.writeValueString ( target, val );
            }
        } else {
            this.writeValueString ( target, val );
        }
    }
    private void writeValueString ( final ByteBuffer target, final String val ) {
        target.put ( ( byte ) 0 );
        Hpack.encodeInteger ( target, val.length(), 7 );
        for ( int j = 0; j < val.length(); ++j ) {
            target.put ( ( byte ) val.charAt ( j ) );
        }
    }
    private void addToDynamicTable ( final String headerName, final String val ) {
        final int pos = this.entryPositionCounter++;
        final DynamicTableEntry d = new DynamicTableEntry ( headerName, val, -pos );
        List<TableEntry> existing = this.dynamicTable.get ( headerName );
        if ( existing == null ) {
            this.dynamicTable.put ( headerName, existing = new ArrayList<TableEntry> ( 1 ) );
        }
        existing.add ( d );
        this.evictionQueue.add ( d );
        this.currentTableSize += d.getSize();
        this.runEvictionIfRequired();
        if ( this.entryPositionCounter == Integer.MAX_VALUE ) {
            this.preventPositionRollover();
        }
    }
    private void preventPositionRollover() {
        for ( final Map.Entry<String, List<TableEntry>> entry : this.dynamicTable.entrySet() ) {
            for ( final TableEntry t : entry.getValue() ) {
                t.position = t.getPosition();
            }
        }
        this.entryPositionCounter = 0;
    }
    private void runEvictionIfRequired() {
        while ( this.currentTableSize > this.maxTableSize ) {
            final TableEntry next = this.evictionQueue.poll();
            if ( next == null ) {
                return;
            }
            this.currentTableSize -= next.size;
            final List<TableEntry> list = this.dynamicTable.get ( next.name );
            list.remove ( next );
            if ( !list.isEmpty() ) {
                continue;
            }
            this.dynamicTable.remove ( next.name );
        }
    }
    private TableEntry findInTable ( final String headerName, final String value ) {
        final TableEntry[] staticTable = HpackEncoder.ENCODING_STATIC_TABLE.get ( headerName );
        if ( staticTable != null ) {
            for ( final TableEntry st : staticTable ) {
                if ( st.value != null && st.value.equals ( value ) ) {
                    return st;
                }
            }
        }
        final List<TableEntry> dynamic = this.dynamicTable.get ( headerName );
        if ( dynamic != null ) {
            for ( final TableEntry st2 : dynamic ) {
                if ( st2.value.equals ( value ) ) {
                    return st2;
                }
            }
        }
        if ( staticTable != null ) {
            return staticTable[0];
        }
        return null;
    }
    public void setMaxTableSize ( final int newSize ) {
        this.newMaxHeaderSize = newSize;
        if ( this.minNewMaxHeaderSize == -1 ) {
            this.minNewMaxHeaderSize = newSize;
        } else {
            this.minNewMaxHeaderSize = Math.min ( newSize, this.minNewMaxHeaderSize );
        }
    }
    private void handleTableSizeChange ( final ByteBuffer target ) {
        if ( this.newMaxHeaderSize == -1 ) {
            return;
        }
        if ( this.minNewMaxHeaderSize != this.newMaxHeaderSize ) {
            target.put ( ( byte ) 32 );
            Hpack.encodeInteger ( target, this.minNewMaxHeaderSize, 5 );
        }
        target.put ( ( byte ) 32 );
        Hpack.encodeInteger ( target, this.newMaxHeaderSize, 5 );
        this.maxTableSize = this.newMaxHeaderSize;
        this.runEvictionIfRequired();
        this.newMaxHeaderSize = -1;
        this.minNewMaxHeaderSize = -1;
    }
    static {
        log = LogFactory.getLog ( HpackEncoder.class );
        sm = StringManager.getManager ( HpackEncoder.class );
        DEFAULT_HEADER_FUNCTION = new HpackHeaderFunction() {
            @Override
            public boolean shouldUseIndexing ( final String headerName, final String value ) {
                return !headerName.equals ( "content-length" ) && !headerName.equals ( "date" );
            }
            @Override
            public boolean shouldUseHuffman ( final String header, final String value ) {
                return value.length() > 5;
            }
            @Override
            public boolean shouldUseHuffman ( final String header ) {
                return header.length() > 5;
            }
        };
        final Map<String, TableEntry[]> map = new HashMap<String, TableEntry[]>();
        for ( int i = 1; i < Hpack.STATIC_TABLE.length; ++i ) {
            final Hpack.HeaderField m = Hpack.STATIC_TABLE[i];
            final TableEntry[] existing = map.get ( m.name );
            if ( existing == null ) {
                map.put ( m.name, new TableEntry[] { new TableEntry ( m.name, m.value, i ) } );
            } else {
                final TableEntry[] newEntry = new TableEntry[existing.length + 1];
                System.arraycopy ( existing, 0, newEntry, 0, existing.length );
                newEntry[existing.length] = new TableEntry ( m.name, m.value, i );
                map.put ( m.name, newEntry );
            }
        }
        ENCODING_STATIC_TABLE = Collections.unmodifiableMap ( ( Map<? extends String, ? extends TableEntry[]> ) map );
    }
    enum State {
        COMPLETE,
        UNDERFLOW;
    }
    private static class TableEntry {
        private final String name;
        private final String value;
        private final int size;
        private int position;
        private TableEntry ( final String name, final String value, final int position ) {
            this.name = name;
            this.value = value;
            this.position = position;
            if ( value != null ) {
                this.size = 32 + name.length() + value.length();
            } else {
                this.size = -1;
            }
        }
        int getPosition() {
            return this.position;
        }
        int getSize() {
            return this.size;
        }
    }
    private class DynamicTableEntry extends TableEntry {
        private DynamicTableEntry ( final String name, final String value, final int position ) {
            super ( name, value, position );
        }
        @Override
        int getPosition() {
            return super.getPosition() + HpackEncoder.this.entryPositionCounter + Hpack.STATIC_TABLE_LENGTH;
        }
    }
    private interface HpackHeaderFunction {
        boolean shouldUseIndexing ( String p0, String p1 );
        boolean shouldUseHuffman ( String p0, String p1 );
        boolean shouldUseHuffman ( String p0 );
    }
}
