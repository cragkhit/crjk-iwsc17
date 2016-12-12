package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public final class ConstantFloat extends Constant {
    private final float bytes;
    ConstantFloat ( final DataInput file ) throws IOException {
        super ( ( byte ) 4 );
        this.bytes = file.readFloat();
    }
    public final float getBytes() {
        return this.bytes;
    }
}
