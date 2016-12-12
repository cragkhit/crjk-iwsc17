package org.jfree.chart.editor;
import org.jfree.chart.util.ResourceBundleWrapper;
import javax.swing.JOptionPane;
import org.jfree.ui.StrokeChooserPanel;
import javax.swing.JColorChooser;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.Paint;
import org.jfree.chart.axis.ColorBar;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.plot.ContourPlot;
import org.jfree.chart.plot.PolarPlot;
import javax.swing.JTabbedPane;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.jfree.layout.LCBLayout;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.util.BooleanUtilities;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.Component;
import org.jfree.chart.plot.Plot;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.StrokeSample;
import org.jfree.ui.PaintSample;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
class DefaultPlotEditor extends JPanel implements ActionListener {
    private static final String[] orientationNames;
    private static final int ORIENTATION_VERTICAL = 0;
    private static final int ORIENTATION_HORIZONTAL = 1;
    private PaintSample backgroundPaintSample;
    private StrokeSample outlineStrokeSample;
    private PaintSample outlinePaintSample;
    private DefaultAxisEditor domainAxisPropertyPanel;
    private DefaultAxisEditor rangeAxisPropertyPanel;
    private DefaultColorBarEditor colorBarAxisPropertyPanel;
    private StrokeSample[] availableStrokeSamples;
    private RectangleInsets plotInsets;
    private PlotOrientation plotOrientation;
    private JComboBox orientationCombo;
    private Boolean drawLines;
    private JCheckBox drawLinesCheckBox;
    private Boolean drawShapes;
    private JCheckBox drawShapesCheckBox;
    protected static ResourceBundle localizationResources;
    public DefaultPlotEditor ( final Plot plot ) {
        final JPanel panel = this.createPlotPanel ( plot );
        this.add ( panel );
    }
    protected JPanel createPlotPanel ( final Plot plot ) {
        this.plotInsets = plot.getInsets();
        this.backgroundPaintSample = new PaintSample ( plot.getBackgroundPaint() );
        this.outlineStrokeSample = new StrokeSample ( plot.getOutlineStroke() );
        this.outlinePaintSample = new PaintSample ( plot.getOutlinePaint() );
        if ( plot instanceof CategoryPlot ) {
            this.plotOrientation = ( ( CategoryPlot ) plot ).getOrientation();
        } else if ( plot instanceof XYPlot ) {
            this.plotOrientation = ( ( XYPlot ) plot ).getOrientation();
        }
        if ( plot instanceof CategoryPlot ) {
            final CategoryItemRenderer renderer = ( ( CategoryPlot ) plot ).getRenderer();
            if ( renderer instanceof LineAndShapeRenderer ) {
                final LineAndShapeRenderer r = ( LineAndShapeRenderer ) renderer;
                this.drawLines = BooleanUtilities.valueOf ( r.getBaseLinesVisible() );
                this.drawShapes = BooleanUtilities.valueOf ( r.getBaseShapesVisible() );
            }
        } else if ( plot instanceof XYPlot ) {
            final XYItemRenderer renderer2 = ( ( XYPlot ) plot ).getRenderer();
            if ( renderer2 instanceof StandardXYItemRenderer ) {
                final StandardXYItemRenderer r2 = ( StandardXYItemRenderer ) renderer2;
                this.drawLines = BooleanUtilities.valueOf ( r2.getPlotLines() );
                this.drawShapes = BooleanUtilities.valueOf ( r2.getBaseShapesVisible() );
            }
        }
        this.setLayout ( new BorderLayout() );
        ( this.availableStrokeSamples = new StrokeSample[4] ) [0] = new StrokeSample ( ( Stroke ) null );
        this.availableStrokeSamples[1] = new StrokeSample ( ( Stroke ) new BasicStroke ( 1.0f ) );
        this.availableStrokeSamples[2] = new StrokeSample ( ( Stroke ) new BasicStroke ( 2.0f ) );
        this.availableStrokeSamples[3] = new StrokeSample ( ( Stroke ) new BasicStroke ( 3.0f ) );
        final JPanel panel = new JPanel ( new BorderLayout() );
        panel.setBorder ( BorderFactory.createTitledBorder ( BorderFactory.createEtchedBorder(), plot.getPlotType() + DefaultPlotEditor.localizationResources.getString ( ":" ) ) );
        final JPanel general = new JPanel ( new BorderLayout() );
        general.setBorder ( BorderFactory.createTitledBorder ( DefaultPlotEditor.localizationResources.getString ( "General" ) ) );
        final JPanel interior = new JPanel ( ( LayoutManager ) new LCBLayout ( 7 ) );
        interior.setBorder ( BorderFactory.createEmptyBorder ( 0, 5, 0, 5 ) );
        interior.add ( new JLabel ( DefaultPlotEditor.localizationResources.getString ( "Outline_stroke" ) ) );
        JButton button = new JButton ( DefaultPlotEditor.localizationResources.getString ( "Select..." ) );
        button.setActionCommand ( "OutlineStroke" );
        button.addActionListener ( this );
        interior.add ( ( Component ) this.outlineStrokeSample );
        interior.add ( button );
        interior.add ( new JLabel ( DefaultPlotEditor.localizationResources.getString ( "Outline_Paint" ) ) );
        button = new JButton ( DefaultPlotEditor.localizationResources.getString ( "Select..." ) );
        button.setActionCommand ( "OutlinePaint" );
        button.addActionListener ( this );
        interior.add ( ( Component ) this.outlinePaintSample );
        interior.add ( button );
        interior.add ( new JLabel ( DefaultPlotEditor.localizationResources.getString ( "Background_paint" ) ) );
        button = new JButton ( DefaultPlotEditor.localizationResources.getString ( "Select..." ) );
        button.setActionCommand ( "BackgroundPaint" );
        button.addActionListener ( this );
        interior.add ( ( Component ) this.backgroundPaintSample );
        interior.add ( button );
        if ( this.plotOrientation != null ) {
            final boolean isVertical = this.plotOrientation.equals ( PlotOrientation.VERTICAL );
            final int index = isVertical ? 0 : 1;
            interior.add ( new JLabel ( DefaultPlotEditor.localizationResources.getString ( "Orientation" ) ) );
            ( this.orientationCombo = new JComboBox ( ( E[] ) DefaultPlotEditor.orientationNames ) ).setSelectedIndex ( index );
            this.orientationCombo.setActionCommand ( "Orientation" );
            this.orientationCombo.addActionListener ( this );
            interior.add ( new JPanel() );
            interior.add ( this.orientationCombo );
        }
        if ( this.drawLines != null ) {
            interior.add ( new JLabel ( DefaultPlotEditor.localizationResources.getString ( "Draw_lines" ) ) );
            ( this.drawLinesCheckBox = new JCheckBox() ).setSelected ( this.drawLines );
            this.drawLinesCheckBox.setActionCommand ( "DrawLines" );
            this.drawLinesCheckBox.addActionListener ( this );
            interior.add ( new JPanel() );
            interior.add ( this.drawLinesCheckBox );
        }
        if ( this.drawShapes != null ) {
            interior.add ( new JLabel ( DefaultPlotEditor.localizationResources.getString ( "Draw_shapes" ) ) );
            ( this.drawShapesCheckBox = new JCheckBox() ).setSelected ( this.drawShapes );
            this.drawShapesCheckBox.setActionCommand ( "DrawShapes" );
            this.drawShapesCheckBox.addActionListener ( this );
            interior.add ( new JPanel() );
            interior.add ( this.drawShapesCheckBox );
        }
        general.add ( interior, "North" );
        final JPanel appearance = new JPanel ( new BorderLayout() );
        appearance.setBorder ( BorderFactory.createEmptyBorder ( 2, 2, 2, 2 ) );
        appearance.add ( general, "North" );
        final JTabbedPane tabs = this.createPlotTabs ( plot );
        tabs.add ( DefaultPlotEditor.localizationResources.getString ( "Appearance" ), appearance );
        panel.add ( tabs );
        return panel;
    }
    protected JTabbedPane createPlotTabs ( final Plot plot ) {
        final JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder ( BorderFactory.createEmptyBorder ( 0, 5, 0, 5 ) );
        Axis domainAxis = null;
        if ( plot instanceof CategoryPlot ) {
            domainAxis = ( ( CategoryPlot ) plot ).getDomainAxis();
        } else if ( plot instanceof XYPlot ) {
            domainAxis = ( ( XYPlot ) plot ).getDomainAxis();
        }
        this.domainAxisPropertyPanel = DefaultAxisEditor.getInstance ( domainAxis );
        if ( this.domainAxisPropertyPanel != null ) {
            this.domainAxisPropertyPanel.setBorder ( BorderFactory.createEmptyBorder ( 2, 2, 2, 2 ) );
            tabs.add ( DefaultPlotEditor.localizationResources.getString ( "Domain_Axis" ), this.domainAxisPropertyPanel );
        }
        Axis rangeAxis = null;
        if ( plot instanceof CategoryPlot ) {
            rangeAxis = ( ( CategoryPlot ) plot ).getRangeAxis();
        } else if ( plot instanceof XYPlot ) {
            rangeAxis = ( ( XYPlot ) plot ).getRangeAxis();
        } else if ( plot instanceof PolarPlot ) {
            rangeAxis = ( ( PolarPlot ) plot ).getAxis();
        }
        this.rangeAxisPropertyPanel = DefaultAxisEditor.getInstance ( rangeAxis );
        if ( this.rangeAxisPropertyPanel != null ) {
            this.rangeAxisPropertyPanel.setBorder ( BorderFactory.createEmptyBorder ( 2, 2, 2, 2 ) );
            tabs.add ( DefaultPlotEditor.localizationResources.getString ( "Range_Axis" ), this.rangeAxisPropertyPanel );
        }
        ColorBar colorBar = null;
        if ( plot instanceof ContourPlot ) {
            colorBar = ( ( ContourPlot ) plot ).getColorBar();
        }
        this.colorBarAxisPropertyPanel = DefaultColorBarEditor.getInstance ( colorBar );
        if ( this.colorBarAxisPropertyPanel != null ) {
            this.colorBarAxisPropertyPanel.setBorder ( BorderFactory.createEmptyBorder ( 2, 2, 2, 2 ) );
            tabs.add ( DefaultPlotEditor.localizationResources.getString ( "Color_Bar" ), this.colorBarAxisPropertyPanel );
        }
        return tabs;
    }
    public RectangleInsets getPlotInsets() {
        if ( this.plotInsets == null ) {
            this.plotInsets = new RectangleInsets ( 0.0, 0.0, 0.0, 0.0 );
        }
        return this.plotInsets;
    }
    public Paint getBackgroundPaint() {
        return this.backgroundPaintSample.getPaint();
    }
    public Stroke getOutlineStroke() {
        return this.outlineStrokeSample.getStroke();
    }
    public Paint getOutlinePaint() {
        return this.outlinePaintSample.getPaint();
    }
    public DefaultAxisEditor getDomainAxisPropertyEditPanel() {
        return this.domainAxisPropertyPanel;
    }
    public DefaultAxisEditor getRangeAxisPropertyEditPanel() {
        return this.rangeAxisPropertyPanel;
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "BackgroundPaint" ) ) {
            this.attemptBackgroundPaintSelection();
        } else if ( command.equals ( "OutlineStroke" ) ) {
            this.attemptOutlineStrokeSelection();
        } else if ( command.equals ( "OutlinePaint" ) ) {
            this.attemptOutlinePaintSelection();
        } else if ( command.equals ( "Orientation" ) ) {
            this.attemptOrientationSelection();
        } else if ( command.equals ( "DrawLines" ) ) {
            this.attemptDrawLinesSelection();
        } else if ( command.equals ( "DrawShapes" ) ) {
            this.attemptDrawShapesSelection();
        }
    }
    private void attemptBackgroundPaintSelection() {
        final Color c = JColorChooser.showDialog ( this, DefaultPlotEditor.localizationResources.getString ( "Background_Color" ), Color.blue );
        if ( c != null ) {
            this.backgroundPaintSample.setPaint ( ( Paint ) c );
        }
    }
    private void attemptOutlineStrokeSelection() {
        final StrokeChooserPanel panel = new StrokeChooserPanel ( this.outlineStrokeSample, this.availableStrokeSamples );
        final int result = JOptionPane.showConfirmDialog ( this, panel, DefaultPlotEditor.localizationResources.getString ( "Stroke_Selection" ), 2, -1 );
        if ( result == 0 ) {
            this.outlineStrokeSample.setStroke ( panel.getSelectedStroke() );
        }
    }
    private void attemptOutlinePaintSelection() {
        final Color c = JColorChooser.showDialog ( this, DefaultPlotEditor.localizationResources.getString ( "Outline_Color" ), Color.blue );
        if ( c != null ) {
            this.outlinePaintSample.setPaint ( ( Paint ) c );
        }
    }
    private void attemptOrientationSelection() {
        final int index = this.orientationCombo.getSelectedIndex();
        if ( index == 0 ) {
            this.plotOrientation = PlotOrientation.VERTICAL;
        } else {
            this.plotOrientation = PlotOrientation.HORIZONTAL;
        }
    }
    private void attemptDrawLinesSelection() {
        this.drawLines = BooleanUtilities.valueOf ( this.drawLinesCheckBox.isSelected() );
    }
    private void attemptDrawShapesSelection() {
        this.drawShapes = BooleanUtilities.valueOf ( this.drawShapesCheckBox.isSelected() );
    }
    public void updatePlotProperties ( final Plot plot ) {
        plot.setOutlinePaint ( this.getOutlinePaint() );
        plot.setOutlineStroke ( this.getOutlineStroke() );
        plot.setBackgroundPaint ( this.getBackgroundPaint() );
        plot.setInsets ( this.getPlotInsets() );
        if ( this.domainAxisPropertyPanel != null ) {
            Axis domainAxis = null;
            if ( plot instanceof CategoryPlot ) {
                final CategoryPlot p = ( CategoryPlot ) plot;
                domainAxis = p.getDomainAxis();
            } else if ( plot instanceof XYPlot ) {
                final XYPlot p2 = ( XYPlot ) plot;
                domainAxis = p2.getDomainAxis();
            }
            if ( domainAxis != null ) {
                this.domainAxisPropertyPanel.setAxisProperties ( domainAxis );
            }
        }
        if ( this.rangeAxisPropertyPanel != null ) {
            Axis rangeAxis = null;
            if ( plot instanceof CategoryPlot ) {
                final CategoryPlot p = ( CategoryPlot ) plot;
                rangeAxis = p.getRangeAxis();
            } else if ( plot instanceof XYPlot ) {
                final XYPlot p2 = ( XYPlot ) plot;
                rangeAxis = p2.getRangeAxis();
            } else if ( plot instanceof PolarPlot ) {
                final PolarPlot p3 = ( PolarPlot ) plot;
                rangeAxis = p3.getAxis();
            }
            if ( rangeAxis != null ) {
                this.rangeAxisPropertyPanel.setAxisProperties ( rangeAxis );
            }
        }
        if ( this.plotOrientation != null ) {
            if ( plot instanceof CategoryPlot ) {
                final CategoryPlot p4 = ( CategoryPlot ) plot;
                p4.setOrientation ( this.plotOrientation );
            } else if ( plot instanceof XYPlot ) {
                final XYPlot p5 = ( XYPlot ) plot;
                p5.setOrientation ( this.plotOrientation );
            }
        }
        if ( this.drawLines != null ) {
            if ( plot instanceof CategoryPlot ) {
                final CategoryPlot p4 = ( CategoryPlot ) plot;
                final CategoryItemRenderer r = p4.getRenderer();
                if ( r instanceof LineAndShapeRenderer ) {
                    ( ( LineAndShapeRenderer ) r ).setLinesVisible ( ( boolean ) this.drawLines );
                }
            } else if ( plot instanceof XYPlot ) {
                final XYPlot p5 = ( XYPlot ) plot;
                final XYItemRenderer r2 = p5.getRenderer();
                if ( r2 instanceof StandardXYItemRenderer ) {
                    ( ( StandardXYItemRenderer ) r2 ).setPlotLines ( this.drawLines );
                }
            }
        }
        if ( this.drawShapes != null ) {
            if ( plot instanceof CategoryPlot ) {
                final CategoryPlot p4 = ( CategoryPlot ) plot;
                final CategoryItemRenderer r = p4.getRenderer();
                if ( r instanceof LineAndShapeRenderer ) {
                    ( ( LineAndShapeRenderer ) r ).setShapesVisible ( ( boolean ) this.drawShapes );
                }
            } else if ( plot instanceof XYPlot ) {
                final XYPlot p5 = ( XYPlot ) plot;
                final XYItemRenderer r2 = p5.getRenderer();
                if ( r2 instanceof StandardXYItemRenderer ) {
                    ( ( StandardXYItemRenderer ) r2 ).setBaseShapesVisible ( this.drawShapes );
                }
            }
        }
        if ( this.colorBarAxisPropertyPanel != null ) {
            ColorBar colorBar = null;
            if ( plot instanceof ContourPlot ) {
                final ContourPlot p6 = ( ContourPlot ) plot;
                colorBar = p6.getColorBar();
            }
            if ( colorBar != null ) {
                this.colorBarAxisPropertyPanel.setAxisProperties ( colorBar );
            }
        }
    }
    static {
        orientationNames = new String[] { "Vertical", "Horizontal" };
        DefaultPlotEditor.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.editor.LocalizationBundle" );
    }
}
