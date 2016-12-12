package org.jfree.chart.util;
import java.awt.Paint;
import org.jfree.chart.HashUtilities;
import java.awt.image.DataBufferInt;
import java.awt.Graphics2D;
import java.awt.image.BufferedImageOp;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.Serializable;
public class DefaultShadowGenerator implements ShadowGenerator, Serializable {
    private static final long serialVersionUID = 2732993885591386064L;
    private int shadowSize;
    private Color shadowColor;
    private float shadowOpacity;
    private double angle;
    private int distance;
    public DefaultShadowGenerator() {
        this ( 5, Color.black, 0.5f, 5, -0.7853981633974483 );
    }
    public DefaultShadowGenerator ( final int size, final Color color, final float opacity, final int distance, final double angle ) {
        ParamChecks.nullNotPermitted ( color, "color" );
        this.shadowSize = size;
        this.shadowColor = color;
        this.shadowOpacity = opacity;
        this.distance = distance;
        this.angle = angle;
    }
    public int getShadowSize() {
        return this.shadowSize;
    }
    public Color getShadowColor() {
        return this.shadowColor;
    }
    public float getShadowOpacity() {
        return this.shadowOpacity;
    }
    public int getDistance() {
        return this.distance;
    }
    public double getAngle() {
        return this.angle;
    }
    @Override
    public int calculateOffsetX() {
        return ( int ) ( Math.cos ( this.angle ) * this.distance ) - this.shadowSize;
    }
    @Override
    public int calculateOffsetY() {
        return - ( int ) ( Math.sin ( this.angle ) * this.distance ) - this.shadowSize;
    }
    @Override
    public BufferedImage createDropShadow ( final BufferedImage source ) {
        final BufferedImage subject = new BufferedImage ( source.getWidth() + this.shadowSize * 2, source.getHeight() + this.shadowSize * 2, 2 );
        final Graphics2D g2 = subject.createGraphics();
        g2.drawImage ( source, null, this.shadowSize, this.shadowSize );
        g2.dispose();
        this.applyShadow ( subject );
        return subject;
    }
    protected void applyShadow ( final BufferedImage image ) {
        final int dstWidth = image.getWidth();
        final int dstHeight = image.getHeight();
        final int left = this.shadowSize - 1 >> 1;
        final int right = this.shadowSize - left;
        final int xStart = left;
        final int xStop = dstWidth - right;
        final int yStart = left;
        final int yStop = dstHeight - right;
        final int shadowRgb = this.shadowColor.getRGB() & 0xFFFFFF;
        final int[] aHistory = new int[this.shadowSize];
        final int[] dataBuffer = ( ( DataBufferInt ) image.getRaster().getDataBuffer() ).getData();
        final int lastPixelOffset = right * dstWidth;
        final float sumDivider = this.shadowOpacity / this.shadowSize;
        int y = 0;
        int bufferOffset = 0;
        while ( y < dstHeight ) {
            int aSum = 0;
            int historyIdx = 0;
            for ( int x = 0; x < this.shadowSize; ++x, ++bufferOffset ) {
                final int a = dataBuffer[bufferOffset] >>> 24;
                aHistory[x] = a;
                aSum += a;
            }
            bufferOffset -= right;
            for ( int x = xStart; x < xStop; ++x, ++bufferOffset ) {
                int a = ( int ) ( aSum * sumDivider );
                dataBuffer[bufferOffset] = ( a << 24 | shadowRgb );
                aSum -= aHistory[historyIdx];
                a = dataBuffer[bufferOffset + right] >>> 24;
                aHistory[historyIdx] = a;
                aSum += a;
                if ( ++historyIdx >= this.shadowSize ) {
                    historyIdx -= this.shadowSize;
                }
            }
            bufferOffset = ++y * dstWidth;
        }
        int x2 = 0;
        bufferOffset = 0;
        while ( x2 < dstWidth ) {
            int aSum = 0;
            int historyIdx = 0;
            for ( int y2 = 0; y2 < this.shadowSize; ++y2, bufferOffset += dstWidth ) {
                final int a = dataBuffer[bufferOffset] >>> 24;
                aHistory[y2] = a;
                aSum += a;
            }
            bufferOffset -= lastPixelOffset;
            for ( int y2 = yStart; y2 < yStop; ++y2, bufferOffset += dstWidth ) {
                int a = ( int ) ( aSum * sumDivider );
                dataBuffer[bufferOffset] = ( a << 24 | shadowRgb );
                aSum -= aHistory[historyIdx];
                a = dataBuffer[bufferOffset + lastPixelOffset] >>> 24;
                aHistory[historyIdx] = a;
                aSum += a;
                if ( ++historyIdx >= this.shadowSize ) {
                    historyIdx -= this.shadowSize;
                }
            }
            bufferOffset = ++x2;
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultShadowGenerator ) ) {
            return false;
        }
        final DefaultShadowGenerator that = ( DefaultShadowGenerator ) obj;
        return this.shadowSize == that.shadowSize && this.shadowColor.equals ( that.shadowColor ) && this.shadowOpacity == that.shadowOpacity && this.distance == that.distance && this.angle == that.angle;
    }
    @Override
    public int hashCode() {
        int hash = HashUtilities.hashCode ( 17, this.shadowSize );
        hash = HashUtilities.hashCode ( hash, this.shadowColor );
        hash = HashUtilities.hashCode ( hash, this.shadowOpacity );
        hash = HashUtilities.hashCode ( hash, this.distance );
        hash = HashUtilities.hashCode ( hash, this.angle );
        return hash;
    }
}
