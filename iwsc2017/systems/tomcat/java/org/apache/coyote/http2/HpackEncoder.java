package org.apache.coyote.http2;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.res.StringManager;
class HpackEncoder {
    private static final Log log = LogFactory.getLog ( HpackEncoder.class );
    private static final StringManager sm = StringManager.getManager ( HpackEncoder.class );
    private static final HpackHeaderFunction DEFAULT_HEADER_FUNCTION = new HpackHeaderFunction() {
        @Override
        public boolean shouldUseIndexing ( String headerName, String value ) {
            return !headerName.equals ( "content-length" ) && !headerName.equals ( "date" );
        }
        @Override
        public boolean shouldUseHuffman ( String header, String value ) {
            return value.length() > 5;
        }
        @Override
        public boolean shouldUseHuffman ( String header ) {
            return header.length() > 5;
        }
    };
    private int headersIterator = -1;
    private boolean firstPass = true;
    private MimeHeaders currentHeaders;
    private int entryPositionCounter;
    private int newMaxHeaderSize = -1;
    private int minNewMaxHeaderSize = -1;
    private static final Map<String, TableEntry[]> ENCODING_STATIC_TABLE;
    private final Deque<TableEntry> evictionQueue = new ArrayDeque<>();
    private final Map<String, List<TableEntry>> dynamicTable = new HashMap<>();
    static {
        Map<String, TableEntry[]> map = new HashMap<>();
        for ( int i = 1; i < Hpack.STATIC_TABLE.length; ++i ) {
            Hpack.HeaderField m = Hpack.STATIC_TABLE[i];
            TableEntry[] existing = map.get ( m.name );
            if ( existing == null ) {
                map.put ( m.name, new TableEntry[] {new TableEntry ( m.name, m.value, i ) } );
            } else {
                TableEntry[] newEntry = new TableEntry[existing.length + 1];
                System.arraycopy ( existing, 0, newEntry, 0, existing.length );
                newEntry[existing.length] = new TableEntry ( m.name, m.value, i );
                map.put ( m.name, newEntry );
            }
        }
        ENCODING_STATIC_TABLE = Collections.unmodifiableMap ( map );
    }
    private int maxTableSize;
    private int currentTableSize;
    private final HpackHeaderFunction hpackHeaderFunction;
    HpackEncoder ( int maxTableSize ) {
        this.maxTableSize = maxTableSize;
        this.hpackHeaderFunction = DEFAULT_HEADER_FUNCTION;
    }
    State encode ( MimeHeaders headers, ByteBuffer target ) {
        int it = headersIterator;
        if ( headersIterator == -1 ) {
            handleTableSizeChange ( target );
            it = 0;
            currentHeaders = headers;
        } else {
            if ( headers != currentHeaders ) {
                throw new IllegalStateException();
            }
        }
        while ( it < currentHeaders.size() ) {
            String headerName = headers.getName ( it ).toString().toLowerCase ( Locale.US );
            boolean skip = false;
            if ( firstPass ) {
                if ( headerName.charAt ( 0 ) != ':' ) {
                    skip = true;
                }
            } else {
                if ( headerName.charAt ( 0 ) == ':' ) {
                    skip = true;
                }
            }
            if ( !skip ) {
                String val = headers.getValue ( it ).toString();
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "hpackEncoder.encodeHeader", headerName, val ) );
                }
                TableEntry tableEntry = findInTable ( headerName, val );
                int required = 11 + headerName.length() + 1 + val.length();
                if ( target.remaining() < required ) {
                    this.headersIterator = it;
                    return State.UNDERFLOW;
                }
                boolean canIndex = hpackHeaderFunction.shouldUseIndexing ( headerName, val ) &&
                                   ( headerName.length() + val.length() + 32 ) < maxTableSize;
                if ( tableEntry == null && canIndex ) {
                    target.put ( ( byte ) ( 1 << 6 ) );
                    writeHuffmanEncodableName ( target, headerName );
                    writeHuffmanEncodableValue ( target, headerName, val );
                    addToDynamicTable ( headerName, val );
                } else if ( tableEntry == null ) {
                    target.put ( ( byte ) ( 1 << 4 ) );
                    writeHuffmanEncodableName ( target, headerName );
                    writeHuffmanEncodableValue ( target, headerName, val );
                } else {
                    if ( val.equals ( tableEntry.value ) ) {
                        target.put ( ( byte ) ( 1 << 7 ) );
                        Hpack.encodeInteger ( target, tableEntry.getPosition(), 7 );
                    } else {
                        if ( canIndex ) {
                            target.put ( ( byte ) ( 1 << 6 ) );
                            Hpack.encodeInteger ( target, tableEntry.getPosition(), 6 );
                            writeHuffmanEncodableValue ( target, headerName, val );
                            addToDynamicTable ( headerName, val );
                        } else {
                            target.put ( ( byte ) ( 1 << 4 ) );
                            Hpack.encodeInteger ( target, tableEntry.getPosition(), 4 );
                            writeHuffmanEncodableValue ( target, headerName, val );
                        }
                    }
                }
            }
            if ( ++it == currentHeaders.size() && firstPass ) {
                firstPass = false;
                it = 0;
            }
        }
        headersIterator = -1;
        firstPass = true;
        return State.COMPLETE;
    }
    private void writeHuffmanEncodableName ( ByteBuffer target, String headerName ) {
        if ( hpackHeaderFunction.shouldUseHuffman ( headerName ) ) {
            if ( HPackHuffman.encode ( target, headerName, true ) ) {
                return;
            }
        }
        target.put ( ( byte ) 0 );
        Hpack.encodeInteger ( target, headerName.length(), 7 );
        for ( int j = 0; j < headerName.length(); ++j ) {
            target.put ( Hpack.toLower ( ( byte ) headerName.charAt ( j ) ) );
        }
    }
    private void writeHuffmanEncodableValue ( ByteBuffer target, String headerName, String val ) {
        if ( hpackHeaderFunction.shouldUseHuffman ( headerName, val ) ) {
            if ( !HPackHuffman.encode ( target, val, false ) ) {
                writeValueString ( target, val );
            }
        } else {
            writeValueString ( target, val );
        }
    }
    private void writeValueString ( ByteBuffer target, String val ) {
        target.put ( ( byte ) 0 );
        Hpack.encodeInteger ( target, val.length(), 7 );
        for ( int j = 0; j < val.length(); ++j ) {
            target.put ( ( byte ) val.charAt ( j ) );
        }
    }
    private void addToDynamicTable ( String headerName, String val ) {
        int pos = entryPositionCounter++;
        DynamicTableEntry d = new DynamicTableEntry ( headerName, val, -pos );
        List<TableEntry> existing = dynamicTable.get ( headerName );
        if ( existing == null ) {
            dynamicTable.put ( headerName, existing = new ArrayList<> ( 1 ) );
        }
        existing.add ( d );
        evictionQueue.add ( d );
        currentTableSize += d.getSize();
        runEvictionIfRequired();
        if ( entryPositionCounter == Integer.MAX_VALUE ) {
            preventPositionRollover();
        }
    }
    private void preventPositionRollover() {
        for ( Map.Entry<String, List<TableEntry>> entry : dynamicTable.entrySet() ) {
            for ( TableEntry t : entry.getValue() ) {
                t.position = t.getPosition();
            }
        }
        entryPositionCounter = 0;
    }
    private void runEvictionIfRequired() {
        while ( currentTableSize > maxTableSize ) {
            TableEntry next = evictionQueue.poll();
            if ( next == null ) {
                return;
            }
            currentTableSize -= next.size;
            List<TableEntry> list = dynamicTable.get ( next.name );
            list.remove ( next );
            if ( list.isEmpty() ) {
                dynamicTable.remove ( next.name );
            }
        }
    }
    private TableEntry findInTable ( String headerName, String value ) {
        TableEntry[] staticTable = ENCODING_STATIC_TABLE.get ( headerName );
        if ( staticTable != null ) {
            for ( TableEntry st : staticTable ) {
                if ( st.value != null && st.value.equals ( value ) ) {
                    return st;
                }
            }
        }
        List<TableEntry> dynamic = dynamicTable.get ( headerName );
        if ( dynamic != null ) {
            for ( TableEntry st : dynamic ) {
                if ( st.value.equals ( value ) ) {
                    return st;
                }
            }
        }
        if ( staticTable != null ) {
            return staticTable[0];
        }
        return null;
    }
    public void setMaxTableSize ( int newSize ) {
        this.newMaxHeaderSize = newSize;
        if ( minNewMaxHeaderSize == -1 ) {
            minNewMaxHeaderSize = newSize;
        } else {
            minNewMaxHeaderSize = Math.min ( newSize, minNewMaxHeaderSize );
        }
    }
    private void handleTableSizeChange ( ByteBuffer target ) {
        if ( newMaxHeaderSize == -1 ) {
            return;
        }
        if ( minNewMaxHeaderSize != newMaxHeaderSize ) {
            target.put ( ( byte ) ( 1 << 5 ) );
            Hpack.encodeInteger ( target, minNewMaxHeaderSize, 5 );
        }
        target.put ( ( byte ) ( 1 << 5 ) );
        Hpack.encodeInteger ( target, newMaxHeaderSize, 5 );
        maxTableSize = newMaxHeaderSize;
        runEvictionIfRequired();
        newMaxHeaderSize = -1;
        minNewMaxHeaderSize = -1;
    }
    enum State {
        COMPLETE,
        UNDERFLOW,
    }
    private static class TableEntry {
        private final String name;
        private final String value;
        private final int size;
        private int position;
        private TableEntry ( String name, String value, int position ) {
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
            return position;
        }
        int getSize() {
            return size;
        }
    }
    private class DynamicTableEntry extends TableEntry {
        private DynamicTableEntry ( String name, String value, int position ) {
            super ( name, value, position );
        }
        @Override
        int getPosition() {
            return super.getPosition() + entryPositionCounter + Hpack.STATIC_TABLE_LENGTH;
        }
    }
    private interface HpackHeaderFunction {
        boolean shouldUseIndexing ( String header, String value );
        boolean shouldUseHuffman ( String header, String value );
        boolean shouldUseHuffman ( String header );
    }
}
