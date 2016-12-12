package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public final class ConstantUtf8 extends Constant {
    private final String bytes;
    static ConstantUtf8 getInstance ( final DataInput input ) throws IOException {
        return new ConstantUtf8 ( input.readUTF() );
    }
    private ConstantUtf8 ( final String bytes ) {
        super ( Const.CONSTANT_Utf8 );
        if ( bytes == null ) {
            throw new IllegalArgumentException ( "bytes must not be null!" );
        }
        this.bytes = bytes;
    }
    public final String getBytes() {
        return bytes;
    }
}
