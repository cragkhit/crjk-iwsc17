package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public final class ConstantUtf8 extends Constant {
    private final String bytes;
    static ConstantUtf8 getInstance ( final DataInput input ) throws IOException {
        return new ConstantUtf8 ( input.readUTF() );
    }
    private ConstantUtf8 ( final String bytes ) {
        super ( ( byte ) 1 );
        if ( bytes == null ) {
            throw new IllegalArgumentException ( "bytes must not be null!" );
        }
        this.bytes = bytes;
    }
    public final String getBytes() {
        return this.bytes;
    }
}
