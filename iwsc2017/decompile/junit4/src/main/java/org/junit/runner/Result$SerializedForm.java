package org.junit.runner;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import org.junit.runner.notification.Failure;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;
private static class SerializedForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private final AtomicInteger fCount;
    private final AtomicInteger fIgnoreCount;
    private final List<Failure> fFailures;
    private final long fRunTime;
    private final long fStartTime;
    public SerializedForm ( final Result result ) {
        this.fCount = Result.access$700 ( result );
        this.fIgnoreCount = Result.access$900 ( result );
        this.fFailures = Collections.synchronizedList ( new ArrayList<Failure> ( Result.access$800 ( result ) ) );
        this.fRunTime = Result.access$600 ( result ).longValue();
        this.fStartTime = Result.access$500 ( result ).longValue();
    }
    private SerializedForm ( final ObjectInputStream.GetField fields ) throws IOException {
        this.fCount = ( AtomicInteger ) fields.get ( "fCount", null );
        this.fIgnoreCount = ( AtomicInteger ) fields.get ( "fIgnoreCount", null );
        this.fFailures = ( List<Failure> ) fields.get ( "fFailures", null );
        this.fRunTime = fields.get ( "fRunTime", 0L );
        this.fStartTime = fields.get ( "fStartTime", 0L );
    }
    public void serialize ( final ObjectOutputStream s ) throws IOException {
        final ObjectOutputStream.PutField fields = s.putFields();
        fields.put ( "fCount", this.fCount );
        fields.put ( "fIgnoreCount", this.fIgnoreCount );
        fields.put ( "fFailures", this.fFailures );
        fields.put ( "fRunTime", this.fRunTime );
        fields.put ( "fStartTime", this.fStartTime );
        s.writeFields();
    }
    public static SerializedForm deserialize ( final ObjectInputStream s ) throws ClassNotFoundException, IOException {
        final ObjectInputStream.GetField fields = s.readFields();
        return new SerializedForm ( fields );
    }
}
