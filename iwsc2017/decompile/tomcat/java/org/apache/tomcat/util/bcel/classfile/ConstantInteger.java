package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public final class ConstantInteger extends Constant {
    private final int bytes;
    ConstantInteger ( final DataInput file ) throws IOException {
        super ( ( byte ) 3 );
        this.bytes = file.readInt();
    }
    public final int getBytes() {
        return this.bytes;
    }
}
