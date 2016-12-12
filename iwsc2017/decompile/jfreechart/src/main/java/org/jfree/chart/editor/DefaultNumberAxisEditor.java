package org.jfree.chart.editor;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.Axis;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import org.jfree.layout.LCBLayout;
import javax.swing.JPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import javax.swing.JTextField;
import java.awt.event.FocusListener;
class DefaultNumberAxisEditor extends DefaultValueAxisEditor implements FocusListener {
    private double manualTickUnitValue;
    private JTextField manualTickUnit;
    public DefaultNumberAxisEditor ( final NumberAxis axis ) {
        super ( axis );
        this.manualTickUnitValue = axis.getTickUnit().getSize();
        this.validateTickUnit();
    }
    @Override
    protected JPanel createTickUnitPanel() {
        final JPanel tickUnitPanel = new JPanel ( ( LayoutManager ) new LCBLayout ( 3 ) );
        tickUnitPanel.setBorder ( BorderFactory.createEmptyBorder ( 4, 4, 4, 4 ) );
        tickUnitPanel.add ( new JPanel() );
        final JCheckBox autoTickUnitSelectionCheckBox = new JCheckBox ( DefaultNumberAxisEditor.localizationResources.getString ( "Auto-TickUnit_Selection" ), this.isAutoTickUnitSelection() );
        autoTickUnitSelectionCheckBox.setActionCommand ( "AutoTickOnOff" );
        autoTickUnitSelectionCheckBox.addActionListener ( this );
        this.setAutoTickUnitSelectionCheckBox ( autoTickUnitSelectionCheckBox );
        tickUnitPanel.add ( this.getAutoTickUnitSelectionCheckBox() );
        tickUnitPanel.add ( new JPanel() );
        tickUnitPanel.add ( new JLabel ( DefaultNumberAxisEditor.localizationResources.getString ( "Manual_TickUnit_value" ) ) );
        ( this.manualTickUnit = new JTextField ( Double.toString ( this.manualTickUnitValue ) ) ).setEnabled ( !this.isAutoTickUnitSelection() );
        this.manualTickUnit.setActionCommand ( "TickUnitValue" );
        this.manualTickUnit.addActionListener ( this );
        this.manualTickUnit.addFocusListener ( this );
        tickUnitPanel.add ( this.manualTickUnit );
        tickUnitPanel.add ( new JPanel() );
        return tickUnitPanel;
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "TickUnitValue" ) ) {
            this.validateTickUnit();
        } else {
            super.actionPerformed ( event );
        }
    }
    @Override
    public void focusLost ( final FocusEvent event ) {
        super.focusLost ( event );
        if ( event.getSource() == this.manualTickUnit ) {
            this.validateTickUnit();
        }
    }
    @Override
    public void toggleAutoTick() {
        super.toggleAutoTick();
        if ( this.isAutoTickUnitSelection() ) {
            this.manualTickUnit.setText ( Double.toString ( this.manualTickUnitValue ) );
            this.manualTickUnit.setEnabled ( false );
        } else {
            this.manualTickUnit.setEnabled ( true );
        }
    }
    public void validateTickUnit() {
        double newTickUnit;
        try {
            newTickUnit = Double.parseDouble ( this.manualTickUnit.getText() );
        } catch ( NumberFormatException e ) {
            newTickUnit = this.manualTickUnitValue;
        }
        if ( newTickUnit > 0.0 ) {
            this.manualTickUnitValue = newTickUnit;
        }
        this.manualTickUnit.setText ( Double.toString ( this.manualTickUnitValue ) );
    }
    @Override
    public void setAxisProperties ( final Axis axis ) {
        super.setAxisProperties ( axis );
        final NumberAxis numberAxis = ( NumberAxis ) axis;
        if ( !this.isAutoTickUnitSelection() ) {
            numberAxis.setTickUnit ( new NumberTickUnit ( this.manualTickUnitValue ) );
        }
    }
}
