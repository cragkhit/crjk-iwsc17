package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public final class ConstantLong extends Constant {
    private final long bytes;
    ConstantLong ( final DataInput input ) throws IOException {
        super ( ( byte ) 5 );
        this.bytes = input.readLong();
    }
    public final long getBytes() {
        return this.bytes;
    }
}
