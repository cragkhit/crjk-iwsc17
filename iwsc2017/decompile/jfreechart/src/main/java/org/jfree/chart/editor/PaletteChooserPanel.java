package org.jfree.chart.editor;
import java.awt.Component;
import javax.swing.ListCellRenderer;
import org.jfree.chart.plot.ColorPalette;
import org.jfree.chart.plot.RainbowPalette;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
class PaletteChooserPanel extends JPanel {
    private JComboBox selector;
    public PaletteChooserPanel ( final PaletteSample current, final PaletteSample[] available ) {
        this.setLayout ( new BorderLayout() );
        ( this.selector = new JComboBox ( ( E[] ) available ) ).setSelectedItem ( current );
        this.selector.setRenderer ( new PaletteSample ( new RainbowPalette() ) );
        this.add ( this.selector );
    }
    public ColorPalette getSelectedPalette() {
        final PaletteSample sample = ( PaletteSample ) this.selector.getSelectedItem();
        return sample.getPalette();
    }
}
