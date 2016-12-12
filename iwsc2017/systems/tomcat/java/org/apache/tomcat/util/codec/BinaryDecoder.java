package org.apache.tomcat.util.codec;
public interface BinaryDecoder extends Decoder {
    byte[] decode ( byte[] source ) throws DecoderException;
}
