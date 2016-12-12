package org.apache.catalina.ha.deploy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.res.StringManager;
public class FileMessageFactory {
    private static final Log log = LogFactory.getLog ( FileMessageFactory.class );
    private static final StringManager sm = StringManager.getManager ( FileMessageFactory.class );
    public static final int READ_SIZE = 1024 * 10;
    protected final File file;
    protected final boolean openForWrite;
    protected boolean closed = false;
    protected FileInputStream in;
    protected FileOutputStream out;
    protected int nrOfMessagesProcessed = 0;
    protected long size = 0;
    protected long totalNrOfMessages = 0;
    protected AtomicLong lastMessageProcessed = new AtomicLong ( 0 );
    protected final Map<Long, FileMessage> msgBuffer = new ConcurrentHashMap<>();
    protected byte[] data = new byte[READ_SIZE];
    protected boolean isWriting = false;
    protected long creationTime = 0;
    protected int maxValidTime = -1;
    private FileMessageFactory ( File f, boolean openForWrite )
    throws FileNotFoundException, IOException {
        this.file = f;
        this.openForWrite = openForWrite;
        if ( log.isDebugEnabled() ) {
            log.debug ( "open file " + f + " write " + openForWrite );
        }
        if ( openForWrite ) {
            if ( !file.exists() )
                if ( !file.createNewFile() ) {
                    throw new IOException ( sm.getString ( "fileNewFail", file ) );
                }
            out = new FileOutputStream ( f );
        } else {
            size = file.length();
            totalNrOfMessages = ( size / READ_SIZE ) + 1;
            in = new FileInputStream ( f );
        }
        creationTime = System.currentTimeMillis();
    }
    public static FileMessageFactory getInstance ( File f, boolean openForWrite )
    throws FileNotFoundException, IOException {
        return new FileMessageFactory ( f, openForWrite );
    }
    public FileMessage readMessage ( FileMessage f )
    throws IllegalArgumentException, IOException {
        checkState ( false );
        int length = in.read ( data );
        if ( length == -1 ) {
            cleanup();
            return null;
        } else {
            f.setData ( data, length );
            f.setTotalNrOfMsgs ( totalNrOfMessages );
            f.setMessageNumber ( ++nrOfMessagesProcessed );
            return f;
        }
    }
    public boolean writeMessage ( FileMessage msg )
    throws IllegalArgumentException, IOException {
        if ( !openForWrite )
            throw new IllegalArgumentException (
                "Can't write message, this factory is reading." );
        if ( log.isDebugEnabled() )
            log.debug ( "Message " + msg + " data " + HexUtils.toHexString ( msg.getData() )
                        + " data length " + msg.getDataLength() + " out " + out );
        if ( msg.getMessageNumber() <= lastMessageProcessed.get() ) {
            log.warn ( "Receive Message again -- Sender ActTimeout too short [ name: "
                       + msg.getContextName()
                       + " war: "
                       + msg.getFileName()
                       + " data: "
                       + HexUtils.toHexString ( msg.getData() )
                       + " data length: " + msg.getDataLength() + " ]" );
            return false;
        }
        FileMessage previous =
            msgBuffer.put ( Long.valueOf ( msg.getMessageNumber() ), msg );
        if ( previous != null ) {
            log.warn ( "Receive Message again -- Sender ActTimeout too short [ name: "
                       + msg.getContextName()
                       + " war: "
                       + msg.getFileName()
                       + " data: "
                       + HexUtils.toHexString ( msg.getData() )
                       + " data length: " + msg.getDataLength() + " ]" );
            return false;
        }
        FileMessage next = null;
        synchronized ( this ) {
            if ( !isWriting ) {
                next = msgBuffer.get ( Long.valueOf ( lastMessageProcessed.get() + 1 ) );
                if ( next != null ) {
                    isWriting = true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        while ( next != null ) {
            out.write ( next.getData(), 0, next.getDataLength() );
            lastMessageProcessed.incrementAndGet();
            out.flush();
            if ( next.getMessageNumber() == next.getTotalNrOfMsgs() ) {
                out.close();
                cleanup();
                return true;
            }
            synchronized ( this ) {
                next =
                    msgBuffer.get ( Long.valueOf ( lastMessageProcessed.get() + 1 ) );
                if ( next == null ) {
                    isWriting = false;
                }
            }
        }
        return false;
    }
    public void cleanup() {
        if ( in != null )
            try {
                in.close();
            } catch ( Exception ignore ) {
            }
        if ( out != null )
            try {
                out.close();
            } catch ( Exception ignore ) {
            }
        in = null;
        out = null;
        size = 0;
        closed = true;
        data = null;
        nrOfMessagesProcessed = 0;
        totalNrOfMessages = 0;
        msgBuffer.clear();
        lastMessageProcessed = null;
    }
    protected void checkState ( boolean openForWrite )
    throws IllegalArgumentException {
        if ( this.openForWrite != openForWrite ) {
            cleanup();
            if ( openForWrite )
                throw new IllegalArgumentException (
                    "Can't write message, this factory is reading." );
            else
                throw new IllegalArgumentException (
                    "Can't read message, this factory is writing." );
        }
        if ( this.closed ) {
            cleanup();
            throw new IllegalArgumentException ( "Factory has been closed." );
        }
    }
    public static void main ( String[] args ) throws Exception {
        System.out
        .println ( "Usage: FileMessageFactory fileToBeRead fileToBeWritten" );
        System.out
        .println ( "Usage: This will make a copy of the file on the local file system" );
        FileMessageFactory read = getInstance ( new File ( args[0] ), false );
        FileMessageFactory write = getInstance ( new File ( args[1] ), true );
        FileMessage msg = new FileMessage ( null, args[0], args[0] );
        msg = read.readMessage ( msg );
        if ( msg == null ) {
            System.out.println ( "Empty input file : " + args[0] );
            return;
        }
        System.out.println ( "Expecting to write " + msg.getTotalNrOfMsgs()
                             + " messages." );
        int cnt = 0;
        while ( msg != null ) {
            write.writeMessage ( msg );
            cnt++;
            msg = read.readMessage ( msg );
        }
        System.out.println ( "Actually wrote " + cnt + " messages." );
    }
    public File getFile() {
        return file;
    }
    public boolean isValid() {
        if ( maxValidTime > 0 ) {
            long timeNow = System.currentTimeMillis();
            int timeIdle = ( int ) ( ( timeNow - creationTime ) / 1000L );
            if ( timeIdle > maxValidTime ) {
                cleanup();
                if ( file.exists() ) {
                    file.delete();
                }
                return false;
            }
        }
        return true;
    }
    public int getMaxValidTime() {
        return maxValidTime;
    }
    public void setMaxValidTime ( int maxValidTime ) {
        this.maxValidTime = maxValidTime;
    }
}
