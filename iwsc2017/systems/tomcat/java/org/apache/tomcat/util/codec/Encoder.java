package org.apache.tomcat.util.codec;
public interface Encoder {
    Object encode ( Object source ) throws EncoderException;
}
