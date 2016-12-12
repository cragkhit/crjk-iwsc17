package org.apache.tomcat.websocket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.websocket.Extension;
import javax.websocket.Extension.Parameter;
import javax.websocket.SendHandler;
import org.apache.tomcat.util.res.StringManager;
public class PerMessageDeflate implements Transformation {
    private static final StringManager sm = StringManager.getManager ( PerMessageDeflate.class );
    private static final String SERVER_NO_CONTEXT_TAKEOVER = "server_no_context_takeover";
    private static final String CLIENT_NO_CONTEXT_TAKEOVER = "client_no_context_takeover";
    private static final String SERVER_MAX_WINDOW_BITS = "server_max_window_bits";
    private static final String CLIENT_MAX_WINDOW_BITS = "client_max_window_bits";
    private static final int RSV_BITMASK = 0b100;
    private static final byte[] EOM_BYTES = new byte[] {0, 0, -1, -1};
    public static final String NAME = "permessage-deflate";
    private final boolean serverContextTakeover;
    private final int serverMaxWindowBits;
    private final boolean clientContextTakeover;
    private final int clientMaxWindowBits;
    private final boolean isServer;
    private final Inflater inflater = new Inflater ( true );
    private final ByteBuffer readBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
    private final Deflater deflater = new Deflater ( Deflater.DEFAULT_COMPRESSION, true );
    private final byte[] EOM_BUFFER = new byte[EOM_BYTES.length + 1];
    private volatile Transformation next;
    private volatile boolean skipDecompression = false;
    private volatile ByteBuffer writeBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
    private volatile boolean firstCompressedFrameWritten = false;
    static PerMessageDeflate negotiate ( List<List<Parameter>> preferences, boolean isServer ) {
        for ( List<Parameter> preference : preferences ) {
            boolean ok = true;
            boolean serverContextTakeover = true;
            int serverMaxWindowBits = -1;
            boolean clientContextTakeover = true;
            int clientMaxWindowBits = -1;
            for ( Parameter param : preference ) {
                if ( SERVER_NO_CONTEXT_TAKEOVER.equals ( param.getName() ) ) {
                    if ( serverContextTakeover ) {
                        serverContextTakeover = false;
                    } else {
                        throw new IllegalArgumentException ( sm.getString (
                                "perMessageDeflate.duplicateParameter",
                                SERVER_NO_CONTEXT_TAKEOVER ) );
                    }
                } else if ( CLIENT_NO_CONTEXT_TAKEOVER.equals ( param.getName() ) ) {
                    if ( clientContextTakeover ) {
                        clientContextTakeover = false;
                    } else {
                        throw new IllegalArgumentException ( sm.getString (
                                "perMessageDeflate.duplicateParameter",
                                CLIENT_NO_CONTEXT_TAKEOVER ) );
                    }
                } else if ( SERVER_MAX_WINDOW_BITS.equals ( param.getName() ) ) {
                    if ( serverMaxWindowBits == -1 ) {
                        serverMaxWindowBits = Integer.parseInt ( param.getValue() );
                        if ( serverMaxWindowBits < 8 || serverMaxWindowBits > 15 ) {
                            throw new IllegalArgumentException ( sm.getString (
                                    "perMessageDeflate.invalidWindowSize",
                                    SERVER_MAX_WINDOW_BITS,
                                    Integer.valueOf ( serverMaxWindowBits ) ) );
                        }
                        if ( isServer && serverMaxWindowBits != 15 ) {
                            ok = false;
                            break;
                        }
                    } else {
                        throw new IllegalArgumentException ( sm.getString (
                                "perMessageDeflate.duplicateParameter",
                                SERVER_MAX_WINDOW_BITS ) );
                    }
                } else if ( CLIENT_MAX_WINDOW_BITS.equals ( param.getName() ) ) {
                    if ( clientMaxWindowBits == -1 ) {
                        if ( param.getValue() == null ) {
                            clientMaxWindowBits = 15;
                        } else {
                            clientMaxWindowBits = Integer.parseInt ( param.getValue() );
                            if ( clientMaxWindowBits < 8 || clientMaxWindowBits > 15 ) {
                                throw new IllegalArgumentException ( sm.getString (
                                        "perMessageDeflate.invalidWindowSize",
                                        CLIENT_MAX_WINDOW_BITS,
                                        Integer.valueOf ( clientMaxWindowBits ) ) );
                            }
                        }
                        if ( !isServer && clientMaxWindowBits != 15 ) {
                            ok = false;
                            break;
                        }
                    } else {
                        throw new IllegalArgumentException ( sm.getString (
                                "perMessageDeflate.duplicateParameter",
                                CLIENT_MAX_WINDOW_BITS ) );
                    }
                } else {
                    throw new IllegalArgumentException ( sm.getString (
                            "perMessageDeflate.unknownParameter", param.getName() ) );
                }
            }
            if ( ok ) {
                return new PerMessageDeflate ( serverContextTakeover, serverMaxWindowBits,
                                               clientContextTakeover, clientMaxWindowBits, isServer );
            }
        }
        return null;
    }
    private PerMessageDeflate ( boolean serverContextTakeover, int serverMaxWindowBits,
                                boolean clientContextTakeover, int clientMaxWindowBits, boolean isServer ) {
        this.serverContextTakeover = serverContextTakeover;
        this.serverMaxWindowBits = serverMaxWindowBits;
        this.clientContextTakeover = clientContextTakeover;
        this.clientMaxWindowBits = clientMaxWindowBits;
        this.isServer = isServer;
    }
    @Override
    public TransformationResult getMoreData ( byte opCode, boolean fin, int rsv, ByteBuffer dest )
    throws IOException {
        if ( Util.isControl ( opCode ) ) {
            return next.getMoreData ( opCode, fin, rsv, dest );
        }
        if ( !Util.isContinuation ( opCode ) ) {
            skipDecompression = ( rsv & RSV_BITMASK ) == 0;
        }
        if ( skipDecompression ) {
            return next.getMoreData ( opCode, fin, rsv, dest );
        }
        int written;
        boolean usedEomBytes = false;
        while ( dest.remaining() > 0 ) {
            try {
                written = inflater.inflate (
                              dest.array(), dest.arrayOffset() + dest.position(), dest.remaining() );
            } catch ( DataFormatException e ) {
                throw new IOException ( sm.getString ( "perMessageDeflate.deflateFailed" ), e );
            }
            dest.position ( dest.position() + written );
            if ( inflater.needsInput() && !usedEomBytes ) {
                if ( dest.hasRemaining() ) {
                    readBuffer.clear();
                    TransformationResult nextResult =
                        next.getMoreData ( opCode, fin, ( rsv ^ RSV_BITMASK ), readBuffer );
                    inflater.setInput (
                        readBuffer.array(), readBuffer.arrayOffset(), readBuffer.position() );
                    if ( TransformationResult.UNDERFLOW.equals ( nextResult ) ) {
                        return nextResult;
                    } else if ( TransformationResult.END_OF_FRAME.equals ( nextResult ) &&
                                readBuffer.position() == 0 ) {
                        if ( fin ) {
                            inflater.setInput ( EOM_BYTES );
                            usedEomBytes = true;
                        } else {
                            return TransformationResult.END_OF_FRAME;
                        }
                    }
                }
            } else if ( written == 0 ) {
                if ( fin && ( isServer && !clientContextTakeover ||
                              !isServer && !serverContextTakeover ) ) {
                    inflater.reset();
                }
                return TransformationResult.END_OF_FRAME;
            }
        }
        return TransformationResult.OVERFLOW;
    }
    @Override
    public boolean validateRsv ( int rsv, byte opCode ) {
        if ( Util.isControl ( opCode ) ) {
            if ( ( rsv & RSV_BITMASK ) > 0 ) {
                return false;
            } else {
                if ( next == null ) {
                    return true;
                } else {
                    return next.validateRsv ( rsv, opCode );
                }
            }
        } else {
            int rsvNext = rsv;
            if ( ( rsv & RSV_BITMASK ) > 0 ) {
                rsvNext = rsv ^ RSV_BITMASK;
            }
            if ( next == null ) {
                return true;
            } else {
                return next.validateRsv ( rsvNext, opCode );
            }
        }
    }
    @Override
    public Extension getExtensionResponse() {
        Extension result = new WsExtension ( NAME );
        List<Extension.Parameter> params = result.getParameters();
        if ( !serverContextTakeover ) {
            params.add ( new WsExtensionParameter ( SERVER_NO_CONTEXT_TAKEOVER, null ) );
        }
        if ( serverMaxWindowBits != -1 ) {
            params.add ( new WsExtensionParameter ( SERVER_MAX_WINDOW_BITS,
                                                    Integer.toString ( serverMaxWindowBits ) ) );
        }
        if ( !clientContextTakeover ) {
            params.add ( new WsExtensionParameter ( CLIENT_NO_CONTEXT_TAKEOVER, null ) );
        }
        if ( clientMaxWindowBits != -1 ) {
            params.add ( new WsExtensionParameter ( CLIENT_MAX_WINDOW_BITS,
                                                    Integer.toString ( clientMaxWindowBits ) ) );
        }
        return result;
    }
    @Override
    public void setNext ( Transformation t ) {
        if ( next == null ) {
            this.next = t;
        } else {
            next.setNext ( t );
        }
    }
    @Override
    public boolean validateRsvBits ( int i ) {
        if ( ( i & RSV_BITMASK ) > 0 ) {
            return false;
        }
        if ( next == null ) {
            return true;
        } else {
            return next.validateRsvBits ( i | RSV_BITMASK );
        }
    }
    @Override
    public List<MessagePart> sendMessagePart ( List<MessagePart> uncompressedParts ) {
        List<MessagePart> allCompressedParts = new ArrayList<>();
        for ( MessagePart uncompressedPart : uncompressedParts ) {
            byte opCode = uncompressedPart.getOpCode();
            if ( Util.isControl ( opCode ) ) {
                allCompressedParts.add ( uncompressedPart );
            } else if ( uncompressedPart.getPayload().limit() == 0 && uncompressedPart.isFin() &&
                        deflater.getBytesRead() == 0 ) {
                allCompressedParts.add ( uncompressedPart );
            } else {
                List<MessagePart> compressedParts = new ArrayList<>();
                ByteBuffer uncompressedPayload = uncompressedPart.getPayload();
                SendHandler uncompressedIntermediateHandler =
                    uncompressedPart.getIntermediateHandler();
                deflater.setInput ( uncompressedPayload.array(),
                                    uncompressedPayload.arrayOffset() + uncompressedPayload.position(),
                                    uncompressedPayload.remaining() );
                int flush = ( uncompressedPart.isFin() ? Deflater.SYNC_FLUSH : Deflater.NO_FLUSH );
                boolean deflateRequired = true;
                while ( deflateRequired ) {
                    ByteBuffer compressedPayload = writeBuffer;
                    int written = deflater.deflate ( compressedPayload.array(),
                                                     compressedPayload.arrayOffset() + compressedPayload.position(),
                                                     compressedPayload.remaining(), flush );
                    compressedPayload.position ( compressedPayload.position() + written );
                    if ( !uncompressedPart.isFin() && compressedPayload.hasRemaining() && deflater.needsInput() ) {
                        break;
                    }
                    MessagePart compressedPart;
                    writeBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
                    compressedPayload.flip();
                    boolean fin = uncompressedPart.isFin();
                    boolean full = compressedPayload.limit() == compressedPayload.capacity();
                    boolean needsInput = deflater.needsInput();
                    long blockingWriteTimeoutExpiry = uncompressedPart.getBlockingWriteTimeoutExpiry();
                    if ( fin && !full && needsInput ) {
                        compressedPayload.limit ( compressedPayload.limit() - EOM_BYTES.length );
                        compressedPart = new MessagePart ( true, getRsv ( uncompressedPart ),
                                                           opCode, compressedPayload, uncompressedIntermediateHandler,
                                                           uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                        deflateRequired = false;
                        startNewMessage();
                    } else if ( full && !needsInput ) {
                        compressedPart = new MessagePart ( false, getRsv ( uncompressedPart ),
                                                           opCode, compressedPayload, uncompressedIntermediateHandler,
                                                           uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                    } else if ( !fin && full && needsInput ) {
                        compressedPart = new MessagePart ( false, getRsv ( uncompressedPart ),
                                                           opCode, compressedPayload, uncompressedIntermediateHandler,
                                                           uncompressedIntermediateHandler, blockingWriteTimeoutExpiry );
                        deflateRequired = false;
                    } else if ( fin && full && needsInput ) {
                        int eomBufferWritten = deflater.deflate ( EOM_BUFFER, 0, EOM_BUFFER.length, Deflater.SYNC_FLUSH );
                        if ( eomBufferWritten < EOM_BUFFER.length ) {
                            compressedPayload.limit ( compressedPayload.limit() - EOM_BYTES.length + eomBufferWritten );
                            compressedPart = new MessagePart ( true,
                                                               getRsv ( uncompressedPart ), opCode, compressedPayload,
                                                               uncompressedIntermediateHandler, uncompressedIntermediateHandler,
                                                               blockingWriteTimeoutExpiry );
                            deflateRequired = false;
                            startNewMessage();
                        } else {
                            writeBuffer.put ( EOM_BUFFER, 0, eomBufferWritten );
                            compressedPart = new MessagePart ( false,
                                                               getRsv ( uncompressedPart ), opCode, compressedPayload,
                                                               uncompressedIntermediateHandler, uncompressedIntermediateHandler,
                                                               blockingWriteTimeoutExpiry );
                        }
                    } else {
                        throw new IllegalStateException ( "Should never happen" );
                    }
                    compressedParts.add ( compressedPart );
                }
                SendHandler uncompressedEndHandler = uncompressedPart.getEndHandler();
                int size = compressedParts.size();
                if ( size > 0 ) {
                    compressedParts.get ( size - 1 ).setEndHandler ( uncompressedEndHandler );
                }
                allCompressedParts.addAll ( compressedParts );
            }
        }
        if ( next == null ) {
            return allCompressedParts;
        } else {
            return next.sendMessagePart ( allCompressedParts );
        }
    }
    private void startNewMessage() {
        firstCompressedFrameWritten = false;
        if ( isServer && !serverContextTakeover || !isServer && !clientContextTakeover ) {
            deflater.reset();
        }
    }
    private int getRsv ( MessagePart uncompressedMessagePart ) {
        int result = uncompressedMessagePart.getRsv();
        if ( !firstCompressedFrameWritten ) {
            result += RSV_BITMASK;
            firstCompressedFrameWritten = true;
        }
        return result;
    }
    @Override
    public void close() {
        next.close();
        inflater.end();
        deflater.end();
    }
}
