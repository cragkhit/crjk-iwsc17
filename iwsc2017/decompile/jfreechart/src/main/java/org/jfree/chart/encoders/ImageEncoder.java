package org.jfree.chart.encoders;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
public interface ImageEncoder {
    byte[] encode ( BufferedImage p0 ) throws IOException;
    void encode ( BufferedImage p0, OutputStream p1 ) throws IOException;
    float getQuality();
    void setQuality ( float p0 );
    boolean isEncodingAlpha();
    void setEncodingAlpha ( boolean p0 );
}
