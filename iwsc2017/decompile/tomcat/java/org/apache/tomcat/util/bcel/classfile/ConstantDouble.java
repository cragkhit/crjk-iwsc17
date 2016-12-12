package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public final class ConstantDouble extends Constant {
    private final double bytes;
    ConstantDouble ( final DataInput file ) throws IOException {
        super ( ( byte ) 6 );
        this.bytes = file.readDouble();
    }
    public final double getBytes() {
        return this.bytes;
    }
}
