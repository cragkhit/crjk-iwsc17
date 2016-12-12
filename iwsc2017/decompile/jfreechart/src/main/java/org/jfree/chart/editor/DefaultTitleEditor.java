package org.jfree.chart.editor;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.JFreeChart;
import javax.swing.JColorChooser;
import java.awt.Color;
import javax.swing.JOptionPane;
import org.jfree.ui.FontChooserPanel;
import java.awt.event.ActionEvent;
import java.awt.Paint;
import org.jfree.ui.FontDisplayField;
import java.awt.Component;
import javax.swing.JLabel;
import org.jfree.layout.LCBLayout;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import java.util.ResourceBundle;
import org.jfree.ui.PaintSample;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
class DefaultTitleEditor extends JPanel implements ActionListener {
    private boolean showTitle;
    private JCheckBox showTitleCheckBox;
    private JTextField titleField;
    private Font titleFont;
    private JTextField fontfield;
    private JButton selectFontButton;
    private PaintSample titlePaint;
    private JButton selectPaintButton;
    protected static ResourceBundle localizationResources;
    public DefaultTitleEditor ( final Title title ) {
        final TextTitle t = ( TextTitle ) ( ( title != null ) ? title : new TextTitle ( DefaultTitleEditor.localizationResources.getString ( "Title" ) ) );
        this.showTitle = ( title != null );
        this.titleFont = t.getFont();
        this.titleField = new JTextField ( t.getText() );
        this.titlePaint = new PaintSample ( t.getPaint() );
        this.setLayout ( new BorderLayout() );
        final JPanel general = new JPanel ( new BorderLayout() );
        general.setBorder ( BorderFactory.createTitledBorder ( BorderFactory.createEtchedBorder(), DefaultTitleEditor.localizationResources.getString ( "General" ) ) );
        final JPanel interior = new JPanel ( ( LayoutManager ) new LCBLayout ( 4 ) );
        interior.setBorder ( BorderFactory.createEmptyBorder ( 0, 5, 0, 5 ) );
        interior.add ( new JLabel ( DefaultTitleEditor.localizationResources.getString ( "Show_Title" ) ) );
        ( this.showTitleCheckBox = new JCheckBox() ).setSelected ( this.showTitle );
        this.showTitleCheckBox.setActionCommand ( "ShowTitle" );
        this.showTitleCheckBox.addActionListener ( this );
        interior.add ( new JPanel() );
        interior.add ( this.showTitleCheckBox );
        final JLabel titleLabel = new JLabel ( DefaultTitleEditor.localizationResources.getString ( "Text" ) );
        interior.add ( titleLabel );
        interior.add ( this.titleField );
        interior.add ( new JPanel() );
        final JLabel fontLabel = new JLabel ( DefaultTitleEditor.localizationResources.getString ( "Font" ) );
        this.fontfield = ( JTextField ) new FontDisplayField ( this.titleFont );
        ( this.selectFontButton = new JButton ( DefaultTitleEditor.localizationResources.getString ( "Select..." ) ) ).setActionCommand ( "SelectFont" );
        this.selectFontButton.addActionListener ( this );
        interior.add ( fontLabel );
        interior.add ( this.fontfield );
        interior.add ( this.selectFontButton );
        final JLabel colorLabel = new JLabel ( DefaultTitleEditor.localizationResources.getString ( "Color" ) );
        ( this.selectPaintButton = new JButton ( DefaultTitleEditor.localizationResources.getString ( "Select..." ) ) ).setActionCommand ( "SelectPaint" );
        this.selectPaintButton.addActionListener ( this );
        interior.add ( colorLabel );
        interior.add ( ( Component ) this.titlePaint );
        interior.add ( this.selectPaintButton );
        this.enableOrDisableControls();
        general.add ( interior );
        this.add ( general, "North" );
    }
    public String getTitleText() {
        return this.titleField.getText();
    }
    public Font getTitleFont() {
        return this.titleFont;
    }
    public Paint getTitlePaint() {
        return this.titlePaint.getPaint();
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "SelectFont" ) ) {
            this.attemptFontSelection();
        } else if ( command.equals ( "SelectPaint" ) ) {
            this.attemptPaintSelection();
        } else if ( command.equals ( "ShowTitle" ) ) {
            this.attemptModifyShowTitle();
        }
    }
    public void attemptFontSelection() {
        final FontChooserPanel panel = new FontChooserPanel ( this.titleFont );
        final int result = JOptionPane.showConfirmDialog ( this, panel, DefaultTitleEditor.localizationResources.getString ( "Font_Selection" ), 2, -1 );
        if ( result == 0 ) {
            this.titleFont = panel.getSelectedFont();
            this.fontfield.setText ( this.titleFont.getFontName() + " " + this.titleFont.getSize() );
        }
    }
    public void attemptPaintSelection() {
        final Paint p = this.titlePaint.getPaint();
        final Color defaultColor = ( Color ) ( ( p instanceof Color ) ? p : Color.blue );
        final Color c = JColorChooser.showDialog ( this, DefaultTitleEditor.localizationResources.getString ( "Title_Color" ), defaultColor );
        if ( c != null ) {
            this.titlePaint.setPaint ( ( Paint ) c );
        }
    }
    private void attemptModifyShowTitle() {
        this.showTitle = this.showTitleCheckBox.isSelected();
        this.enableOrDisableControls();
    }
    private void enableOrDisableControls() {
        final boolean enabled = this.showTitle;
        this.titleField.setEnabled ( enabled );
        this.selectFontButton.setEnabled ( enabled );
        this.selectPaintButton.setEnabled ( enabled );
    }
    public void setTitleProperties ( final JFreeChart chart ) {
        if ( this.showTitle ) {
            TextTitle title = chart.getTitle();
            if ( title == null ) {
                title = new TextTitle();
                chart.setTitle ( title );
            }
            title.setText ( this.getTitleText() );
            title.setFont ( this.getTitleFont() );
            title.setPaint ( this.getTitlePaint() );
        } else {
            chart.setTitle ( ( TextTitle ) null );
        }
    }
    static {
        DefaultTitleEditor.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.editor.LocalizationBundle" );
    }
}
