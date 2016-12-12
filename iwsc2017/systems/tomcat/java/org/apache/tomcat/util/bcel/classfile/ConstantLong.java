package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public final class ConstantLong extends Constant {
    private final long bytes;
    ConstantLong ( final DataInput input ) throws IOException {
        super ( Const.CONSTANT_Long );
        this.bytes = input.readLong();
    }
    public final long getBytes() {
        return bytes;
    }
}
