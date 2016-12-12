package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.util.ArrayList;
import java.io.DataInput;
import java.util.List;
public class AnnotationEntry {
    private final int type_index;
    private final ConstantPool constant_pool;
    private final List<ElementValuePair> element_value_pairs;
    AnnotationEntry ( final DataInput input, final ConstantPool constant_pool ) throws IOException {
        this.constant_pool = constant_pool;
        this.type_index = input.readUnsignedShort();
        final int num_element_value_pairs = input.readUnsignedShort();
        this.element_value_pairs = new ArrayList<ElementValuePair> ( num_element_value_pairs );
        for ( int i = 0; i < num_element_value_pairs; ++i ) {
            this.element_value_pairs.add ( new ElementValuePair ( input, constant_pool ) );
        }
    }
    public String getAnnotationType() {
        final ConstantUtf8 c = ( ConstantUtf8 ) this.constant_pool.getConstant ( this.type_index, ( byte ) 1 );
        return c.getBytes();
    }
    public List<ElementValuePair> getElementValuePairs() {
        return this.element_value_pairs;
    }
}
