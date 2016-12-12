package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
public class Annotations {
    private final AnnotationEntry[] annotation_table;
    Annotations ( final DataInput input, final ConstantPool constant_pool ) throws IOException {
        final int annotation_table_length = input.readUnsignedShort();
        annotation_table = new AnnotationEntry[annotation_table_length];
        for ( int i = 0; i < annotation_table_length; i++ ) {
            annotation_table[i] = new AnnotationEntry ( input, constant_pool );
        }
    }
    public AnnotationEntry[] getAnnotationEntries() {
        return annotation_table;
    }
}
