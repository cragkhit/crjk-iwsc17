package org.apache.catalina.filters;
protected static class Duration {
    protected final int amount;
    protected final DurationUnit unit;
    public Duration ( final int amount, final DurationUnit unit ) {
        this.amount = amount;
        this.unit = unit;
    }
    public int getAmount() {
        return this.amount;
    }
    public DurationUnit getUnit() {
        return this.unit;
    }
    @Override
    public String toString() {
        return this.amount + " " + this.unit;
    }
}
