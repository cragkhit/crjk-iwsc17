package org.jfree.chart.editor;
import org.jfree.ui.RectangleInsets;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.NumberTickUnit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import org.jfree.layout.LCBLayout;
import javax.swing.JPanel;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PolarPlot;
import javax.swing.JTextField;
import java.awt.event.FocusListener;
public class DefaultPolarPlotEditor extends DefaultPlotEditor implements FocusListener {
    private JTextField manualTickUnit;
    private JTextField angleOffset;
    private double manualTickUnitValue;
    private double angleOffsetValue;
    public DefaultPolarPlotEditor ( final PolarPlot plot ) {
        super ( plot );
        this.angleOffsetValue = plot.getAngleOffset();
        this.angleOffset.setText ( Double.toString ( this.angleOffsetValue ) );
        this.manualTickUnitValue = plot.getAngleTickUnit().getSize();
        this.manualTickUnit.setText ( Double.toString ( this.manualTickUnitValue ) );
    }
    @Override
    protected JTabbedPane createPlotTabs ( final Plot plot ) {
        final JTabbedPane tabs = super.createPlotTabs ( plot );
        tabs.insertTab ( DefaultPolarPlotEditor.localizationResources.getString ( "General1" ), null, this.createPlotPanel(), null, 0 );
        tabs.setSelectedIndex ( 0 );
        return tabs;
    }
    private JPanel createPlotPanel() {
        final JPanel plotPanel = new JPanel ( ( LayoutManager ) new LCBLayout ( 3 ) );
        plotPanel.setBorder ( BorderFactory.createEmptyBorder ( 4, 4, 4, 4 ) );
        plotPanel.add ( new JLabel ( DefaultPolarPlotEditor.localizationResources.getString ( "AngleOffset" ) ) );
        ( this.angleOffset = new JTextField ( Double.toString ( this.angleOffsetValue ) ) ).setActionCommand ( "AngleOffsetValue" );
        this.angleOffset.addActionListener ( this );
        this.angleOffset.addFocusListener ( this );
        plotPanel.add ( this.angleOffset );
        plotPanel.add ( new JPanel() );
        plotPanel.add ( new JLabel ( DefaultPolarPlotEditor.localizationResources.getString ( "Manual_TickUnit_value" ) ) );
        ( this.manualTickUnit = new JTextField ( Double.toString ( this.manualTickUnitValue ) ) ).setActionCommand ( "TickUnitValue" );
        this.manualTickUnit.addActionListener ( this );
        this.manualTickUnit.addFocusListener ( this );
        plotPanel.add ( this.manualTickUnit );
        plotPanel.add ( new JPanel() );
        return plotPanel;
    }
    @Override
    public void focusGained ( final FocusEvent event ) {
    }
    @Override
    public void focusLost ( final FocusEvent event ) {
        if ( event.getSource() == this.angleOffset ) {
            this.validateAngleOffset();
        } else if ( event.getSource() == this.manualTickUnit ) {
            this.validateTickUnit();
        }
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "AngleOffsetValue" ) ) {
            this.validateAngleOffset();
        } else if ( command.equals ( "TickUnitValue" ) ) {
            this.validateTickUnit();
        }
    }
    public void validateAngleOffset() {
        double newOffset;
        try {
            newOffset = Double.parseDouble ( this.angleOffset.getText() );
        } catch ( NumberFormatException e ) {
            newOffset = this.angleOffsetValue;
        }
        this.angleOffsetValue = newOffset;
        this.angleOffset.setText ( Double.toString ( this.angleOffsetValue ) );
    }
    public void validateTickUnit() {
        double newTickUnit;
        try {
            newTickUnit = Double.parseDouble ( this.manualTickUnit.getText() );
        } catch ( NumberFormatException e ) {
            newTickUnit = this.manualTickUnitValue;
        }
        if ( newTickUnit > 0.0 && newTickUnit < 360.0 ) {
            this.manualTickUnitValue = newTickUnit;
        }
        this.manualTickUnit.setText ( Double.toString ( this.manualTickUnitValue ) );
    }
    @Override
    public void updatePlotProperties ( final Plot plot ) {
        super.updatePlotProperties ( plot );
        final PolarPlot pp = ( PolarPlot ) plot;
        pp.setAngleTickUnit ( new NumberTickUnit ( this.manualTickUnitValue ) );
        pp.setAngleOffset ( this.angleOffsetValue );
    }
}
