package org.jfree.chart.encoders;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import org.jfree.chart.util.ParamChecks;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
public class SunPNGEncoderAdapter implements ImageEncoder {
    @Override
    public float getQuality() {
        return 0.0f;
    }
    @Override
    public void setQuality ( final float quality ) {
    }
    @Override
    public boolean isEncodingAlpha() {
        return false;
    }
    @Override
    public void setEncodingAlpha ( final boolean encodingAlpha ) {
    }
    @Override
    public byte[] encode ( final BufferedImage bufferedImage ) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.encode ( bufferedImage, outputStream );
        return outputStream.toByteArray();
    }
    @Override
    public void encode ( final BufferedImage bufferedImage, final OutputStream outputStream ) throws IOException {
        ParamChecks.nullNotPermitted ( bufferedImage, "bufferedImage" );
        ParamChecks.nullNotPermitted ( outputStream, "outputStream" );
        ImageIO.write ( bufferedImage, "png", outputStream );
    }
}
