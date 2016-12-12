package org.jfree.chart.encoders;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
public class EncoderUtil {
    public static byte[] encode ( final BufferedImage image, final String format ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format );
        return imageEncoder.encode ( image );
    }
    public static byte[] encode ( final BufferedImage image, final String format, final boolean encodeAlpha ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format, encodeAlpha );
        return imageEncoder.encode ( image );
    }
    public static byte[] encode ( final BufferedImage image, final String format, final float quality ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format, quality );
        return imageEncoder.encode ( image );
    }
    public static byte[] encode ( final BufferedImage image, final String format, final float quality, final boolean encodeAlpha ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format, quality, encodeAlpha );
        return imageEncoder.encode ( image );
    }
    public static void writeBufferedImage ( final BufferedImage image, final String format, final OutputStream outputStream ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format );
        imageEncoder.encode ( image, outputStream );
    }
    public static void writeBufferedImage ( final BufferedImage image, final String format, final OutputStream outputStream, final float quality ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format, quality );
        imageEncoder.encode ( image, outputStream );
    }
    public static void writeBufferedImage ( final BufferedImage image, final String format, final OutputStream outputStream, final boolean encodeAlpha ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format, encodeAlpha );
        imageEncoder.encode ( image, outputStream );
    }
    public static void writeBufferedImage ( final BufferedImage image, final String format, final OutputStream outputStream, final float quality, final boolean encodeAlpha ) throws IOException {
        final ImageEncoder imageEncoder = ImageEncoderFactory.newInstance ( format, quality, encodeAlpha );
        imageEncoder.encode ( image, outputStream );
    }
}
