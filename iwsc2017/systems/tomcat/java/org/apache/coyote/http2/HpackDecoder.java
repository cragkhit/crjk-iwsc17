package org.apache.coyote.http2;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
public class HpackDecoder {
    protected static final StringManager sm = StringManager.getManager ( HpackDecoder.class );
    private static final int DEFAULT_RING_BUFFER_SIZE = 10;
    private HeaderEmitter headerEmitter;
    private Hpack.HeaderField[] headerTable;
    private int firstSlotPosition = 0;
    private int filledTableSlots = 0;
    private int currentMemorySize = 0;
    private int maxMemorySize;
    private int maxHeaderCount = Constants.DEFAULT_MAX_HEADER_COUNT;
    private int maxHeaderSize = Constants.DEFAULT_MAX_HEADER_SIZE;
    private volatile int headerCount = 0;
    private volatile boolean countedCookie;
    private volatile int headerSize = 0;
    private final StringBuilder stringBuilder = new StringBuilder();
    HpackDecoder ( int maxMemorySize ) {
        this.maxMemorySize = maxMemorySize;
        headerTable = new Hpack.HeaderField[DEFAULT_RING_BUFFER_SIZE];
    }
    HpackDecoder() {
        this ( Hpack.DEFAULT_TABLE_SIZE );
    }
    void decode ( ByteBuffer buffer ) throws HpackException {
        while ( buffer.hasRemaining() ) {
            int originalPos = buffer.position();
            byte b = buffer.get();
            if ( ( b & 0b10000000 ) != 0 ) {
                buffer.position ( buffer.position() - 1 );
                int index = Hpack.decodeInteger ( buffer, 7 );
                if ( index == -1 ) {
                    buffer.position ( originalPos );
                    return;
                } else if ( index == 0 ) {
                    throw new HpackException (
                        sm.getString ( "hpackdecoder.zeroNotValidHeaderTableIndex" ) );
                }
                handleIndex ( index );
            } else if ( ( b & 0b01000000 ) != 0 ) {
                String headerName = readHeaderName ( buffer, 6 );
                if ( headerName == null ) {
                    buffer.position ( originalPos );
                    return;
                }
                String headerValue = readHpackString ( buffer );
                if ( headerValue == null ) {
                    buffer.position ( originalPos );
                    return;
                }
                emitHeader ( headerName, headerValue );
                addEntryToHeaderTable ( new Hpack.HeaderField ( headerName, headerValue ) );
            } else if ( ( b & 0b11110000 ) == 0 ) {
                String headerName = readHeaderName ( buffer, 4 );
                if ( headerName == null ) {
                    buffer.position ( originalPos );
                    return;
                }
                String headerValue = readHpackString ( buffer );
                if ( headerValue == null ) {
                    buffer.position ( originalPos );
                    return;
                }
                emitHeader ( headerName, headerValue );
            } else if ( ( b & 0b11110000 ) == 0b00010000 ) {
                String headerName = readHeaderName ( buffer, 4 );
                if ( headerName == null ) {
                    buffer.position ( originalPos );
                    return;
                }
                String headerValue = readHpackString ( buffer );
                if ( headerValue == null ) {
                    buffer.position ( originalPos );
                    return;
                }
                emitHeader ( headerName, headerValue );
            } else if ( ( b & 0b11100000 ) == 0b00100000 ) {
                if ( !handleMaxMemorySizeChange ( buffer, originalPos ) ) {
                    return;
                }
            } else {
                throw new RuntimeException ( "Not yet implemented" );
            }
        }
    }
    private boolean handleMaxMemorySizeChange ( ByteBuffer buffer, int originalPos ) throws HpackException {
        buffer.position ( buffer.position() - 1 );
        int size = Hpack.decodeInteger ( buffer, 5 );
        if ( size == -1 ) {
            buffer.position ( originalPos );
            return false;
        }
        maxMemorySize = size;
        if ( currentMemorySize > maxMemorySize ) {
            int newTableSlots = filledTableSlots;
            int tableLength = headerTable.length;
            int newSize = currentMemorySize;
            while ( newSize > maxMemorySize ) {
                int clearIndex = firstSlotPosition;
                firstSlotPosition++;
                if ( firstSlotPosition == tableLength ) {
                    firstSlotPosition = 0;
                }
                Hpack.HeaderField oldData = headerTable[clearIndex];
                headerTable[clearIndex] = null;
                newSize -= oldData.size;
                newTableSlots--;
            }
            this.filledTableSlots = newTableSlots;
            currentMemorySize = newSize;
        }
        return true;
    }
    private String readHeaderName ( ByteBuffer buffer, int prefixLength ) throws HpackException {
        buffer.position ( buffer.position() - 1 );
        int index = Hpack.decodeInteger ( buffer, prefixLength );
        if ( index == -1 ) {
            return null;
        } else if ( index != 0 ) {
            return handleIndexedHeaderName ( index );
        } else {
            return readHpackString ( buffer );
        }
    }
    private String readHpackString ( ByteBuffer buffer ) throws HpackException {
        if ( !buffer.hasRemaining() ) {
            return null;
        }
        byte data = buffer.get ( buffer.position() );
        int length = Hpack.decodeInteger ( buffer, 7 );
        if ( buffer.remaining() < length ) {
            return null;
        }
        boolean huffman = ( data & 0b10000000 ) != 0;
        if ( huffman ) {
            return readHuffmanString ( length, buffer );
        }
        for ( int i = 0; i < length; ++i ) {
            stringBuilder.append ( ( char ) buffer.get() );
        }
        String ret = stringBuilder.toString();
        stringBuilder.setLength ( 0 );
        return ret;
    }
    private String readHuffmanString ( int length, ByteBuffer buffer ) throws HpackException {
        HPackHuffman.decode ( buffer, length, stringBuilder );
        String ret = stringBuilder.toString();
        stringBuilder.setLength ( 0 );
        return ret;
    }
    private String handleIndexedHeaderName ( int index ) throws HpackException {
        if ( index <= Hpack.STATIC_TABLE_LENGTH ) {
            return Hpack.STATIC_TABLE[index].name;
        } else {
            if ( index >= Hpack.STATIC_TABLE_LENGTH + filledTableSlots ) {
                throw new HpackException();
            }
            int adjustedIndex = getRealIndex ( index - Hpack.STATIC_TABLE_LENGTH );
            Hpack.HeaderField res = headerTable[adjustedIndex];
            if ( res == null ) {
                throw new HpackException();
            }
            return res.name;
        }
    }
    private void handleIndex ( int index ) throws HpackException {
        if ( index <= Hpack.STATIC_TABLE_LENGTH ) {
            addStaticTableEntry ( index );
        } else {
            int adjustedIndex = getRealIndex ( index - Hpack.STATIC_TABLE_LENGTH );
            Hpack.HeaderField headerField = headerTable[adjustedIndex];
            emitHeader ( headerField.name, headerField.value );
        }
    }
    int getRealIndex ( int index ) {
        return ( firstSlotPosition + ( filledTableSlots - index ) ) % headerTable.length;
    }
    private void addStaticTableEntry ( int index ) throws HpackException {
        Hpack.HeaderField entry = Hpack.STATIC_TABLE[index];
        if ( entry.value == null ) {
            throw new HpackException();
        }
        emitHeader ( entry.name, entry.value );
    }
    private void addEntryToHeaderTable ( Hpack.HeaderField entry ) {
        if ( entry.size > maxMemorySize ) {
            while ( filledTableSlots > 0 ) {
                headerTable[firstSlotPosition] = null;
                firstSlotPosition++;
                if ( firstSlotPosition == headerTable.length ) {
                    firstSlotPosition = 0;
                }
                filledTableSlots--;
            }
            currentMemorySize = 0;
            return;
        }
        resizeIfRequired();
        int newTableSlots = filledTableSlots + 1;
        int tableLength = headerTable.length;
        int index = ( firstSlotPosition + filledTableSlots ) % tableLength;
        headerTable[index] = entry;
        int newSize = currentMemorySize + entry.size;
        while ( newSize > maxMemorySize ) {
            int clearIndex = firstSlotPosition;
            firstSlotPosition++;
            if ( firstSlotPosition == tableLength ) {
                firstSlotPosition = 0;
            }
            Hpack.HeaderField oldData = headerTable[clearIndex];
            headerTable[clearIndex] = null;
            newSize -= oldData.size;
            newTableSlots--;
        }
        this.filledTableSlots = newTableSlots;
        currentMemorySize = newSize;
    }
    private void resizeIfRequired() {
        if ( filledTableSlots == headerTable.length ) {
            Hpack.HeaderField[] newArray = new Hpack.HeaderField[headerTable.length + 10];
            for ( int i = 0; i < headerTable.length; ++i ) {
                newArray[i] = headerTable[ ( firstSlotPosition + i ) % headerTable.length];
            }
            firstSlotPosition = 0;
            headerTable = newArray;
        }
    }
    interface HeaderEmitter {
        void emitHeader ( String name, String value );
        void validateHeaders() throws StreamException;
    }
    HeaderEmitter getHeaderEmitter() {
        return headerEmitter;
    }
    void setHeaderEmitter ( HeaderEmitter headerEmitter ) {
        this.headerEmitter = headerEmitter;
        headerCount = 0;
        countedCookie = false;
        headerSize = 0;
    }
    void setMaxHeaderCount ( int maxHeaderCount ) {
        this.maxHeaderCount = maxHeaderCount;
    }
    void setMaxHeaderSize ( int maxHeaderSize ) {
        this.maxHeaderSize = maxHeaderSize;
    }
    private void emitHeader ( String name, String value ) {
        if ( "cookie".equals ( name ) ) {
            if ( !countedCookie ) {
                headerCount ++;
                countedCookie = true;
            }
        } else {
            headerCount ++;
        }
        int inc = 3 + name.length() + value.length();
        headerSize += inc;
        if ( !isHeaderCountExceeded() && !isHeaderSizeExceeded ( 0 ) ) {
            headerEmitter.emitHeader ( name, value );
        }
    }
    boolean isHeaderCountExceeded() {
        if ( maxHeaderCount < 0 ) {
            return false;
        }
        return headerCount > maxHeaderCount;
    }
    boolean isHeaderSizeExceeded ( int unreadSize ) {
        if ( maxHeaderSize < 0 ) {
            return false;
        }
        return ( headerSize + unreadSize ) > maxHeaderSize;
    }
    boolean isHeaderSwallowSizeExceeded ( int unreadSize ) {
        if ( maxHeaderSize < 0 ) {
            return false;
        }
        return ( headerSize + unreadSize ) > ( 2 * maxHeaderSize );
    }
    int getFirstSlotPosition() {
        return firstSlotPosition;
    }
    Hpack.HeaderField[] getHeaderTable() {
        return headerTable;
    }
    int getFilledTableSlots() {
        return filledTableSlots;
    }
    int getCurrentMemorySize() {
        return currentMemorySize;
    }
    int getMaxMemorySize() {
        return maxMemorySize;
    }
}
