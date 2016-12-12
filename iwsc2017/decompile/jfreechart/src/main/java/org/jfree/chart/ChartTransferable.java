package org.jfree.chart;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
public class ChartTransferable implements Transferable {
    final DataFlavor imageFlavor;
    private JFreeChart chart;
    private int width;
    private int height;
    private int minDrawWidth;
    private int minDrawHeight;
    private int maxDrawWidth;
    private int maxDrawHeight;
    public ChartTransferable ( final JFreeChart chart, final int width, final int height ) {
        this ( chart, width, height, true );
    }
    public ChartTransferable ( final JFreeChart chart, final int width, final int height, final boolean cloneData ) {
        this ( chart, width, height, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true );
    }
    public ChartTransferable ( final JFreeChart chart, final int width, final int height, final int minDrawW, final int minDrawH, final int maxDrawW, final int maxDrawH, final boolean cloneData ) {
        this.imageFlavor = new DataFlavor ( "image/x-java-image; class=java.awt.Image", "Image" );
        try {
            this.chart = ( JFreeChart ) chart.clone();
        } catch ( CloneNotSupportedException e ) {
            this.chart = chart;
        }
        this.width = width;
        this.height = height;
        this.minDrawWidth = minDrawW;
        this.minDrawHeight = minDrawH;
        this.maxDrawWidth = maxDrawW;
        this.maxDrawHeight = maxDrawH;
    }
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { this.imageFlavor };
    }
    @Override
    public boolean isDataFlavorSupported ( final DataFlavor flavor ) {
        return this.imageFlavor.equals ( flavor );
    }
    @Override
    public Object getTransferData ( final DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
        if ( this.imageFlavor.equals ( flavor ) ) {
            return this.createBufferedImage ( this.chart, this.width, this.height, this.minDrawWidth, this.minDrawHeight, this.maxDrawWidth, this.maxDrawHeight );
        }
        throw new UnsupportedFlavorException ( flavor );
    }
    private BufferedImage createBufferedImage ( final JFreeChart chart, final int w, final int h, final int minDrawW, final int minDrawH, final int maxDrawW, final int maxDrawH ) {
        final BufferedImage image = new BufferedImage ( w, h, 2 );
        final Graphics2D g2 = image.createGraphics();
        boolean scale = false;
        double drawWidth = w;
        double drawHeight = h;
        double scaleX = 1.0;
        double scaleY = 1.0;
        if ( drawWidth < minDrawW ) {
            scaleX = drawWidth / minDrawW;
            drawWidth = minDrawW;
            scale = true;
        } else if ( drawWidth > maxDrawW ) {
            scaleX = drawWidth / maxDrawW;
            drawWidth = maxDrawW;
            scale = true;
        }
        if ( drawHeight < minDrawH ) {
            scaleY = drawHeight / minDrawH;
            drawHeight = minDrawH;
            scale = true;
        } else if ( drawHeight > maxDrawH ) {
            scaleY = drawHeight / maxDrawH;
            drawHeight = maxDrawH;
            scale = true;
        }
        final Rectangle2D chartArea = new Rectangle2D.Double ( 0.0, 0.0, drawWidth, drawHeight );
        if ( scale ) {
            final AffineTransform st = AffineTransform.getScaleInstance ( scaleX, scaleY );
            g2.transform ( st );
        }
        chart.draw ( g2, chartArea, null, null );
        g2.dispose();
        return image;
    }
}
