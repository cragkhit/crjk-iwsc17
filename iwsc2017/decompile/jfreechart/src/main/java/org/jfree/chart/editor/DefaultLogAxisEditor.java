package org.jfree.chart.editor;
import java.awt.Font;
import java.awt.Paint;
import org.jfree.ui.RectangleInsets;
import javax.swing.JTabbedPane;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.Axis;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.LogAxis;
import javax.swing.JTextField;
public class DefaultLogAxisEditor extends DefaultValueAxisEditor {
    private double manualTickUnitValue;
    private JTextField manualTickUnit;
    public DefaultLogAxisEditor ( final LogAxis axis ) {
        super ( axis );
        this.manualTickUnitValue = axis.getTickUnit().getSize();
        this.manualTickUnit.setText ( Double.toString ( this.manualTickUnitValue ) );
    }
    @Override
    protected JPanel createTickUnitPanel() {
        final JPanel tickUnitPanel = super.createTickUnitPanel();
        tickUnitPanel.add ( new JLabel ( DefaultLogAxisEditor.localizationResources.getString ( "Manual_TickUnit_value" ) ) );
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
        final LogAxis logAxis = ( LogAxis ) axis;
        if ( !this.isAutoTickUnitSelection() ) {
            logAxis.setTickUnit ( new NumberTickUnit ( this.manualTickUnitValue ) );
        }
    }
}
