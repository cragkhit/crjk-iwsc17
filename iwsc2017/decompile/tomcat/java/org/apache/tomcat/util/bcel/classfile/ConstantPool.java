package org.apache.tomcat.util.bcel.classfile;
import org.apache.tomcat.util.bcel.Const;
import java.io.IOException;
import java.io.DataInput;
public class ConstantPool {
    private final Constant[] constant_pool;
    ConstantPool ( final DataInput input ) throws IOException, ClassFormatException {
        final int constant_pool_count = input.readUnsignedShort();
        this.constant_pool = new Constant[constant_pool_count];
        for ( int i = 1; i < constant_pool_count; ++i ) {
            this.constant_pool[i] = Constant.readConstant ( input );
            if ( this.constant_pool[i] != null ) {
                final byte tag = this.constant_pool[i].getTag();
                if ( tag == 6 || tag == 5 ) {
                    ++i;
                }
            }
        }
    }
    public Constant getConstant ( final int index ) {
        if ( index >= this.constant_pool.length || index < 0 ) {
            throw new ClassFormatException ( "Invalid constant pool reference: " + index + ". Constant pool size is: " + this.constant_pool.length );
        }
        return this.constant_pool[index];
    }
    public Constant getConstant ( final int index, final byte tag ) throws ClassFormatException {
        final Constant c = this.getConstant ( index );
        if ( c == null ) {
            throw new ClassFormatException ( "Constant pool at index " + index + " is null." );
        }
        if ( c.getTag() != tag ) {
            throw new ClassFormatException ( "Expected class `" + Const.getConstantName ( tag ) + "' at index " + index + " and got " + c );
        }
        return c;
    }
}
