package org.jfree.chart.encoders;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.Image;
import com.keypoint.PngEncoder;
import org.jfree.chart.util.ParamChecks;
import java.awt.image.BufferedImage;
public class KeypointPNGEncoderAdapter implements ImageEncoder {
    private int quality;
    private boolean encodingAlpha;
    public KeypointPNGEncoderAdapter() {
        this.quality = 9;
        this.encodingAlpha = false;
    }
    @Override
    public float getQuality() {
        return this.quality;
    }
    @Override
    public void setQuality ( final float quality ) {
        this.quality = ( int ) quality;
    }
    @Override
    public boolean isEncodingAlpha() {
        return this.encodingAlpha;
    }
    @Override
    public void setEncodingAlpha ( final boolean encodingAlpha ) {
        this.encodingAlpha = encodingAlpha;
    }
    @Override
    public byte[] encode ( final BufferedImage bufferedImage ) throws IOException {
        ParamChecks.nullNotPermitted ( bufferedImage, "bufferedImage" );
        final PngEncoder encoder = new PngEncoder ( ( Image ) bufferedImage, this.encodingAlpha, 0, this.quality );
        return encoder.pngEncode();
    }
    @Override
    public void encode ( final BufferedImage bufferedImage, final OutputStream outputStream ) throws IOException {
        ParamChecks.nullNotPermitted ( bufferedImage, "bufferedImage" );
        ParamChecks.nullNotPermitted ( outputStream, "outputStream" );
        final PngEncoder encoder = new PngEncoder ( ( Image ) bufferedImage, this.encodingAlpha, 0, this.quality );
        outputStream.write ( encoder.pngEncode() );
    }
}
