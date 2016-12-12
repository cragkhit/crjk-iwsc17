package org.apache.tomcat.websocket;
import javax.websocket.SendHandler;
import java.util.Collection;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.io.IOException;
import java.util.Iterator;
import javax.websocket.Extension;
import java.util.List;
import java.util.zip.Deflater;
import java.nio.ByteBuffer;
import java.util.zip.Inflater;
import org.apache.tomcat.util.res.StringManager;
public class PerMessageDeflate implements Transformation {
    private static final StringManager sm;
    private static final String SERVER_NO_CONTEXT_TAKEOVER = "server_no_context_takeover";
    private static final String CLIENT_NO_CONTEXT_TAKEOVER = "client_no_context_takeover";
    private static final String SERVER_MAX_WINDOW_BITS = "server_max_window_bits";
    private static final String CLIENT_MAX_WINDOW_BITS = "client_max_window_bits";
    private static final int RSV_BITMASK = 4;
    private static final byte[] EOM_BYTES;
    public static final String NAME = "permessage-deflate";
    private final boolean serverContextTakeover;
    private final int serverMaxWindowBits;
    private final boolean clientContextTakeover;
    private final int clientMaxWindowBits;
    private final boolean isServer;
    private final Inflater inflater;
    private final ByteBuffer readBuffer;
    private final Deflater deflater;
    private final byte[] EOM_BUFFER;
    private volatile Transformation next;
    private volatile boolean skipDecompression;
    private volatile ByteBuffer writeBuffer;
    private volatile boolean firstCompressedFrameWritten;
    static PerMessageDeflate negotiate ( final List<List<Extension.Parameter>> preferences, final boolean isServer ) {
        for ( final List<Extension.Parameter> preference : preferences ) {
            boolean ok = true;
            boolean serverContextTakeover = true;
            int serverMaxWindowBits = -1;
            boolean clientContextTakeover = true;
            int clientMaxWindowBits = -1;
            for ( final Extension.Parameter param : preference ) {
                if ( "server_no_context_takeover".equals ( param.getName() ) ) {
                    if ( !serverContextTakeover ) {
                        throw new IllegalArgumentException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.duplicateParameter", "server_no_context_takeover" ) );
                    }
                    serverContextTakeover = false;
                } else if ( "client_no_context_takeover".equals ( param.getName() ) ) {
                    if ( !clientContextTakeover ) {
                        throw new IllegalArgumentException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.duplicateParameter", "client_no_context_takeover" ) );
                    }
                    clientContextTakeover = false;
                } else if ( "server_max_window_bits".equals ( param.getName() ) ) {
                    if ( serverMaxWindowBits != -1 ) {
                        throw new IllegalArgumentException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.duplicateParameter", "server_max_window_bits" ) );
                    }
                    serverMaxWindowBits = Integer.parseInt ( param.getValue() );
                    if ( serverMaxWindowBits < 8 || serverMaxWindowBits > 15 ) {
                        throw new IllegalArgumentException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.invalidWindowSize", "server_max_window_bits", serverMaxWindowBits ) );
                    }
                    if ( isServer && serverMaxWindowBits != 15 ) {
                        ok = false;
                        break;
                    }
                    continue;
                } else {
                    if ( !"client_max_window_bits".equals ( param.getName() ) ) {
                        throw new IllegalArgumentException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.unknownParameter", param.getName() ) );
                    }
                    if ( clientMaxWindowBits != -1 ) {
                        throw new IllegalArgumentException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.duplicateParameter", "client_max_window_bits" ) );
                    }
                    if ( param.getValue() == null ) {
                        clientMaxWindowBits = 15;
                    } else {
                        clientMaxWindowBits = Integer.parseInt ( param.getValue() );
                        if ( clientMaxWindowBits < 8 || clientMaxWindowBits > 15 ) {
                            throw new IllegalArgumentException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.invalidWindowSize", "client_max_window_bits", clientMaxWindowBits ) );
                        }
                    }
                    if ( !isServer && clientMaxWindowBits != 15 ) {
                        ok = false;
                        break;
                    }
                    continue;
                }
            }
            if ( ok ) {
                return new PerMessageDeflate ( serverContextTakeover, serverMaxWindowBits, clientContextTakeover, clientMaxWindowBits, isServer );
            }
        }
        return null;
    }
    private PerMessageDeflate ( final boolean serverContextTakeover, final int serverMaxWindowBits, final boolean clientContextTakeover, final int clientMaxWindowBits, final boolean isServer ) {
        this.inflater = new Inflater ( true );
        this.readBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
        this.deflater = new Deflater ( -1, true );
        this.EOM_BUFFER = new byte[PerMessageDeflate.EOM_BYTES.length + 1];
        this.skipDecompression = false;
        this.writeBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
        this.firstCompressedFrameWritten = false;
        this.serverContextTakeover = serverContextTakeover;
        this.serverMaxWindowBits = serverMaxWindowBits;
        this.clientContextTakeover = clientContextTakeover;
        this.clientMaxWindowBits = clientMaxWindowBits;
        this.isServer = isServer;
    }
    @Override
    public TransformationResult getMoreData ( final byte opCode, final boolean fin, final int rsv, final ByteBuffer dest ) throws IOException {
        if ( Util.isControl ( opCode ) ) {
            return this.next.getMoreData ( opCode, fin, rsv, dest );
        }
        if ( !Util.isContinuation ( opCode ) ) {
            this.skipDecompression = ( ( rsv & 0x4 ) == 0x0 );
        }
        if ( this.skipDecompression ) {
            return this.next.getMoreData ( opCode, fin, rsv, dest );
        }
        boolean usedEomBytes = false;
        while ( dest.remaining() > 0 ) {
            int written;
            try {
                written = this.inflater.inflate ( dest.array(), dest.arrayOffset() + dest.position(), dest.remaining() );
            } catch ( DataFormatException e ) {
                throw new IOException ( PerMessageDeflate.sm.getString ( "perMessageDeflate.deflateFailed" ), e );
            }
            dest.position ( dest.position() + written );
            if ( this.inflater.needsInput() && !usedEomBytes ) {
                if ( !dest.hasRemaining() ) {
                    continue;
                }
                this.readBuffer.clear();
                final TransformationResult nextResult = this.next.getMoreData ( opCode, fin, rsv ^ 0x4, this.readBuffer );
                this.inflater.setInput ( this.readBuffer.array(), this.readBuffer.arrayOffset(), this.readBuffer.position() );
                if ( TransformationResult.UNDERFLOW.equals ( nextResult ) ) {
                    return nextResult;
                }
                if ( !TransformationResult.END_OF_FRAME.equals ( nextResult ) || this.readBuffer.position() != 0 ) {
                    continue;
                }
                if ( !fin ) {
                    return TransformationResult.END_OF_FRAME;
                }
                this.inflater.setInput ( PerMessageDeflate.EOM_BYTES );
                usedEomBytes = true;
            } else {
                if ( written == 0 ) {
                    if ( fin && ( ( this.isServer && !this.clientContextTakeover ) || ( !this.isServer && !this.serverContextTakeover ) ) ) {
                        this.inflater.reset();
                    }
                    return TransformationResult.END_OF_FRAME;
                }
                continue;
            }
        }
        return TransformationResult.OVERFLOW;
    }
    @Override
    public boolean validateRsv ( final int rsv, final byte opCode ) {
        if ( Util.isControl ( opCode ) ) {
            return ( rsv & 0x4 ) <= 0 && ( this.next == null || this.next.validateRsv ( rsv, opCode ) );
        }
        int rsvNext = rsv;
        if ( ( rsv & 0x4 ) > 0 ) {
            rsvNext = ( rsv ^ 0x4 );
        }
        return this.next == null || this.next.validateRsv ( rsvNext, opCode );
    }
    @Override
    public Extension getExtensionResponse() {
        final Extension result = ( Extension ) new WsExtension ( "permessage-deflate" );
        final List<Extension.Parameter> params = ( List<Extension.Parameter> ) result.getParameters();
        if ( !this.serverContextTakeover ) {
            params.add ( ( Extension.Parameter ) new WsExtensionParameter ( "server_no_context_takeover", null ) );
        }
        if ( this.serverMaxWindowBits != -1 ) {
            params.add ( ( Extension.Parameter ) new WsExtensionParameter ( "server_max_window_bits", Integer.toString ( this.serverMaxWindowBits ) ) );
        }
        if ( !this.clientContextTakeover ) {
            params.add ( ( Extension.Parameter ) new WsExtensionParameter ( "client_no_context_takeover", null ) );
        }
        if ( this.clientMaxWindowBits != -1 ) {
            params.add ( ( Extension.Parameter ) new WsExtensionParameter ( "client_max_window_bits", Integer.toString ( this.clientMaxWindowBits ) ) );
        }
        return result;
    }
    @Override
    public void setNext ( final Transformation t ) {
        if ( this.next == null ) {
            this.next = t;
        } else {
            this.next.setNext ( t );
        }
    }
    @Override
    public boolean validateRsvBits ( final int i ) {
        return ( i & 0x4 ) <= 0 && ( this.next == null || this.next.validateRsvBits ( i | 0x4 ) );
    }
    @Override
    public List<MessagePart> sendMessagePart ( final List<MessagePart> uncompressedParts ) {
        final List<MessagePart> allCompressedParts = new ArrayList<MessagePart>();
        for ( final MessagePart uncompressedPart : uncompressedParts ) {
            final byte opCode = uncompressedPart.getOpCode();
            if ( Util.isControl ( opCode ) ) {
                allCompressedParts.add ( uncompressedPart );
            } else if ( uncompressedPart.getPayload().limit() == 0 && uncompressedPart.isFin() && this.deflater.getBytesRead() == 0L ) {
                allCompressedParts.add ( uncompressedPart );
            } else {
                final List<MessagePart> compressedParts = new ArrayList<MessagePart>();
                final ByteBuffer uncompressedPayload = uncompressedPart.getPayload();
                final SendHandler uncompressedIntermediateHandler = uncompressedPart.getIntermediateHandler();
                this.deflater.setInput ( uncompressedPayload.array(), uncompressedPayload.arrayOffset() + uncompressedPayload.position(), uncompressedPayload.remaining() );
                final int flush = uncompressedPart.isFin() ? 2 : 0;
                boolean deflateRequired = true;
                while ( deflateRequired ) {
                    final ByteBuffer compressedPayload = this.writeBuffer;
                    final int written = this.deflater.deflate ( compressedPayload.array(), compressedPayload.arrayOffset() + compressedPayload.position(), compressedPayload.remaining(), flush );
                    compressedPayload.position ( compressedPayload.position() + written );
                    if ( !uncompressedPart.isFin() && compressedPayload.hasRemaining() && this.deflater.needsInput() ) {
                        break;
                    }
                    this.writeBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
                    compressedPayload.flip();
                    final boolean fin = uncompressedPart.isFin();
                    final boolean full = compressedPayload.limit() == compressedPayload.capacity();
                    final boolean needsInput = this.deflater.needsInput();
                    final long blockingWriteTimeoutExpiry = uncompressedPart.getBlockingWriteTimeoutExpiry();
                    MessagePart compressedPart;
                    if ( fin && !full && needsInput ) {
                        compressedPayload.limit ( compressedPayload.limit() - PerMessageDeflate.EOM_BYTES.length );
                        compressedPart = new MessagePart ( true, this.getRsv ( uncompressedPart ), opCode, compressedPayload, uncompressedIntermediateHandler, uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                        deflateRequired = false;
                        this.startNewMessage();
                    } else if ( full && !needsInput ) {
                        compressedPart = new MessagePart ( false, this.getRsv ( uncompressedPart ), opCode, compressedPayload, uncompressedIntermediateHandler, uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                    } else if ( !fin && full && needsInput ) {
                        compressedPart = new MessagePart ( false, this.getRsv ( uncompressedPart ), opCode, compressedPayload, uncompressedIntermediateHandler, uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                        deflateRequired = false;
                    } else {
                        if ( !fin || !full || !needsInput ) {
                            throw new IllegalStateException ( "Should never happen" );
                        }
                        final int eomBufferWritten = this.deflater.deflate ( this.EOM_BUFFER, 0, this.EOM_BUFFER.length, 2 );
                        if ( eomBufferWritten < this.EOM_BUFFER.length ) {
                            compressedPayload.limit ( compressedPayload.limit() - PerMessageDeflate.EOM_BYTES.length + eomBufferWritten );
                            compressedPart = new MessagePart ( true, this.getRsv ( uncompressedPart ), opCode, compressedPayload, uncompressedIntermediateHandler, uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                            deflateRequired = false;
                            this.startNewMessage();
                        } else {
                            this.writeBuffer.put ( this.EOM_BUFFER, 0, eomBufferWritten );
                            compressedPart = new MessagePart ( false, this.getRsv ( uncompressedPart ), opCode, compressedPayload, uncompressedIntermediateHandler, uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                        }
                    }
                    compressedParts.add ( compressedPart );
                }
                final SendHandler uncompressedEndHandler = uncompressedPart.getEndHandler();
                final int size = compressedParts.size();
                if ( size > 0 ) {
                    compressedParts.get ( size - 1 ).setEndHandler ( uncompressedEndHandler );
                }
                allCompressedParts.addAll ( compressedParts );
            }
        }
        if ( this.next == null ) {
            return allCompressedParts;
        }
        return this.next.sendMessagePart ( allCompressedParts );
    }
    private void startNewMessage() {
        this.firstCompressedFrameWritten = false;
        if ( ( this.isServer && !this.serverContextTakeover ) || ( !this.isServer && !this.clientContextTakeover ) ) {
            this.deflater.reset();
        }
    }
    private int getRsv ( final MessagePart uncompressedMessagePart ) {
        int result = uncompressedMessagePart.getRsv();
        if ( !this.firstCompressedFrameWritten ) {
            result += 4;
            this.firstCompressedFrameWritten = true;
        }
        return result;
    }
    @Override
    public void close() {
        this.next.close();
        this.inflater.end();
        this.deflater.end();
    }
    static {
        sm = StringManager.getManager ( PerMessageDeflate.class );
        EOM_BYTES = new byte[] { 0, 0, -1, -1 };
    }
}
