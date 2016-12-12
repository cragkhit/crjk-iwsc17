package org.apache.tomcat.util.codec;
public interface BinaryEncoder extends Encoder {
    byte[] encode ( byte[] source ) throws EncoderException;
}
