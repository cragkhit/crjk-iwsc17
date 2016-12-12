package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public final class ConstantInteger extends Constant {
    private final int bytes;
    ConstantInteger ( final DataInput file ) throws IOException {
        super ( Const.CONSTANT_Integer );
        this.bytes = file.readInt();
    }
    public final int getBytes() {
        return bytes;
    }
}
