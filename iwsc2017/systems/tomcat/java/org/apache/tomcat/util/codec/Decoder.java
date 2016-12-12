package org.apache.tomcat.util.codec;
public interface Decoder {
    Object decode ( Object source ) throws DecoderException;
}
