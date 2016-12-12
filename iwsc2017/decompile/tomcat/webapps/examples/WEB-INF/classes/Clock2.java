import java.awt.Graphics;
import java.util.Locale;
import java.awt.Color;
import java.util.Date;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.applet.Applet;

// 
// Decompiled by Procyon v0.5.29
// 

public class Clock2 extends Applet implements Runnable
{
    private static final long serialVersionUID = 1L;
    Thread timer;
    int lastxs;
    int lastys;
    int lastxm;
    int lastym;
    int lastxh;
    int lastyh;
    SimpleDateFormat formatter;
    String lastdate;
    Font clockFaceFont;
    Date currentDate;
    Color handColor;
    Color numberColor;
    
    @Override
    public void init() {
        final boolean b = false;
        this.lastyh = (b ? 1 : 0);
        this.lastxh = (b ? 1 : 0);
        this.lastym = (b ? 1 : 0);
        this.lastxm = (b ? 1 : 0);
        this.lastys = (b ? 1 : 0);
        this.lastxs = (b ? 1 : 0);
        this.formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy", Locale.getDefault());
        this.currentDate = new Date();
        this.lastdate = this.formatter.format(this.currentDate);
        this.clockFaceFont = new Font("Serif", 0, 14);
        this.handColor = Color.blue;
        this.numberColor = Color.darkGray;
        try {
            this.setBackground(new Color(Integer.parseInt(this.getParameter("bgcolor"), 16)));
        }
        catch (Exception ex) {}
        try {
            this.handColor = new Color(Integer.parseInt(this.getParameter("fgcolor1"), 16));
        }
        catch (Exception ex2) {}
        try {
            this.numberColor = new Color(Integer.parseInt(this.getParameter("fgcolor2"), 16));
        }
        catch (Exception ex3) {}
        this.resize(300, 300);
    }
    
    public void plotpoints(final int x0, final int y0, final int x, final int y, final Graphics g) {
        g.drawLine(x0 + x, y0 + y, x0 + x, y0 + y);
        g.drawLine(x0 + y, y0 + x, x0 + y, y0 + x);
        g.drawLine(x0 + y, y0 - x, x0 + y, y0 - x);
        g.drawLine(x0 + x, y0 - y, x0 + x, y0 - y);
        g.drawLine(x0 - x, y0 - y, x0 - x, y0 - y);
        g.drawLine(x0 - y, y0 - x, x0 - y, y0 - x);
        g.drawLine(x0 - y, y0 + x, x0 - y, y0 + x);
        g.drawLine(x0 - x, y0 + y, x0 - x, y0 + y);
    }
    
    public void circle(final int x0, final int y0, final int r, final Graphics g) {
        int x = 0;
        int y = r;
        float d = 1 - r;
        this.plotpoints(x0, y0, x, y, g);
        while (y > x) {
            if (d < 0.0f) {
                d = d + 2 * x + 3.0f;
                ++x;
            }
            else {
                d = d + 2 * (x - y) + 5.0f;
                ++x;
                --y;
            }
            this.plotpoints(x0, y0, x, y, g);
        }
    }
    
    @Override
    public void paint(final Graphics g) {
        int s = 0;
        int m = 10;
        int h = 10;
        this.currentDate = new Date();
        final SimpleDateFormat formatter = new SimpleDateFormat("s", Locale.getDefault());
        try {
            s = Integer.parseInt(formatter.format(this.currentDate));
        }
        catch (NumberFormatException n) {
            s = 0;
        }
        formatter.applyPattern("m");
        try {
            m = Integer.parseInt(formatter.format(this.currentDate));
        }
        catch (NumberFormatException n) {
            m = 10;
        }
        formatter.applyPattern("h");
        try {
            h = Integer.parseInt(formatter.format(this.currentDate));
        }
        catch (NumberFormatException n) {
            h = 10;
        }
        formatter.applyPattern("EEE MMM dd HH:mm:ss yyyy");
        final String today = formatter.format(this.currentDate);
        final int xcenter = 80;
        final int ycenter = 55;
        final int xs = (int)(Math.cos(s * 3.14f / 30.0f - 1.57f) * 45.0 + xcenter);
        final int ys = (int)(Math.sin(s * 3.14f / 30.0f - 1.57f) * 45.0 + ycenter);
        final int xm = (int)(Math.cos(m * 3.14f / 30.0f - 1.57f) * 40.0 + xcenter);
        final int ym = (int)(Math.sin(m * 3.14f / 30.0f - 1.57f) * 40.0 + ycenter);
        final int xh = (int)(Math.cos((h * 30 + m / 2) * 3.14f / 180.0f - 1.57f) * 30.0 + xcenter);
        final int yh = (int)(Math.sin((h * 30 + m / 2) * 3.14f / 180.0f - 1.57f) * 30.0 + ycenter);
        g.setFont(this.clockFaceFont);
        g.setColor(this.handColor);
        this.circle(xcenter, ycenter, 50, g);
        g.setColor(this.numberColor);
        g.drawString("9", xcenter - 45, ycenter + 3);
        g.drawString("3", xcenter + 40, ycenter + 3);
        g.drawString("12", xcenter - 5, ycenter - 37);
        g.drawString("6", xcenter - 3, ycenter + 45);
        g.setColor(this.getBackground());
        if (xs != this.lastxs || ys != this.lastys) {
            g.drawLine(xcenter, ycenter, this.lastxs, this.lastys);
            g.drawString(this.lastdate, 5, 125);
        }
        if (xm != this.lastxm || ym != this.lastym) {
            g.drawLine(xcenter, ycenter - 1, this.lastxm, this.lastym);
            g.drawLine(xcenter - 1, ycenter, this.lastxm, this.lastym);
        }
        if (xh != this.lastxh || yh != this.lastyh) {
            g.drawLine(xcenter, ycenter - 1, this.lastxh, this.lastyh);
            g.drawLine(xcenter - 1, ycenter, this.lastxh, this.lastyh);
        }
        g.setColor(this.numberColor);
        g.drawString("", 5, 125);
        g.drawString(today, 5, 125);
        g.drawLine(xcenter, ycenter, xs, ys);
        g.setColor(this.handColor);
        g.drawLine(xcenter, ycenter - 1, xm, ym);
        g.drawLine(xcenter - 1, ycenter, xm, ym);
        g.drawLine(xcenter, ycenter - 1, xh, yh);
        g.drawLine(xcenter - 1, ycenter, xh, yh);
        this.lastxs = xs;
        this.lastys = ys;
        this.lastxm = xm;
        this.lastym = ym;
        this.lastxh = xh;
        this.lastyh = yh;
        this.lastdate = today;
        this.currentDate = null;
    }
    
    @Override
    public void start() {
        (this.timer = new Thread(this)).start();
    }
    
    @Override
    public void stop() {
        this.timer = null;
    }
    
    @Override
    public void run() {
        final Thread me = Thread.currentThread();
        while (this.timer == me) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException ex) {}
            this.repaint();
        }
    }
    
    @Override
    public void update(final Graphics g) {
        this.paint(g);
    }
    
    @Override
    public String getAppletInfo() {
        return "Title: A Clock \nAuthor: Rachel Gollub, 1995 \nAn analog clock.";
    }
    
    @Override
    public String[][] getParameterInfo() {
        final String[][] info = { { "bgcolor", "hexadecimal RGB number", "The background color. Default is the color of your browser." }, { "fgcolor1", "hexadecimal RGB number", "The color of the hands and dial. Default is blue." }, { "fgcolor2", "hexadecimal RGB number", "The color of the seconds hand and numbers. Default is dark gray." } };
        return info;
    }
}
