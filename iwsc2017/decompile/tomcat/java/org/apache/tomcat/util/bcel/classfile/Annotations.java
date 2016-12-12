package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public class Annotations {
    private final AnnotationEntry[] annotation_table;
    Annotations ( final DataInput input, final ConstantPool constant_pool ) throws IOException {
        final int annotation_table_length = input.readUnsignedShort();
        this.annotation_table = new AnnotationEntry[annotation_table_length];
        for ( int i = 0; i < annotation_table_length; ++i ) {
            this.annotation_table[i] = new AnnotationEntry ( input, constant_pool );
        }
    }
    public AnnotationEntry[] getAnnotationEntries() {
        return this.annotation_table;
    }
}
