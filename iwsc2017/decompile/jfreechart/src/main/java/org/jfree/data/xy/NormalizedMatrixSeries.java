package org.jfree.data.xy;
public class NormalizedMatrixSeries extends MatrixSeries {
    public static final double DEFAULT_SCALE_FACTOR = 1.0;
    private double m_scaleFactor;
    private double m_totalSum;
    public NormalizedMatrixSeries ( final String name, final int rows, final int columns ) {
        super ( name, rows, columns );
        this.m_scaleFactor = 1.0;
        this.m_totalSum = Double.MIN_VALUE;
    }
    @Override
    public Number getItem ( final int itemIndex ) {
        final int i = this.getItemRow ( itemIndex );
        final int j = this.getItemColumn ( itemIndex );
        final double mij = this.get ( i, j ) * this.m_scaleFactor;
        final Number n = new Double ( mij / this.m_totalSum );
        return n;
    }
    public void setScaleFactor ( final double factor ) {
        this.m_scaleFactor = factor;
    }
    public double getScaleFactor() {
        return this.m_scaleFactor;
    }
    @Override
    public void update ( final int i, final int j, final double mij ) {
        this.m_totalSum -= this.get ( i, j );
        this.m_totalSum += mij;
        super.update ( i, j, mij );
    }
    @Override
    public void zeroAll() {
        this.m_totalSum = 0.0;
        super.zeroAll();
    }
}
