package org.apache.tomcat.websocket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.List;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.Utf8Decoder;
import org.apache.tomcat.util.res.StringManager;
public abstract class WsFrameBase {
    private static final StringManager sm =
        StringManager.getManager ( WsFrameBase.class );
    protected final WsSession wsSession;
    protected final ByteBuffer inputBuffer;
    private final Transformation transformation;
    private final ByteBuffer controlBufferBinary = ByteBuffer.allocate ( 125 );
    private final CharBuffer controlBufferText = CharBuffer.allocate ( 125 );
    private final CharsetDecoder utf8DecoderControl = new Utf8Decoder().
    onMalformedInput ( CodingErrorAction.REPORT ).
    onUnmappableCharacter ( CodingErrorAction.REPORT );
    private final CharsetDecoder utf8DecoderMessage = new Utf8Decoder().
    onMalformedInput ( CodingErrorAction.REPORT ).
    onUnmappableCharacter ( CodingErrorAction.REPORT );
    private boolean continuationExpected = false;
    private boolean textMessage = false;
    private ByteBuffer messageBufferBinary;
    private CharBuffer messageBufferText;
    private MessageHandler binaryMsgHandler = null;
    private MessageHandler textMsgHandler = null;
    private boolean fin = false;
    private int rsv = 0;
    private byte opCode = 0;
    private final byte[] mask = new byte[4];
    private int maskIndex = 0;
    private long payloadLength = 0;
    private volatile long payloadWritten = 0;
    private volatile State state = State.NEW_FRAME;
    private volatile boolean open = true;
    public WsFrameBase ( WsSession wsSession, Transformation transformation ) {
        inputBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
        inputBuffer.position ( 0 ).limit ( 0 );
        messageBufferBinary =
            ByteBuffer.allocate ( wsSession.getMaxBinaryMessageBufferSize() );
        messageBufferText =
            CharBuffer.allocate ( wsSession.getMaxTextMessageBufferSize() );
        this.wsSession = wsSession;
        Transformation finalTransformation;
        if ( isMasked() ) {
            finalTransformation = new UnmaskTransformation();
        } else {
            finalTransformation = new NoopTransformation();
        }
        if ( transformation == null ) {
            this.transformation = finalTransformation;
        } else {
            transformation.setNext ( finalTransformation );
            this.transformation = transformation;
        }
    }
    protected void processInputBuffer() throws IOException {
        while ( true ) {
            wsSession.updateLastActive();
            if ( state == State.NEW_FRAME ) {
                if ( !processInitialHeader() ) {
                    break;
                }
                if ( !open ) {
                    throw new IOException ( sm.getString ( "wsFrame.closed" ) );
                }
            }
            if ( state == State.PARTIAL_HEADER ) {
                if ( !processRemainingHeader() ) {
                    break;
                }
            }
            if ( state == State.DATA ) {
                if ( !processData() ) {
                    break;
                }
            }
        }
    }
    private boolean processInitialHeader() throws IOException {
        if ( inputBuffer.remaining() < 2 ) {
            return false;
        }
        int b = inputBuffer.get();
        fin = ( b & 0x80 ) > 0;
        rsv = ( b & 0x70 ) >>> 4;
        opCode = ( byte ) ( b & 0x0F );
        if ( !transformation.validateRsv ( rsv, opCode ) ) {
            throw new WsIOException ( new CloseReason (
                                          CloseCodes.PROTOCOL_ERROR,
                                          sm.getString ( "wsFrame.wrongRsv", Integer.valueOf ( rsv ),
                                                  Integer.valueOf ( opCode ) ) ) );
        }
        if ( Util.isControl ( opCode ) ) {
            if ( !fin ) {
                throw new WsIOException ( new CloseReason (
                                              CloseCodes.PROTOCOL_ERROR,
                                              sm.getString ( "wsFrame.controlFragmented" ) ) );
            }
            if ( opCode != Constants.OPCODE_PING &&
                    opCode != Constants.OPCODE_PONG &&
                    opCode != Constants.OPCODE_CLOSE ) {
                throw new WsIOException ( new CloseReason (
                                              CloseCodes.PROTOCOL_ERROR,
                                              sm.getString ( "wsFrame.invalidOpCode",
                                                      Integer.valueOf ( opCode ) ) ) );
            }
        } else {
            if ( continuationExpected ) {
                if ( !Util.isContinuation ( opCode ) ) {
                    throw new WsIOException ( new CloseReason (
                                                  CloseCodes.PROTOCOL_ERROR,
                                                  sm.getString ( "wsFrame.noContinuation" ) ) );
                }
            } else {
                try {
                    if ( opCode == Constants.OPCODE_BINARY ) {
                        textMessage = false;
                        int size = wsSession.getMaxBinaryMessageBufferSize();
                        if ( size != messageBufferBinary.capacity() ) {
                            messageBufferBinary = ByteBuffer.allocate ( size );
                        }
                        binaryMsgHandler = wsSession.getBinaryMessageHandler();
                        textMsgHandler = null;
                    } else if ( opCode == Constants.OPCODE_TEXT ) {
                        textMessage = true;
                        int size = wsSession.getMaxTextMessageBufferSize();
                        if ( size != messageBufferText.capacity() ) {
                            messageBufferText = CharBuffer.allocate ( size );
                        }
                        binaryMsgHandler = null;
                        textMsgHandler = wsSession.getTextMessageHandler();
                    } else {
                        throw new WsIOException ( new CloseReason (
                                                      CloseCodes.PROTOCOL_ERROR,
                                                      sm.getString ( "wsFrame.invalidOpCode",
                                                              Integer.valueOf ( opCode ) ) ) );
                    }
                } catch ( IllegalStateException ise ) {
                    throw new WsIOException ( new CloseReason (
                                                  CloseCodes.PROTOCOL_ERROR,
                                                  sm.getString ( "wsFrame.sessionClosed" ) ) );
                }
            }
            continuationExpected = !fin;
        }
        b = inputBuffer.get();
        if ( ( b & 0x80 ) == 0 && isMasked() ) {
            throw new WsIOException ( new CloseReason (
                                          CloseCodes.PROTOCOL_ERROR,
                                          sm.getString ( "wsFrame.notMasked" ) ) );
        }
        payloadLength = b & 0x7F;
        state = State.PARTIAL_HEADER;
        if ( getLog().isDebugEnabled() ) {
            getLog().debug ( sm.getString ( "wsFrame.partialHeaderComplete", Boolean.toString ( fin ),
                                            Integer.toString ( rsv ), Integer.toString ( opCode ), Long.toString ( payloadLength ) ) );
        }
        return true;
    }
    protected abstract boolean isMasked();
    protected abstract Log getLog();
    private boolean processRemainingHeader() throws IOException {
        int headerLength;
        if ( isMasked() ) {
            headerLength = 4;
        } else {
            headerLength = 0;
        }
        if ( payloadLength == 126 ) {
            headerLength += 2;
        } else if ( payloadLength == 127 ) {
            headerLength += 8;
        }
        if ( inputBuffer.remaining() < headerLength ) {
            return false;
        }
        if ( payloadLength == 126 ) {
            payloadLength = byteArrayToLong ( inputBuffer.array(),
                                              inputBuffer.arrayOffset() + inputBuffer.position(), 2 );
            inputBuffer.position ( inputBuffer.position() + 2 );
        } else if ( payloadLength == 127 ) {
            payloadLength = byteArrayToLong ( inputBuffer.array(),
                                              inputBuffer.arrayOffset() + inputBuffer.position(), 8 );
            inputBuffer.position ( inputBuffer.position() + 8 );
        }
        if ( Util.isControl ( opCode ) ) {
            if ( payloadLength > 125 ) {
                throw new WsIOException ( new CloseReason (
                                              CloseCodes.PROTOCOL_ERROR,
                                              sm.getString ( "wsFrame.controlPayloadTooBig",
                                                      Long.valueOf ( payloadLength ) ) ) );
            }
            if ( !fin ) {
                throw new WsIOException ( new CloseReason (
                                              CloseCodes.PROTOCOL_ERROR,
                                              sm.getString ( "wsFrame.controlNoFin" ) ) );
            }
        }
        if ( isMasked() ) {
            inputBuffer.get ( mask, 0, 4 );
        }
        state = State.DATA;
        return true;
    }
    private boolean processData() throws IOException {
        boolean result;
        if ( Util.isControl ( opCode ) ) {
            result = processDataControl();
        } else if ( textMessage ) {
            if ( textMsgHandler == null ) {
                result = swallowInput();
            } else {
                result = processDataText();
            }
        } else {
            if ( binaryMsgHandler == null ) {
                result = swallowInput();
            } else {
                result = processDataBinary();
            }
        }
        checkRoomPayload();
        return result;
    }
    private boolean processDataControl() throws IOException {
        TransformationResult tr = transformation.getMoreData ( opCode, fin, rsv, controlBufferBinary );
        if ( TransformationResult.UNDERFLOW.equals ( tr ) ) {
            return false;
        }
        controlBufferBinary.flip();
        if ( opCode == Constants.OPCODE_CLOSE ) {
            open = false;
            String reason = null;
            int code = CloseCodes.NORMAL_CLOSURE.getCode();
            if ( controlBufferBinary.remaining() == 1 ) {
                controlBufferBinary.clear();
                throw new WsIOException ( new CloseReason (
                                              CloseCodes.PROTOCOL_ERROR,
                                              sm.getString ( "wsFrame.oneByteCloseCode" ) ) );
            }
            if ( controlBufferBinary.remaining() > 1 ) {
                code = controlBufferBinary.getShort();
                if ( controlBufferBinary.remaining() > 0 ) {
                    CoderResult cr = utf8DecoderControl.decode (
                                         controlBufferBinary, controlBufferText, true );
                    if ( cr.isError() ) {
                        controlBufferBinary.clear();
                        controlBufferText.clear();
                        throw new WsIOException ( new CloseReason (
                                                      CloseCodes.PROTOCOL_ERROR,
                                                      sm.getString ( "wsFrame.invalidUtf8Close" ) ) );
                    }
                    controlBufferText.flip();
                    reason = controlBufferText.toString();
                }
            }
            wsSession.onClose ( new CloseReason ( Util.getCloseCode ( code ), reason ) );
        } else if ( opCode == Constants.OPCODE_PING ) {
            if ( wsSession.isOpen() ) {
                wsSession.getBasicRemote().sendPong ( controlBufferBinary );
            }
        } else if ( opCode == Constants.OPCODE_PONG ) {
            MessageHandler.Whole<PongMessage> mhPong =
                wsSession.getPongMessageHandler();
            if ( mhPong != null ) {
                try {
                    mhPong.onMessage ( new WsPongMessage ( controlBufferBinary ) );
                } catch ( Throwable t ) {
                    handleThrowableOnSend ( t );
                } finally {
                    controlBufferBinary.clear();
                }
            }
        } else {
            controlBufferBinary.clear();
            throw new WsIOException ( new CloseReason (
                                          CloseCodes.PROTOCOL_ERROR,
                                          sm.getString ( "wsFrame.invalidOpCode",
                                                  Integer.valueOf ( opCode ) ) ) );
        }
        controlBufferBinary.clear();
        newFrame();
        return true;
    }
    @SuppressWarnings ( "unchecked" )
    protected void sendMessageText ( boolean last ) throws WsIOException {
        if ( textMsgHandler instanceof WrappedMessageHandler ) {
            long maxMessageSize =
                ( ( WrappedMessageHandler ) textMsgHandler ).getMaxMessageSize();
            if ( maxMessageSize > -1 &&
                    messageBufferText.remaining() > maxMessageSize ) {
                throw new WsIOException ( new CloseReason ( CloseCodes.TOO_BIG,
                                          sm.getString ( "wsFrame.messageTooBig",
                                                  Long.valueOf ( messageBufferText.remaining() ),
                                                  Long.valueOf ( maxMessageSize ) ) ) );
            }
        }
        try {
            if ( textMsgHandler instanceof MessageHandler.Partial<?> ) {
                ( ( MessageHandler.Partial<String> ) textMsgHandler ).onMessage (
                    messageBufferText.toString(), last );
            } else {
                ( ( MessageHandler.Whole<String> ) textMsgHandler ).onMessage (
                    messageBufferText.toString() );
            }
        } catch ( Throwable t ) {
            handleThrowableOnSend ( t );
        } finally {
            messageBufferText.clear();
        }
    }
    private boolean processDataText() throws IOException {
        TransformationResult tr = transformation.getMoreData ( opCode, fin, rsv, messageBufferBinary );
        while ( !TransformationResult.END_OF_FRAME.equals ( tr ) ) {
            messageBufferBinary.flip();
            while ( true ) {
                CoderResult cr = utf8DecoderMessage.decode (
                                     messageBufferBinary, messageBufferText, false );
                if ( cr.isError() ) {
                    throw new WsIOException ( new CloseReason (
                                                  CloseCodes.NOT_CONSISTENT,
                                                  sm.getString ( "wsFrame.invalidUtf8" ) ) );
                } else if ( cr.isOverflow() ) {
                    if ( usePartial() ) {
                        messageBufferText.flip();
                        sendMessageText ( false );
                        messageBufferText.clear();
                    } else {
                        throw new WsIOException ( new CloseReason (
                                                      CloseCodes.TOO_BIG,
                                                      sm.getString ( "wsFrame.textMessageTooBig" ) ) );
                    }
                } else if ( cr.isUnderflow() ) {
                    messageBufferBinary.compact();
                    if ( TransformationResult.OVERFLOW.equals ( tr ) ) {
                        break;
                    } else {
                        return false;
                    }
                }
            }
            tr = transformation.getMoreData ( opCode, fin, rsv, messageBufferBinary );
        }
        messageBufferBinary.flip();
        boolean last = false;
        while ( true ) {
            CoderResult cr = utf8DecoderMessage.decode ( messageBufferBinary,
                             messageBufferText, last );
            if ( cr.isError() ) {
                throw new WsIOException ( new CloseReason (
                                              CloseCodes.NOT_CONSISTENT,
                                              sm.getString ( "wsFrame.invalidUtf8" ) ) );
            } else if ( cr.isOverflow() ) {
                if ( usePartial() ) {
                    messageBufferText.flip();
                    sendMessageText ( false );
                    messageBufferText.clear();
                } else {
                    throw new WsIOException ( new CloseReason (
                                                  CloseCodes.TOO_BIG,
                                                  sm.getString ( "wsFrame.textMessageTooBig" ) ) );
                }
            } else if ( cr.isUnderflow() && !last ) {
                if ( continuationExpected ) {
                    if ( usePartial() ) {
                        messageBufferText.flip();
                        sendMessageText ( false );
                        messageBufferText.clear();
                    }
                    messageBufferBinary.compact();
                    newFrame();
                    return true;
                } else {
                    last = true;
                }
            } else {
                messageBufferText.flip();
                sendMessageText ( true );
                newMessage();
                return true;
            }
        }
    }
    private boolean processDataBinary() throws IOException {
        TransformationResult tr = transformation.getMoreData ( opCode, fin, rsv, messageBufferBinary );
        while ( !TransformationResult.END_OF_FRAME.equals ( tr ) ) {
            if ( TransformationResult.UNDERFLOW.equals ( tr ) ) {
                return false;
            }
            if ( !usePartial() ) {
                CloseReason cr = new CloseReason ( CloseCodes.TOO_BIG,
                                                   sm.getString ( "wsFrame.bufferTooSmall",
                                                           Integer.valueOf (
                                                                   messageBufferBinary.capacity() ),
                                                           Long.valueOf ( payloadLength ) ) );
                throw new WsIOException ( cr );
            }
            messageBufferBinary.flip();
            ByteBuffer copy =
                ByteBuffer.allocate ( messageBufferBinary.limit() );
            copy.put ( messageBufferBinary );
            copy.flip();
            sendMessageBinary ( copy, false );
            messageBufferBinary.clear();
            tr = transformation.getMoreData ( opCode, fin, rsv, messageBufferBinary );
        }
        if ( usePartial() || !continuationExpected ) {
            messageBufferBinary.flip();
            ByteBuffer copy =
                ByteBuffer.allocate ( messageBufferBinary.limit() );
            copy.put ( messageBufferBinary );
            copy.flip();
            sendMessageBinary ( copy, !continuationExpected );
            messageBufferBinary.clear();
        }
        if ( continuationExpected ) {
            newFrame();
        } else {
            newMessage();
        }
        return true;
    }
    private void handleThrowableOnSend ( Throwable t ) throws WsIOException {
        ExceptionUtils.handleThrowable ( t );
        wsSession.getLocal().onError ( wsSession, t );
        CloseReason cr = new CloseReason ( CloseCodes.CLOSED_ABNORMALLY,
                                           sm.getString ( "wsFrame.ioeTriggeredClose" ) );
        throw new WsIOException ( cr );
    }
    @SuppressWarnings ( "unchecked" )
    protected void sendMessageBinary ( ByteBuffer msg, boolean last )
    throws WsIOException {
        if ( binaryMsgHandler instanceof WrappedMessageHandler ) {
            long maxMessageSize =
                ( ( WrappedMessageHandler ) binaryMsgHandler ).getMaxMessageSize();
            if ( maxMessageSize > -1 && msg.remaining() > maxMessageSize ) {
                throw new WsIOException ( new CloseReason ( CloseCodes.TOO_BIG,
                                          sm.getString ( "wsFrame.messageTooBig",
                                                  Long.valueOf ( msg.remaining() ),
                                                  Long.valueOf ( maxMessageSize ) ) ) );
            }
        }
        try {
            if ( binaryMsgHandler instanceof MessageHandler.Partial<?> ) {
                ( ( MessageHandler.Partial<ByteBuffer> ) binaryMsgHandler ).onMessage ( msg, last );
            } else {
                ( ( MessageHandler.Whole<ByteBuffer> ) binaryMsgHandler ).onMessage ( msg );
            }
        } catch ( Throwable t ) {
            handleThrowableOnSend ( t );
        }
    }
    private void newMessage() {
        messageBufferBinary.clear();
        messageBufferText.clear();
        utf8DecoderMessage.reset();
        continuationExpected = false;
        newFrame();
    }
    private void newFrame() {
        if ( inputBuffer.remaining() == 0 ) {
            inputBuffer.position ( 0 ).limit ( 0 );
        }
        maskIndex = 0;
        payloadWritten = 0;
        state = State.NEW_FRAME;
        checkRoomHeaders();
    }
    private void checkRoomHeaders() {
        if ( inputBuffer.capacity() - inputBuffer.position() < 131 ) {
            makeRoom();
        }
    }
    private void checkRoomPayload() {
        if ( inputBuffer.capacity() - inputBuffer.position() - payloadLength + payloadWritten < 0 ) {
            makeRoom();
        }
    }
    private void makeRoom() {
        inputBuffer.compact();
        inputBuffer.flip();
    }
    private boolean usePartial() {
        if ( Util.isControl ( opCode ) ) {
            return false;
        } else if ( textMessage ) {
            return textMsgHandler instanceof MessageHandler.Partial;
        } else {
            return binaryMsgHandler instanceof MessageHandler.Partial;
        }
    }
    private boolean swallowInput() {
        long toSkip = Math.min ( payloadLength - payloadWritten, inputBuffer.remaining() );
        inputBuffer.position ( inputBuffer.position() + ( int ) toSkip );
        payloadWritten += toSkip;
        if ( payloadWritten == payloadLength ) {
            if ( continuationExpected ) {
                newFrame();
            } else {
                newMessage();
            }
            return true;
        } else {
            return false;
        }
    }
    protected static long byteArrayToLong ( byte[] b, int start, int len )
    throws IOException {
        if ( len > 8 ) {
            throw new IOException ( sm.getString ( "wsFrame.byteToLongFail",
                                                   Long.valueOf ( len ) ) );
        }
        int shift = 0;
        long result = 0;
        for ( int i = start + len - 1; i >= start; i-- ) {
            result = result + ( ( b[i] & 0xFF ) << shift );
            shift += 8;
        }
        return result;
    }
    protected boolean isOpen() {
        return open;
    }
    protected Transformation getTransformation() {
        return transformation;
    }
    private static enum State {
        NEW_FRAME, PARTIAL_HEADER, DATA
    }
    private abstract class TerminalTransformation implements Transformation {
        @Override
        public boolean validateRsvBits ( int i ) {
            return true;
        }
        @Override
        public Extension getExtensionResponse() {
            return null;
        }
        @Override
        public void setNext ( Transformation t ) {
        }
        @Override
        public boolean validateRsv ( int rsv, byte opCode ) {
            return rsv == 0;
        }
        @Override
        public void close() {
        }
    }
    private final class NoopTransformation extends TerminalTransformation {
        @Override
        public TransformationResult getMoreData ( byte opCode, boolean fin, int rsv,
                ByteBuffer dest ) {
            long toWrite = Math.min (
                               payloadLength - payloadWritten, inputBuffer.remaining() );
            toWrite = Math.min ( toWrite, dest.remaining() );
            int orgLimit = inputBuffer.limit();
            inputBuffer.limit ( inputBuffer.position() + ( int ) toWrite );
            dest.put ( inputBuffer );
            inputBuffer.limit ( orgLimit );
            payloadWritten += toWrite;
            if ( payloadWritten == payloadLength ) {
                return TransformationResult.END_OF_FRAME;
            } else if ( inputBuffer.remaining() == 0 ) {
                return TransformationResult.UNDERFLOW;
            } else {
                return TransformationResult.OVERFLOW;
            }
        }
        @Override
        public List<MessagePart> sendMessagePart ( List<MessagePart> messageParts ) {
            return messageParts;
        }
    }
    private final class UnmaskTransformation extends TerminalTransformation {
        @Override
        public TransformationResult getMoreData ( byte opCode, boolean fin, int rsv,
                ByteBuffer dest ) {
            while ( payloadWritten < payloadLength && inputBuffer.remaining() > 0 &&
                    dest.hasRemaining() ) {
                byte b = ( byte ) ( ( inputBuffer.get() ^ mask[maskIndex] ) & 0xFF );
                maskIndex++;
                if ( maskIndex == 4 ) {
                    maskIndex = 0;
                }
                payloadWritten++;
                dest.put ( b );
            }
            if ( payloadWritten == payloadLength ) {
                return TransformationResult.END_OF_FRAME;
            } else if ( inputBuffer.remaining() == 0 ) {
                return TransformationResult.UNDERFLOW;
            } else {
                return TransformationResult.OVERFLOW;
            }
        }
        @Override
        public List<MessagePart> sendMessagePart ( List<MessagePart> messageParts ) {
            return messageParts;
        }
    }
}
