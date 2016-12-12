package org.jfree.chart.plot;
import org.jfree.chart.axis.ValueAxis;
import java.awt.Color;
import java.awt.Paint;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.Font;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import java.awt.Component;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.RectangleInsets;
import java.awt.LayoutManager;
import java.awt.CardLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultValueDataset;
import java.io.Serializable;
import javax.swing.JPanel;
public class JThermometer extends JPanel implements Serializable {
    private static final long serialVersionUID = 1079905665515589820L;
    private DefaultValueDataset data;
    private JFreeChart chart;
    private ChartPanel panel;
    private ThermometerPlot plot;
    public JThermometer() {
        super ( new CardLayout() );
        ( this.plot = new ThermometerPlot() ).setInsets ( new RectangleInsets ( 5.0, 5.0, 5.0, 5.0 ) );
        this.data = new DefaultValueDataset();
        this.plot.setDataset ( this.data );
        this.chart = new JFreeChart ( null, JFreeChart.DEFAULT_TITLE_FONT, this.plot, false );
        this.add ( this.panel = new ChartPanel ( this.chart, false ), "Panel" );
        this.setBackground ( this.getBackground() );
    }
    public void addSubtitle ( final Title subtitle ) {
        this.chart.addSubtitle ( subtitle );
    }
    public void addSubtitle ( final String subtitle ) {
        this.chart.addSubtitle ( new TextTitle ( subtitle ) );
    }
    public void addSubtitle ( final String subtitle, final Font font ) {
        this.chart.addSubtitle ( new TextTitle ( subtitle, font ) );
    }
    public void setValueFormat ( final DecimalFormat df ) {
        this.plot.setValueFormat ( df );
    }
    public void setRange ( final double lower, final double upper ) {
        this.plot.setRange ( lower, upper );
    }
    public void setSubrangeInfo ( final int range, final double displayLow, final double displayHigh ) {
        this.plot.setSubrangeInfo ( range, displayLow, displayHigh );
    }
    public void setSubrangeInfo ( final int range, final double rangeLow, final double rangeHigh, final double displayLow, final double displayHigh ) {
        this.plot.setSubrangeInfo ( range, rangeLow, rangeHigh, displayLow, displayHigh );
    }
    public void setValueLocation ( final int loc ) {
        this.plot.setValueLocation ( loc );
        this.panel.repaint();
    }
    public void setValuePaint ( final Paint paint ) {
        this.plot.setValuePaint ( paint );
    }
    public Number getValue() {
        if ( this.data != null ) {
            return this.data.getValue();
        }
        return null;
    }
    public void setValue ( final double value ) {
        this.setValue ( new Double ( value ) );
    }
    public void setValue ( final Number value ) {
        if ( this.data != null ) {
            this.data.setValue ( value );
        }
    }
    public void setUnits ( final int i ) {
        if ( this.plot != null ) {
            this.plot.setUnits ( i );
        }
    }
    public void setOutlinePaint ( final Paint p ) {
        if ( this.plot != null ) {
            this.plot.setOutlinePaint ( p );
        }
    }
    @Override
    public void setForeground ( final Color fg ) {
        super.setForeground ( fg );
        if ( this.plot != null ) {
            this.plot.setThermometerPaint ( fg );
        }
    }
    @Override
    public void setBackground ( final Color bg ) {
        super.setBackground ( bg );
        if ( this.plot != null ) {
            this.plot.setBackgroundPaint ( bg );
        }
        if ( this.chart != null ) {
            this.chart.setBackgroundPaint ( bg );
        }
        if ( this.panel != null ) {
            this.panel.setBackground ( bg );
        }
    }
    public void setValueFont ( final Font f ) {
        if ( this.plot != null ) {
            this.plot.setValueFont ( f );
        }
    }
    public Font getTickLabelFont() {
        final ValueAxis axis = this.plot.getRangeAxis();
        return axis.getTickLabelFont();
    }
    public void setTickLabelFont ( final Font font ) {
        final ValueAxis axis = this.plot.getRangeAxis();
        axis.setTickLabelFont ( font );
    }
    public void changeTickFontSize ( final int delta ) {
        final Font f = this.getTickLabelFont();
        final String fName = f.getFontName();
        final Font newFont = new Font ( fName, f.getStyle(), f.getSize() + delta );
        this.setTickLabelFont ( newFont );
    }
    public void setTickFontStyle ( final int style ) {
        final Font f = this.getTickLabelFont();
        final String fName = f.getFontName();
        final Font newFont = new Font ( fName, style, f.getSize() );
        this.setTickLabelFont ( newFont );
    }
    public void setFollowDataInSubranges ( final boolean flag ) {
        this.plot.setFollowDataInSubranges ( flag );
    }
    public void setShowValueLines ( final boolean b ) {
        this.plot.setShowValueLines ( b );
    }
    public void setShowAxisLocation ( final int location ) {
        this.plot.setAxisLocation ( location );
    }
    public int getShowAxisLocation() {
        return this.plot.getAxisLocation();
    }
}
