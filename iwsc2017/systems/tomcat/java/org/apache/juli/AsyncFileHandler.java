package org.apache.juli;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;
public class AsyncFileHandler extends FileHandler {
    public static final int OVERFLOW_DROP_LAST    = 1;
    public static final int OVERFLOW_DROP_FIRST   = 2;
    public static final int OVERFLOW_DROP_FLUSH   = 3;
    public static final int OVERFLOW_DROP_CURRENT = 4;
    public static final int DEFAULT_OVERFLOW_DROP_TYPE = 1;
    public static final int DEFAULT_MAX_RECORDS        = 10000;
    public static final int DEFAULT_LOGGER_SLEEP_TIME  = 1000;
    public static final int OVERFLOW_DROP_TYPE = Integer.parseInt (
                System.getProperty ( "org.apache.juli.AsyncOverflowDropType",
                                     Integer.toString ( DEFAULT_OVERFLOW_DROP_TYPE ) ) );
    public static final int MAX_RECORDS = Integer.parseInt (
            System.getProperty ( "org.apache.juli.AsyncMaxRecordCount",
                                 Integer.toString ( DEFAULT_MAX_RECORDS ) ) );
    public static final int LOGGER_SLEEP_TIME = Integer.parseInt (
                System.getProperty ( "org.apache.juli.AsyncLoggerPollInterval",
                                     Integer.toString ( DEFAULT_LOGGER_SLEEP_TIME ) ) );
    protected static final LinkedBlockingDeque<LogEntry> queue =
        new LinkedBlockingDeque<> ( MAX_RECORDS );
    protected static final LoggerThread logger = new LoggerThread();
    static {
        logger.start();
    }
    protected volatile boolean closed = false;
    public AsyncFileHandler() {
        this ( null, null, null );
    }
    public AsyncFileHandler ( String directory, String prefix, String suffix ) {
        super ( directory, prefix, suffix );
        open();
    }
    @Override
    public void close() {
        if ( closed ) {
            return;
        }
        closed = true;
        super.close();
    }
    @Override
    protected void open() {
        if ( !closed ) {
            return;
        }
        closed = false;
        super.open();
    }
    @Override
    public void publish ( LogRecord record ) {
        if ( !isLoggable ( record ) ) {
            return;
        }
        record.getSourceMethodName();
        LogEntry entry = new LogEntry ( record, this );
        boolean added = false;
        try {
            while ( !added && !queue.offer ( entry ) ) {
                switch ( OVERFLOW_DROP_TYPE ) {
                case OVERFLOW_DROP_LAST: {
                    queue.pollLast();
                    break;
                }
                case OVERFLOW_DROP_FIRST: {
                    queue.pollFirst();
                    break;
                }
                case OVERFLOW_DROP_FLUSH: {
                    added = queue.offer ( entry, 1000, TimeUnit.MILLISECONDS );
                    break;
                }
                case OVERFLOW_DROP_CURRENT: {
                    added = true;
                    break;
                }
                }
            }
        } catch ( InterruptedException x ) {
        }
    }
    protected void publishInternal ( LogRecord record ) {
        super.publish ( record );
    }
    protected static class LoggerThread extends Thread {
        protected final boolean run = true;
        public LoggerThread() {
            this.setDaemon ( true );
            this.setName ( "AsyncFileHandlerWriter-" + System.identityHashCode ( this ) );
        }
        @Override
        public void run() {
            while ( run ) {
                try {
                    LogEntry entry = queue.poll ( LOGGER_SLEEP_TIME, TimeUnit.MILLISECONDS );
                    if ( entry != null ) {
                        entry.flush();
                    }
                } catch ( InterruptedException x ) {
                } catch ( Exception x ) {
                    x.printStackTrace();
                }
            }
        }
    }
    protected static class LogEntry {
        private final LogRecord record;
        private final AsyncFileHandler handler;
        public LogEntry ( LogRecord record, AsyncFileHandler handler ) {
            super();
            this.record = record;
            this.handler = handler;
        }
        public boolean flush() {
            if ( handler.closed ) {
                return false;
            } else {
                handler.publishInternal ( record );
                return true;
            }
        }
    }
}
