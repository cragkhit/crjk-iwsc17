package org.jfree.chart;
import java.awt.Container;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
public class ChartFrame extends JFrame {
    private ChartPanel chartPanel;
    public ChartFrame ( final String title, final JFreeChart chart ) {
        this ( title, chart, false );
    }
    public ChartFrame ( final String title, final JFreeChart chart, final boolean scrollPane ) {
        super ( title );
        this.setDefaultCloseOperation ( 2 );
        this.chartPanel = new ChartPanel ( chart );
        if ( scrollPane ) {
            this.setContentPane ( new JScrollPane ( this.chartPanel ) );
        } else {
            this.setContentPane ( this.chartPanel );
        }
    }
    public ChartPanel getChartPanel() {
        return this.chartPanel;
    }
}
