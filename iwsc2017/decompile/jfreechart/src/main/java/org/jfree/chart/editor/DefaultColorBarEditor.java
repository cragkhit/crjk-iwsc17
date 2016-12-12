package org.jfree.chart.editor;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.axis.Axis;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import org.jfree.layout.LCBLayout;
import org.jfree.chart.plot.GreyPalette;
import org.jfree.chart.plot.ColorPalette;
import org.jfree.chart.plot.RainbowPalette;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ColorBar;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
class DefaultColorBarEditor extends DefaultNumberAxisEditor {
    private JCheckBox invertPaletteCheckBox;
    private boolean invertPalette;
    private JCheckBox stepPaletteCheckBox;
    private boolean stepPalette;
    private PaletteSample currentPalette;
    private PaletteSample[] availablePaletteSamples;
    protected static ResourceBundle localizationResources;
    public DefaultColorBarEditor ( final ColorBar colorBar ) {
        super ( ( NumberAxis ) colorBar.getAxis() );
        this.invertPalette = false;
        this.stepPalette = false;
        this.invertPalette = colorBar.getColorPalette().isInverse();
        this.stepPalette = colorBar.getColorPalette().isStepped();
        this.currentPalette = new PaletteSample ( colorBar.getColorPalette() );
        ( this.availablePaletteSamples = new PaletteSample[2] ) [0] = new PaletteSample ( new RainbowPalette() );
        this.availablePaletteSamples[1] = new PaletteSample ( new GreyPalette() );
        final JTabbedPane other = this.getOtherTabs();
        final JPanel palettePanel = new JPanel ( ( LayoutManager ) new LCBLayout ( 4 ) );
        palettePanel.setBorder ( BorderFactory.createEmptyBorder ( 4, 4, 4, 4 ) );
        palettePanel.add ( new JPanel() );
        ( this.invertPaletteCheckBox = new JCheckBox ( DefaultColorBarEditor.localizationResources.getString ( "Invert_Palette" ), this.invertPalette ) ).setActionCommand ( "invertPalette" );
        this.invertPaletteCheckBox.addActionListener ( this );
        palettePanel.add ( this.invertPaletteCheckBox );
        palettePanel.add ( new JPanel() );
        palettePanel.add ( new JPanel() );
        ( this.stepPaletteCheckBox = new JCheckBox ( DefaultColorBarEditor.localizationResources.getString ( "Step_Palette" ), this.stepPalette ) ).setActionCommand ( "stepPalette" );
        this.stepPaletteCheckBox.addActionListener ( this );
        palettePanel.add ( this.stepPaletteCheckBox );
        palettePanel.add ( new JPanel() );
        palettePanel.add ( new JLabel ( DefaultColorBarEditor.localizationResources.getString ( "Palette" ) ) );
        final JButton button = new JButton ( DefaultColorBarEditor.localizationResources.getString ( "Set_palette..." ) );
        button.setActionCommand ( "PaletteChoice" );
        button.addActionListener ( this );
        palettePanel.add ( this.currentPalette );
        palettePanel.add ( button );
        other.add ( DefaultColorBarEditor.localizationResources.getString ( "Palette" ), palettePanel );
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "PaletteChoice" ) ) {
            this.attemptPaletteSelection();
        } else if ( command.equals ( "invertPalette" ) ) {
            this.invertPalette = this.invertPaletteCheckBox.isSelected();
        } else if ( command.equals ( "stepPalette" ) ) {
            this.stepPalette = this.stepPaletteCheckBox.isSelected();
        } else {
            super.actionPerformed ( event );
        }
    }
    private void attemptPaletteSelection() {
        final PaletteChooserPanel panel = new PaletteChooserPanel ( null, this.availablePaletteSamples );
        final int result = JOptionPane.showConfirmDialog ( this, panel, DefaultColorBarEditor.localizationResources.getString ( "Palette_Selection" ), 2, -1 );
        if ( result == 0 ) {
            final double zmin = this.currentPalette.getPalette().getMinZ();
            final double zmax = this.currentPalette.getPalette().getMaxZ();
            this.currentPalette.setPalette ( panel.getSelectedPalette() );
            this.currentPalette.getPalette().setMinZ ( zmin );
            this.currentPalette.getPalette().setMaxZ ( zmax );
        }
    }
    public void setAxisProperties ( final ColorBar colorBar ) {
        super.setAxisProperties ( colorBar.getAxis() );
        colorBar.setColorPalette ( this.currentPalette.getPalette() );
        colorBar.getColorPalette().setInverse ( this.invertPalette );
        colorBar.getColorPalette().setStepped ( this.stepPalette );
    }
    public static DefaultColorBarEditor getInstance ( final ColorBar colorBar ) {
        if ( colorBar != null ) {
            return new DefaultColorBarEditor ( colorBar );
        }
        return null;
    }
    static {
        DefaultColorBarEditor.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.editor.LocalizationBundle" );
    }
}
