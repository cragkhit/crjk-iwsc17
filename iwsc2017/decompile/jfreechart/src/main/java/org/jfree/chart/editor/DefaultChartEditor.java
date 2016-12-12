package org.jfree.chart.editor;
import org.jfree.chart.util.ResourceBundleWrapper;
import javax.swing.JColorChooser;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.Paint;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.title.Title;
import org.jfree.chart.plot.PolarPlot;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Component;
import org.jfree.layout.LCBLayout;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import org.jfree.chart.JFreeChart;
import java.util.ResourceBundle;
import org.jfree.ui.PaintSample;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
class DefaultChartEditor extends JPanel implements ActionListener, ChartEditor {
    private DefaultTitleEditor titleEditor;
    private DefaultPlotEditor plotEditor;
    private JCheckBox antialias;
    private PaintSample background;
    protected static ResourceBundle localizationResources;
    public DefaultChartEditor ( final JFreeChart chart ) {
        this.setLayout ( new BorderLayout() );
        final JPanel other = new JPanel ( new BorderLayout() );
        other.setBorder ( BorderFactory.createEmptyBorder ( 2, 2, 2, 2 ) );
        final JPanel general = new JPanel ( new BorderLayout() );
        general.setBorder ( BorderFactory.createTitledBorder ( BorderFactory.createEtchedBorder(), DefaultChartEditor.localizationResources.getString ( "General" ) ) );
        final JPanel interior = new JPanel ( ( LayoutManager ) new LCBLayout ( 6 ) );
        interior.setBorder ( BorderFactory.createEmptyBorder ( 0, 5, 0, 5 ) );
        ( this.antialias = new JCheckBox ( DefaultChartEditor.localizationResources.getString ( "Draw_anti-aliased" ) ) ).setSelected ( chart.getAntiAlias() );
        interior.add ( this.antialias );
        interior.add ( new JLabel ( "" ) );
        interior.add ( new JLabel ( "" ) );
        interior.add ( new JLabel ( DefaultChartEditor.localizationResources.getString ( "Background_paint" ) ) );
        interior.add ( ( Component ) ( this.background = new PaintSample ( chart.getBackgroundPaint() ) ) );
        JButton button = new JButton ( DefaultChartEditor.localizationResources.getString ( "Select..." ) );
        button.setActionCommand ( "BackgroundPaint" );
        button.addActionListener ( this );
        interior.add ( button );
        interior.add ( new JLabel ( DefaultChartEditor.localizationResources.getString ( "Series_Paint" ) ) );
        JTextField info = new JTextField ( DefaultChartEditor.localizationResources.getString ( "No_editor_implemented" ) );
        info.setEnabled ( false );
        interior.add ( info );
        button = new JButton ( DefaultChartEditor.localizationResources.getString ( "Edit..." ) );
        button.setEnabled ( false );
        interior.add ( button );
        interior.add ( new JLabel ( DefaultChartEditor.localizationResources.getString ( "Series_Stroke" ) ) );
        info = new JTextField ( DefaultChartEditor.localizationResources.getString ( "No_editor_implemented" ) );
        info.setEnabled ( false );
        interior.add ( info );
        button = new JButton ( DefaultChartEditor.localizationResources.getString ( "Edit..." ) );
        button.setEnabled ( false );
        interior.add ( button );
        interior.add ( new JLabel ( DefaultChartEditor.localizationResources.getString ( "Series_Outline_Paint" ) ) );
        info = new JTextField ( DefaultChartEditor.localizationResources.getString ( "No_editor_implemented" ) );
        info.setEnabled ( false );
        interior.add ( info );
        button = new JButton ( DefaultChartEditor.localizationResources.getString ( "Edit..." ) );
        button.setEnabled ( false );
        interior.add ( button );
        interior.add ( new JLabel ( DefaultChartEditor.localizationResources.getString ( "Series_Outline_Stroke" ) ) );
        info = new JTextField ( DefaultChartEditor.localizationResources.getString ( "No_editor_implemented" ) );
        info.setEnabled ( false );
        interior.add ( info );
        button = new JButton ( DefaultChartEditor.localizationResources.getString ( "Edit..." ) );
        button.setEnabled ( false );
        interior.add ( button );
        general.add ( interior, "North" );
        other.add ( general, "North" );
        final JPanel parts = new JPanel ( new BorderLayout() );
        final Title title = chart.getTitle();
        final Plot plot = chart.getPlot();
        final JTabbedPane tabs = new JTabbedPane();
        ( this.titleEditor = new DefaultTitleEditor ( title ) ).setBorder ( BorderFactory.createEmptyBorder ( 2, 2, 2, 2 ) );
        tabs.addTab ( DefaultChartEditor.localizationResources.getString ( "Title" ), this.titleEditor );
        if ( plot instanceof PolarPlot ) {
            this.plotEditor = new DefaultPolarPlotEditor ( ( PolarPlot ) plot );
        } else {
            this.plotEditor = new DefaultPlotEditor ( plot );
        }
        this.plotEditor.setBorder ( BorderFactory.createEmptyBorder ( 2, 2, 2, 2 ) );
        tabs.addTab ( DefaultChartEditor.localizationResources.getString ( "Plot" ), this.plotEditor );
        tabs.add ( DefaultChartEditor.localizationResources.getString ( "Other" ), other );
        parts.add ( tabs, "North" );
        this.add ( parts );
    }
    public DefaultTitleEditor getTitleEditor() {
        return this.titleEditor;
    }
    public DefaultPlotEditor getPlotEditor() {
        return this.plotEditor;
    }
    public boolean getAntiAlias() {
        return this.antialias.isSelected();
    }
    public Paint getBackgroundPaint() {
        return this.background.getPaint();
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "BackgroundPaint" ) ) {
            this.attemptModifyBackgroundPaint();
        }
    }
    private void attemptModifyBackgroundPaint() {
        final Color c = JColorChooser.showDialog ( this, DefaultChartEditor.localizationResources.getString ( "Background_Color" ), Color.blue );
        if ( c != null ) {
            this.background.setPaint ( ( Paint ) c );
        }
    }
    @Override
    public void updateChart ( final JFreeChart chart ) {
        this.titleEditor.setTitleProperties ( chart );
        this.plotEditor.updatePlotProperties ( chart.getPlot() );
        chart.setAntiAlias ( this.getAntiAlias() );
        chart.setBackgroundPaint ( this.getBackgroundPaint() );
    }
    static {
        DefaultChartEditor.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.editor.LocalizationBundle" );
    }
}
