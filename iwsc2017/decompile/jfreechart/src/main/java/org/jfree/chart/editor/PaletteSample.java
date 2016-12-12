package org.jfree.chart.editor;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Component;
import javax.swing.JList;
import java.awt.Dimension;
import org.jfree.chart.plot.ColorPalette;
import javax.swing.ListCellRenderer;
import javax.swing.JComponent;
public class PaletteSample extends JComponent implements ListCellRenderer {
    private ColorPalette palette;
    private Dimension preferredSize;
    public PaletteSample ( final ColorPalette palette ) {
        this.palette = palette;
        this.preferredSize = new Dimension ( 80, 18 );
    }
    @Override
    public Component getListCellRendererComponent ( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus ) {
        if ( value instanceof PaletteSample ) {
            final PaletteSample in = ( PaletteSample ) value;
            this.setPalette ( in.getPalette() );
        }
        return this;
    }
    public ColorPalette getPalette() {
        return this.palette;
    }
    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize;
    }
    public void paintComponent ( final Graphics g ) {
        final Graphics2D g2 = ( Graphics2D ) g;
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        final Dimension size = this.getSize();
        final Insets insets = this.getInsets();
        final double ww = size.getWidth() - insets.left - insets.right;
        final double hh = size.getHeight() - insets.top - insets.bottom;
        g2.setStroke ( new BasicStroke ( 1.0f ) );
        final double y1 = insets.top;
        final double y2 = y1 + hh;
        double xx = insets.left;
        final Line2D line = new Line2D.Double();
        int count = 0;
        while ( xx <= insets.left + ww ) {
            ++count;
            line.setLine ( xx, y1, xx, y2 );
            g2.setPaint ( this.palette.getColor ( count ) );
            g2.draw ( line );
            ++xx;
        }
    }
    public void setPalette ( final ColorPalette palette ) {
        this.palette = palette;
        this.repaint();
    }
}
