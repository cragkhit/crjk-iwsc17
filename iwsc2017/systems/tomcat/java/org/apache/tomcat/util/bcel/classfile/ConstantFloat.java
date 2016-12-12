package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public final class ConstantFloat extends Constant {
    private final float bytes;
    ConstantFloat ( final DataInput file ) throws IOException {
        super ( Const.CONSTANT_Float );
        this.bytes = file.readFloat();
    }
    public final float getBytes() {
        return bytes;
    }
}
