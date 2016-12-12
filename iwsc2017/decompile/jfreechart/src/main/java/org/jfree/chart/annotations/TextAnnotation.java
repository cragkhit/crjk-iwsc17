package org.jfree.chart.annotations;
import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.TextAnchor;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
public class TextAnnotation extends AbstractAnnotation implements Serializable {
    private static final long serialVersionUID = 7008912287533127432L;
    public static final Font DEFAULT_FONT;
    public static final Paint DEFAULT_PAINT;
    public static final TextAnchor DEFAULT_TEXT_ANCHOR;
    public static final TextAnchor DEFAULT_ROTATION_ANCHOR;
    public static final double DEFAULT_ROTATION_ANGLE = 0.0;
    private String text;
    private Font font;
    private transient Paint paint;
    private TextAnchor textAnchor;
    private TextAnchor rotationAnchor;
    private double rotationAngle;
    protected TextAnnotation ( final String text ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        this.text = text;
        this.font = TextAnnotation.DEFAULT_FONT;
        this.paint = TextAnnotation.DEFAULT_PAINT;
        this.textAnchor = TextAnnotation.DEFAULT_TEXT_ANCHOR;
        this.rotationAnchor = TextAnnotation.DEFAULT_ROTATION_ANCHOR;
        this.rotationAngle = 0.0;
    }
    public String getText() {
        return this.text;
    }
    public void setText ( final String text ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        this.text = text;
        this.fireAnnotationChanged();
    }
    public Font getFont() {
        return this.font;
    }
    public void setFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.font = font;
        this.fireAnnotationChanged();
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.fireAnnotationChanged();
    }
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }
    public void setTextAnchor ( final TextAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.textAnchor = anchor;
        this.fireAnnotationChanged();
    }
    public TextAnchor getRotationAnchor() {
        return this.rotationAnchor;
    }
    public void setRotationAnchor ( final TextAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.rotationAnchor = anchor;
        this.fireAnnotationChanged();
    }
    public double getRotationAngle() {
        return this.rotationAngle;
    }
    public void setRotationAngle ( final double angle ) {
        this.rotationAngle = angle;
        this.fireAnnotationChanged();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TextAnnotation ) ) {
            return false;
        }
        final TextAnnotation that = ( TextAnnotation ) obj;
        return ObjectUtilities.equal ( ( Object ) this.text, ( Object ) that.getText() ) && ObjectUtilities.equal ( ( Object ) this.font, ( Object ) that.getFont() ) && PaintUtilities.equal ( this.paint, that.getPaint() ) && ObjectUtilities.equal ( ( Object ) this.textAnchor, ( Object ) that.getTextAnchor() ) && ObjectUtilities.equal ( ( Object ) this.rotationAnchor, ( Object ) that.getRotationAnchor() ) && this.rotationAngle == that.getRotationAngle();
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + this.font.hashCode();
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.paint );
        result = 37 * result + this.rotationAnchor.hashCode();
        final long temp = Double.doubleToLongBits ( this.rotationAngle );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        result = 37 * result + this.text.hashCode();
        result = 37 * result + this.textAnchor.hashCode();
        return result;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
    }
    static {
        DEFAULT_FONT = new Font ( "SansSerif", 0, 10 );
        DEFAULT_PAINT = Color.black;
        DEFAULT_TEXT_ANCHOR = TextAnchor.CENTER;
        DEFAULT_ROTATION_ANCHOR = TextAnchor.CENTER;
    }
}
