package org.apache.juli;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;
import java.util.concurrent.LinkedBlockingDeque;
public class AsyncFileHandler extends FileHandler {
    public static final int OVERFLOW_DROP_LAST = 1;
    public static final int OVERFLOW_DROP_FIRST = 2;
    public static final int OVERFLOW_DROP_FLUSH = 3;
    public static final int OVERFLOW_DROP_CURRENT = 4;
    public static final int DEFAULT_OVERFLOW_DROP_TYPE = 1;
    public static final int DEFAULT_MAX_RECORDS = 10000;
    public static final int DEFAULT_LOGGER_SLEEP_TIME = 1000;
    public static final int OVERFLOW_DROP_TYPE;
    public static final int MAX_RECORDS;
    public static final int LOGGER_SLEEP_TIME;
    protected static final LinkedBlockingDeque<LogEntry> queue;
    protected static final LoggerThread logger;
    protected volatile boolean closed;
    public AsyncFileHandler() {
        this ( null, null, null );
    }
    public AsyncFileHandler ( final String directory, final String prefix, final String suffix ) {
        super ( directory, prefix, suffix );
        this.closed = false;
        this.open();
    }
    @Override
    public void close() {
        if ( this.closed ) {
            return;
        }
        this.closed = true;
        super.close();
    }
    @Override
    protected void open() {
        if ( !this.closed ) {
            return;
        }
        this.closed = false;
        super.open();
    }
    @Override
    public void publish ( final LogRecord record ) {
        if ( !this.isLoggable ( record ) ) {
            return;
        }
        record.getSourceMethodName();
        final LogEntry entry = new LogEntry ( record, this );
        boolean added = false;
        try {
            while ( !added && !AsyncFileHandler.queue.offer ( entry ) ) {
                switch ( AsyncFileHandler.OVERFLOW_DROP_TYPE ) {
                case 1: {
                    AsyncFileHandler.queue.pollLast();
                    continue;
                }
                case 2: {
                    AsyncFileHandler.queue.pollFirst();
                    continue;
                }
                case 3: {
                    added = AsyncFileHandler.queue.offer ( entry, 1000L, TimeUnit.MILLISECONDS );
                    continue;
                }
                case 4: {
                    added = true;
                    continue;
                }
                }
            }
        } catch ( InterruptedException ex ) {}
    }
    protected void publishInternal ( final LogRecord record ) {
        super.publish ( record );
    }
    static {
        OVERFLOW_DROP_TYPE = Integer.parseInt ( System.getProperty ( "org.apache.juli.AsyncOverflowDropType", Integer.toString ( 1 ) ) );
        MAX_RECORDS = Integer.parseInt ( System.getProperty ( "org.apache.juli.AsyncMaxRecordCount", Integer.toString ( 10000 ) ) );
        LOGGER_SLEEP_TIME = Integer.parseInt ( System.getProperty ( "org.apache.juli.AsyncLoggerPollInterval", Integer.toString ( 1000 ) ) );
        queue = new LinkedBlockingDeque<LogEntry> ( AsyncFileHandler.MAX_RECORDS );
        ( logger = new LoggerThread() ).start();
    }
    protected static class LoggerThread extends Thread {
        protected final boolean run = true;
        public LoggerThread() {
            this.setDaemon ( this.run );
            this.setName ( "AsyncFileHandlerWriter-" + System.identityHashCode ( this ) );
        }
        @Override
        public void run() {
            while ( true ) {
                try {
                    while ( true ) {
                        final LogEntry entry = AsyncFileHandler.queue.poll ( AsyncFileHandler.LOGGER_SLEEP_TIME, TimeUnit.MILLISECONDS );
                        if ( entry != null ) {
                            entry.flush();
                        }
                    }
                } catch ( InterruptedException ex ) {
                    continue;
                } catch ( Exception x ) {
                    x.printStackTrace();
                    continue;
                }
                break;
            }
        }
    }
    protected static class LogEntry {
        private final LogRecord record;
        private final AsyncFileHandler handler;
        public LogEntry ( final LogRecord record, final AsyncFileHandler handler ) {
            this.record = record;
            this.handler = handler;
        }
        public boolean flush() {
            if ( this.handler.closed ) {
                return false;
            }
            this.handler.publishInternal ( this.record );
            return true;
        }
    }
}
