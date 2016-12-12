// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

public final class DrawMessage
{
    private int type;
    private byte colorR;
    private byte colorG;
    private byte colorB;
    private byte colorA;
    private double thickness;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    
    public int getType() {
        return this.type;
    }
    
    public void setType(final int type) {
        this.type = type;
    }
    
    public double getThickness() {
        return this.thickness;
    }
    
    public void setThickness(final double thickness) {
        this.thickness = thickness;
    }
    
    public byte getColorR() {
        return this.colorR;
    }
    
    public void setColorR(final byte colorR) {
        this.colorR = colorR;
    }
    
    public byte getColorG() {
        return this.colorG;
    }
    
    public void setColorG(final byte colorG) {
        this.colorG = colorG;
    }
    
    public byte getColorB() {
        return this.colorB;
    }
    
    public void setColorB(final byte colorB) {
        this.colorB = colorB;
    }
    
    public byte getColorA() {
        return this.colorA;
    }
    
    public void setColorA(final byte colorA) {
        this.colorA = colorA;
    }
    
    public double getX1() {
        return this.x1;
    }
    
    public void setX1(final double x1) {
        this.x1 = x1;
    }
    
    public double getX2() {
        return this.x2;
    }
    
    public void setX2(final double x2) {
        this.x2 = x2;
    }
    
    public double getY1() {
        return this.y1;
    }
    
    public void setY1(final double y1) {
        this.y1 = y1;
    }
    
    public double getY2() {
        return this.y2;
    }
    
    public void setY2(final double y2) {
        this.y2 = y2;
    }
    
    public DrawMessage(final int type, final byte colorR, final byte colorG, final byte colorB, final byte colorA, final double thickness, final double x1, final double x2, final double y1, final double y2) {
        this.type = type;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        this.colorA = colorA;
        this.thickness = thickness;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }
    
    public void draw(final Graphics2D g) {
        g.setStroke(new BasicStroke((float)this.thickness, 1, 0));
        g.setColor(new Color(this.colorR & 0xFF, this.colorG & 0xFF, this.colorB & 0xFF, this.colorA & 0xFF));
        if (this.x1 == this.x2 && this.y1 == this.y2) {
            final Arc2D arc = new Arc2D.Double(this.x1, this.y1, 0.0, 0.0, 0.0, 360.0, 0);
            g.draw(arc);
        }
        else if (this.type == 1 || this.type == 2) {
            final Line2D line = new Line2D.Double(this.x1, this.y1, this.x2, this.y2);
            g.draw(line);
        }
        else if (this.type == 3 || this.type == 4) {
            double x1 = this.x1;
            double x2 = this.x2;
            double y1 = this.y1;
            double y2 = this.y2;
            if (x1 > x2) {
                x1 = this.x2;
                x2 = this.x1;
            }
            if (y1 > y2) {
                y1 = this.y2;
                y2 = this.y1;
            }
            if (this.type == 3) {
                final Rectangle2D rect = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
                g.draw(rect);
            }
            else if (this.type == 4) {
                final Arc2D arc2 = new Arc2D.Double(x1, y1, x2 - x1, y2 - y1, 0.0, 360.0, 0);
                g.draw(arc2);
            }
        }
    }
    
    @Override
    public String toString() {
        return this.type + "," + (this.colorR & 0xFF) + "," + (this.colorG & 0xFF) + "," + (this.colorB & 0xFF) + "," + (this.colorA & 0xFF) + "," + this.thickness + "," + this.x1 + "," + this.y1 + "," + this.x2 + "," + this.y2;
    }
    
    public static DrawMessage parseFromString(final String str) throws ParseException {
        final byte[] colors = new byte[4];
        final double[] coords = new double[4];
        int type;
        double thickness;
        try {
            final String[] elements = str.split(",");
            type = Integer.parseInt(elements[0]);
            if (type < 1 || type > 4) {
                throw new ParseException("Invalid type: " + type);
            }
            for (int i = 0; i < colors.length; ++i) {
                colors[i] = (byte)Integer.parseInt(elements[1 + i]);
            }
            thickness = Double.parseDouble(elements[5]);
            if (Double.isNaN(thickness) || thickness < 0.0 || thickness > 100.0) {
                throw new ParseException("Invalid thickness: " + thickness);
            }
            for (int i = 0; i < coords.length; ++i) {
                coords[i] = Double.parseDouble(elements[6 + i]);
                if (Double.isNaN(coords[i])) {
                    throw new ParseException("Invalid coordinate: " + coords[i]);
                }
            }
        }
        catch (RuntimeException ex) {
            throw new ParseException(ex);
        }
        final DrawMessage m = new DrawMessage(type, colors[0], colors[1], colors[2], colors[3], thickness, coords[0], coords[2], coords[1], coords[3]);
        return m;
    }
    
    public static class ParseException extends Exception
    {
        private static final long serialVersionUID = -6651972769789842960L;
        
        public ParseException(final Throwable root) {
            super(root);
        }
        
        public ParseException(final String message) {
            super(message);
        }
    }
}
