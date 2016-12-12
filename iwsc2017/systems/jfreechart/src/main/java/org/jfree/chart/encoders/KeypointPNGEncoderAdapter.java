

package org.jfree.chart.encoders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import com.keypoint.PngEncoder;
import org.jfree.chart.util.ParamChecks;


public class KeypointPNGEncoderAdapter implements ImageEncoder {


    private int quality = 9;


    private boolean encodingAlpha = false;


    @Override
    public float getQuality() {
        return this.quality;
    }


    @Override
    public void setQuality ( float quality ) {
        this.quality = ( int ) quality;
    }


    @Override
    public boolean isEncodingAlpha() {
        return this.encodingAlpha;
    }


    @Override
    public void setEncodingAlpha ( boolean encodingAlpha ) {
        this.encodingAlpha = encodingAlpha;
    }


    @Override
    public byte[] encode ( BufferedImage bufferedImage ) throws IOException {
        ParamChecks.nullNotPermitted ( bufferedImage, "bufferedImage" );
        PngEncoder encoder = new PngEncoder ( bufferedImage, this.encodingAlpha,
                                              0, this.quality );
        return encoder.pngEncode();
    }


    @Override
    public void encode ( BufferedImage bufferedImage, OutputStream outputStream )
    throws IOException {
        ParamChecks.nullNotPermitted ( bufferedImage, "bufferedImage" );
        ParamChecks.nullNotPermitted ( outputStream, "outputStream" );
        PngEncoder encoder = new PngEncoder ( bufferedImage, this.encodingAlpha,
                                              0, this.quality );
        outputStream.write ( encoder.pngEncode() );
    }

}
