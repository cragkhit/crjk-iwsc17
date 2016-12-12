package org.apache.catalina.ha.deploy;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.Member;
import org.apache.tomcat.util.buf.HexUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class FileMessageFactory {
    private static final Log log;
    private static final StringManager sm;
    public static final int READ_SIZE = 10240;
    protected final File file;
    protected final boolean openForWrite;
    protected boolean closed;
    protected FileInputStream in;
    protected FileOutputStream out;
    protected int nrOfMessagesProcessed;
    protected long size;
    protected long totalNrOfMessages;
    protected AtomicLong lastMessageProcessed;
    protected final Map<Long, FileMessage> msgBuffer;
    protected byte[] data;
    protected boolean isWriting;
    protected long creationTime;
    protected int maxValidTime;
    private FileMessageFactory ( final File f, final boolean openForWrite ) throws FileNotFoundException, IOException {
        this.closed = false;
        this.nrOfMessagesProcessed = 0;
        this.size = 0L;
        this.totalNrOfMessages = 0L;
        this.lastMessageProcessed = new AtomicLong ( 0L );
        this.msgBuffer = new ConcurrentHashMap<Long, FileMessage>();
        this.data = new byte[10240];
        this.isWriting = false;
        this.creationTime = 0L;
        this.maxValidTime = -1;
        this.file = f;
        this.openForWrite = openForWrite;
        if ( FileMessageFactory.log.isDebugEnabled() ) {
            FileMessageFactory.log.debug ( "open file " + f + " write " + openForWrite );
        }
        if ( openForWrite ) {
            if ( !this.file.exists() && !this.file.createNewFile() ) {
                throw new IOException ( FileMessageFactory.sm.getString ( "fileNewFail", this.file ) );
            }
            this.out = new FileOutputStream ( f );
        } else {
            this.size = this.file.length();
            this.totalNrOfMessages = this.size / 10240L + 1L;
            this.in = new FileInputStream ( f );
        }
        this.creationTime = System.currentTimeMillis();
    }
    public static FileMessageFactory getInstance ( final File f, final boolean openForWrite ) throws FileNotFoundException, IOException {
        return new FileMessageFactory ( f, openForWrite );
    }
    public FileMessage readMessage ( final FileMessage f ) throws IllegalArgumentException, IOException {
        this.checkState ( false );
        final int length = this.in.read ( this.data );
        if ( length == -1 ) {
            this.cleanup();
            return null;
        }
        f.setData ( this.data, length );
        f.setTotalNrOfMsgs ( this.totalNrOfMessages );
        f.setMessageNumber ( ++this.nrOfMessagesProcessed );
        return f;
    }
    public boolean writeMessage ( final FileMessage msg ) throws IllegalArgumentException, IOException {
        if ( !this.openForWrite ) {
            throw new IllegalArgumentException ( "Can't write message, this factory is reading." );
        }
        if ( FileMessageFactory.log.isDebugEnabled() ) {
            FileMessageFactory.log.debug ( "Message " + msg + " data " + HexUtils.toHexString ( msg.getData() ) + " data length " + msg.getDataLength() + " out " + this.out );
        }
        if ( msg.getMessageNumber() <= this.lastMessageProcessed.get() ) {
            FileMessageFactory.log.warn ( "Receive Message again -- Sender ActTimeout too short [ name: " + msg.getContextName() + " war: " + msg.getFileName() + " data: " + HexUtils.toHexString ( msg.getData() ) + " data length: " + msg.getDataLength() + " ]" );
            return false;
        }
        final FileMessage previous = this.msgBuffer.put ( ( long ) msg.getMessageNumber(), msg );
        if ( previous != null ) {
            FileMessageFactory.log.warn ( "Receive Message again -- Sender ActTimeout too short [ name: " + msg.getContextName() + " war: " + msg.getFileName() + " data: " + HexUtils.toHexString ( msg.getData() ) + " data length: " + msg.getDataLength() + " ]" );
            return false;
        }
        FileMessage next = null;
        synchronized ( this ) {
            if ( this.isWriting ) {
                return false;
            }
            next = this.msgBuffer.get ( this.lastMessageProcessed.get() + 1L );
            if ( next == null ) {
                return false;
            }
            this.isWriting = true;
        }
        while ( next != null ) {
            this.out.write ( next.getData(), 0, next.getDataLength() );
            this.lastMessageProcessed.incrementAndGet();
            this.out.flush();
            if ( next.getMessageNumber() == next.getTotalNrOfMsgs() ) {
                this.out.close();
                this.cleanup();
                return true;
            }
            synchronized ( this ) {
                next = this.msgBuffer.get ( this.lastMessageProcessed.get() + 1L );
                if ( next != null ) {
                    continue;
                }
                this.isWriting = false;
            }
        }
        return false;
    }
    public void cleanup() {
        if ( this.in != null ) {
            try {
                this.in.close();
            } catch ( Exception ex ) {}
        }
        if ( this.out != null ) {
            try {
                this.out.close();
            } catch ( Exception ex2 ) {}
        }
        this.in = null;
        this.out = null;
        this.size = 0L;
        this.closed = true;
        this.data = null;
        this.nrOfMessagesProcessed = 0;
        this.totalNrOfMessages = 0L;
        this.msgBuffer.clear();
        this.lastMessageProcessed = null;
    }
    protected void checkState ( final boolean openForWrite ) throws IllegalArgumentException {
        if ( this.openForWrite != openForWrite ) {
            this.cleanup();
            if ( openForWrite ) {
                throw new IllegalArgumentException ( "Can't write message, this factory is reading." );
            }
            throw new IllegalArgumentException ( "Can't read message, this factory is writing." );
        } else if ( this.closed ) {
            this.cleanup();
            throw new IllegalArgumentException ( "Factory has been closed." );
        }
    }
    public static void main ( final String[] args ) throws Exception {
        System.out.println ( "Usage: FileMessageFactory fileToBeRead fileToBeWritten" );
        System.out.println ( "Usage: This will make a copy of the file on the local file system" );
        final FileMessageFactory read = getInstance ( new File ( args[0] ), false );
        final FileMessageFactory write = getInstance ( new File ( args[1] ), true );
        FileMessage msg = new FileMessage ( null, args[0], args[0] );
        msg = read.readMessage ( msg );
        if ( msg == null ) {
            System.out.println ( "Empty input file : " + args[0] );
            return;
        }
        System.out.println ( "Expecting to write " + msg.getTotalNrOfMsgs() + " messages." );
        int cnt = 0;
        while ( msg != null ) {
            write.writeMessage ( msg );
            ++cnt;
            msg = read.readMessage ( msg );
        }
        System.out.println ( "Actually wrote " + cnt + " messages." );
    }
    public File getFile() {
        return this.file;
    }
    public boolean isValid() {
        if ( this.maxValidTime > 0 ) {
            final long timeNow = System.currentTimeMillis();
            final int timeIdle = ( int ) ( ( timeNow - this.creationTime ) / 1000L );
            if ( timeIdle > this.maxValidTime ) {
                this.cleanup();
                if ( this.file.exists() ) {
                    this.file.delete();
                }
                return false;
            }
        }
        return true;
    }
    public int getMaxValidTime() {
        return this.maxValidTime;
    }
    public void setMaxValidTime ( final int maxValidTime ) {
        this.maxValidTime = maxValidTime;
    }
    static {
        log = LogFactory.getLog ( FileMessageFactory.class );
        sm = StringManager.getManager ( FileMessageFactory.class );
    }
}
