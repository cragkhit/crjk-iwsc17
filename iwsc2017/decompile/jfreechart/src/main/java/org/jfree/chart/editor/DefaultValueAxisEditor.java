package org.jfree.chart.editor;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.awt.event.FocusEvent;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import org.jfree.ui.StrokeChooserPanel;
import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import org.jfree.layout.LCBLayout;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Color;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueAxis;
import java.util.ResourceBundle;
import org.jfree.ui.StrokeSample;
import org.jfree.ui.PaintSample;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import java.awt.event.FocusListener;
class DefaultValueAxisEditor extends DefaultAxisEditor implements FocusListener {
    private boolean autoRange;
    private boolean autoTickUnitSelection;
    private double minimumValue;
    private double maximumValue;
    private JCheckBox autoRangeCheckBox;
    private JCheckBox autoTickUnitSelectionCheckBox;
    private JTextField minimumRangeValue;
    private JTextField maximumRangeValue;
    private PaintSample gridPaintSample;
    private StrokeSample gridStrokeSample;
    private StrokeSample[] availableStrokeSamples;
    protected static ResourceBundle localizationResources;
    public DefaultValueAxisEditor ( final ValueAxis axis ) {
        super ( axis );
        this.autoRange = axis.isAutoRange();
        this.minimumValue = axis.getLowerBound();
        this.maximumValue = axis.getUpperBound();
        this.autoTickUnitSelection = axis.isAutoTickUnitSelection();
        this.gridPaintSample = new PaintSample ( ( Paint ) Color.blue );
        this.gridStrokeSample = new StrokeSample ( ( Stroke ) new BasicStroke ( 1.0f ) );
        ( this.availableStrokeSamples = new StrokeSample[3] ) [0] = new StrokeSample ( ( Stroke ) new BasicStroke ( 1.0f ) );
        this.availableStrokeSamples[1] = new StrokeSample ( ( Stroke ) new BasicStroke ( 2.0f ) );
        this.availableStrokeSamples[2] = new StrokeSample ( ( Stroke ) new BasicStroke ( 3.0f ) );
        final JTabbedPane other = this.getOtherTabs();
        final JPanel range = new JPanel ( ( LayoutManager ) new LCBLayout ( 3 ) );
        range.setBorder ( BorderFactory.createEmptyBorder ( 4, 4, 4, 4 ) );
        range.add ( new JPanel() );
        ( this.autoRangeCheckBox = new JCheckBox ( DefaultValueAxisEditor.localizationResources.getString ( "Auto-adjust_range" ), this.autoRange ) ).setActionCommand ( "AutoRangeOnOff" );
        this.autoRangeCheckBox.addActionListener ( this );
        range.add ( this.autoRangeCheckBox );
        range.add ( new JPanel() );
        range.add ( new JLabel ( DefaultValueAxisEditor.localizationResources.getString ( "Minimum_range_value" ) ) );
        ( this.minimumRangeValue = new JTextField ( Double.toString ( this.minimumValue ) ) ).setEnabled ( !this.autoRange );
        this.minimumRangeValue.setActionCommand ( "MinimumRange" );
        this.minimumRangeValue.addActionListener ( this );
        this.minimumRangeValue.addFocusListener ( this );
        range.add ( this.minimumRangeValue );
        range.add ( new JPanel() );
        range.add ( new JLabel ( DefaultValueAxisEditor.localizationResources.getString ( "Maximum_range_value" ) ) );
        ( this.maximumRangeValue = new JTextField ( Double.toString ( this.maximumValue ) ) ).setEnabled ( !this.autoRange );
        this.maximumRangeValue.setActionCommand ( "MaximumRange" );
        this.maximumRangeValue.addActionListener ( this );
        this.maximumRangeValue.addFocusListener ( this );
        range.add ( this.maximumRangeValue );
        range.add ( new JPanel() );
        other.add ( DefaultValueAxisEditor.localizationResources.getString ( "Range" ), range );
        other.add ( DefaultValueAxisEditor.localizationResources.getString ( "TickUnit" ), this.createTickUnitPanel() );
    }
    protected JPanel createTickUnitPanel() {
        final JPanel tickUnitPanel = new JPanel ( ( LayoutManager ) new LCBLayout ( 3 ) );
        tickUnitPanel.setBorder ( BorderFactory.createEmptyBorder ( 4, 4, 4, 4 ) );
        tickUnitPanel.add ( new JPanel() );
        ( this.autoTickUnitSelectionCheckBox = new JCheckBox ( DefaultValueAxisEditor.localizationResources.getString ( "Auto-TickUnit_Selection" ), this.autoTickUnitSelection ) ).setActionCommand ( "AutoTickOnOff" );
        this.autoTickUnitSelectionCheckBox.addActionListener ( this );
        tickUnitPanel.add ( this.autoTickUnitSelectionCheckBox );
        tickUnitPanel.add ( new JPanel() );
        return tickUnitPanel;
    }
    protected boolean isAutoTickUnitSelection() {
        return this.autoTickUnitSelection;
    }
    protected void setAutoTickUnitSelection ( final boolean autoTickUnitSelection ) {
        this.autoTickUnitSelection = autoTickUnitSelection;
    }
    protected JCheckBox getAutoTickUnitSelectionCheckBox() {
        return this.autoTickUnitSelectionCheckBox;
    }
    protected void setAutoTickUnitSelectionCheckBox ( final JCheckBox autoTickUnitSelectionCheckBox ) {
        this.autoTickUnitSelectionCheckBox = autoTickUnitSelectionCheckBox;
    }
    public boolean isAutoRange() {
        return this.autoRange;
    }
    public double getMinimumValue() {
        return this.minimumValue;
    }
    public double getMaximumValue() {
        return this.maximumValue;
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "GridStroke" ) ) {
            this.attemptGridStrokeSelection();
        } else if ( command.equals ( "GridPaint" ) ) {
            this.attemptGridPaintSelection();
        } else if ( command.equals ( "AutoRangeOnOff" ) ) {
            this.toggleAutoRange();
        } else if ( command.equals ( "MinimumRange" ) ) {
            this.validateMinimum();
        } else if ( command.equals ( "MaximumRange" ) ) {
            this.validateMaximum();
        } else if ( command.equals ( "AutoTickOnOff" ) ) {
            this.toggleAutoTick();
        } else {
            super.actionPerformed ( event );
        }
    }
    protected void attemptGridStrokeSelection() {
        final StrokeChooserPanel panel = new StrokeChooserPanel ( this.gridStrokeSample, this.availableStrokeSamples );
        final int result = JOptionPane.showConfirmDialog ( this, panel, DefaultValueAxisEditor.localizationResources.getString ( "Stroke_Selection" ), 2, -1 );
        if ( result == 0 ) {
            this.gridStrokeSample.setStroke ( panel.getSelectedStroke() );
        }
    }
    protected void attemptGridPaintSelection() {
        final Color c = JColorChooser.showDialog ( this, DefaultValueAxisEditor.localizationResources.getString ( "Grid_Color" ), Color.blue );
        if ( c != null ) {
            this.gridPaintSample.setPaint ( ( Paint ) c );
        }
    }
    @Override
    public void focusGained ( final FocusEvent event ) {
    }
    @Override
    public void focusLost ( final FocusEvent event ) {
        if ( event.getSource() == this.minimumRangeValue ) {
            this.validateMinimum();
        } else if ( event.getSource() == this.maximumRangeValue ) {
            this.validateMaximum();
        }
    }
    public void toggleAutoRange() {
        this.autoRange = this.autoRangeCheckBox.isSelected();
        if ( this.autoRange ) {
            this.minimumRangeValue.setText ( Double.toString ( this.minimumValue ) );
            this.minimumRangeValue.setEnabled ( false );
            this.maximumRangeValue.setText ( Double.toString ( this.maximumValue ) );
            this.maximumRangeValue.setEnabled ( false );
        } else {
            this.minimumRangeValue.setEnabled ( true );
            this.maximumRangeValue.setEnabled ( true );
        }
    }
    public void toggleAutoTick() {
        this.autoTickUnitSelection = this.autoTickUnitSelectionCheckBox.isSelected();
    }
    public void validateMinimum() {
        double newMin;
        try {
            newMin = Double.parseDouble ( this.minimumRangeValue.getText() );
            if ( newMin >= this.maximumValue ) {
                newMin = this.minimumValue;
            }
        } catch ( NumberFormatException e ) {
            newMin = this.minimumValue;
        }
        this.minimumValue = newMin;
        this.minimumRangeValue.setText ( Double.toString ( this.minimumValue ) );
    }
    public void validateMaximum() {
        double newMax;
        try {
            newMax = Double.parseDouble ( this.maximumRangeValue.getText() );
            if ( newMax <= this.minimumValue ) {
                newMax = this.maximumValue;
            }
        } catch ( NumberFormatException e ) {
            newMax = this.maximumValue;
        }
        this.maximumValue = newMax;
        this.maximumRangeValue.setText ( Double.toString ( this.maximumValue ) );
    }
    @Override
    public void setAxisProperties ( final Axis axis ) {
        super.setAxisProperties ( axis );
        final ValueAxis valueAxis = ( ValueAxis ) axis;
        valueAxis.setAutoRange ( this.autoRange );
        if ( !this.autoRange ) {
            valueAxis.setRange ( this.minimumValue, this.maximumValue );
        }
        valueAxis.setAutoTickUnitSelection ( this.autoTickUnitSelection );
    }
    static {
        DefaultValueAxisEditor.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.editor.LocalizationBundle" );
    }
}
