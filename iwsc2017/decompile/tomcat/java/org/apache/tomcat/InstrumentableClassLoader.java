package org.apache.tomcat;
import java.lang.instrument.ClassFileTransformer;
public interface InstrumentableClassLoader {
    void addTransformer ( ClassFileTransformer p0 );
    void removeTransformer ( ClassFileTransformer p0 );
    ClassLoader copyWithoutTransformers();
}
