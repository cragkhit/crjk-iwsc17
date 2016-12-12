package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public class ConstantPool {
    private final Constant[] constant_pool;
    ConstantPool ( final DataInput input ) throws IOException, ClassFormatException {
        final int constant_pool_count = input.readUnsignedShort();
        constant_pool = new Constant[constant_pool_count];
        for ( int i = 1; i < constant_pool_count; i++ ) {
            constant_pool[i] = Constant.readConstant ( input );
            if ( constant_pool[i] != null ) {
                byte tag = constant_pool[i].getTag();
                if ( ( tag == Const.CONSTANT_Double ) || ( tag == Const.CONSTANT_Long ) ) {
                    i++;
                }
            }
        }
    }
    public Constant getConstant ( final int index ) {
        if ( index >= constant_pool.length || index < 0 ) {
            throw new ClassFormatException ( "Invalid constant pool reference: " + index
                                             + ". Constant pool size is: " + constant_pool.length );
        }
        return constant_pool[index];
    }
    public Constant getConstant ( final int index, final byte tag ) throws ClassFormatException {
        Constant c;
        c = getConstant ( index );
        if ( c == null ) {
            throw new ClassFormatException ( "Constant pool at index " + index + " is null." );
        }
        if ( c.getTag() != tag ) {
            throw new ClassFormatException ( "Expected class `" + Const.getConstantName ( tag )
                                             + "' at index " + index + " and got " + c );
        }
        return c;
    }
}
