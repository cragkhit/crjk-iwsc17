package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public final class ConstantDouble extends Constant {
    private final double bytes;
    ConstantDouble ( final DataInput file ) throws IOException {
        super ( Const.CONSTANT_Double );
        this.bytes = file.readDouble();
    }
    public final double getBytes() {
        return bytes;
    }
}
